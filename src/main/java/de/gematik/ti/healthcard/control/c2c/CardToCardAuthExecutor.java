/*
 * Copyright (c) 2020 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.ti.healthcard.control.c2c;

import java.io.IOException;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.healthcard.control.exceptions.CardToCardProcessException;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.IHealthCardType;
import de.gematik.ti.healthcardaccess.cardobjects.Format2Pin;
import de.gematik.ti.healthcardaccess.cardobjects.GemCvCertificate;
import de.gematik.ti.healthcardaccess.cardobjects.PsoAlgorithm;
import de.gematik.ti.healthcardaccess.commands.*;
import de.gematik.ti.healthcardaccess.healthcards.Egk2;
import de.gematik.ti.healthcardaccess.healthcards.Egk21;
import de.gematik.ti.healthcardaccess.healthcards.HealthCardStatusValid;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.utils.primitives.Bytes;

/**
 * Run a process of C2C of authentification without negociation of Sessionkey
  */
public class CardToCardAuthExecutor {
    private static final Logger LOG = LoggerFactory.getLogger(CardToCardAuthExecutor.class);
    private static ResultOperation<Response> result;

    /**
     * start process of mutal authentication from here <br/>
     * No Pin required. Pin should be verified before C2C
     * @param eGK
     * @param authCard
     * @return
     * @throws IOException
     */
    public static ResultOperation<C2CSuccessState> authenticateC2C(final IHealthCard eGK, final IHealthCard authCard) {
        if (eGK == null || authCard == null) {
            throw new CardToCardProcessException("targetCard " + eGK + " and sourceCard " + authCard + " must not be 'null' for CardToCard-process.");
        }

        if (eGK.getStatus().isValid()) {
            final IHealthCardType cardType = ((HealthCardStatusValid) eGK.getStatus()).getHealthCardType();
            if (!(cardType instanceof Egk2 || cardType instanceof Egk21)) {
                throw new CardToCardProcessException("eGK parameter is not of Egk2 or Egk21 type");
            }
        }

        doSelectRoot(eGK, authCard);

        LOG.debug("starting C2C authentication");
        ResultOperation<C2CSuccessState> resultCardToCard = executeAuth(authCard, null, eGK)
                .validate(C2CSuccessState.C2C_SUCCESS::validateSuccessState);
        LOG.debug("starting C2C authentication in the reverse direction");
        // authenticate HBA/SMC-B against eGK
        resultCardToCard = resultCardToCard.flatMap(__ -> executeAuth(eGK, null, authCard)
                .validate(C2CSuccessState.C2C_SUCCESS::validateSuccessState));
        return resultCardToCard;
    }

    /**
     * start process of mutal authentication from here
     * @param eGK
     * @param authCard
     * @param pinAuthCard
     * @return
     * @throws IOException
     */
    public static ResultOperation<C2CSuccessState> authenticateC2C(final IHealthCard eGK, final IHealthCard authCard, final Format2Pin pinAuthCard) {
        if (eGK == null || authCard == null) {
            throw new CardToCardProcessException("targetCard " + eGK + " and sourceCard " + authCard + " must not be 'null' for CardToCard-process.");
        }

        if (eGK.getStatus().isValid()) {
            final IHealthCardType cardType = ((HealthCardStatusValid) eGK.getStatus()).getHealthCardType();
            if (!(cardType instanceof Egk2 || cardType instanceof Egk21)) {
                throw new CardToCardProcessException("eGK parameter is not of Egk2 or Egk21 type");
            }
        }

        doSelectRoot(eGK, authCard);

        LOG.debug("starting C2C authentication");
        ResultOperation<C2CSuccessState> resultCardToCard = executeAuth(authCard, pinAuthCard, eGK)
                .validate(C2CSuccessState.C2C_SUCCESS::validateSuccessState);
        LOG.debug("starting C2C authentication in the reverse direction");

        // authenticate HBA/SMC-B against eGK

        resultCardToCard = resultCardToCard.flatMap(__ -> executeAuth(eGK, null, authCard)
                .validate(C2CSuccessState.C2C_SUCCESS::validateSuccessState));
        return resultCardToCard;
    }

    /**
     * This method includes all steps one after another in a C2C-process <br/>
     * All methods of steps begin with 'perform....()'
     * @param cardInt
     * @param cardExt
     */
    private static ResultOperation<C2CSuccessState> executeAuth(final IHealthCard cardInt, final Format2Pin pinAuthCard, final IHealthCard cardExt) {// NOCS(ZD):
        final CardFileSystemData cardIntFs = CardFileSystemData.getCardFileSystemData(cardInt);
        final CardFileSystemData cardExtFs = CardFileSystemData.getCardFileSystemData(cardExt);
        if (pinAuthCard != null) {
            doPinVerification(cardInt, pinAuthCard, cardIntFs);
        }

        doCACertVerification(cardInt, cardExt, cardExtFs);
        doAutrCertVerification(cardInt, cardExt, cardExtFs);

        final ResultOperation<byte[]> challengeIccsnInt = createToken(cardInt, cardIntFs);

        doInternalExternalAuth(cardInt, cardExt, challengeIccsnInt);
        return result.map(C2CSuccessState.C2C_SUCCESS::validateResponse);

    }

    /**
     * Select the root of card
     * @param cardInt
     * @param cardExt
     * @return
     */
    private static void doSelectRoot(final IHealthCard cardInt, final IHealthCard cardExt) {
        final SelectCommand selectRootCommand = new SelectCommand(false, false);
        result = selectRootCommand.executeOn(cardInt).validate(Response.ResponseStatus.SUCCESS::validateResult)
                .flatMap(__ -> selectRootCommand.executeOn(cardExt).validate(Response.ResponseStatus.SUCCESS::validateResult));
    }

    /**
     * verify pin on cardInt to satisfy the sec-pre-condition
     * @param cardInt
     * @param pin
     * @param cardIntFs
     * @return
     */
    private static void doPinVerification(final IHealthCard cardInt, final Format2Pin pin,
            final CardFileSystemData cardIntFs) {
        LOG.debug("pin: " + pin);
        result = result.flatMap(__ -> new VerifyCommand(cardIntFs.pinChRef, false, pin).executeOn(cardInt))
                .validate(Response.ResponseStatus.SUCCESS::validateResult);
    }

    /**
     * read Card Authority Certificate from Hba and verify on egk
     */
    private static void doCACertVerification(final IHealthCard cardInt, final IHealthCard cardExt,
            final CardFileSystemData cardExtFs) {
        result = result.flatMap(__ -> new ReadCommand(cardExtFs.cvcCASfid, 0).executeOn(cardExt))
                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                .map(Response::getResponseData)
                .map(GemCvCertificate::new)
                .flatMap(caCertificateFromExt -> new ManageSecurityEnvironmentCommand(
                        ManageSecurityEnvironmentCommand.MseUseCase.KEY_SELECTION_FOR_CV_CERTIFICATE_VALIDATION, caCertificateFromExt)
                                .executeOn(cardInt)
                                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                                .flatMap(__ -> new PsoVerifyCertificateCommand(caCertificateFromExt)
                                        .executeOn(cardInt)
                                        .validate(Response.ResponseStatus.SUCCESS::validateResult)));
    }

    /**
     * read 'autr' from cardExt and do msn on both cards
     * @param cardInt
     * @param cardExt
     * @param cardExtFs
     * @return
     * @throws IOException
     */
    private static void doAutrCertVerification(final IHealthCard cardInt,
            final IHealthCard cardExt, final CardFileSystemData cardExtFs) {
        result = result.flatMap(__ -> new ReadCommand(cardExtFs.cvcAutrSfid, 0).executeOn(cardExt))
                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                .map(Response::getResponseData)
                .map(GemCvCertificate::new)
                .flatMap(autCertificateFromExt -> new ManageSecurityEnvironmentCommand(
                        ManageSecurityEnvironmentCommand.MseUseCase.KEY_SELECTION_FOR_CV_CERTIFICATE_VALIDATION, autCertificateFromExt)
                                .executeOn(cardInt)
                                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                                .flatMap(__ -> new PsoVerifyCertificateCommand(autCertificateFromExt).executeOn(cardInt))
                                .validate(Response.ResponseStatus.SUCCESS::validateResult).flatMap(__ -> new ManageSecurityEnvironmentCommand(
                                        ManageSecurityEnvironmentCommand.MseUseCase.KEY_SELECTION_FOR_INTERNAL_ASYMMETRIC_AUTHENTICATION,
                                        new PsoAlgorithm(PsoAlgorithm.Algorithm.AUTHENTICATE_ELC_ROLE_AUTHENTICATION),
                                        cardExtFs.key, false)
                                                .executeOn(cardExt))
                                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                                .flatMap(__ -> new ManageSecurityEnvironmentCommand(
                                        ManageSecurityEnvironmentCommand.MseUseCase.KEY_SELECTION_FOR_EXTERNAL_ASYMMETRIC_AUTHENTICATION,
                                        new PsoAlgorithm(PsoAlgorithm.Algorithm.AUTHENTICATE_ELC_ROLE_CHECK),
                                        autCertificateFromExt.getHolderReference().getContents())
                                                .executeOn(cardInt))
                                .validate(Response.ResponseStatus.SUCCESS::validateResult));
    }

    /**
     * get iccsn and challengeRandom and create a token for internalAuth
     * @param cardInt
     * @param cardIntFs
     * @return
     */
    private static ResultOperation<byte[]> createToken(final IHealthCard cardInt, final CardFileSystemData cardIntFs) {
        final ResultOperation<byte[]> resultOperation = result.flatMap(__ -> new ReadCommand(cardIntFs.gdo).executeOn(cardInt)
                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                .map(Response::getResponseData)
                .map(gdoData -> Arrays.copyOfRange(gdoData, 4, 12)));
        return resultOperation;
    }

    /**
     * run internalAuth and externalAuth
     * @param challengeIccsnInt
     * @param cardInt
     * @param cardExt
     * @return
     */
    private static void doInternalExternalAuth(final IHealthCard cardInt,
            final IHealthCard cardExt, final ResultOperation<byte[]> challengeIccsnInt) {
        result = challengeIccsnInt.flatMap(iccSnOfEgkData -> new GetChallengeCommand(16).executeOn(cardInt)
                .map(Response::getResponseData)
                .map(challengeFromEgkData -> Bytes.concatNullables(challengeFromEgkData, iccSnOfEgkData))).flatMap(
                        a -> new InternalAuthenticateCommand(
                                new PsoAlgorithm(PsoAlgorithm.Algorithm.AUTHENTICATE_ELC_ROLE_AUTHENTICATION), a)
                                        .executeOn(cardExt))
                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                .map(Response::getResponseData)
                .flatMap(cypheredAuthString -> new ExternalMutualAuthenticateCommand(
                        new PsoAlgorithm(PsoAlgorithm.Algorithm.AUTHENTICATE_ELC_ROLE_CHECK), cypheredAuthString, false)
                                .executeOn(cardInt))
                .validate(Response.ResponseStatus.SUCCESS::validateResult);
    }

}

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

package de.gematik.ti.healthcard.control.security;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cardfilesystem.egk21mf.Df;
import cardfilesystem.egk21mf.MrPin;
import cardfilesystem.egk2mf.Pin;
import de.gematik.ti.healthcard.control.entities.PinResult;
import de.gematik.ti.healthcard.control.entities.PinType;
import de.gematik.ti.healthcard.control.exceptions.HealthcardControlRuntimeException;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.IHealthCardType;
import de.gematik.ti.healthcardaccess.cardobjects.ApplicationIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.Format2Pin;
import de.gematik.ti.healthcardaccess.cardobjects.Password;
import de.gematik.ti.healthcardaccess.commands.GetPinStatusCommand;
import de.gematik.ti.healthcardaccess.commands.SelectCommand;
import de.gematik.ti.healthcardaccess.commands.VerifyCommand;
import de.gematik.ti.healthcardaccess.healthcards.*;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.healthcardaccess.result.Response.ResponseStatus;

/**
 * execute pin verification
  */
public class PinVerifier {
    private static final Logger LOG = LoggerFactory.getLogger(PinVerifier.class);
    private final IHealthCard cardHc;

    /**
     * init the card
     * @param card
     */
    public PinVerifier(final IHealthCard card) {
        cardHc = card;
    }

    public static boolean isCardValidForVerify(final IHealthCard card) {
        return card.getStatus().isValid();

    }

    public static int[] stringToIntarray(final String value) {
        final int[] intArray = new int[value.length()];
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                LOG.error("Contains an invalid digit");
                break;
            }
            intArray[i] = Integer.parseInt(String.valueOf(value.charAt(i)));
        }
        return intArray;
    }

    /**
     * do verify on CardType (eGK2 or eGK2.1)
     * @param pinValue
     * @param pinType
     * @return
     */
    public ResultOperation<PinResult> verifyPin(final int[] pinValue, final PinType pinType) {
        if (isCardValidForVerify(cardHc)) {
            final Map<Class, RunnableFuture<ResultOperation<PinResult>>> doByClass = new HashMap<>();
            doByClass.put(Egk2.class, new FutureTask<>(() -> verifyPinEgk2(pinValue, pinType)));
            doByClass.put(Egk21.class, new FutureTask<>(() -> verifyPinEgk21(pinValue, pinType)));
            doByClass.put(Hba2.class, new FutureTask<>(() -> verifyPinCH(pinValue, pinType, cardfilesystem.hba2mf.Pin.CH.PWID)));
            doByClass.put(Hba21.class, new FutureTask<>(() -> verifyPinCH(pinValue, pinType, cardfilesystem.hba21mf.Pin.CH.PWID)));
            doByClass.put(Smcb2.class, new FutureTask<>(() -> verifyPinCH(pinValue, pinType, cardfilesystem.smcb2mf.Pin.Smc.PWID)));
            doByClass.put(Smcb21.class, new FutureTask<>(() -> verifyPinCH(pinValue, pinType, cardfilesystem.smcb21mf.Pin.Smc.PWID)));

            final Class<? extends IHealthCardType> cardType = ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType().getClass();
            final ResultOperation<PinResult> result = getPinResultResultOperation(doByClass, cardType);
            if (result != null) {
                return result;
            }
        }
        throw new HealthcardControlRuntimeException("Card not valid for verifyPin: " + cardHc.getStatus().getClass().getSimpleName());
    }

    private ResultOperation<PinResult> getPinResultResultOperation(final Map<Class, RunnableFuture<ResultOperation<PinResult>>> doByClass,
            final Class<? extends IHealthCardType> cardType) {
        if (doByClass.containsKey(cardType)) {
            final RunnableFuture runnable = doByClass.get(cardType);
            runnable.run();
            try {
                final Object result = runnable.get();
                if (result != null) {
                    return (ResultOperation<PinResult>) result;
                }
            } catch (final Exception e) {
                throw new HealthcardControlRuntimeException(
                        "Execute VerifyPin for " + cardHc.getStatus().getClass().getSimpleName() + " failed: " + e.getLocalizedMessage());
            }
        } else {
            throw new HealthcardControlRuntimeException("Unknown cardType for verifyPin: " + cardHc.getStatus().getClass().getSimpleName());
        }
        return null;
    }

    private ResultOperation<PinResult> verifyPinCH(final int[] pinValue, final PinType pinType, final int pwid) {
        LOG.debug("pinType {}, pwid {}", pinType, pwid);
        final Password password = new Password(pwid);
        return getSelectRootResult()
                .flatMap(__ -> new GetPinStatusCommand(password, false).executeOn(cardHc))
                .map(Response::getResponseStatus).map(ResponseStatus::name).map(PinResult::new)
                .flatMap(pinResult -> new VerifyCommand(password, false, new Format2Pin(pinValue)).executeOn(cardHc)
                        .map(Response::getResponseStatus).map(status -> pinResult.setPinVerifiSuccess(status == ResponseStatus.SUCCESS)));
    }

    private ResultOperation<Response> getSelectRootResult() {
        return new SelectCommand(false, false).executeOn(cardHc).validate(ResponseStatus.SUCCESS::validateResult);
    }

    private ResultOperation<Response> getSelectRootHcaResult() {
        return getSelectRootResult().flatMap(__ -> new SelectCommand(new ApplicationIdentifier(Df.HCA.AID)).executeOn(cardHc))
                .validate(ResponseStatus.SUCCESS::validateResult);
    }

    /**
     * solution for <br>eGL2</b>
     * @param pinValue
     * @param pinType
     * @return
     */
    private ResultOperation<PinResult> verifyPinEgk21(final int[] pinValue, final PinType pinType) {
        LOG.debug("pinType: " + pinType);
        final int[] pwid = { 0 };
        if (pinType == PinType.PIN_CH) {
            pwid[0] = cardfilesystem.egk21mf.Pin.Ch.PWID;
            final Password password = new Password(pwid[0]);
            return getSelectRootResult()
                    .flatMap(__ -> new GetPinStatusCommand(password, false).executeOn(cardHc))
                    .map(Response::getResponseStatus).map(ResponseStatus::name).map(PinResult::new)
                    .flatMap(pinResult -> new VerifyCommand(password, false, new Format2Pin(pinValue)).executeOn(cardHc)
                            .map(Response::getResponseStatus).map(status -> pinResult.setPinVerifiSuccess(status == ResponseStatus.SUCCESS)));
        } else {
            switch (pinType) {
                case MRPIN_AMTS:
                    pwid[0] = MrPin.Amts.PWID;
                    break;
                case MRPIN_DPE:
                    pwid[0] = MrPin.Dpe.PWID;
                    break;
                case MRPIN_GDD:
                    pwid[0] = MrPin.Gdd.PWID;
                    break;
                case MRPIN_NFD:
                    pwid[0] = MrPin.Nfd.PWID;
                    break;
                case MRPIN_NFD_READ:
                    pwid[0] = MrPin.NfdRead.PWID;
                    break;
                case MRPIN_OSE:
                    pwid[0] = MrPin.Ose.PWID;
                    break;
                default:
                    LOG.warn("The 'pinType' " + pinType + "is not yet considered in process.");
            }
            final Password password = new Password(pwid[0]);
            return getSelectRootResult()
                    .flatMap(__ -> new GetPinStatusCommand(password, false).executeOn(cardHc))
                    .map(Response::getResponseStatus).map(ResponseStatus::name).map(PinResult::new)

                    .flatMap(pinResult -> new VerifyCommand(password, false, new Format2Pin(pinValue)).executeOn(cardHc)
                            .map(Response::getResponseStatus).map(status -> pinResult.setPinVerifiSuccess(status == ResponseStatus.SUCCESS)));
        }
    }

    /**
     * solution for <br>eGL2.1</b>
     * @param pinValue
     * @param pinType
     * @return
     */
    private ResultOperation<PinResult> verifyPinEgk2(final int[] pinValue, final PinType pinType) {
        LOG.debug("pinType: " + pinType);
        final int[] pwid = { 0 };
        if (pinType == PinType.PIN_CH) {
            pwid[0] = Pin.Ch.PWID;
            final Password password = new Password(pwid[0]);
            return getSelectRootResult()
                    .flatMap(__ -> new GetPinStatusCommand(password, false).executeOn(cardHc))
                    .map(Response::getResponseStatus).map(ResponseStatus::name).map(PinResult::new)
                    .flatMap(pinResult -> new VerifyCommand(password, false, new Format2Pin(pinValue)).executeOn(cardHc)
                            .map(Response::getResponseStatus).map(status -> pinResult.setPinVerifiSuccess(status == ResponseStatus.SUCCESS)));
        } else {
            final String[] aid = { "" };
            switch (pinType) {
                case MRPIN_AMTS:
                    aid[0] = cardfilesystem.egk2mf.df.hca.Df.Amts.AID;
                    pwid[0] = cardfilesystem.egk2mf.df.hca.df.amts.MrPin.Amts.PWID;
                    break;
                case MRPIN_DPE:
                    aid[0] = cardfilesystem.egk2mf.df.hca.Df.DPE.AID;
                    pwid[0] = cardfilesystem.egk2mf.df.hca.df.dpe.MrPin.Dpe.PWID;
                    break;
                case MRPIN_DPE_READ:
                    aid[0] = cardfilesystem.egk2mf.df.hca.Df.DPE.AID;
                    pwid[0] = cardfilesystem.egk2mf.df.hca.df.dpe.MrPin.DpeRead.PWID;
                    break;
                case MRPIN_GDD:
                    aid[0] = cardfilesystem.egk2mf.df.hca.Df.GDD.AID;
                    pwid[0] = cardfilesystem.egk2mf.df.hca.df.gdd.MrPin.Gdd.PWID;
                    break;
                case MRPIN_NFD:
                    aid[0] = cardfilesystem.egk2mf.df.hca.Df.NFD.AID;
                    pwid[0] = cardfilesystem.egk2mf.df.hca.df.nfd.MrPin.Nfd.PWID;
                    break;
                case MRPIN_NFD_READ:
                    aid[0] = cardfilesystem.egk2mf.df.hca.Df.NFD.AID;
                    pwid[0] = cardfilesystem.egk2mf.df.hca.df.nfd.MrPin.NfdRead.PWID;
                    break;
                case MRPIN_OSE:
                    aid[0] = cardfilesystem.egk2mf.df.hca.Df.OSE.AID;
                    pwid[0] = cardfilesystem.egk2mf.df.hca.df.ose.MrPin.Ose.PW;
                    break;
                default:
                    LOG.warn("The 'pinType' " + pinType + "is not yet considered in process.");
            }
            LOG.debug("aid: {}, pwid: {}", aid[0], pwid[0]);
            final Password password = new Password(pwid[0]);
            final ResultOperation<PinResult> pinResultResultOperation = getSelectRootHcaResult()
                    .flatMap(__ -> new SelectCommand(new ApplicationIdentifier(aid[0])).executeOn(cardHc))
                    .validate(ResponseStatus.SUCCESS::validateResult)
                    .flatMap(__ -> new GetPinStatusCommand(password, true).executeOn(cardHc))
                    .map(Response::getResponseStatus).map(ResponseStatus::name).map(PinResult::new)

                    .flatMap(pinResult -> new VerifyCommand(password, true, new Format2Pin(pinValue)).executeOn(cardHc)
                            .map(Response::getResponseStatus).map(status -> pinResult.setPinVerifiSuccess(status == ResponseStatus.SUCCESS)));
            return pinResultResultOperation;
        }
    }

}

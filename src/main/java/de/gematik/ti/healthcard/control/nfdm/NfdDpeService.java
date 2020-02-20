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

package de.gematik.ti.healthcard.control.nfdm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import de.gematik.ti.healthcard.control.CardUnlocker;
import de.gematik.ti.healthcard.control.CardVerifier;
import de.gematik.ti.healthcard.control.c2c.C2CSuccessState;
import de.gematik.ti.healthcard.control.c2c.CardToCardAuthExecutor;
import de.gematik.ti.healthcard.control.entities.PinResult;
import de.gematik.ti.healthcard.control.entities.PinType;
import de.gematik.ti.healthcard.control.entities.card.ProtocolEntry;
import de.gematik.ti.healthcard.control.entities.card.certificate.AuthenticationCertificateState;
import de.gematik.ti.healthcard.control.entities.card.pin.PinState;
import de.gematik.ti.healthcard.control.exceptions.HealthcardControlRuntimeException;
import de.gematik.ti.healthcard.control.exceptions.NfdDpeExtractException;
import de.gematik.ti.healthcard.control.nfdm.states.*;
import de.gematik.ti.healthcard.control.protocol.ProtocolRecordBuilder;
import de.gematik.ti.healthcard.control.protocol.ProtocolWriter;
import de.gematik.ti.healthcard.control.role.ProfessionalRole;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.IHealthCardStatus;
import de.gematik.ti.healthcardaccess.IHealthCardType;
import de.gematik.ti.healthcardaccess.cardobjects.ApplicationIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.FileIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.Password;
import de.gematik.ti.healthcardaccess.cardobjects.ShortFileIdentifier;
import de.gematik.ti.healthcardaccess.healthcards.Egk2;
import de.gematik.ti.healthcardaccess.healthcards.Egk21;
import de.gematik.ti.healthcardaccess.healthcards.HealthCardStatusValid;
import de.gematik.ti.healthcardaccess.operation.Result;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.operation.Subscriber;
import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.schema.gen.dpe.v1_0.DPE_DPE_Document;
import de.gematik.ti.schema.gen.nfd.v1_3.NFD_NFD_Document;
import de.gematik.ti.utils.codec.Hex;

/**
     */
public class NfdDpeService {

    private static final Logger LOG = LoggerFactory.getLogger(NfdDpeService.class);
    private static final String NFD_DATATYPE = "2";
    private final IHealthCard authorisingCard;
    private final IHealthCard cardToRead;
    private final PinState[] pinStateEnum = new PinState[1];
    private final ProfessionalRole[] role = new ProfessionalRole[1];
    private final AccessRule[] accessRules = new AccessRule[1];
    private final AccessRight[] accessRights = new AccessRight[1];
    private NfdDpeReader nfdDpeReader;
    private CardVerifier cardToReadVerifier;
    private Password passwordMrPinDpe;
    private Password passwordMrNfd;
    private ApplicationIdentifier nfdDfAid;
    private ApplicationIdentifier dpeDfAid;
    private ShortFileIdentifier statusNfdSfid;
    private ShortFileIdentifier nfdSfid;
    private ShortFileIdentifier dpeEfSfid;
    private FileIdentifier nfdEfFid;
    private FileIdentifier dpeEfFid;
    private ShortFileIdentifier dpeStatusEfSfid;

    /*
     * @see gemSpec_FM_NFDM_V1.4.0 --> Tabelle 17: Tab_FM_NFDM_005 – Operation ReadNFD
     *
     *
     * Der auf der eGK gespeicherte NFD ist valide gegen das XML- Schema für den NFD (s. [gemSpec_InfoNFDM#3]).
     */
    public NfdDpeService(final IHealthCard cardToRead, final IHealthCard authorisingCard) {
        initFileReference(cardToRead);
        this.authorisingCard = authorisingCard;
        this.cardToRead = cardToRead;
        nfdDpeReader = new NfdDpeReader(cardToRead);
    }

    /**
     * Get cardObject in dependent on cardType
     * @param cardHc
     */
    private void initFileReference(final IHealthCard cardHc) {
        if (cardHc.getStatus().isValid() && ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType() instanceof Egk2) {
            passwordMrPinDpe = new Password(cardfilesystem.egk2mf.df.hca.df.dpe.MrPin.Dpe.PWID);
            passwordMrNfd = new Password(cardfilesystem.egk2mf.df.hca.df.nfd.MrPin.Nfd.PWID);
            statusNfdSfid = new ShortFileIdentifier(cardfilesystem.egk2mf.df.hca.df.nfd.Ef.EfStatusNfd.SFID);
            nfdDfAid = new ApplicationIdentifier(cardfilesystem.egk2mf.df.hca.Df.Nfd.AID);
            nfdEfFid = new FileIdentifier(cardfilesystem.egk2mf.df.hca.df.nfd.Ef.Nfd.FID);
            nfdSfid = new ShortFileIdentifier(cardfilesystem.egk2mf.df.hca.df.nfd.Ef.Nfd.SFID);
            dpeStatusEfSfid = new ShortFileIdentifier(cardfilesystem.egk2mf.df.hca.df.dpe.Ef.StatusDPE.SFID);
            dpeEfFid = new FileIdentifier(cardfilesystem.egk2mf.df.hca.df.dpe.Ef.Dpe.FID);
            dpeDfAid = new ApplicationIdentifier(cardfilesystem.egk2mf.df.hca.Df.Dpe.AID);
            dpeEfSfid = new ShortFileIdentifier(cardfilesystem.egk2mf.df.hca.df.dpe.Ef.Dpe.SFID);
        } else if (cardHc.getStatus().isValid() && ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType() instanceof Egk21) {
            passwordMrPinDpe = new Password(cardfilesystem.egk21mf.MrPin.Dpe.PWID);
            passwordMrNfd = new Password(cardfilesystem.egk21mf.MrPin.Nfd.PWID);
            nfdDfAid = new ApplicationIdentifier(cardfilesystem.egk21mf.df.hca.Df.Nfd.AID);
            statusNfdSfid = new ShortFileIdentifier(cardfilesystem.egk21mf.df.hca.df.nfd.Ef.EfStatusNfd.SFID);
            nfdEfFid = new FileIdentifier(cardfilesystem.egk21mf.df.hca.df.nfd.Ef.Nfd.FID);
            nfdSfid = new ShortFileIdentifier(cardfilesystem.egk21mf.df.hca.df.nfd.Ef.Nfd.SFID);
            dpeStatusEfSfid = new ShortFileIdentifier(cardfilesystem.egk21mf.df.hca.df.dpe.Ef.StatusDPE.SFID);
            dpeEfFid = new FileIdentifier(cardfilesystem.egk21mf.df.hca.df.dpe.Ef.Dpe.FID);
            dpeDfAid = new ApplicationIdentifier(cardfilesystem.egk21mf.df.hca.Df.Dpe.AID);
            dpeEfSfid = new ShortFileIdentifier(cardfilesystem.egk21mf.df.hca.df.dpe.Ef.Dpe.SFID);
        } else {
            throw new HealthcardControlRuntimeException(
                    "Cardtype " + ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType() + " is invalid or unknown");
        }
    }

    /**
     * Returns a document with patient emergency data after successful C2C authentication and PIN Verification. 
     * The parameter emergencyIndicator to be passed regulate access in addition to C2C authentication, 
     * updateIndicator whether the document should be updated (true) or only read access should take place (false).
     * @param emergencyIndicator
     * @param updateIndicator
     * @return NFD_NFD_Document
     */
    public NFD_NFD_Document getNfdDocument(final boolean emergencyIndicator, final boolean updateIndicator) {
        final NFD_NFD_Document[] nfdDocument = new NFD_NFD_Document[1];
        final ResultOperation<C2CSuccessState> resultOperaitonAuth = prepareAuth(emergencyIndicator, updateIndicator, passwordMrPinDpe, DataType.NFD);
        final ResultOperation<Document> resultOperaiton = resultOperaitonAuth.flatMap(__ -> prepareGetDocument(nfdDfAid, nfdSfid, statusNfdSfid,
                nfdEfFid));
        resultOperaiton
                .map(document -> NFD_NFD_Document.deserialize(document.getDocumentElement()))
                .subscribe(new NfdDpeServiceSubscriber<NFD_NFD_Document>(nfdDocument));
        return nfdDocument[0];
    }

    /**
     * Returns a document with data for the personal declaration of the insured after successful C2C authentication and PIN Verification. 
     * The parameter emergencyIndicator to be passed regulate access in addition to C2C authentication, 
     * updateIndicator whether the document should be updated (true) or only read access should take place (false).
     * @param emergencyIndicator
     * @param updateIndicator
     * @return DPE_DPE_Document
     */
    public DPE_DPE_Document getDpeDocument(final boolean emergencyIndicator, final boolean updateIndicator) {
        final DPE_DPE_Document[] dpeDocument = new DPE_DPE_Document[1];
        final ResultOperation<C2CSuccessState> resultOperaitonAuth = prepareAuth(emergencyIndicator, updateIndicator, passwordMrPinDpe, DataType.DPE);
        final ResultOperation<Document> resultOperaiton = resultOperaitonAuth.flatMap(__ -> prepareGetDocument(dpeDfAid, dpeEfSfid,
                dpeStatusEfSfid, dpeEfFid));
        resultOperaiton
                .map(document -> DPE_DPE_Document.deserialize(document.getDocumentElement()))
                .subscribe(new NfdDpeServiceSubscriber<DPE_DPE_Document>(dpeDocument));
        return dpeDocument[0];
    }

    /**
     *
     * @param emergencyIndicator
     * @param updateIndicator
     * @param password
     * @return
     */
    private ResultOperation<C2CSuccessState> prepareAuth(final boolean emergencyIndicator, final boolean updateIndicator, final Password password,
            final DataType dataType) {
        cardToReadVerifier = new CardVerifier(cardToRead);
        final CardVerifier authorisingCardVerifier = new CardVerifier(authorisingCard); // für C2C
        LOG.debug("emergencyIndicator: {}, updateIndicator: {}", emergencyIndicator, updateIndicator);
        final CardUnlocker cardUnlocker = new CardUnlocker(authorisingCard);
        return cardToReadVerifier
                .checkCard().validate(Response.ResponseStatus.SUCCESS::validateResult).flatMap(__ -> cardToReadVerifier.checkAuthCertificate())
                .validate(AuthenticationCertificateState.VALIDATION_SUCCESS::getValidationResult)
                .flatMap(__ -> cardToReadVerifier.getPinState(password))
                .map(pinState -> pinStateEnum[0] = pinState).flatMap(__ -> cardUnlocker.getProfessionalRole())
                .map(professionalRole -> role[0] = professionalRole)
                .map(__ -> AccessRule.getAccessRule(pinStateEnum[0], emergencyIndicator, updateIndicator))
                .map(accessRule -> accessRules[0] = accessRule)
                .map(__ -> new AccessRightWrapper(cardToRead, dataType).getAccessRight(role[0], accessRules[0]))
                .map(accessRight -> accessRights[0] = accessRight)
                .flatMap(__ -> checkAccessRights(accessRights[0]))
                .flatMap(__ -> authorisingCardVerifier.verifyPin(PinType.PIN_CH))
                .validate(pr -> {
                    return validatePinResult(pr);
                })
                .flatMap(__ -> CardToCardAuthExecutor.authenticateC2C(cardToRead, authorisingCard))
                .validate(C2CSuccessState.C2C_SUCCESS::validateSuccessState);
    }

    /**
     *
     * @param aid ApplicationIdentifier of DF.NFD or DF.DPE
     * @param efSfid ShortFileIdentifier von EF.NFD or EF.DPE
     * @param statusEfSfid EF.Status von NFD or DPE
     * @param fid FileIdentifier von EF.NFD or EF.DPE
     * @return
     */
    private ResultOperation<Document> prepareGetDocument(final ApplicationIdentifier aid,
            final ShortFileIdentifier efSfid, final ShortFileIdentifier statusEfSfid, final FileIdentifier fid) {
        final CardUnlocker cardUnlocker = new CardUnlocker(authorisingCard);
        nfdDpeReader = new NfdDpeReader(cardToRead);
        return createProtocolRecord().map(this::writeProtocol)
                .flatMap(__ -> verifyPinByAccessRight(accessRights[0]))
                .validate(pr -> {
                    return validatePinResult(pr);
                })
                .flatMap(__ -> createProtocolRecord()).map(this::writeProtocol).flatMap(__ -> nfdDpeReader.readLifeCycleState(aid)).validate(
                        NfdDpeLifeCycleState.LCS_OPERATIONAL_STATE_ACTIVATED::validateLifeCycleState)
                .flatMap(__ -> nfdDpeReader.checkConsistency(aid, statusEfSfid))
                .validate(NfdDpeState.NO_TRANSACTIONS_OPEN::validateState).flatMap(__ -> nfdDpeReader.checkContainerVersion(aid, statusEfSfid))
                .validate(
                        NfdDpeVersionState.NFD_DPE_VERSION_PERMITTED::validateVersion)
                .flatMap(__ -> nfdDpeReader.checkSize(aid, efSfid))
                .validate(NfdDpeDataAvailableState.DATA_AVAILABLE::validateDataAvailableState).flatMap(__ -> nfdDpeReader.extractDocument(aid, efSfid, fid));

    }

    private Result<PinResult> validatePinResult(final PinResult pr) {
        if (pr.isPinVerifiSuccess()) {
            return Result.success(pr);
        } else {
            return Result.failure(new Exception(pr.getErrorTextByFailure()));
        }
    }

    // TODO: it is only for read. For write, erase etc result for 'typeAccess' is not right.
    private ResultOperation<byte[]> createProtocolRecord() {
        String typeAccess = "";
        LOG.debug("accessRules[0]: " + accessRules[0]);
        switch (accessRules[0]) {
            case R1:
                typeAccess = "N";
                break;
            case R2:
                typeAccess = "A";
                break;
        }
        LOG.debug("typeAccess: " + typeAccess);
        LOG.debug("accessRights[0]: " + accessRights[0]);
        switch (accessRights[0]) {
            case ACCESS_MRPIN_NFD:
            case ACCESS_MRPIN_NFD_READ:
            case ACCESS_MRPIN_DPE_READ:
            case ACCESS_MRPIN_DPE:
                typeAccess = "R";
                break;
            case ACCESS_NO_PIN:
                typeAccess = "r";
        }
        LOG.debug("typeAccess: " + typeAccess);
        final byte[] protocolRecord = ProtocolRecordBuilder.buildRecord(authorisingCard, NFD_DATATYPE, typeAccess);
        LOG.debug("ProtocolEntry: " + Hex.encodeHexString(protocolRecord));
        return ResultOperation.unitRo(protocolRecord);
    }

    private ResultOperation writeProtocol(final byte[] protocolRecord) {
        final IHealthCardStatus healthCardStatus = cardToRead.getStatus();
        final IHealthCardType cardType = ((HealthCardStatusValid) healthCardStatus).getHealthCardType();
        final ProtocolWriter protocolWriter = new ProtocolWriter(cardToRead);
        if (cardType instanceof Egk2) {
            return protocolWriter.write(ProtocolEntry.createProtocolEntry(protocolRecord));
        } else if (cardType instanceof Egk21) {
            return protocolWriter.write(ProtocolEntry.createProtocolEntry(protocolRecord));
        }
        return ResultOperation.unitRo(Response.ResponseStatus.SUCCESS);
    }

    /**
     * Check ACCESS prior to C2C and writeProtocol
     * @param accessRight
     * @return
     */
    private ResultOperation<AccessRight> checkAccessRights(final AccessRight accessRight) {
        if (accessRight == AccessRight.NO_ACCESS) {
            throw new NfdDpeExtractException("You are not authorized to read the emergency data (NFD) or personal explanations (DPE)");
        }
        return ResultOperation.unitRo(accessRight);
    }

    private ResultOperation<PinResult> verifyPinByAccessRight(final AccessRight accessRight) {
        boolean isPinVerificationRequired = true;
        PinType pinType = null;

        LOG.debug("accessRight:" + accessRight);
        switch (accessRight) {
            case ACCESS_MRPIN_NFD:
                pinType = PinType.MRPIN_NFD;
                break;
            case ACCESS_MRPIN_NFD_READ:
                pinType = PinType.MRPIN_NFD_READ;
                break;
            case ACCESS_MRPIN_DPE:
                pinType = PinType.MRPIN_DPE;
                break;
            case ACCESS_MRPIN_DPE_READ: // for Egk2 only
                pinType = PinType.MRPIN_DPE_READ;
                break;
            case NO_ACCESS:
                throw new NfdDpeExtractException("You are not authorized to read the emergency data (NFD) or personal explanations (DPE)");
            default:
                isPinVerificationRequired = false;
                break;
        }

        if (isPinVerificationRequired) {
            return cardToReadVerifier.verifyPin(pinType);
        }
        return ResultOperation.unitRo(new PinResult("NO_ERROR").setPinVerifiSuccess(true));
    }

    /**
     * For receiving NFD_Document or DPE_Document
     * @param <T>
     */
    public class NfdDpeServiceSubscriber<T> implements Subscriber {
        private final T[] document;

        public NfdDpeServiceSubscriber(final T[] document) {
            this.document = document;
        }

        @Override
        public void onSuccess(final Object value) {
            document[0] = (T) value;
            LOG.debug("Success on extract Document");
        }

        @Override
        public void onError(final Throwable t) throws RuntimeException {
            LOG.error("Error on extract Document occured: " + t);
        }
    }
}

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

package de.gematik.ti.healthcard.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cardfilesystem.Egk21FileSystem;
import cardfilesystem.egk2mf.Df;
import de.gematik.ti.healthcard.control.entities.PinResult;
import de.gematik.ti.healthcard.control.entities.PinType;
import de.gematik.ti.healthcard.control.entities.card.certificate.AuthenticationCertificateState;
import de.gematik.ti.healthcard.control.entities.card.pin.PinState;
import de.gematik.ti.healthcard.control.exceptions.HealthcardControlRuntimeException;
import de.gematik.ti.healthcard.control.security.PinVerifier;
import de.gematik.ti.healthcardaccess.AbstractHealthCardCommand;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.cardobjects.ApplicationIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.Password;
import de.gematik.ti.healthcardaccess.cardobjects.ShortFileIdentifier;
import de.gematik.ti.healthcardaccess.commands.GetPinStatusCommand;
import de.gematik.ti.healthcardaccess.commands.ReadCommand;
import de.gematik.ti.healthcardaccess.commands.SelectCommand;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.openhealthcard.events.control.RequestTransmitterPinNumber;

/**
 * Contains functionalities to check Card and CardContent
 * TODO: AID, SFID in this class should initiated according to card-generation.
    */
public class CardVerifier {
    private static final Logger LOG = LoggerFactory.getLogger(CardVerifier.class);
    private final IHealthCard card;

    public CardVerifier(final IHealthCard card) {
        this.card = card;
    }

    /**
     * Checks the /MF/DF.ESIGN/EF.C.CH.AUT certificate
     *
     * @return result of validation
     *
     */
    public ResultOperation<AuthenticationCertificateState> checkAuthCertificate() {
        // Kon  EGK 00 A4 04 0C 0A A0 00 00 01 67 45 53 49 47 4E --- SELECT ESIGN
        final ApplicationIdentifier aid = new ApplicationIdentifier(Egk21FileSystem.DF.ESIGN.AID);
        final AbstractHealthCardCommand selectESignCommand = new SelectCommand(aid);

        // Kon  EGK 00 B0 81 00 00 00 00 --- READ BYNARY -SFID=’01’
        final ShortFileIdentifier sfId = new ShortFileIdentifier(Egk21FileSystem.DF.ESIGN.EF.C_CH_AUT_R2048.SFID);
        final AbstractHealthCardCommand readESignCommand = new ReadCommand(sfId);

        return selectESignCommand.executeOn(card).flatMap(__ -> readESignCommand.executeOn(card))
                .map(AuthenticationCertificateState::getCertificateValidationResult);
    }

    /**
     * Performs a PIN entry to a card
     * The operation causes a prompt for entering the PinReference designated PIN - regardless of whether the PIN previously successful
     * entered and checked. The card reader transmits the PIN for verification to the chosen card. The test result provides information
     * about the success or failure of the PIN verification and, if necessary, the number of remaining PIN entry attempts.
     *
     * @return
     * @TODO implementation of TUC_MOKT_412 verifyPIN (gemSpec_MobKT_V2.11.1)
     * @TODO implementation of TIP1-A_2286 verifyPIN (gemKPT_Arch_TIP_V2.6.0)
     */
    public ResultOperation<PinResult> verifyPin(final PinType pinType) {
        LOG.debug("pinType: " + pinType);
        final boolean valid = card.getStatus().isValid();
        LOG.debug("valid: " + valid);
        final boolean verifable = PinVerifier.isCardValidForVerify(card);
        LOG.debug("verifable: " + verifable);
        if (!valid) {
            throw new HealthcardControlRuntimeException("card is invalid");
        } else if (!verifable) {
            throw new HealthcardControlRuntimeException("verification for card is not yet supported");
        }

        final CallbackHandlePin callback = new CallbackHandlePin(card);
        final Runnable task = () -> {
            final RequestTransmitterPinNumber requestTransmitterPinNumber = new RequestTransmitterPinNumber();
            requestTransmitterPinNumber.request(callback, pinType.toString(), "Please input pin for " + pinType);
        };
        new Thread(task).start();

        return callback.call();
    }

    /**
     * The operation returns the status of the pin of a selected card designated by PinReference.
     * The PIN status contains information about the security status, whether it is deactivated, the remaining PIN entry attempts and the transport status.
     *
     * @param password ICardItem
     * @return pinState result
     */
    public ResultOperation<PinState> getPinState(final Password password) {
        final AbstractHealthCardCommand getPinStatusCommand = new GetPinStatusCommand(password, false);
        return getPinStatusCommand.executeOn(card).map(PinState::getPinStateResult);
    }

    /**
     * Checks whether there is a technical blocking of the card
     *
     * @return status of hca folder (response status should be success
     */
    public ResultOperation<Response> checkCard() {
        final ApplicationIdentifier aid = new ApplicationIdentifier(Df.Hca.AID);
        final AbstractHealthCardCommand getHcaStatusCommand = new SelectCommand(aid);
        return getHcaStatusCommand.executeOn(card);
    }

}

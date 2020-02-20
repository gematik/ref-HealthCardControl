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

import cardfilesystem.egk21mf.Df;
import cardfilesystem.egk21mf.df.esign.Ef;
import de.gematik.ti.healthcard.control.entities.C2CMode;
import de.gematik.ti.healthcard.control.exceptions.AuthenticationCertificateException;
import de.gematik.ti.healthcard.control.role.ProfessionalRole;
import de.gematik.ti.healthcardaccess.AbstractHealthCardCommand;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.IHealthCardStatus;
import de.gematik.ti.healthcardaccess.IHealthCardType;
import de.gematik.ti.healthcardaccess.cardobjects.ApplicationIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.ShortFileIdentifier;
import de.gematik.ti.healthcardaccess.commands.ReadCommand;
import de.gematik.ti.healthcardaccess.commands.SelectCommand;
import de.gematik.ti.healthcardaccess.healthcards.Egk21;
import de.gematik.ti.healthcardaccess.healthcards.Hba21;
import de.gematik.ti.healthcardaccess.healthcards.HealthCardStatusValid;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;

/**
    */
public class CardUnlocker {

    private final IHealthCard card;

    public CardUnlocker(final IHealthCard card) {
        this.card = card;
    }

    /**
     * Operation performs card-to-card authentication between two smart cards.
     * TargetCardRef identifies the card to be unlocked (e.g., eGK), SourceCardRef the
     * activating (e.g., SMC-B).
     *
     * @TODO implementation of TIP1-A_2292 (gemKPT_Arch_TIP_V2.6.0)
     * @TODO implementation of TUC_MOKT_405 authenticateCardToCard (gemSpec_MobKT_V2.11.1)
     *
     * @param targetCard identifies the card to be unlocked (e.g., eGK)
     * @param c2CMode C2CMode determines the type of authentication
     * @return
     */
    public boolean doCard2Card(final IHealthCard targetCard, final C2CMode c2CMode) {
        final IHealthCard sourceCard = card; // identifies the card for activating (e.g., SMC-B)
        return false;
    }

    /**
     * Determine the technical role of a card
     *
     * @return professional role
     */
    public ResultOperation<ProfessionalRole> getProfessionalRole() {

        final IHealthCardStatus healthCardStatus = card.getStatus();

        final ApplicationIdentifier aid = new ApplicationIdentifier(Df.Esign.AID);

        ShortFileIdentifier sfId = null;

        if (healthCardStatus.isValid()) {
            sfId = new ShortFileIdentifier(Ef.CchAutR2048.SFID); // 0x1
            final IHealthCardType cardType = ((HealthCardStatusValid) healthCardStatus).getHealthCardType();
            if (cardType instanceof Egk21) {
                sfId = new ShortFileIdentifier(cardfilesystem.egk21mf.df.esign.Ef.CchAutE256.SFID); // 0x04
            } else if (cardType instanceof Hba21) {
                sfId = new ShortFileIdentifier(cardfilesystem.hba21mf.df.esign.Ef.ChpAutE256.SFID); // 0x06
            }
        } else {
            throw new AuthenticationCertificateException("failed to extract role information from certificate");
        }

        // Read ESIGN
        final AbstractHealthCardCommand selectESignCommand = new SelectCommand(aid);
        final AbstractHealthCardCommand readESignCommand = new ReadCommand(sfId);

        return selectESignCommand.executeOn(card)
                .flatMap(__ -> readESignCommand.executeOn(card))
                .map(ProfessionalRole::getRole);
    }
}

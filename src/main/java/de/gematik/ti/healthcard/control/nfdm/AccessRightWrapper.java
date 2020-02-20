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

import de.gematik.ti.healthcard.control.exceptions.HealthcardControlRuntimeException;
import de.gematik.ti.healthcard.control.nfdm.states.AccessRight;
import de.gematik.ti.healthcard.control.nfdm.states.AccessRule;
import de.gematik.ti.healthcard.control.nfdm.states.DataType;
import de.gematik.ti.healthcard.control.role.ProfessionalRole;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.IHealthCardType;
import de.gematik.ti.healthcardaccess.healthcards.Egk2;
import de.gematik.ti.healthcardaccess.healthcards.Egk21;
import de.gematik.ti.healthcardaccess.healthcards.HealthCardStatusValid;

/**
 * choose the suitable {@link AccessRight} by cardGenaration and DataType
  */
public class AccessRightWrapper {
    private final IHealthCard healthCard;
    private final DataType dataType;

    public AccessRightWrapper(IHealthCard cardToRead, DataType dataType) {
        this.healthCard = cardToRead;
        this.dataType = dataType;
    }

    public AccessRight getAccessRight(ProfessionalRole professionalRole, AccessRule accessRule) {
        IHealthCardType healthCardType = ((HealthCardStatusValid) healthCard.getStatus()).getHealthCardType();
        if (dataType == DataType.NFD) {
            return AccessRight.getAccessRightNfd(professionalRole, accessRule);
        }

        else if (dataType == DataType.DPE) {
            if (healthCardType instanceof Egk2) {
                return AccessRight.getAccessRightDpeEgk2(professionalRole, accessRule);
            } else if (healthCardType instanceof Egk21) {
                return AccessRight.getAccessRightDpeEgk21(professionalRole, accessRule);
            } else {
                throw new HealthcardControlRuntimeException("Cardtype " + healthCardType + " is in this case invalid.");
            }
        } else {
            throw new HealthcardControlRuntimeException("DataType " + dataType + " is in this case invalid.");
        }

    }
}

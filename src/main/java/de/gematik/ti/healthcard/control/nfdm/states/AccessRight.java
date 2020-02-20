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

package de.gematik.ti.healthcard.control.nfdm.states;

import java.util.HashMap;
import java.util.Map;

import de.gematik.ti.healthcard.control.role.ProfessionalRole;

/**
 * Functionalities to check the access right to read NFD data
 *
   */
public enum AccessRight {
    ACCESS_MRPIN_NFD,
    ACCESS_MRPIN_NFD_READ,
    ACCESS_MRPIN_DPE,
    ACCESS_MRPIN_DPE_READ,
    ACCESS_NO_PIN,
    EMERGENCY_ACCESS_NO_PIN,
    NO_ACCESS;

    private static final int RULE_1 = 1;
    private static final int RULE_2 = 2;
    private static final int RULE_3 = 3;

    private static final Map<ProfessionalRole, Integer> ROLE_INTEGER_MAP_NFD = new HashMap<ProfessionalRole, Integer>() {
        {
            put(ProfessionalRole.ARZT, RULE_1);
            put(ProfessionalRole.PRAXIS_ARZT, RULE_1);
            put(ProfessionalRole.KRANKENHAUS, RULE_1);
            put(ProfessionalRole.ZAHNARZT, RULE_1);
            put(ProfessionalRole.PRAXIS_ZAHNARZT, RULE_1);
            put(ProfessionalRole.APOTHEKER, RULE_2);
            put(ProfessionalRole.MITARBEITER_APOTHEKE, RULE_2);
            put(ProfessionalRole.PS_PSYCHOTHERAPEUT, RULE_2);
            put(ProfessionalRole.PRAXIS_PSYCHOTHERAPEUT, RULE_2);
            put(ProfessionalRole.OEFFENTLICHE_APOTHEKE, RULE_2);
            put(ProfessionalRole.BUNDESWEHR_APOTHEKE, RULE_2);
            put(ProfessionalRole.KRANKENHAUS_APOTHEKE, RULE_2);
            put(ProfessionalRole.ANDERER_HEILBERUF, RULE_3);
        }
    };

    /* */
    private static final Map<ProfessionalRole, Integer> ROLE_INTEGER_MAP_DPE_EGK2 = new HashMap<ProfessionalRole, Integer>() {
        {
            put(ProfessionalRole.ARZT, RULE_1);
            put(ProfessionalRole.PRAXIS_ARZT, RULE_1);
            put(ProfessionalRole.KRANKENHAUS, RULE_1);
            put(ProfessionalRole.VERSICHERTER, RULE_2);
        }
    };

    private static final Map<ProfessionalRole, Integer> ROLE_INTEGER_MAP_DPE_EGK21 = new HashMap<ProfessionalRole, Integer>() {
        {
            put(ProfessionalRole.ARZT, RULE_1);
            put(ProfessionalRole.PRAXIS_ARZT, RULE_1);
            put(ProfessionalRole.KRANKENHAUS, RULE_1);
        }
    };

    private static AccessRight getRule1AccessRight(final AccessRule accessRule, AccessRight accessRightDefault) {
        AccessRight right = accessRightDefault;
        if (AccessRule.R1.equals(accessRule)) {
            right = EMERGENCY_ACCESS_NO_PIN;

        } else if (AccessRule.R2.equals(accessRule) || AccessRule.R4.equals(accessRule)) {
            right = ACCESS_NO_PIN;
        }
        return right;
    }

    /**
     * Returns the access right to read NFD data
     * @param professionalRole role extracted from certificate
     * @param accessRule Access rule, which is applied
     * @return Access right
     */
    public static AccessRight getAccessRightNfd(ProfessionalRole professionalRole, AccessRule accessRule) {
        AccessRight right = NO_ACCESS;
        if (ROLE_INTEGER_MAP_NFD.containsKey(professionalRole)) {
            final int ruleInt = ROLE_INTEGER_MAP_NFD.get(professionalRole);
            switch (ruleInt) {
                case RULE_1:
                    right = getRule1AccessRight(accessRule, ACCESS_MRPIN_NFD);
                    break;

                case RULE_2:
                    if (AccessRule.R3.equals(accessRule) || AccessRule.R4.equals(accessRule)) {
                        right = ACCESS_MRPIN_NFD_READ;
                    }
                    break;

                case RULE_3:
                    if (AccessRule.R1.equals(accessRule)) {
                        right = EMERGENCY_ACCESS_NO_PIN;
                    } else if (AccessRule.R3.equals(accessRule) || AccessRule.R4.equals(accessRule)) {
                        right = ACCESS_MRPIN_NFD_READ;
                    }
                    break;

                default: // NO_ACCESS
            }
        }
        return right;
    }

    /**
     * Returns the access right to read NFD data
     * @param professionalRole role extracted from certificate
     * @param accessRule Access rule, which is applied
     * @return Access right
     */
    public static AccessRight getAccessRightDpeEgk2(ProfessionalRole professionalRole, AccessRule accessRule) {
        AccessRight right = NO_ACCESS;
        if (ROLE_INTEGER_MAP_DPE_EGK2.containsKey(professionalRole)) {
            final int ruleInt = ROLE_INTEGER_MAP_DPE_EGK2.get(professionalRole);
            switch (ruleInt) {
                case RULE_1:
                    right = getRule1AccessRight(accessRule, ACCESS_MRPIN_DPE);
                    break;

                case RULE_2:
                    if (AccessRule.R3.equals(accessRule) || AccessRule.R4.equals(accessRule)) {
                        right = ACCESS_MRPIN_DPE_READ;
                    }
                    break;

                case RULE_3:
                    if (AccessRule.R1.equals(accessRule)) {
                        right = EMERGENCY_ACCESS_NO_PIN;
                    } else if (AccessRule.R3.equals(accessRule) || AccessRule.R4.equals(accessRule)) {
                        right = ACCESS_MRPIN_DPE_READ;
                    }
                    break;

                default: // NO_ACCESS
            }
        }
        return right;
    }

    /**
     * Returns the access right to read NFD data
     * @param professionalRole role extracted from certificate
     * @param accessRule Access rule, which is applied
     * @return Access right
     */
    public static AccessRight getAccessRightDpeEgk21(ProfessionalRole professionalRole, AccessRule accessRule) {
        AccessRight right = NO_ACCESS;
        if (ROLE_INTEGER_MAP_DPE_EGK21.containsKey(professionalRole)) {
            final int ruleInt = ROLE_INTEGER_MAP_DPE_EGK21.get(professionalRole);
            if (ruleInt == RULE_1) {
                right = getRule1AccessRight(accessRule, ACCESS_MRPIN_DPE);
            }
        }
        return right;
    }
}

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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import de.gematik.ti.healthcard.control.nfdm.AccessRightWrapper;
import de.gematik.ti.healthcard.control.role.ProfessionalRole;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.IHealthCardType;
import de.gematik.ti.healthcardaccess.healthcards.Egk2;
import de.gematik.ti.healthcardaccess.healthcards.HealthCardStatusValid;

/**
 * Test {@link AccessRight}
  */
public class AccessRightTest {

    private static IHealthCard egk2Health;
    private static AccessRightWrapper accessRightWrapperEgk2;

    @BeforeClass
    public static void setUp() {
        egk2Health = Mockito.mock(IHealthCard.class);

        IHealthCardType type = Mockito.mock(IHealthCardType.class);
        HealthCardStatusValid valid = Mockito.mock(HealthCardStatusValid.class);
        Mockito.when(valid.getHealthCardType()).thenReturn(new Egk2());
        Mockito.when(egk2Health.getStatus()).thenReturn(valid);

        accessRightWrapperEgk2 = new AccessRightWrapper(egk2Health, DataType.NFD);
    }

    @Test
    public void testRuleR1() {
        AccessRight doctorR1Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.ARZT, AccessRule.R1);
        Assert.assertEquals(AccessRight.EMERGENCY_ACCESS_NO_PIN, doctorR1Right);

        AccessRight pharmacistR1Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.APOTHEKER, AccessRule.R1);
        Assert.assertEquals(AccessRight.NO_ACCESS, pharmacistR1Right);

        AccessRight paramedicR1Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.ANDERER_HEILBERUF, AccessRule.R1);
        Assert.assertEquals(AccessRight.EMERGENCY_ACCESS_NO_PIN, paramedicR1Right);

        AccessRight insuredR1Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.VERSICHERTER, AccessRule.R1);
        Assert.assertEquals(AccessRight.NO_ACCESS, insuredR1Right);

        AccessRight hospitalR1Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.KRANKENHAUS, AccessRule.R1);
        Assert.assertEquals(AccessRight.EMERGENCY_ACCESS_NO_PIN, hospitalR1Right);
    }

    @Test
    public void testRuleR2() {
        AccessRight doctorR2Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.ARZT, AccessRule.R2);
        Assert.assertEquals(AccessRight.ACCESS_NO_PIN, doctorR2Right);

        AccessRight pharmacistR2Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.APOTHEKER, AccessRule.R2);
        Assert.assertEquals(AccessRight.NO_ACCESS, pharmacistR2Right);

        AccessRight paramedicR2Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.ANDERER_HEILBERUF, AccessRule.R2);
        Assert.assertEquals(AccessRight.NO_ACCESS, paramedicR2Right);

        AccessRight insuredR2Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.VERSICHERTER, AccessRule.R2);
        Assert.assertEquals(AccessRight.NO_ACCESS, insuredR2Right);

        AccessRight hospitalR2Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.KRANKENHAUS, AccessRule.R2);
        Assert.assertEquals(AccessRight.ACCESS_NO_PIN, hospitalR2Right);
    }

    @Test
    public void testRuleR3() {
        AccessRight doctorR3Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.ARZT, AccessRule.R3);
        Assert.assertEquals(AccessRight.ACCESS_MRPIN_NFD, doctorR3Right);

        AccessRight pharmacistR3Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.APOTHEKER, AccessRule.R3);
        Assert.assertEquals(AccessRight.ACCESS_MRPIN_NFD_READ, pharmacistR3Right);

        AccessRight paramedicR3Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.ANDERER_HEILBERUF, AccessRule.R3);
        Assert.assertEquals(AccessRight.ACCESS_MRPIN_NFD_READ, paramedicR3Right);

        AccessRight insuredR3Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.VERSICHERTER, AccessRule.R3);
        Assert.assertEquals(AccessRight.NO_ACCESS, insuredR3Right);

        AccessRight hospitalR3Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.KRANKENHAUS, AccessRule.R3);
        Assert.assertEquals(AccessRight.ACCESS_MRPIN_NFD, hospitalR3Right);
    }

    @Test
    public void testRuleR4() {
        AccessRight doctorR4Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.ARZT, AccessRule.R4);
        Assert.assertEquals(AccessRight.ACCESS_NO_PIN, doctorR4Right);

        AccessRight pharmacistR4Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.APOTHEKER, AccessRule.R4);
        Assert.assertEquals(AccessRight.ACCESS_MRPIN_NFD_READ, pharmacistR4Right);

        AccessRight paramedicR4Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.ANDERER_HEILBERUF, AccessRule.R4);
        Assert.assertEquals(AccessRight.ACCESS_MRPIN_NFD_READ, paramedicR4Right);

        AccessRight insuredR4Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.VERSICHERTER, AccessRule.R4);
        Assert.assertEquals(AccessRight.NO_ACCESS, insuredR4Right);

        AccessRight hospitalR4Right = accessRightWrapperEgk2.getAccessRight(ProfessionalRole.KRANKENHAUS, AccessRule.R4);
        Assert.assertEquals(AccessRight.ACCESS_NO_PIN, hospitalR4Right);
    }
}

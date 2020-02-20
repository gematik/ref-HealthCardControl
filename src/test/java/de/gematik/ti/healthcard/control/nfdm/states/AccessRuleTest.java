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
import org.junit.Test;

import de.gematik.ti.healthcard.control.entities.card.pin.PinState;

/**
 * Test {@link AccessRule}
  */
public class AccessRuleTest {

    @Test
    public void testGetAccessRuleRule1() {
        AccessRule rule1 = AccessRule.getAccessRule(PinState.RETRY_COUNTER_COUNT_03, true, false);
        Assert.assertEquals(AccessRule.R1, rule1);
    }

    @Test
    public void testGetAccessRuleRule2() {
        AccessRule rule2 = AccessRule.getAccessRule(PinState.RETRY_COUNTER_COUNT_03, false, true);
        Assert.assertEquals(AccessRule.R2, rule2);
    }

    @Test
    public void testGetAccessRuleRule3() {
        AccessRule rule3 = AccessRule.getAccessRule(PinState.RETRY_COUNTER_COUNT_03, false, false);
        Assert.assertEquals(AccessRule.R3, rule3);
    }

    @Test
    public void testGetAccessRuleRule4() {
        AccessRule rule4 = AccessRule.getAccessRule(PinState.PASSWORD_DISABLED, false, false);
        Assert.assertEquals(AccessRule.R4, rule4);
    }
}

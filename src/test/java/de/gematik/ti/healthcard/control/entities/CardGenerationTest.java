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

package de.gematik.ti.healthcard.control.entities;

import org.junit.Assert;
import org.junit.Test;

public class CardGenerationTest {

    @Test
    public void getCardGeneration() {
        Assert.assertEquals(CardGeneration.G2_1, CardGeneration.getCardGeneration(50000));
        Assert.assertEquals(CardGeneration.G1, CardGeneration.getCardGeneration(10000));
        Assert.assertEquals(CardGeneration.G1, CardGeneration.getCardGeneration(30002));
        Assert.assertEquals(CardGeneration.G1P, CardGeneration.getCardGeneration(30003));
        Assert.assertEquals(CardGeneration.G2, CardGeneration.getCardGeneration(40000));
        Assert.assertEquals(CardGeneration.G2, CardGeneration.getCardGeneration(40300));
        Assert.assertEquals(CardGeneration.G2_1, CardGeneration.getCardGeneration(40400));
        Assert.assertEquals(CardGeneration.G2_1, CardGeneration.getCardGeneration(40401));
        Assert.assertEquals(CardGeneration.UNKNOWN, CardGeneration.getCardGeneration(0));
    }
}

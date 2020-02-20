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

import org.junit.Assert;
import org.junit.Test;

import de.gematik.ti.healthcard.control.entities.CardGeneration;
import de.gematik.ti.healthcard.control.exceptions.WrongObjectSystemVersionArraySizeException;

public class CardGenerationExtractorTest {

    @Test
    public void getCardGeneration() {
        Assert.assertEquals(CardGeneration.G2_1, CardGenerationExtractor.getCardGeneration(new byte[] { 0x04, 0x04, 0x00 }));
        Assert.assertEquals(CardGeneration.G1, CardGenerationExtractor.getCardGeneration(new byte[] { 0x03, 0x00, 0x00 }));
        Assert.assertEquals(CardGeneration.G1P, CardGenerationExtractor.getCardGeneration(new byte[] { 0x03, 0x00, 0x03 }));
        Assert.assertEquals(CardGeneration.G2, CardGenerationExtractor.getCardGeneration(new byte[] { 0x04, 0x00, 0x00 }));
        Assert.assertEquals(CardGeneration.G2, CardGenerationExtractor.getCardGeneration(new byte[] { 0x04, 0x01, 0x00 }));
    }

    @Test(expected = WrongObjectSystemVersionArraySizeException.class)
    public void getCardGenerationExceptionSize2() {
        CardGenerationExtractor.getCardGeneration(new byte[] { 0x00, 0x00 });
    }

    @Test(expected = WrongObjectSystemVersionArraySizeException.class)
    public void getCardGenerationExceptionSize4() {
        CardGenerationExtractor.getCardGeneration(new byte[] { 0x00, 0x00, 0x00, 0x00 });
    }
}

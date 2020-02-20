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

package de.gematik.ti.healthcard.control.protocol;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.utils.codec.Hex;
import de.gematik.ti.utils.primitives.Bytes;

/**
 * UnitTest
  */
public class EgkCardProtocolUnitTest {
    private static final Logger LOG = LoggerFactory.getLogger(EgkCardProtocolUnitTest.class);
    private static final int MAXIMUMRECORDLENGTH = 46;

    @Test
    public void testIsEmpty() {
        byte[] bytes = Hex.decode("010000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009000");
        byte[] subarray = Bytes.copyByteArray(bytes, 0, MAXIMUMRECORDLENGTH);
        boolean notAtEnd = isEmpty(subarray);
        Assert.assertThat(notAtEnd, Is.is(false));

        bytes = Hex.decode("000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000009000");
        subarray = Bytes.copyByteArray(bytes, 0, MAXIMUMRECORDLENGTH);
        LOG.info(Hex.encodeHexString(subarray));
        notAtEnd = isEmpty(subarray);
        Assert.assertThat(notAtEnd, Is.is(true));
    }

    private static boolean isEmpty(byte[] bytes) {
        for (byte bt : bytes) {
            if (bt != 0) {
                LOG.debug("bt:" + bt);
                return false;
            }
        }
        return true;
    }
}

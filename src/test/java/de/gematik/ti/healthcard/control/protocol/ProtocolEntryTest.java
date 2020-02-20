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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.healthcard.control.entities.card.ProtocolEntry;
import de.gematik.ti.utils.codec.Hex;

/**
  */
public class ProtocolEntryTest {
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolEntryTest.class);
    private static final long TIMESTAMP_MILIS = 1561128299409L;
    private static final String DATATYPE = "1";
    private static final String ACTORID = "80276883110000017222";
    private static final String ACTORNAME = "GEM.SMCB-CA4 TEST-ONLY";
    private static final String TYPEACCESS = "2";
    private static final String RECORD = "5D0CED6B31328027688311000001722247454D2E534D43422D43413420544553542D4F4E4C592020202020202020";

    @Test
    public void testParse() {
        byte[] recordBytes = Hex.decode(RECORD);
        ProtocolEntry pe = ProtocolEntry.createProtocolEntry(recordBytes);
        long seconds = pe.getTimeStamp();
        long secondsIs = TIMESTAMP_MILIS / 1000L;
        Assert.assertThat(seconds, Is.is(secondsIs));
        Assert.assertThat(pe.getDataType(), Is.is(DATATYPE));
        Assert.assertThat(pe.getTypeAccess(), Is.is(TYPEACCESS));
        Assert.assertThat(pe.getActorId(), Is.is(ACTORID));
        Assert.assertThat(pe.getActorName(), Is.is(ACTORNAME));
    }

    @Test
    public void testBuild() {
        byte[] timeStampBytes = ProtocolRecordBuilder.getByteArrayOfLongvalue(TIMESTAMP_MILIS / 1000L);
        Assert.assertThat(timeStampBytes.length, Is.is(4));
        byte[] recordBytes = ProtocolRecordBuilder.buildRecord(timeStampBytes, DATATYPE, TYPEACCESS, ACTORID, ACTORNAME);
        Assert.assertThat(recordBytes.length, Is.is(46));
        String record = Hex.encodeHexString(recordBytes);
        Assert.assertThat(record, Is.is(RECORD));

    }

    @Test
    public void shouldEqual() {
        byte[] recordBytes = Hex.decode(RECORD);
        ProtocolEntry pe = ProtocolEntry.createProtocolEntry(recordBytes);

        ProtocolEntry peDouble = ProtocolEntry.createProtocolEntry(recordBytes);

        Assert.assertThat(pe, IsEqual.equalTo(peDouble));
    }

    @Test
    public void testTimeStampCurrenttimeMillis() {
        long timeMilis = System.currentTimeMillis() / 1000L;

        byte[] timeStampBytes = ProtocolRecordBuilder.getByteArrayOfLongvalue(timeMilis);
        LOG.debug("tiemstamp.len: " + timeStampBytes.length);
        Assert.assertThat(timeStampBytes.length, Is.is(4));

        String actorName = "(Niño Gômez, Lukas y)";
        try {
            byte[] recordBytes = new byte[30];
            byte[] actorNameBytes = actorName.getBytes("UTF-8");
            System.arraycopy(actorNameBytes, 0, recordBytes, 0, actorNameBytes.length);
            LOG.debug("bytes: " + Arrays.toString(recordBytes));
            Assert.assertThat(recordBytes.length, Is.is(30));
        } catch (UnsupportedEncodingException e) {
            Assert.fail(e.toString());
        }
    }
}

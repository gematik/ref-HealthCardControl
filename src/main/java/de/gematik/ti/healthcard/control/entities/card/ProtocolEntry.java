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

package de.gematik.ti.healthcard.control.entities.card;

import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.utils.codec.Hex;
import de.gematik.ti.utils.primitives.Bytes;

/**
 * Based on <b>gemSpec_Karten_Fach_TIP_G2.1_3.0.0</b>
 * 
    */
public class ProtocolEntry {

    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolEntry.class);
    private static final int MAXIMUMRECORDLENGTH = 46;
    private byte[] record;
    private long timeStamp = 0;
    private String dataType = "0";
    private String typeAccess = "0";
    private String actorId = "0";
    private String actorName = "0";

    /* Only a validProtocolEntry make sense */
    private boolean validProtocolEntry = true;

    public ProtocolEntry(String hexValue) {
        this(Hex.decode(hexValue));
    }

    /**
     * Get a valid <code>protocolEntry</code> using the input <code>record</code>
     * @param record
     */
    public ProtocolEntry(byte[] record) {
        checkRecordValidity(record);
        if (validProtocolEntry) {
            this.record = record;
            timeStamp = getLongValue(Hex.encodeHexString(Bytes.copyByteArray(record, 0, 4)));
            dataType = new String(Bytes.copyByteArray(record, 4, 1), CHARSET).trim();
            typeAccess = new String(Bytes.copyByteArray(record, 5, 1), CHARSET).trim();
            actorId = Hex.encodeHexString(Bytes.copyByteArray(record, 6, 10)).replace(" ", "");
            actorName = new String(Bytes.copyByteArray(record, 16, 30), CHARSET).trim();
        }
    }

    /**
     * return empty if entry consists of '0' only
     *
     * @param bytes
     * @return
     */
    private static boolean isEmpty(byte[] bytes) {
        for (byte bt : bytes) {
            if (bt != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get {@link ProtocolEntry} from a bytes array
     * @param record
     * @return
     */
    public static ProtocolEntry createProtocolEntry(final byte[] record) {
        ProtocolEntry protocolEntry = new ProtocolEntry(record);
        return protocolEntry;
    }

    public void checkRecordValidity(byte[] record) {
        if (isEmpty(record)) {
            LOG.warn("no protocol data");
            validProtocolEntry = false;
        }
        if (record.length != MAXIMUMRECORDLENGTH) {
            LOG.error("record is invalid because wrong length " + record.length + ". Its length must be " + MAXIMUMRECORDLENGTH);
            validProtocolEntry = false;
        }
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getDataType() {
        return dataType;
    }

    public String getTypeAccess() {
        return typeAccess;
    }

    public String getActorId() {
        return actorId;
    }

    public String getActorName() {
        return actorName;
    }

    public byte[] getRecord() {
        return record;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProtocolEntry) {
            ProtocolEntry pe = (ProtocolEntry) obj;
            boolean equalsDataType = getDataType().equals(pe.getDataType());
            boolean equalsTypeAccess = getTypeAccess().equals(pe.getTypeAccess());
            boolean equalsActorName = getActorName().equals(pe.getActorName());
            boolean equalsActorId = getActorId().equals(pe.getActorId());
            return equalsDataType && equalsTypeAccess && equalsActorName && equalsActorId
                    && getTimeStamp() == pe.getTimeStamp();
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString() + "@validProtocolEntry: " + validProtocolEntry + "; timeStamp: " + getTimeStamp() + "; actorId: " + getActorId()
                + "; actorName: "
                + getActorName() + "; typeAccess: "
                + getTypeAccess() + "; dataType: " + getDataType();
    }

    public boolean isValidProtocolEntry() {
        LOG.debug(this + " isValidProtocolEntry: " + validProtocolEntry);
        return validProtocolEntry;
    }

    /**
     * Get long value from a hexString
     * @param hexValue
     * @return
     */
    public static long getLongValue(final String hexValue) {
        return Long.valueOf(hexValue, 16).longValue();
    }
}

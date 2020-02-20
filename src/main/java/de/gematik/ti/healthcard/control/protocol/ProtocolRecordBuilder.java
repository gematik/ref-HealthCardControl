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

import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cardfilesystem.Hba2FileSystem;
import cardfilesystem.egk21mf.Df;
import cardfilesystem.egk21mf.df.esign.Ef;
import de.gematik.ti.healthcard.control.entities.card.ProtocolEntry;
import de.gematik.ti.healthcard.control.entities.card.certificate.CertificateUtil;
import de.gematik.ti.healthcard.control.exceptions.HealthcardControlRuntimeException;
import de.gematik.ti.healthcardaccess.AbstractHealthCardCommand;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.IHealthCardStatus;
import de.gematik.ti.healthcardaccess.IHealthCardType;
import de.gematik.ti.healthcardaccess.cardobjects.ApplicationIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.ShortFileIdentifier;
import de.gematik.ti.healthcardaccess.commands.ReadCommand;
import de.gematik.ti.healthcardaccess.commands.SelectCommand;
import de.gematik.ti.healthcardaccess.healthcards.*;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.utils.codec.Hex;
import de.gematik.ti.utils.primitives.Bytes;

/**
 * Build record as a byte-array to write in EF.LOGGING
  */
public class ProtocolRecordBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolRecordBuilder.class);
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final int MAXIMUMRECORDLENGTH = 46;

    private ProtocolRecordBuilder() {
    }

    /**
     * Get a valid byte-array of record <br/>
     * timeStamp is the currentMilis of system
     *
     * @param actorId
     * @param actorName
     * @param typeAccess
     * @param dataType
     * @return
     */
    public static byte[] buildRecord(String actorId, String actorName, String dataType, String typeAccess) {
        byte[] timeStampBytes = getByteArrayOfLongvalue(System.currentTimeMillis() / 1000L);
        return buildRecord(timeStampBytes, dataType, typeAccess, actorId, actorName);
    }

    /**
     * Get a valid byte-array of record with given timeStamp
     *
     * @param timeStampBytes
     * @param dataType:      defined by 'Fachanwendung'
     * @param typeAccess:    defined by 'Fachanwendung'
     * @param actorId:       iccsn of hba or smcb
     * @param actorName:     for smcb it is CN in OSIG-Certificate
     * @return record to write in
     */
    public static byte[] buildRecord(byte[] timeStampBytes, String dataType, String typeAccess, String actorId, String actorName) {
        if (timeStampBytes.length != 4) {
            throw new HealthcardControlRuntimeException("Wrong length of 'timestampe': " + timeStampBytes.length);
        }
        byte[] dataTypeBytes = dataType.getBytes(CHARSET);
        if (dataTypeBytes.length != 1) {
            throw new HealthcardControlRuntimeException("Wrong length of 'dataType': " + dataTypeBytes.length);
        }
        byte[] typeAccessBytes = typeAccess.getBytes(CHARSET);
        if (typeAccessBytes.length != 1) {
            throw new HealthcardControlRuntimeException("Wrong length of 'typeAccess': " + typeAccessBytes.length);
        }
        byte[] actorIdBytes = Hex.decode(actorId);
        if (actorIdBytes.length != 10) {
            throw new HealthcardControlRuntimeException("Wrong length of 'actorId': " + actorIdBytes.length);
        }
        byte[] actorNameBytes = padString(actorName, 30);
        if (actorNameBytes.length != 30) {
            throw new HealthcardControlRuntimeException("Wrong length of 'actorName': " + actorNameBytes.length);
        }
        byte[] record = Bytes.concatNullables(timeStampBytes, dataTypeBytes, typeAccessBytes, actorIdBytes, actorNameBytes);
        if (record.length != MAXIMUMRECORDLENGTH) {
            throw new HealthcardControlRuntimeException("Wrong length of record: " + record.length);
        }
        return record;
    }

    /**
     * Get required info from card to build a record of {@link ProtocolEntry}
     *
     * @param cardHc
     * @param typeAccess
     * @param dataType
     * @return
     */
    public static byte[] buildRecord(final IHealthCard cardHc, final String dataType, final String typeAccess) {
        final String cardClassName = checkCard(cardHc);
        String actorId = readIcc(cardHc);
        LOG.debug("actorId: " + actorId);
        final String actorName = readActorNameFromAutCert(cardHc, cardClassName);
        return buildRecord(actorId, actorName, dataType, typeAccess);
    }

    /**
     * Determine actorName using data from the healthCard
     * @param cardHc
     * @param cardClassName
     * @return
     */
    private static String readActorNameFromAutCert(final IHealthCard cardHc, final String cardClassName) {
        final ApplicationIdentifier aid = new ApplicationIdentifier(Df.Esign.AID);
        ShortFileIdentifier sfId = null;
        switch (cardClassName) {
            case "Hba21":
                sfId = new ShortFileIdentifier(cardfilesystem.hba21mf.df.esign.Ef.ChpAutE256.SFID);
                break;
            case "Hba2":
                sfId = new ShortFileIdentifier(Ef.CchAutR2048.SFID);
                break;
            case "Smcb21":
                sfId = new ShortFileIdentifier(cardfilesystem.smcb21mf.df.esign.Ef.C_HCI_OSIG_E256.SFID);
                break;
            case "Smcb2":
                sfId = new ShortFileIdentifier(cardfilesystem.smcb2mf.df.esign.Ef.C_HCI_OSIG_R2048.SFID);
                break;
            default:
                LOG.error("not supported cardtype: " + cardClassName);
        }

        final AbstractHealthCardCommand selectESignCommand = new SelectCommand(aid);
        final AbstractHealthCardCommand readESignCommand = new ReadCommand(sfId);

        ResultOperation<byte[]> resultOperation = selectESignCommand.executeOn(cardHc)
                .flatMap(__ -> readESignCommand.executeOn(cardHc)).map(Response::getResponseData);
        Subscribers.CertificateSubscriber certificateSubscriber = new Subscribers.CertificateSubscriber();
        resultOperation.subscribe(certificateSubscriber);
        final byte[] certbytes = certificateSubscriber.getCertificate();
        final X509Certificate certificate = CertificateUtil.getCertificate(certbytes);
        switch (cardClassName) {
            case "Hba21":
            case "Hba2":
                final String surnameGivenname = CertificateUtil.getSurnameGivenname(certificate);
                LOG.trace("certificate: \n" + certificate);
                LOG.debug("surnameGivenname: " + surnameGivenname);
                return surnameGivenname;
            case "Smcb2":
            case "Smcb21":
                final String commonname = CertificateUtil.getCommonName(certificate);
                LOG.debug("commonname: " + commonname);
                return commonname;
            default:
                LOG.error("Unknown cardtype: " + cardClassName);
        }
        throw new HealthcardControlRuntimeException("actorName for cardtype " + cardClassName + " is not available");
    }

    /**
     * get ICCSN, set actorId with ICCSN.
     * @param cardHc
     * @return
     */
    private static String readIcc(final IHealthCard cardHc) {
        ResultOperation<String> responseResultOperation = new SelectCommand(false, false).executeOn(cardHc)
                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                .flatMap(__ -> new ReadCommand(getSfid(cardHc)).executeOn(cardHc))
                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                .map(Response::getResponseData)
                .flatMap(gdoData -> {
                    byte[] bs = Arrays.copyOfRange(gdoData, 2, 12);
                    return ResultOperation.unitRo(Hex.encodeHexString(bs));
                });
        Subscribers.ActorIdSubscriber actorIdSubscriber = new Subscribers.ActorIdSubscriber();
        responseResultOperation.subscribe(actorIdSubscriber);
        return actorIdSubscriber.getActorId();
    }

    /**
     * Gdo.SFID is same for all cardtypes, i.e. 0x02
     * @param cardHc
     * @return
     */
    private static ShortFileIdentifier getSfid(IHealthCard cardHc) {
        int commonSfid = Hba2FileSystem.EF.GDO.SFID;
        return new ShortFileIdentifier(commonSfid);
    }

    /**
     * Get a array of byte from a given long value.
     *
     * @param value
     * @return
     */
    public static byte[] getByteArrayOfLongvalue(final long value) {
        if (value == 0) {
            return new byte[] { 0 };
        }

        byte[] array = new byte[] { (byte) (value >>> 56), (byte) (value >>> 48), (byte) (value >>> 40),
                (byte) (value >>> 32), (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };

        // remove leading '0'
        while (array.length > 0 && array[0] == 0) {
            array = Bytes.copyByteArray(array, 1, array.length - 1);
        }
        return array;
    }

    /**
     * pad a string at the end with space to a given size
     *
     * @param actorName
     * @param count
     * @return
     */
    private static byte[] padString(String actorName, int count) {
        byte[] actorNameBytes = actorName.getBytes(CHARSET);
        if (actorName.length() < count) {
            LOG.debug("spaces are added to actorName");
            byte[] bytes = new byte[count];
            Arrays.fill(bytes, (byte) 0x20);
            System.arraycopy(actorNameBytes, 0, bytes, 0, actorNameBytes.length);
            return bytes;
        } else if (actorName.length() > count) {
            byte[] bytes = new byte[count];
            LOG.debug("actorName is too long, it is shortened");
            System.arraycopy(actorNameBytes, 0, bytes, 0, count);
        }
        return actorNameBytes;
    }

    /**
     * Get class name of a card for identification
     * @param cardHc
     * @return
     */
    private static String checkCard(final IHealthCard cardHc) {
        final IHealthCardStatus hcStatus = cardHc.getStatus();
        if (hcStatus.isValid()) {
            final IHealthCardType cardType = ((HealthCardStatusValid) hcStatus).getHealthCardType();
            if (cardType instanceof Hba21) {
                return Hba21.class.getSimpleName();
            }
            if (cardType instanceof Hba2) {
                return Hba2.class.getSimpleName();
            }
            if (cardType instanceof Smcb2) {
                return Smcb2.class.getSimpleName();
            }
            if (cardType instanceof Smcb21) {
                return Smcb21.class.getSimpleName();
            }
        }
        throw new HealthcardControlRuntimeException("card is invalid");
    }

}

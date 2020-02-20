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

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.gematik.ti.healthcard.control.exceptions.HealthcardControlRuntimeException;
import de.gematik.ti.healthcard.control.nfdm.states.NfdDpeDataAvailableState;
import de.gematik.ti.healthcard.control.nfdm.states.NfdDpeLifeCycleState;
import de.gematik.ti.healthcard.control.nfdm.states.NfdDpeState;
import de.gematik.ti.healthcard.control.nfdm.states.NfdDpeVersionState;
import de.gematik.ti.healthcardaccess.AbstractHealthCardCommand;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.cardobjects.ApplicationIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.FileIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.ShortFileIdentifier;
import de.gematik.ti.healthcardaccess.commands.ReadCommand;
import de.gematik.ti.healthcardaccess.commands.SelectCommand;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.operation.Subscriber;
import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.utils.codec.Hex;
import de.gematik.ti.utils.primitives.Bytes;

/**
 * Functionalities to read and check NFD on Card
 *
*/
public class NfdDpeReader {

    private static final Logger LOG = LoggerFactory.getLogger(NfdDpeReader.class);
    private static final String TAG = "NfdDpeReader: ";
    private static final int RADIX = 16;
    private final Subscriber<byte[]> subscriber = getSubscriber();
    private IHealthCard cardToRead = null;

    NfdDpeReader(final IHealthCard cardToRead) {
        this.cardToRead = cardToRead;
    }

    private static int dataInfoToLength(final byte[] dataInfo) {
        final String le1 = Hex.encodeHexString(dataInfo);
        final int nfdSize = Integer.parseInt(le1, RADIX);
        LOG.debug("Size int: " + nfdSize);
        return nfdSize;
    }

    private static ResultOperation<Document> convertStringToXMLDocument(final String xmlString) {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            return ResultOperation.unitRo(builder.parse(new InputSource(new StringReader(xmlString))));
        } catch (final Exception e) {
            LOG.error(TAG, "Error on parsing xml " + e.getMessage());
        }
        return null;
    }

    private static ResultOperation<String> getUncompressed(final byte[] compressedDpe) {
        final StringBuilder sb;
        final Charset charSet = getCharSettingFirst(compressedDpe);
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(compressedDpe);
             final GZIPInputStream gis = new GZIPInputStream(bis);
             final BufferedReader br = new BufferedReader(new InputStreamReader(gis, charSet))) {
            sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (final IOException e) {
            LOG.error(TAG, "Error on uncompressing: " + e);
            throw new HealthcardControlRuntimeException("Error on uncompressing Data " + e);
        }
        final String uncompressedString;
        uncompressedString = sb.toString();
        LOG.debug("Uncompressed: " + uncompressedString);
        return ResultOperation.unitRo(uncompressedString);
    }

    /**
     * Get out the charSet in xml use default charSet UTF-8 <br/>
     * Then return the charSet in xml back to use
     * @param compressedData
     * @return
     */
    private static Charset getCharSettingFirst(final byte[] compressedData) {
        String xml = "";
        try (final ByteArrayInputStream bis = new ByteArrayInputStream(compressedData); final GZIPInputStream gis = new GZIPInputStream(bis)) {
            final byte[] bytes = new byte[100];
            gis.read(bytes);
            xml = new String(bytes);
        } catch (final Exception e) {
            LOG.error(e.toString());
            throw new HealthcardControlRuntimeException("While reading compressedData an error occured " + e);
        }
        final String def = "encoding=";
        final int defLength = def.length();

        final String[] sa = new String[] { "" };
        final String[] split = xml.split(" ");
        Arrays.stream(split).filter(s -> s.trim().toLowerCase().startsWith(def)).flatMap(str -> {
            Stream<Character> characterStream = str.substring(defLength).chars().mapToObj(c -> (char) c);
            Optional<Character> first = characterStream.findFirst();
            if (!first.isPresent()) {
                return Stream.of();
            }
            Character separator = first.get();
            String temp = str.substring(defLength + 1);
            int nextPosition = temp.indexOf(separator);
            characterStream = temp.substring(0, nextPosition).chars().mapToObj(c -> (char) c);
            return characterStream;
        }).forEach(a -> sa[0] = sa[0] + a);
        final Charset newCharSet = Charset.forName(sa[0]);
        return newCharSet;
    }

    /**
     * Check for visibility of NFD application
     * <p>
     * The NFD on the eGK is not hidden, the folder DF.NFD of the card may be the value of the attribute
     * lifeCycleStatus does not have the value "deactivated".
     *
     * @param dfAid
     * @return ResultOperation<NfdLifeCycleState>
     */
    ResultOperation<NfdDpeLifeCycleState> readLifeCycleState(final ApplicationIdentifier dfAid) {
        final AbstractHealthCardCommand selectCommand = new SelectCommand(dfAid, false, true, 0);
        return selectCommand.executeOn(cardToRead).map(NfdDpeLifeCycleState::getLifeCycleStateResult);
    }

    /**
     * Check technical NFD State
     * <p>
     * The NFD stored on the eGK is technically consistent;
     * the value of the information element status of the file
     * EF.StatusNFD according to [gemSpec_eGK_Fach_NFDM # 2.2] the
     * eGK is "0".
     *
     * @param aid
     * @param sfid
     * @return ResultOperation<NfdState>
     */
    ResultOperation<NfdDpeState> checkConsistency(final ApplicationIdentifier aid, final ShortFileIdentifier sfid) {
        final AbstractHealthCardCommand selectNfdFolderCommand = new SelectCommand(aid);
        final AbstractHealthCardCommand readStatusCommand = new ReadCommand(sfid);
        return selectNfdFolderCommand.executeOn(cardToRead).flatMap(__ -> readStatusCommand.executeOn(cardToRead)).map(NfdDpeState::getState);
    }

    /**
     * Check Version of the NFD Storage structures
     * <p>
     * The version of the internal memory structure (see 6.1.2.2 gemSpec_FM_NFDM_V1.4.0) of the
     * Files of the Emergency Data Set application on the card (NFD Memory structure) is supported by the module
     *
     * @return ResultOperation<NfdVersionState>
     */
    ResultOperation<NfdDpeVersionState> checkContainerVersion(final ApplicationIdentifier aid, final ShortFileIdentifier statusSfid) {
        final AbstractHealthCardCommand selectNfdFolderCommand = new SelectCommand(aid);
        final AbstractHealthCardCommand readStatusCommand = new ReadCommand(statusSfid);
        return selectNfdFolderCommand.executeOn(cardToRead).flatMap(__ -> readStatusCommand.executeOn(cardToRead))
                .map(NfdDpeVersionState::getVersionState);
    }

    /**
     * Check existence of NFD data on card
     * <p>
     * It is a gzip-compressed NFD according to [RFC1952] in the information element NFD of file EF.NFD according to [gemSpec_eGK_Fach_NFDM # 2.1] stored
     * Information element length NFD of file EF.NFD of cardhas a value not equal to '00 00'.
     *
     * @return ResultOperation<NfdDataAvailableState>
     */
    ResultOperation<NfdDpeDataAvailableState> checkSize(final ApplicationIdentifier aid, final ShortFileIdentifier sfid) {
        final AbstractHealthCardCommand selectNfdCommand = new SelectCommand(aid);
        final AbstractHealthCardCommand readCommand = new ReadCommand(sfid, 0, 2);
        return selectNfdCommand.executeOn(cardToRead).flatMap(__ -> readCommand.executeOn(cardToRead))
                .map(NfdDpeDataAvailableState::getDataAvailableState);
    }

    private ResultOperation<byte[]> extractEfSize(final ApplicationIdentifier aid, final ShortFileIdentifier sfid) {
        final AbstractHealthCardCommand selectCommand = new SelectCommand(aid);
        final byte[][] nfdSize = new byte[1][];
        selectCommand.executeOn(cardToRead).validate(Response.ResponseStatus.SUCCESS::validateResult)
                .flatMap(__ -> new ReadCommand(sfid, 0, 2).executeOn(cardToRead)).map(Response::getResponseData).map(bytes -> nfdSize[0] = bytes)
                .subscribe(subscriber);
        LOG.debug("extract-Size hex: " + Hex.encodeHexString(nfdSize[0]));
        return ResultOperation.unitRo(nfdSize[0]);
    }

    private Subscriber<byte[]> getSubscriber() {
        return new Subscriber<byte[]>() {
            @Override
            public void onSuccess(final byte[] value) {
                LOG.debug("Subscriber: " + Hex.encodeHexString(value));
            }

            @Override
            public void onError(final Throwable t) throws RuntimeException {
                LOG.debug("Subscriber: " + t.getMessage());
            }
        };
    }

    /**
     * The operation reads the NFD in the NFD information element of the file EF.NFD of the card
     *
     * @return ResultOperation<Document> Document contains the serialized elements of NFD
     */
    public ResultOperation<Document> extractDocument(final ApplicationIdentifier aid, final ShortFileIdentifier sfid, final FileIdentifier fid) {
        return extractEfSize(aid, sfid)
                .map(NfdDpeReader::dataInfoToLength).flatMap((Integer bytesToRead) -> getCompressedData(bytesToRead, fid)).flatMap(
                        NfdDpeReader::getUncompressed)
                .flatMap(NfdDpeReader::convertStringToXMLDocument);
    }

    private ResultOperation<byte[]> getCompressedData(Integer bytesToRead, final FileIdentifier fid) {
        final AbstractHealthCardCommand selectCommand = new SelectCommand(fid, false);
        AbstractHealthCardCommand readDpeCommand;
        final byte[][] compressedData = { new byte[] {} };
        int offset = 2;
        while (bytesToRead > 0) {
            final int readBytesLength;
            if (bytesToRead > cardToRead.getCurrentCardChannel().getMaxMessageLength()) {
                readDpeCommand = new ReadCommand(offset, cardToRead.getCurrentCardChannel().getMaxMessageLength());
                readBytesLength = cardToRead.getCurrentCardChannel().getMaxMessageLength();
            } else {
                readDpeCommand = new ReadCommand(offset, bytesToRead);
                readBytesLength = bytesToRead;
            }

            final AbstractHealthCardCommand finalReadDpeCommand = readDpeCommand;
            selectCommand.executeOn(cardToRead).validate(Response.ResponseStatus.SUCCESS::validateResult)
                    .flatMap(__ -> finalReadDpeCommand.executeOn(cardToRead).map(Response::getResponseData).map(bytes -> {
                        LOG.debug("extracted Bytes: " + Hex.encodeHexString(bytes));
                        compressedData[0] = Bytes.concatNullables(compressedData[0], bytes);
                        return bytes;
                    })).subscribe(subscriber);

            LOG.debug("Compressed: " + Hex.encodeHexString(compressedData[0]));
            offset += readBytesLength;
            bytesToRead -= readBytesLength;
        }
        return ResultOperation.unitRo(compressedData[0]);
    }

}

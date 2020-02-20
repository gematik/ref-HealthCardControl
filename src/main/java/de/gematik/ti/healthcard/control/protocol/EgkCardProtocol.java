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

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.healthcard.control.entities.card.ProtocolEntry;
import de.gematik.ti.healthcard.control.exceptions.HealthcardControlRuntimeException;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.cardobjects.Format2Pin;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;

/**
 * Functionalities to write and read log entries accessProtocolEntry
 *
    */
public class EgkCardProtocol {
    private static final Logger LOG = LoggerFactory.getLogger(EgkCardProtocol.class);
    private final IHealthCard cardEgkHc;
    private final Format2Pin pin;

    private ProtocolReader protocolReader = null;

    /**
     * For Read
     * @param cardEgkHc
     * @param pin
     */
    public EgkCardProtocol(final IHealthCard cardEgkHc, Format2Pin pin) {
        this.cardEgkHc = cardEgkHc;
        this.pin = pin;
    }

    /**
     * The operation writes the log entry accessProtocolEntry to the eGK
     * The prerequisite is that a preceding C2C Authentication has already been established the required security state.
     *
     *
     * @param protocolEntry entry to write
     * @return
     */
    public ResultOperation write(final ProtocolEntry protocolEntry) {
        ProtocolWriter protocolWriter = new ProtocolWriter(cardEgkHc);
        return protocolWriter.write(protocolEntry);
    }

    /**
     * The operation returns the access log of the eGK.
     * The prerequisite is that the required safety status of the eGK has already been established before.
     *
     * @TODO implementation of TIP1-A_7023 (gemKPT_Arch_TIP_V2.6.0)
     *
     * @return
     */
    public ResultOperation<Boolean> read() {
        protocolReader = new ProtocolReader(cardEgkHc, pin);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        LOG.debug("executor submits the protocolReader: " + protocolReader);
        executor.submit(protocolReader);
        executor.shutdown();
        return protocolReader.read();

    }

    /**
     * get all protocolEntries after read, for ef.logging it should be 50 constantly.
     * @return
     */
    public Collection<ProtocolEntry> getAllProtocolEntries() {
        if (protocolReader == null) {
            throw new HealthcardControlRuntimeException("call 'read()' firstly");
        }
        LOG.debug("protocolReader run call()");
        return protocolReader.call();
    }

    public Stream<ProtocolEntry> getValidProtocolEntries() {
        return getAllProtocolEntries().stream().filter(ProtocolEntry::isValidProtocolEntry);
    }
}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.healthcard.control.entities.card.ProtocolEntry;
import de.gematik.ti.healthcard.control.exceptions.HealthcardControlRuntimeException;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.cardobjects.*;
import de.gematik.ti.healthcardaccess.commands.ReadRecordCommand;
import de.gematik.ti.healthcardaccess.commands.SelectCommand;
import de.gematik.ti.healthcardaccess.commands.VerifyCommand;
import de.gematik.ti.healthcardaccess.healthcards.Egk2;
import de.gematik.ti.healthcardaccess.healthcards.Egk21;
import de.gematik.ti.healthcardaccess.healthcards.HealthCardStatusValid;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.result.Response;

/**
 * Implement the read-protocol process
 *
  */
public class ProtocolReader implements Callable {
    private static final int MAXIMUMNUMBEROFRECORDS = 50;
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolReader.class);
    private final IHealthCard cardEgk;
    private final ResultOperation<byte[]> responseResultOperation;
    private final Collection<ProtocolEntry> allProtocolEntries = new ArrayList();
    private static Password mrPinHomePWID;
    private static ApplicationIdentifier dfHcaAID;
    private static FileIdentifier efLoggingFID;
    private static ShortFileIdentifier efLoggingSFID;

    /**
     * @param cardEgk
     * @param pin
     */
    public ProtocolReader(IHealthCard cardEgk, Format2Pin pin) {
        this.cardEgk = cardEgk;
        initFileReference(cardEgk);
        responseResultOperation = new VerifyCommand(mrPinHomePWID, false, pin).executeOn(cardEgk)
                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                .flatMap(__ -> new SelectCommand(dfHcaAID).executeOn(cardEgk)
                        .validate(Response.ResponseStatus.SUCCESS::validateResult))
                .flatMap(__ -> new SelectCommand(efLoggingFID, false).executeOn(cardEgk)
                        .map(Response::getResponseData));
    }

    /**
     * Get cardObject in dependent on cardType
     * @param cardHc
     */
    private static void initFileReference(IHealthCard cardHc) {
        if (cardHc.getStatus().isValid() && ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType() instanceof Egk2) {
            mrPinHomePWID = new Password(cardfilesystem.egk2mf.MrPin.home.PWID);
            dfHcaAID = new ApplicationIdentifier(cardfilesystem.egk2mf.Df.Hca.AID);
            efLoggingFID = new FileIdentifier(cardfilesystem.egk2mf.df.hca.Ef.Logging.FID);//
            efLoggingSFID = new ShortFileIdentifier(cardfilesystem.egk2mf.df.hca.Ef.Logging.SFID);
        } else if (cardHc.getStatus().isValid() && ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType() instanceof Egk21) {
            mrPinHomePWID = new Password(cardfilesystem.egk21mf.MrPin.home.PWID);
            dfHcaAID = new ApplicationIdentifier(cardfilesystem.egk21mf.Df.Hca.AID);
            efLoggingFID = new FileIdentifier(cardfilesystem.egk21mf.df.hca.Ef.Logging.FID);//
            efLoggingSFID = new ShortFileIdentifier(cardfilesystem.egk21mf.df.hca.Ef.Logging.SFID);
        } else {
            throw new HealthcardControlRuntimeException(
                    "Cardtype " + ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType() + " is invalid or unknown");
        }
    }

    /**
     * Result after read
     * @return
     */
    @Override
    public Collection<ProtocolEntry> call() {
        return allProtocolEntries;
    }

    /**
     *
     * @return
     */
    public ResultOperation<Boolean> read() {
        ResultOperation<Boolean> resultOperation = doFirstRead(1, allProtocolEntries);
        for (int recordNumber = 2; recordNumber < MAXIMUMNUMBEROFRECORDS + 1; recordNumber++) {
            resultOperation = doRead(resultOperation, recordNumber, allProtocolEntries);
        }
        return resultOperation;
    }

    /**
     * with the selection of Record-file
     * @param recordNumber
     * @param list
     * @return
     */
    private ResultOperation<Boolean> doFirstRead(int recordNumber, Collection list) {
        ResultOperation<Boolean> resultOperation = responseResultOperation
                .flatMap(__ -> new ReadRecordCommand(efLoggingSFID, recordNumber).executeOn(cardEgk))
                .map(Response::getResponseData).map(ProtocolEntry::new).map((ProtocolEntry e) -> list.add(e));
        return resultOperation;
    }

    /**
     * aftre {@link #doFirstRead(int, Collection)} <br/>
     * no selection of Record-file,
     * @param resultOperation
     * @param recordNumber
     * @param list
     * @return
     */
    private ResultOperation<Boolean> doRead(ResultOperation<Boolean> resultOperation, int recordNumber, Collection list) {
        resultOperation = resultOperation
                .flatMap(__ -> new ReadRecordCommand(efLoggingSFID, recordNumber).executeOn(cardEgk))
                .map(Response::getResponseData).map(ProtocolEntry::new).map((ProtocolEntry e) -> list.add(e));
        return resultOperation;
    }

    /**
     * all ProtocolEntries from the read
     * @return
     */
    public Collection<ProtocolEntry> getAllProtocolEntries() {
        return allProtocolEntries;
    }

}

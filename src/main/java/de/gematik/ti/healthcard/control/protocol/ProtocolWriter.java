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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.healthcard.control.entities.card.ProtocolEntry;
import de.gematik.ti.healthcard.control.exceptions.HealthcardControlRuntimeException;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.healthcardaccess.cardobjects.ApplicationIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.FileIdentifier;
import de.gematik.ti.healthcardaccess.cardobjects.ShortFileIdentifier;
import de.gematik.ti.healthcardaccess.commands.AppendRecordCommand;
import de.gematik.ti.healthcardaccess.commands.SelectCommand;
import de.gematik.ti.healthcardaccess.healthcards.Egk2;
import de.gematik.ti.healthcardaccess.healthcards.Egk21;
import de.gematik.ti.healthcardaccess.healthcards.HealthCardStatusValid;
import de.gematik.ti.healthcardaccess.operation.ResultOperation;
import de.gematik.ti.healthcardaccess.result.Response;

/**
 * Implement the write-protocol process
  */
public class ProtocolWriter {
    private static final Logger LOG = LoggerFactory.getLogger(ProtocolWriter.class);
    private final IHealthCard cardEgkHc;
    private final ResultOperation<Response> responseResultOperation;
    private FileIdentifier efLoggingFid;
    private ShortFileIdentifier efLoggingSfid;
    private ApplicationIdentifier dfHcaAid;

    /**
     * run Card2Card-authentication
     * @param cardEgkHc
     */
    public ProtocolWriter(IHealthCard cardEgkHc) {
        initFileReference(cardEgkHc);
        this.cardEgkHc = cardEgkHc;
        responseResultOperation = new SelectCommand(dfHcaAid).executeOn(cardEgkHc)
                .validate(Response.ResponseStatus.SUCCESS::validateResult)
                .flatMap(__ -> new SelectCommand(efLoggingFid, false).executeOn(cardEgkHc)
                        .validate(Response.ResponseStatus.SUCCESS::validateResult));
    }

    /**
     * Get cardObject in dependent on cardType
     * @param cardHc
     */
    private void initFileReference(IHealthCard cardHc) {
        if (cardHc.getStatus().isValid() && ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType() instanceof Egk2) {
            efLoggingFid = new FileIdentifier(cardfilesystem.egk2mf.df.hca.Ef.Logging.FID);
            efLoggingSfid = new ShortFileIdentifier(cardfilesystem.egk2mf.df.hca.Ef.Logging.SFID);
            dfHcaAid = new ApplicationIdentifier(cardfilesystem.egk2mf.Df.Hca.AID);
        } else if (cardHc.getStatus().isValid() && ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType() instanceof Egk21) {
            efLoggingFid = new FileIdentifier(cardfilesystem.egk21mf.df.hca.Ef.Logging.FID);
            efLoggingSfid = new ShortFileIdentifier(cardfilesystem.egk21mf.df.hca.Ef.Logging.SFID);
            dfHcaAid = new ApplicationIdentifier(cardfilesystem.egk21mf.Df.Hca.AID);
        } else {
            throw new HealthcardControlRuntimeException(
                    "Cardtype " + ((HealthCardStatusValid) cardHc.getStatus()).getHealthCardType() + " is invalid or unknown");
        }
    }

    /**
     * execute selectCommand and appendRecordCommand
     * @param protocolEntry
     * @return
     */
    public ResultOperation write(ProtocolEntry protocolEntry) {
        LOG.debug(protocolEntry.toString());
        ResultOperation<Response> resultOp = responseResultOperation
                .flatMap(__ -> new AppendRecordCommand(efLoggingSfid, protocolEntry.getRecord()).executeOn(cardEgkHc))
                .validate(Response.ResponseStatus.SUCCESS::validateResult);
        return resultOp;
    }

}

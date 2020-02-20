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

import de.gematik.ti.healthcardaccess.operation.Subscriber;

/**
 * Provide subscriber used in #ProtocolRecordBuilder
 *
  */
public class Subscribers {
    private static final Logger LOG = LoggerFactory.getLogger(Subscribers.class);

    private Subscribers() {
    }

    /**
     * For receiving 'actorId'
     */
    public static class ActorIdSubscriber implements Subscriber {
        private String actorId;

        @Override
        public void onSuccess(Object value) {
            actorId = (String) value;
            LOG.debug("actorId " + actorId);
        }

        @Override
        public void onError(Throwable t) throws RuntimeException {
            LOG.error("Error occured  " + t.toString());
        }

        public String getActorId() {
            return actorId;
        }

    }

    /**
     * For receiving 'certificate'
     */
    public static class CertificateSubscriber implements Subscriber {
        private byte[] certificate;

        @Override
        public void onSuccess(Object value) {
            certificate = (byte[]) value;
            LOG.debug("certificate " + certificate);
        }

        @Override
        public void onError(Throwable t) throws RuntimeException {
            LOG.error("Error occured " + t.toString());
        }

        public byte[] getCertificate() {
            return certificate;
        }
    }

}

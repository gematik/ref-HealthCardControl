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

import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.schema.gen.nfd.v1_3.NFD_NFD_Document;

/**
   */
public class NfdManager {

    /*
     * @see gemSpec_FM_NFDM_V1.4.0 --> Tabelle 17: Tab_FM_NFDM_005 – Operation ReadNFD
     *
     *
     * Der auf der eGK gespeicherte NFD ist valide gegen das XML- Schema für den NFD (s. [gemSpec_InfoNFDM#3]).
     */

    private final NfdDpeReader nfdDpeReader;

    public NfdManager(final IHealthCard card) {
        nfdDpeReader = new NfdDpeReader(card);
    }

    public NFD_NFD_Document getDocument() {
        return new NFD_NFD_Document();
    }
}

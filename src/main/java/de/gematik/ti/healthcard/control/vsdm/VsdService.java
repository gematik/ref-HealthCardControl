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

package de.gematik.ti.healthcard.control.vsdm;

import de.gematik.ti.healthcard.control.entities.card.GvdDocument;
import de.gematik.ti.healthcard.control.entities.card.PdDocument;
import de.gematik.ti.healthcard.control.entities.card.VdDocument;
import de.gematik.ti.healthcardaccess.HealthCard;

/**
   */
public class VsdService {

    private final VsdReader vsdReader;
    private final HealthCard authorisingCard;

    public VsdService(final HealthCard cardToRead, final HealthCard authorisingCard) {
        this.authorisingCard = authorisingCard; // @Info für die Freischaltung
        vsdReader = new VsdReader(cardToRead);
    }

    public VdDocument getVdDocument() {
        return new VdDocument();
    }

    public PdDocument getPdDocument() {
        return new PdDocument();
    }

    public GvdDocument getGvdDocument() {
        return new GvdDocument();
    }
}

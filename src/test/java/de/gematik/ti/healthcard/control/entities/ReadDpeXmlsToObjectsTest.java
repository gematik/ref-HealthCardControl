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

package de.gematik.ti.healthcard.control.entities;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import de.gematik.ti.schema.gen.dpe.v1_0.DPE_DPE_Document;

public class ReadDpeXmlsToObjectsTest extends AbstractReadXmlsTest {

    @Test
    public void readDpe() throws Exception {
        Document document = readDocument("../resources/dpe/dpe.xml");
        DPE_DPE_Document deserialize = DPE_DPE_Document.deserialize(document.getDocumentElement());
        Assert.assertNotNull(deserialize);
        Assert.assertNotNull(deserialize.DPE_Version);
        Assert.assertNotNull(deserialize.Persoenliche_Erklaerungen);
        Assert.assertNotNull(deserialize.Persoenliche_Erklaerungen.DPE_letzte_Aktualisierung_date);
        Assert.assertNotNull(deserialize.Persoenliche_Erklaerungen.DPE_letzte_Aktualisierung_time);
        Assert.assertNotNull(deserialize.Persoenliche_Erklaerungen.ID_DPE);
        Assert.assertNotNull(deserialize.Persoenliche_Erklaerungen.DPE_Versicherter);
        Assert.assertThat(deserialize.Persoenliche_Erklaerungen.DPE_Versicherter_Einwilligung.DPE_VE_Arzt_Nachname, Is.is("Müller"));
    }
}

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

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import de.gematik.ti.schema.gen.nfd.v1_3.NFD_NFD_Document;

public class ReadNfdXmlsToObjectsTest extends AbstractReadXmlsTest {

    @Test
    public void readNfd() throws Exception {
        Document document = readDocument("../resources/nfd/NFD_Beispiel.xml");
        NFD_NFD_Document deserialize = NFD_NFD_Document.deserialize(document.getDocumentElement());
        Assert.assertNotNull(deserialize);
        Assert.assertNotNull(deserialize.NFD_Version);
        Assert.assertNotNull(deserialize.Notfalldaten);
        Assert.assertNotNull(deserialize.Notfalldaten.ID);
        Assert.assertNotNull(deserialize.Notfalldaten.ID_NFD);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_letzte_Aktualisierung_date);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_letzte_Aktualisierung_time);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.Versicherter);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.Versicherter.Versicherten_ID);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.Versicherter.Geburtsdatum);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.Versicherter.Vorname);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.Versicherter.Nachname);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.Versicherter.Geschlecht);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution);
        Assert.assertEquals(1, deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.size());
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).BAI_Art);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Arzt_Nachname);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Arzt_Vorname);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Institution_Bezeichnung);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Arzt_Bezeichnung);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Adresse);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Adresse.Adresse);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Adresse.Adresse.Postleitzahl);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Adresse.Adresse.Ort);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Adresse.Adresse.Strasse);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Adresse.Adresse.Hausnummer);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Adresse.Adresse.Anschriftenzusatz);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Adresse.Adresse.Wohnsitzlaendercode);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Kommunikation);
        Assert.assertEquals(1, deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Kommunikation.size());
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Kommunikation.get(0));
        Assert.assertNotNull(
                deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Kommunikation.get(0).Kommunikationsdaten);
        Assert.assertNotNull(
                deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Kommunikation.get(0).Kommunikationsdaten.EMail);
        Assert.assertNotNull(
                deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Kommunikation.get(0).Kommunikationsdaten.Faxnummer);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Behandelnder_Arzt_Institution.get(0).NFD_BAI_Kommunikation
                .get(0).Kommunikationsdaten.Telefonnummer);

        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Benachrichtigungskontakt);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Benachrichtigungskontakt.NFD_BK_Bezeichnung);
        Assert.assertEquals(1, deserialize.Notfalldaten.NFD_Versicherter.NFD_Benachrichtigungskontakt.NFD_BK_Kommunikation.size());
        // ...
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter.NFD_Versicherter_Kommunikation);
        Assert.assertEquals(1, deserialize.Notfalldaten.NFD_Versicherter.NFD_Versicherter_Kommunikation.size());

        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Versicherter_Einwilligung);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Befunddaten);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Freiwillige_Zusatzinformationen);
        Assert.assertNotNull(deserialize.Notfalldaten.NFD_Medikationseintrag);
        Assert.assertEquals(2, deserialize.Notfalldaten.NFD_Medikationseintrag.size());

        Assert.assertNotNull(deserialize.SignatureArzt);
    }
}

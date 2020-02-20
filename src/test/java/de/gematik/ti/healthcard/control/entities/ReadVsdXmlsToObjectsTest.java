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

import de.gematik.ti.schema.gen.vsd.v5_2.gvd.VSD_UC_GeschuetzteVersichertendatenXML;
import de.gematik.ti.schema.gen.vsd.v5_2.pd.VSD_UC_PersoenlicheVersichertendatenXML;
import de.gematik.ti.schema.gen.vsd.v5_2.vd.VSD_UC_AllgemeineVersicherungsdatenXML;

public class ReadVsdXmlsToObjectsTest extends AbstractReadXmlsTest {

    @Test
    public void readGvd() throws Exception {
        Document document = readDocument("../resources/vsd/ef_gvd.xml");
        VSD_UC_GeschuetzteVersichertendatenXML deserialize = VSD_UC_GeschuetzteVersichertendatenXML.deserialize(document.getDocumentElement());
        Assert.assertNotNull(deserialize);
        System.out.println("CDM_VERSION: " + deserialize.CDM_VERSION);
        System.out.println("Zuzahlungsstatus: " + deserialize.Zuzahlungsstatus);
        System.out.println("Selektivvertraege: " + deserialize.Selektivvertraege);
        System.out.println("BesonderePersonengruppe: " + deserialize.BesonderePersonengruppe);
        System.out.println("DMP_Kennzeichnung: " + deserialize.DMP_Kennzeichnung);
        System.out.println("RuhenderLeistungsanspruch: " + deserialize.RuhenderLeistungsanspruch);
        Assert.assertNotNull(deserialize.CDM_VERSION);
        Assert.assertNotNull(deserialize.Zuzahlungsstatus);
        Assert.assertNotNull(deserialize.Selektivvertraege);
        Assert.assertNull(deserialize.BesonderePersonengruppe);
        Assert.assertNull(deserialize.DMP_Kennzeichnung);
        Assert.assertNull(deserialize.RuhenderLeistungsanspruch);
    }

    @Test
    public void readVd() throws Exception {
        Document document = readDocument("../resources/vsd/ef_vd.xml");

        VSD_UC_AllgemeineVersicherungsdatenXML deserialize = VSD_UC_AllgemeineVersicherungsdatenXML.deserialize(document.getDocumentElement());
        Assert.assertNotNull(deserialize);
        System.out.println("CDM_VERSION: " + deserialize.CDM_VERSION);
        System.out.println("Versicherter: " + deserialize.Versicherter);
        Assert.assertNotNull(deserialize.CDM_VERSION);
        Assert.assertNotNull(deserialize.Versicherter);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz.Beginn);
        Assert.assertNull(deserialize.Versicherter.Versicherungsschutz.Ende);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz.Kostentraeger);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz.Kostentraeger.Kostentraegerlaendercode);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz.Kostentraeger.Kostentraegerkennung);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz.Kostentraeger.Name);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz.Kostentraeger.AbrechnenderKostentraeger);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz.Kostentraeger.AbrechnenderKostentraeger.Kostentraegerkennung);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz.Kostentraeger.AbrechnenderKostentraeger.Kostentraegerlaendercode);
        Assert.assertNotNull(deserialize.Versicherter.Versicherungsschutz.Kostentraeger.AbrechnenderKostentraeger.Name);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos.ZusatzinfosGKV);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos.ZusatzinfosGKV.Versichertenart);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos.ZusatzinfosGKV.Zusatzinfos_Abrechnung_GKV);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos.ZusatzinfosGKV.Zusatzinfos_Abrechnung_GKV.WOP);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos.ZusatzinfosGKV.Zusatzinfos_Abrechnung_GKV.Kostenerstattung);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos.ZusatzinfosGKV.Zusatzinfos_Abrechnung_GKV.Kostenerstattung.AerztlicheVersorgung);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos.ZusatzinfosGKV.Zusatzinfos_Abrechnung_GKV.Kostenerstattung.ZahnaerztlicheVersorgung);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos.ZusatzinfosGKV.Zusatzinfos_Abrechnung_GKV.Kostenerstattung.StationaererBereich);
        Assert.assertNotNull(deserialize.Versicherter.Zusatzinfos.ZusatzinfosGKV.Zusatzinfos_Abrechnung_GKV.Kostenerstattung.VeranlassteLeistungen);
    }

    @Test
    public void readPd() throws Exception {
        Document document = readDocument("../resources/vsd/ef_pd.xml");

        VSD_UC_PersoenlicheVersichertendatenXML deserialize = VSD_UC_PersoenlicheVersichertendatenXML.deserialize(document.getDocumentElement());
        Assert.assertNotNull(deserialize);
        System.out.println("CDM_VERSION: " + deserialize.CDM_VERSION);
        System.out.println("Versicherter: " + deserialize.Versicherter);
        Assert.assertNotNull(deserialize.CDM_VERSION);
        Assert.assertNotNull(deserialize.Versicherter);
        Assert.assertNotNull(deserialize.Versicherter.Versicherten_ID);
        Assert.assertNotNull(deserialize.Versicherter.Person);
        Assert.assertNotNull(deserialize.Versicherter.Person.Geburtsdatum);
        Assert.assertNotNull(deserialize.Versicherter.Person.Geschlecht);
        Assert.assertNotNull(deserialize.Versicherter.Person.Nachname);
        Assert.assertNotNull(deserialize.Versicherter.Person.Vorname);
        Assert.assertNotNull(deserialize.Versicherter.Person.Titel);
        Assert.assertNotNull(deserialize.Versicherter.Person.StrassenAdresse);
        Assert.assertNotNull(deserialize.Versicherter.Person.StrassenAdresse.Postleitzahl);
        Assert.assertNotNull(deserialize.Versicherter.Person.StrassenAdresse.Ort);
        Assert.assertNotNull(deserialize.Versicherter.Person.StrassenAdresse.Land);
        Assert.assertNotNull(deserialize.Versicherter.Person.StrassenAdresse.Land.Wohnsitzlaendercode);
        Assert.assertNotNull(deserialize.Versicherter.Person.StrassenAdresse.Strasse);
        Assert.assertNotNull(deserialize.Versicherter.Person.StrassenAdresse.Hausnummer);
    }

}

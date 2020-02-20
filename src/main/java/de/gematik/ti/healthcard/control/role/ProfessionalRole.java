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

package de.gematik.ti.healthcard.control.role;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.spongycastle.asn1.*;

import de.gematik.ti.healthcard.control.entities.card.certificate.CertificateUtil;
import de.gematik.ti.healthcard.control.exceptions.AuthenticationCertificateException;
import de.gematik.ti.healthcard.control.exceptions.WrongRoleException;
import de.gematik.ti.healthcardaccess.operation.Result;
import de.gematik.ti.healthcardaccess.result.Response;

/**
 * Professional role of the role model in the external view of the TI platform
 *
    */
public enum ProfessionalRole {
    VERSICHERTER,
    ARZT,
    ZAHNARZT,
    APOTHEKER,
    MITARBEITER_APOTHEKE,
    SONSTIGE_LEISTUNGSERBRINGER,
    ANDERER_HEILBERUF,
    PS_PSYCHOTHERAPEUT,
    MITARBEITER_KOSTENTRAEGER,

    PRAXIS_ARZT,
    PRAXIS_ZAHNARZT,
    PRAXIS_PSYCHOTHERAPEUT,
    KRANKENHAUS,
    OEFFENTLICHE_APOTHEKE,
    KRANKENHAUS_APOTHEKE,
    BUNDESWEHR_APOTHEKE,
    MOBILE_EINRICHTUNG_RETTUNGSDIENST,
    BS_GEMATIK,
    LEO_ZAHNAERZTE,
    ADV_KOSTENTRAEGER;

    private static final String ERROR_MESSAGE = "failed to extract profession info sequence from certificate";

    private static ASN1Encodable oid;

    /**
     * Extracts the technical role from read eSign folder
     * @param response response
     * @return technical role
     * @throws IOException if an error occurs
     */
    public static ProfessionalRole getRole(final Response response) throws IOException {

        final byte[] osArray = getExtensionValue(response.getResponseData());
        String oidString = "";

        final DEROctetString os;
        try (final ASN1InputStream asn1Source = new ASN1InputStream(osArray)) {

            os = (DEROctetString) asn1Source.readObject().toASN1Primitive();

            final byte[] octets = os.getOctets();
            try (final ASN1InputStream seqSrc = new ASN1InputStream(octets)) {
                final DLSequence admissionsSequence = (DLSequence) seqSrc.readObject();
                final Enumeration admissionsSequenceObjects = admissionsSequence.getObjects();
                final DLSequence professionInfosSequence;

                professionInfosSequence = getPlSequence(admissionsSequenceObjects);

                extractOid(professionInfosSequence);

                oidString = oid.toString();
            }
        }
        return mapOID(oidString);
    }

    private static void extractOid(final DLSequence professionInfosSequence) {
        final DLSequence professionInfoSequence;
        Enumeration professionInfoObjects;
        if (professionInfosSequence.getObjects().nextElement() instanceof ASN1Sequence) {
            professionInfoSequence = (DLSequence) professionInfosSequence.getObjects().nextElement();

            professionInfoObjects = professionInfoSequence.getObjects();
            final DLSequence directoryStringSequence;

            DLSequence professionOidSequence;
            Object professionOidObject;

            do {
                professionOidObject = professionInfoObjects.nextElement();
            } while (!(professionOidObject instanceof ASN1Sequence)
                    && professionInfoObjects.hasMoreElements());

            if (professionOidObject instanceof ASN1Sequence) {
                directoryStringSequence = (DLSequence) professionOidObject;
                professionInfoObjects = directoryStringSequence.getObjects();

            } else {
                throw new AuthenticationCertificateException(ERROR_MESSAGE);
            }

            do {
                professionOidObject = professionInfoObjects.nextElement();
            } while (!(professionOidObject instanceof ASN1Sequence)
                    && professionInfoObjects.hasMoreElements());

            if (professionOidObject instanceof ASN1Sequence) {
                professionOidSequence = (DLSequence) professionOidObject;
                professionOidSequence = (DLSequence) professionOidSequence.getObjectAt(1);
                oid = professionOidSequence.getObjectAt(0);

            } else {
                throw new AuthenticationCertificateException(ERROR_MESSAGE);
            }
        }
    }

    private static DLSequence getPlSequence(final Enumeration admissionsSequenceObjects) {
        Object professionInfoObject;
        final DLSequence professionInfosSequence;
        do {
            professionInfoObject = admissionsSequenceObjects.nextElement();
        } while (!(professionInfoObject instanceof ASN1Sequence) && admissionsSequenceObjects.hasMoreElements());

        if (professionInfoObject instanceof ASN1Sequence) {
            professionInfosSequence = (DLSequence) professionInfoObject;
        } else {
            throw new AuthenticationCertificateException(ERROR_MESSAGE);
        }
        return professionInfosSequence;
    }

    private static ProfessionalRole mapOID(final String oidString) { // NOCS(SAB): Map mit notwendigen OIDs
        final Map<String, ProfessionalRole> roleMap = new HashMap<>();
        roleMap.put("1.2.276.0.76.4.30", ARZT);
        roleMap.put("1.2.276.0.76.4.31", ZAHNARZT);
        roleMap.put("1.2.276.0.76.4.32", APOTHEKER); // Apotheker/-in
        roleMap.put("1.2.276.0.76.4.33", APOTHEKER); // Apothekerassistent/-in
        roleMap.put("1.2.276.0.76.4.34", APOTHEKER); // Pharmazieingenieur/-in
        roleMap.put("1.2.276.0.76.4.35", MITARBEITER_APOTHEKE); // pharmazeutisch-technische/-r Assistent/-in
        roleMap.put("1.2.276.0.76.4.36", MITARBEITER_APOTHEKE); // pharmazeutisch-kaufmännische/-r Angestellte
        roleMap.put("1.2.276.0.76.4.37", MITARBEITER_APOTHEKE); // Apothekenhelfer/-in
        roleMap.put("1.2.276.0.76.4.38", APOTHEKER); // Apothekenassistent/-in
        roleMap.put("1.2.276.0.76.4.39", MITARBEITER_APOTHEKE); // Pharmazeutische/-r Assistent/-in
        roleMap.put("1.2.276.0.76.4.40", MITARBEITER_APOTHEKE); // Apothekenfacharbeiter/-in
        roleMap.put("1.2.276.0.76.4.41", MITARBEITER_APOTHEKE); // Pharmaziepraktikant/-in
        roleMap.put("1.2.276.0.76.4.42", MITARBEITER_APOTHEKE); // Stud.pharm. oder Famulant/-in
        roleMap.put("1.2.276.0.76.4.43", MITARBEITER_APOTHEKE); // PTA-Praktikant/-in
        roleMap.put("1.2.276.0.76.4.44", MITARBEITER_APOTHEKE); // PKA Auszubildende/-r
        roleMap.put("1.2.276.0.76.4.45", ARZT); // Psychotherapeut/-in (wird mit ProfessionItem ARZT beschrieben
        roleMap.put("1.2.276.0.76.4.46", PS_PSYCHOTHERAPEUT); // Psychologische/-r Psychotherapeut/-in
        roleMap.put("1.2.276.0.76.4.47", PS_PSYCHOTHERAPEUT); // Kinder- und Jugendlichenpsychotherapeut/-in
        roleMap.put("1.2.276.0.76.4.48", ANDERER_HEILBERUF); // Rettungsassistent/-in
        roleMap.put("1.2.276.0.76.4.49", VERSICHERTER); // Versicherte/-r
        roleMap.put("1.2.276.0.76.4.178", ANDERER_HEILBERUF); // Notfallsanitäter/-in
        roleMap.put("1.2.276.0.76.4.50", PRAXIS_ARZT); // Betriebsstaette Arzt
        roleMap.put("1.2.276.0.76.4.51", PRAXIS_ZAHNARZT); // Zahnarztpraxix
        roleMap.put("1.2.276.0.76.4.52", PRAXIS_PSYCHOTHERAPEUT); // Betriebsstaette Psychotherapeut
        roleMap.put("1.2.276.0.76.4.53", KRANKENHAUS); // Krankenhaus
        roleMap.put("1.2.276.0.76.4.54", OEFFENTLICHE_APOTHEKE); // Oeffentliche Apotheke
        roleMap.put("1.2.276.0.76.4.55", KRANKENHAUS_APOTHEKE); // Krankenhausapotheke
        roleMap.put("1.2.276.0.76.4.56", BUNDESWEHR_APOTHEKE); // Bundeswehrapotheke
        roleMap.put("1.2.276.0.76.4.57", MOBILE_EINRICHTUNG_RETTUNGSDIENST); // Betriebsstaette Mobile Einrichtung Rettungsdienst
        roleMap.put("1.2.276.0.76.4.58", BS_GEMATIK); // Betriebsstaette GEMATIK
        roleMap.put("1.2.276.0.76.4.59", MITARBEITER_KOSTENTRAEGER); // Betriebsstaette Kostentraeger
        roleMap.put("1.2.276.0.76.4.187", LEO_ZAHNAERZTE); // Betriebsstaette Leistungserbringerorganisation Zahnaerzte
        roleMap.put("1.2.276.0.76.4.190", ADV_KOSTENTRAEGER); // ADV-Umgebung bei Kostentraeger

        return roleMap.get(oidString);
    }

    private static byte[] getExtensionValue(final byte[] responseData) {
        final X509Certificate cert = CertificateUtil.getCertificate(responseData);
        return cert.getExtensionValue("1.3.36.8.3.3");
    }

    public Result<ProfessionalRole> validateProfessionalRole(final ProfessionalRole role) {
        if (this == role) {
            return Result.success(role);
        } else {
            return Result.failure(new WrongRoleException(String.format("expected role: %s, but was: %s", this, role)));
        }
    }

}

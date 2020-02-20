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

package de.gematik.ti.healthcard.control.entities.card.certificate;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import de.gematik.ti.healthcard.control.exceptions.AuthenticationCertificateException;
import de.gematik.ti.healthcardaccess.operation.Result;
import de.gematik.ti.healthcardaccess.result.Response;

/**
   */
public enum AuthenticationCertificateState {

    VALIDATION_SUCCESS,
    VALIDATION_ERROR;

    public static AuthenticationCertificateState getCertificateValidationResult(final Response response) {
        final byte[] autCertBytes = response.getResponseData();
        final X509Certificate cert;
        try {
            cert = X509Certificate.getInstance(autCertBytes);
            cert.checkValidity();
            return VALIDATION_SUCCESS;

        } catch (final CertificateException e) {
            return VALIDATION_ERROR;
        }
    }

    public Result<AuthenticationCertificateState> getValidationResult(final AuthenticationCertificateState state) {
        if (this == VALIDATION_SUCCESS) {
            return Result.success(state);
        } else {
            return Result.failure(new AuthenticationCertificateException(String.format("expected status: %s, but was: %s", this, state)));
        }
    }
}

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

package de.gematik.ti.healthcard.control.nfdm.states;

import de.gematik.ti.healthcard.control.exceptions.NfdDpeExtractException;
import de.gematik.ti.healthcardaccess.operation.Result;
import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.utils.codec.Hex;

/**
 * Functionalities to check the Version of the NFD Storage structures
 *
    */
public enum NfdDpeVersionState {

    NFD_DPE_VERSION_PERMITTED,
    NFD_DPE_VERSION_INADMISSIBLE;

    private static final int POSITION = 20;
    private static final String PERMITTED_VERSION = "0010000000";

    /**
     * Returns the actual NFD version state
     * @param response response from card
     * @return version state
     */
    public static NfdDpeVersionState getVersionState(final Response response) {
        final byte[] responseData = response.getResponseData();
        final byte[] versionData = new byte[responseData.length - POSITION];

        System.arraycopy(responseData, POSITION, versionData, 0, responseData.length - POSITION);

        String version = Hex.encodeHexString(versionData);
        if (PERMITTED_VERSION.equals(version)) {
            return NFD_DPE_VERSION_PERMITTED;
        } else {
            return NFD_DPE_VERSION_INADMISSIBLE;
        }
    }

    /**
     * validates the determined state
     * @param state determined state
     * @return success, if the determined status matches the expected status, failure if not
     */
    public Result<NfdDpeVersionState> validateVersion(final NfdDpeVersionState state) {
        if (this == NFD_DPE_VERSION_PERMITTED) {
            return Result.success(state);
        } else {
            return Result.failure(new NfdDpeExtractException(String.format("expected state: %s, but was: %s", this, state)));
        }
    }
}

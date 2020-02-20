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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.healthcard.control.exceptions.NfdDpeExtractException;
import de.gematik.ti.healthcardaccess.operation.Result;
import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.utils.codec.Hex;

/**
 * Functionalities to check if NFD data available
 *
    */
public enum NfdDpeDataAvailableState {
    DATA_AVAILABLE,
    NO_DATA_AVAILABLE;

    private static final Logger LOG = LoggerFactory.getLogger(NfdDpeDataAvailableState.class);
    private static final String TAG = "NfdDpeDataAvailableState: ";

    /**
     * Returns whether current NFD data is available
     * @param response Response from card
     * @return NFD data available state
     */
    public static NfdDpeDataAvailableState getDataAvailableState(final Response response) {
        final byte[] responseData = response.getResponseData();
        final byte[] le = new byte[2];

        String leString = "";

        if (responseData.length >= 2) {
            System.arraycopy(responseData, 0, le, 0, 2);
            leString = Hex.encodeHexString(le);
            LOG.debug(TAG, "DataLength: " + leString);
        }

        if ("0000".equals(leString) || leString.length() < 2) {
            return NO_DATA_AVAILABLE;
        } else {
            return DATA_AVAILABLE;
        }
    }

    /**
     * validates the determined state
     * @param state determined state
     * @return success, if the determined status matches the expected status, failure if not
     */
    public Result<NfdDpeDataAvailableState> validateDataAvailableState(final NfdDpeDataAvailableState state) {
        if (this == state) {
            return Result.success(state);
        } else {
            return Result.failure(new NfdDpeExtractException(String.format("expected state: %s, but was: %s", this, state)));
        }
    }
}

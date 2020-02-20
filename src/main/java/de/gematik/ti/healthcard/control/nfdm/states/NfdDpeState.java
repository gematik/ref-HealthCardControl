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

/**
 * Functionalities to check the technical consistency of the NFD folder
 *
   */
public enum NfdDpeState {

    TRANSACTIONS_OPEN, // Status im StatusElement == '1'
    NO_TRANSACTIONS_OPEN; // Status im StatusElement == '0'

    /**
     * Returns the actual state of technical consistency of the NFD folder
     * @param response Response from card
     * @return NFD state
     */
    public static NfdDpeState getState(final Response response) {

        final byte[] responseData = response.getResponseData();
        final byte[] stateBytes = new byte[1];

        System.arraycopy(responseData, 0, stateBytes, 0, 1);
        final char nfdState = (char) stateBytes[0];

        if (nfdState == '0') {
            return NO_TRANSACTIONS_OPEN;
        } else {
            return TRANSACTIONS_OPEN;
        }
    }

    /**
     * validates the determined state
     * @param state determined state
     * @return success, if the determined status matches the expected status, failure if not
     */
    public Result<NfdDpeState> validateState(final NfdDpeState state) {
        if (this == state) {
            return Result.success(state);
        } else {
            return Result.failure(new NfdDpeExtractException(String.format("State: expected state: %s, but was: %s", this, state)));
        }
    }
}

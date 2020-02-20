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
import de.gematik.ti.healthcardaccess.WrongCardDataException;
import de.gematik.ti.healthcardaccess.cardobjects.FileControlParameter;
import de.gematik.ti.healthcardaccess.operation.Result;
import de.gematik.ti.healthcardaccess.result.Response;

/**
 * Functionalities to check the visibility of NFD folder
 *
   */
public enum NfdDpeLifeCycleState {

    LCS_CREATION_STATE,
    LCS_INITIALISATION_STATE,
    LCS_OPERATIONAL_STATE_ACTIVATED,
    LCS_OPERATIONAL_STATE_DEACTIVATED,
    LCS_TERMINATION_STATE;

    /**
     * Returns the actual life cycle state
     * @param response Response from card
     * @return life cycle state
     * @throws WrongCardDataException if an error occurs
     */
    public static NfdDpeLifeCycleState getLifeCycleStateResult(final Response response) throws WrongCardDataException {
        final byte[] responseData = response.getResponseData();
        if (responseData.length == 0) {
            return LCS_OPERATIONAL_STATE_DEACTIVATED;
        } else {
            final FileControlParameter fcp = new FileControlParameter(response.getResponseData());
            final FileControlParameter.LifeCycleStates lifeCycleStatus = fcp.getLifeCycleStatus();

            switch (lifeCycleStatus) {
                case LCS_OPERATIONAL_STATE_ACTIVATED:
                    return LCS_OPERATIONAL_STATE_ACTIVATED;
                case LCS_CREATION_STATE:
                    return LCS_CREATION_STATE;
                case LCS_TERMINATION_STATE:
                    return LCS_TERMINATION_STATE;
                case LCS_INITIALISATION_STATE:
                    return LCS_INITIALISATION_STATE;
                case LCS_OPERATIONAL_STATE_DEACTIVATED:
                    return LCS_OPERATIONAL_STATE_DEACTIVATED;
                default:
                    return LCS_OPERATIONAL_STATE_DEACTIVATED;
            }
        }
    }

    /**
     * validates the determined state
     * @param state determined state
     * @return success, if the determined status matches the expected status, failure if not
     */
    public Result<NfdDpeLifeCycleState> validateLifeCycleState(final NfdDpeLifeCycleState state) {
        if (!state.equals(LCS_OPERATIONAL_STATE_DEACTIVATED)) {
            return Result.success(state);
        } else {
            return Result.failure(new NfdDpeExtractException(String.format("NfdLifeCycleState: expected state: %s, but was: %s", this, state)));
        }
    }
}

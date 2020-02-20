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

package de.gematik.ti.healthcard.control.entities.card.pin;

import de.gematik.ti.healthcard.control.exceptions.PinStateException;
import de.gematik.ti.healthcardaccess.operation.Result;
import de.gematik.ti.healthcardaccess.result.Response;

/**
    */
public enum PinState {
    TRANSPORT_STATUS_TRANSPORT_PIN,
    TRANSPORT_STATUS_EMPTY_PIN,
    PASSWORD_DISABLED,
    RETRY_COUNTER_COUNT_00,
    RETRY_COUNTER_COUNT_01,
    RETRY_COUNTER_COUNT_02,
    RETRY_COUNTER_COUNT_03,
    NO_ERROR,
    SECURITY_STATUS_NOT_SATISFIED,
    PASSWORD_NOT_FOUND;

    public static PinState getPinStateResult(final Response response) {
        switch (response.getResponseStatus()) {
            case TRANSPORT_STATUS_TRANSPORT_PIN:
                return TRANSPORT_STATUS_TRANSPORT_PIN;
            case TRANSPORT_STATUS_EMPTY_PIN:
                return TRANSPORT_STATUS_EMPTY_PIN;
            case PASSWORD_DISABLED:
                return PASSWORD_DISABLED;
            case RETRY_COUNTER_COUNT_01:
                return RETRY_COUNTER_COUNT_01;
            case RETRY_COUNTER_COUNT_02:
                return RETRY_COUNTER_COUNT_02;
            case RETRY_COUNTER_COUNT_03:
                return RETRY_COUNTER_COUNT_03;
            case SUCCESS:
                return NO_ERROR;
            case SECURITY_STATUS_NOT_SATISFIED:
                return SECURITY_STATUS_NOT_SATISFIED;
            case PASSWORD_NOT_FOUND:
                return PASSWORD_NOT_FOUND;
            default:
                return RETRY_COUNTER_COUNT_00; // Pin blocked
        }
    }

    public Result<PinState> validatePinState(final PinState state) {
        if (this == state) {
            return Result.success(state);
        } else {
            return Result.failure(new PinStateException(
                    String.format("expected status: %s, but was: %s", this, state)));
        }
    }
}

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

package de.gematik.ti.healthcard.control.c2c;

import de.gematik.ti.healthcard.control.exceptions.CardToCardProcessException;
import de.gematik.ti.healthcardaccess.operation.Result;
import de.gematik.ti.healthcardaccess.result.Response;

public enum C2CSuccessState {
    C2C_SUCCESS,
    C2C_FAILED;

    public Result<C2CSuccessState> validateSuccessState(final C2CSuccessState state) {
        if (this == state) {
            return Result.success(state);
        } else {
            return Result.failure(new CardToCardProcessException(
                    String.format("expected status: %s, but was: %s", this, state)));
        }
    }

    public C2CSuccessState validateResponse(final Response response) {
        switch (response.getResponseStatus()) {
            case SUCCESS:
                return C2C_SUCCESS;
            default:
                return C2C_FAILED;
        }
    }
}

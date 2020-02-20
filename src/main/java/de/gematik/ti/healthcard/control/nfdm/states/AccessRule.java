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

import de.gematik.ti.healthcard.control.entities.card.pin.PinState;

/**
 * Functionalities to check Access rule to read NFD on Card
 *
   */
public enum AccessRule {

    R1,
    R2,
    R3,
    R4;

    /**
     * Returns Access rule
     */
    public static AccessRule getAccessRule(final PinState pinState,
            final boolean emergencyIndicator, final boolean updateIndicator) {

        if (emergencyIndicator) {
            return R1;
        } else if (updateIndicator) {
            return R2;
        } else if (pinState != PinState.PASSWORD_DISABLED) {
            return R3;
        } else {
            return R4;
        }
    }
}

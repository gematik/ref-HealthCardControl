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

/**
 * C2CMode determines the type of authentication:
 * • one-sided, mutually
 * • with or without negotiation of keys for a secure channel
 * • Authentication type such as Role Authentication or Device Authentication(according to the CV certificates contained on the smart cards).
 * • Optimized activation / authentication of the eGK in which the eGK is activated by the source but authenticated by the connector.
 *
  * @version
 */
public enum C2CMode {

    // @TODO: Add Values for TIP1-A_2292 (gemKPT_Arch_TIP_V2.6.0)

    MUTUALLY
}

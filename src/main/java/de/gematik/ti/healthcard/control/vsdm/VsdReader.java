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

package de.gematik.ti.healthcard.control.vsdm;

import de.gematik.ti.healthcardaccess.HealthCard;

/**
 * Functionalities to read Vsd on Card
 * 
 * @TODO implementation of VSDM-UC_01 (gemSysL_VSDM_2.0.0)
 * @TODO implementation of gemSysL_VSDM_2.0.0
 *
   */
public class VsdReader {

    private final HealthCard cardToRead;

    public VsdReader(final HealthCard cardToRead) {
        this.cardToRead = cardToRead;
    }

    /**
     * The operation reads the StatusVD in the VSD information element of the file EF.StatusVD of the card
     *
     * @TODO implementation of VSDM-A_2043 (gemSysL_VSDM_2.0.0)
     *
     * @return data as byte array
     */
    public byte[] readStatusVsd() {
        return new byte[0];
    }

    /**
     * The operation reads the PD in the VSD information element of the file EF.PD of the card
     *
     * @TODO implementation of VSDM-A_2044 (gemSysL_VSDM_2.0.0)
     *
     * @return data as byte array
     */
    public byte[] readPd() {
        return new byte[0];
    }

    /**
     * The operation reads the GVD in the VSD information element of the file EF.GVD of the card
     *
     * @TODO implementation of VSDM-A_2045 (gemSysL_VSDM_2.0.0)
     *
     * @return data as byte array
     */
    public byte[] readGvd() {
        return new byte[0];
    }

    /**
     * The operation reads the VD in the VSD information element of the file EF.VD of the card
     *
     * @TODO implementation of VSDM-A_2044 (gemSysL_VSDM_2.0.0)
     *
     * @return data as byte array
     */
    public byte[] readVd() {
        return new byte[0];
    }
}

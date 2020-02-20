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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The test result provides information about PIN verification
 */

public class PinResult {
    private static final Logger LOG = LoggerFactory.getLogger(PinResult.class);
    private boolean pinVerifiSuccess;
    private int numberRemain = -1;
    private String errorTextByFailure = null;
    private String warningText = null;

    /**
     *
     * @param status
     */
    public PinResult(final String status) {
        LOG.debug("status: " + status);
        switch (status) {
            case "NO_ERROR":
                break;
            case "RETRY_COUNTER_COUNT_00":
                setNumberRemain(0);
                warningText = "No retry remains";
                break;
            case "RETRY_COUNTER_COUNT_01":
                setNumberRemain(1);
                warningText = "1 retry remains";
                break;
            case "RETRY_COUNTER_COUNT_02":
                setNumberRemain(2);
                warningText = "2 retries remain";
                break;
            case "RETRY_COUNTER_COUNT_03":
                setNumberRemain(3);
                warningText = "3 retries remain";
                break;
            case "TRANSPORT_STATUS_TRANSPORT_PIN":
                errorTextByFailure = "It's a transport-Pin. Please initialize your Pin at first.";
                break;
            case "TRANSPORT_STATUS_EMPTY_PIN":
                errorTextByFailure = "It's a LeerPin. Please initialize your Pin at first.";
                break;
            case "PASSWORD_DISABLED":
                errorTextByFailure = "PASSWORD_DISABLED";
                break;
            case "SECURITY_STATUS_NOT_SATISFIED":
                errorTextByFailure = "SECURITY_STATUS_NOT_SATISFIED";
                break;
            case "PASSWORD_NOT_FOUND":
                errorTextByFailure = "PASSWORD_NOT_FOUND";
                break;
            default:
                LOG.error("status +" + status + "' is not defined in PinResult.");
                break;
        }
    }

    public int getNumberRemain() {
        return numberRemain;
    }

    public void setNumberRemain(final int numberRemain) {
        this.numberRemain = numberRemain;
    }

    public boolean isPinVerifiSuccess() {
        return pinVerifiSuccess;
    }

    public PinResult setPinVerifiSuccess(final boolean pinVerifiSuccess) {
        this.pinVerifiSuccess = pinVerifiSuccess;
        return this;
    }

    public String getErrorTextByFailure() {
        return errorTextByFailure;
    }

    public String getWarningText() {
        return warningText;
    }
}

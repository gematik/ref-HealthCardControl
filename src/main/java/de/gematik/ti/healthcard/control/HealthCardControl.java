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

package de.gematik.ti.healthcard.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * include::{userguide}/HCARDC_Structure.adoc[tag=HealthCardControl]
 *
   */
public final class HealthCardControl {
    private static final Logger LOG = LoggerFactory.getLogger(HealthCardControl.class);
    private static final String TAG = "HealthCardControl: ";
    private static HealthCardControl instance;

    private HealthCardControl() {
    }

    /**
     * Get the singleton instance
     * @return HealthCardControl instance
     */
    public static HealthCardControl getInstance() {
        if (instance == null) {
            instance = new HealthCardControl();
        }
        return instance;
    }

    /**
     * Start the detection of cards and other Services to response on eventbus request
     */
    public static void start() {
        CardDetector.startDetection();
        TrustedChannelPaceKeyRequestHandler.startHandling();
    }

    /**
     * Stop the detection of cards and other Services to response on eventbus request
     */
    public static void stop() {
        CardDetector.stopDetection();
        TrustedChannelPaceKeyRequestHandler.stopHandling();
    }

}

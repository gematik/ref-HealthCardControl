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

import org.junit.Assert;
import org.junit.Test;

import de.gematik.ti.healthcardaccess.result.Response;
import de.gematik.ti.utils.codec.Hex;

/**
 * Test {@link PinState}
  */
public class PinStateTest {

    private Response transportPinEmptyResponse = new Response(Response.ResponseStatus.TRANSPORT_STATUS_EMPTY_PIN, Hex.decode(""));
    private Response transportPinResponse = new Response(Response.ResponseStatus.TRANSPORT_STATUS_TRANSPORT_PIN, Hex.decode(""));
    private Response passwordDisabledResponse = new Response(Response.ResponseStatus.PASSWORD_DISABLED, Hex.decode(""));
    private Response retryCounter3Response = new Response(Response.ResponseStatus.RETRY_COUNTER_COUNT_03, Hex.decode(""));
    private Response retryCounter2Response = new Response(Response.ResponseStatus.RETRY_COUNTER_COUNT_02, Hex.decode(""));
    private Response retryCounter1Response = new Response(Response.ResponseStatus.RETRY_COUNTER_COUNT_01, Hex.decode(""));
    private Response passwordBlockedResponse = new Response(Response.ResponseStatus.RETRY_COUNTER_COUNT_00, Hex.decode(""));
    private Response noErrorResponse = new Response(Response.ResponseStatus.SUCCESS, Hex.decode(""));
    private Response securityStatusNotSatisfiedResponse = new Response(Response.ResponseStatus.SECURITY_STATUS_NOT_SATISFIED, Hex.decode(""));
    private Response passwordNotFoundResponse = new Response(Response.ResponseStatus.PASSWORD_NOT_FOUND, Hex.decode(""));

    @Test
    public void testGetPinStateResult() {
        PinState pinState1 = PinState.getPinStateResult(transportPinEmptyResponse);
        Assert.assertEquals(PinState.TRANSPORT_STATUS_EMPTY_PIN, pinState1);
        pinState1 = PinState.getPinStateResult(transportPinResponse);
        Assert.assertEquals(PinState.TRANSPORT_STATUS_TRANSPORT_PIN, pinState1);
        pinState1 = PinState.getPinStateResult(passwordDisabledResponse);
        Assert.assertEquals(PinState.PASSWORD_DISABLED, pinState1);
        pinState1 = PinState.getPinStateResult(retryCounter3Response);
        Assert.assertEquals(PinState.RETRY_COUNTER_COUNT_03, pinState1);
        pinState1 = PinState.getPinStateResult(retryCounter2Response);
        Assert.assertEquals(PinState.RETRY_COUNTER_COUNT_02, pinState1);
        pinState1 = PinState.getPinStateResult(retryCounter1Response);
        Assert.assertEquals(PinState.RETRY_COUNTER_COUNT_01, pinState1);
        pinState1 = PinState.getPinStateResult(passwordBlockedResponse);
        Assert.assertEquals(PinState.RETRY_COUNTER_COUNT_00, pinState1);
        pinState1 = PinState.getPinStateResult(noErrorResponse);
        Assert.assertEquals(PinState.NO_ERROR, pinState1);
        pinState1 = PinState.getPinStateResult(securityStatusNotSatisfiedResponse);
        Assert.assertEquals(PinState.SECURITY_STATUS_NOT_SATISFIED, pinState1);
        pinState1 = PinState.getPinStateResult(passwordNotFoundResponse);
        Assert.assertEquals(PinState.PASSWORD_NOT_FOUND, pinState1);
    }

}

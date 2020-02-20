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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.card.CardException;
import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.cardreader.provider.api.card.ICardChannel;
import de.gematik.ti.cardreader.provider.api.command.ICommandApdu;
import de.gematik.ti.cardreader.provider.api.command.ResponseApdu;
import de.gematik.ti.cardreader.provider.api.events.CardReaderDisconnectedEvent;
import de.gematik.ti.cardreader.provider.api.events.card.CardAbsentEvent;
import de.gematik.ti.cardreader.provider.api.events.card.CardPresentEvent;
import de.gematik.ti.healthcard.control.events.healthcard.absent.*;
import de.gematik.ti.healthcard.control.events.healthcard.present.*;
import de.gematik.ti.healthcardaccess.IHealthCard;
import de.gematik.ti.utils.codec.Hex;

@RunWith(Parameterized.class)
public class CardDetectorTest {
    private static final Logger LOG = LoggerFactory.getLogger(CardDetectorTest.class);

    private final String selectRootResponse;
    private final String readVersion2Response;
    private final Class absentEventClass;
    private final Class presentEventClass;

    private CountDownLatch presentLatch;
    private CountDownLatch absentLatch;
    private final ICardReader iCardReader = Mockito.mock(ICardReader.class);
    private AbstractPresentEventTransferCallback presentEventCallback;

    public CardDetectorTest(String selectRootResponse, String readVersion2Response, Class absentEventClass, Class presentEventClass) {
        this.selectRootResponse = selectRootResponse;
        this.readVersion2Response = readVersion2Response;
        this.absentEventClass = absentEventClass;
        this.presentEventClass = presentEventClass;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "623682017883023F008407D276000144800085030074188A0105A11C9101018A0204048A020C048B01039101028A0204048A020C048B01059000",
                        "EF2BC003020000C103040300C2104445472B4453333645474B3031010000C403010000C503020000C7030100009000",
                        Egk2HealthCardAbsentEvent.class,
                        Egk2HealthCardPresentEvent.class },
                { "6281F982017883023F008A01058406D27600014606A181919103010500AB81898C0280FAA041AF37B803950110B803950120A02BB4199501307F4C1306082A8214004C048119530700800000000000B406950130830114B406950130830118A40695010883011F8C02808490008401EAAF37B803950110B803950120A02BB4199501307F4C1306082A8214004C048119530700800000000000B406950130830114B406950130830118A25188014851043FFF2F0988013051043FFF2F0688011851043FFF2F0388013851043FFF2F0788012051043FFF2F0488018851043FFF2F1188011051043FFF2F028801F051043FFF2F008801E851043FFF2F019000",
                        "EF26C003020000C103040300C210545359534954434F5353423230020103C403010000C5030200009000",
                        Smcb2HealthCardAbsentEvent.class,
                        Smcb2HealthCardPresentEvent.class },
                { "621383023F008A01058201788407D27600014480009000",
                        "EF2BC003020000C103040400C21044454F52476548435F38303030030000C403010000C503020000C7030100009000",
                        Egk21HealthCardAbsentEvent.class,
                        Egk21HealthCardPresentEvent.class },
                { "623582017883023F008406D27600014601850300F0168A0105A11C9101018A0204048A020C048B01039101028A0204048A020C048B01059000",
                        "EF26C003020000C103040300C2104445472B445333364842413031010000C403010000C5030200009000",
                        Hba2HealthCardAbsentEvent.class,
                        Hba2HealthCardPresentEvent.class },
        });
    }

    public abstract class AbstractPresentEventTransferCallback {
        abstract void onSuccess(AbstractHealthCardPresentEvent event);
    }

    private AbstractAbsentEventTransferCallback absentEventCallback;

    public abstract class AbstractAbsentEventTransferCallback {
        abstract void onSuccess(AbstractHealthCardAbsentEvent event);
    }

    @Before
    public void setup() {
        presentLatch = new CountDownLatch(1);
        absentLatch = new CountDownLatch(1);
        Mockito.when(iCardReader.getName()).thenReturn("JunitTest Reader");
        EventBus.getDefault().register(this);
    }

    @After
    public void end() {
        EventBus.getDefault().unregister(this);
    }

    @Test
    public void getInstance() {
        Assert.assertNotNull(CardDetector.getInstance());
    }

    @Test
    public void startStopDetection() {
        Assert.assertFalse(EventBus.getDefault().isRegistered(CardDetector.getInstance()));
        CardDetector.startDetection();
        Assert.assertTrue(EventBus.getDefault().isRegistered(CardDetector.getInstance()));
        CardDetector.stopDetection();
        Assert.assertFalse(EventBus.getDefault().isRegistered(CardDetector.getInstance()));
    }

    @Test
    public void handleCardPresentAbsentEvents() throws Exception {
        handleCardPresentEvents();
        handleCardAbsentEvents();
    }

    @Test
    public void handleCardReaderDisconnectedEvents() throws Exception {
        handleCardPresentEvents();

        final boolean[] testPassed = handleCardAbsentEventsPrepare();
        CardDetector.getInstance().handleCardReaderDisconnectedEvents(new CardReaderDisconnectedEvent(iCardReader));

        checkAbsentEventCorrect(testPassed[0]);
    }

    @Test
    public void handleCardPresentAbsentEventsWithNullCard() throws Exception {
        checkHealthCardMap(0);

        final boolean[] testPassed = new boolean[] { true };
        presentEventCallback = new AbstractPresentEventTransferCallback() {

            @Override
            public void onSuccess(AbstractHealthCardPresentEvent event) {
                LOG.debug("JUNIT Event " + event + " empfangen!");
                testPassed[0] = false;
                Assert.assertTrue(false);
            }
        };

        CardPresentEvent event = new CardPresentEvent(iCardReader);
        CardDetector.getInstance().handleCardPresentEvents(event);

        checkHealthCardMap(0);
        Assert.assertTrue(testPassed[0]);

    }

    @Test
    public void handleCardPresentAbsentEventsWithConnectException() throws Exception {
        checkHealthCardMap(0);

        final boolean[] testPassed = new boolean[] { true };
        presentEventCallback = new AbstractPresentEventTransferCallback() {

            @Override
            public void onSuccess(AbstractHealthCardPresentEvent event) {
                LOG.debug("JUNIT Event " + event + " empfangen!");
                testPassed[0] = false;
                Assert.assertTrue(false);
            }
        };

        Mockito.when(iCardReader.connect()).thenThrow(new CardException("JunitTest"));
        CardPresentEvent event = new CardPresentEvent(iCardReader);
        CardDetector.getInstance().handleCardPresentEvents(event);

        checkHealthCardMap(0);
        Assert.assertTrue(testPassed[0]);

    }

    private void handleCardPresentEvents() throws Exception {
        checkHealthCardMap(0);
        final boolean[] testPassed = new boolean[] { false };
        presentEventCallback = new AbstractPresentEventTransferCallback() {

            @Override
            public void onSuccess(AbstractHealthCardPresentEvent event) {
                LOG.debug("JUNIT Event " + event + " empfangen!");
                Assert.assertNotNull(event);
                Assert.assertNotNull(event.getHealthCard());
                Assert.assertNotNull(event.getHealthCard().getStatus());
                Assert.assertEquals(presentEventClass, event.getClass());
                Assert.assertTrue(event.getHealthCard().getStatus().isValid());
                testPassed[0] = true;
            }
        };

        sendPresentEvent();

        Assert.assertTrue(testPassed[0]);
        checkHealthCardMap(1);

    }

    private void sendPresentEvent() throws CardException {
        ICard card = Mockito.mock(ICard.class);
        ICardChannel cardChannel = Mockito.mock(ICardChannel.class);
        Mockito.when(card.openLogicalChannel()).thenReturn(cardChannel);
        Mockito.when(card.openBasicChannel()).thenReturn(cardChannel);
        Mockito.verify(cardChannel, times(0)).transmit(any(ICommandApdu.class));

        ResponseApdu responseApduSelect = new ResponseApdu(
                Hex.decode(selectRootResponse));
        Mockito.when(cardChannel.transmit(Mockito.argThat(new SelectRootMatcher()))).thenReturn(responseApduSelect);

        ResponseApdu responseApduRead = new ResponseApdu(
                Hex.decode(readVersion2Response));
        Mockito.when(cardChannel.transmit(Mockito.argThat(new ReadVersion2Matcher()))).thenReturn(responseApduRead);

        CardPresentEvent event = new CardPresentEvent(iCardReader);
        Mockito.when(iCardReader.connect()).thenReturn(card);
        CardDetector.getInstance().handleCardPresentEvents(event);

    }

    private void handleCardAbsentEvents() throws Exception {
        final boolean[] testPassed = handleCardAbsentEventsPrepare();

        CardAbsentEvent cardAbsentEvent = new CardAbsentEvent(iCardReader);
        CardDetector.getInstance().handleCardAbsentEvents(cardAbsentEvent);

        checkAbsentEventCorrect(testPassed[0]);

    }

    private void checkAbsentEventCorrect(final boolean condition) throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        checkHealthCardMap(0);
        Assert.assertTrue(condition);
    }

    private boolean[] handleCardAbsentEventsPrepare() throws NoSuchFieldException, IllegalAccessException {
        checkHealthCardMap(1);

        final boolean[] testPassed = new boolean[] { false };
        absentEventCallback = new AbstractAbsentEventTransferCallback() {

            @Override
            public void onSuccess(AbstractHealthCardAbsentEvent event) {
                LOG.debug("JUNIT Event " + event + " empfangen!");
                Assert.assertNotNull(event);
                Assert.assertNotNull(event.getHealthCard());
                Assert.assertNotNull(event.getHealthCard().getStatus());
                Assert.assertEquals(absentEventClass, event.getClass());
                Assert.assertTrue(event.getHealthCard().getStatus().isValid());
                testPassed[0] = true;
            }
        };
        return testPassed;
    }

    private void checkHealthCardMap(final int size) throws NoSuchFieldException, IllegalAccessException {
        Field privateStringField = CardDetector.class.getDeclaredField("healthCardMap");
        privateStringField.setAccessible(true);
        Map<ICardReader, IHealthCard> healthCardMap = (Map<ICardReader, IHealthCard>) privateStringField.get(CardDetector.getInstance());
        Assert.assertEquals(size, healthCardMap.size());
    }

    // EventBus Messages Receive
    @Subscribe
    public void onReceiveEventBusPresentEvent(AbstractHealthCardPresentEvent event) {
        Assert.assertNotNull(presentEventCallback);
        presentEventCallback.onSuccess(event);
        presentLatch.countDown();
    }

    @Subscribe
    public void onReceiveEventBusAbsentEvent(AbstractHealthCardAbsentEvent event) {
        Assert.assertNotNull(absentEventCallback);
        absentEventCallback.onSuccess(event);
        absentLatch.countDown();
    }

    // Matchers for Tests
    class SelectRootMatcher implements ArgumentMatcher<ICommandApdu> {

        @Override
        public boolean matches(ICommandApdu commandAPDU) {
            if (commandAPDU == null) {
                return false;
            }
            String hexString = Hex.encodeHexString(commandAPDU.getBytes());
            boolean equals = hexString.equals("00A4040400");
            LOG.debug("hexString: {}, equals: {}", hexString, equals); // don't use assert according to javadoc. it maybe false.
            return equals;
        }
    }

    class ReadVersion2Matcher implements ArgumentMatcher<ICommandApdu> {

        @Override
        public boolean matches(ICommandApdu commandAPDU) {
            if (commandAPDU == null) {
                return false;
            }
            String hexString = Hex.encodeHexString(commandAPDU.getBytes());
            boolean equals = hexString.equals("00B0910000");
            LOG.debug("hexString: {}, equals: {}", hexString, equals); // don't use assert according to javadoc. it maybe false.
            return equals;
        }
    }
}

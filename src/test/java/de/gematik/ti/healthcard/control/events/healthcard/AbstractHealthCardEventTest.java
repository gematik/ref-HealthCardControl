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

package de.gematik.ti.healthcard.control.events.healthcard;

import org.junit.Assert;
import org.powermock.api.mockito.PowerMockito;

import de.gematik.ti.cardreader.provider.api.ICardReader;
import de.gematik.ti.cardreader.provider.api.card.ICard;
import de.gematik.ti.healthcard.control.events.healthcard.absent.AbstractHealthCardAbsentEvent;
import de.gematik.ti.healthcard.control.events.healthcard.present.AbstractHealthCardPresentEvent;
import de.gematik.ti.healthcardaccess.HealthCard;
import de.gematik.ti.healthcardaccess.IHealthCard;

public abstract class AbstractHealthCardEventTest {

    protected final ICardReader cardReader = PowerMockito.mock(ICardReader.class);
    protected final IHealthCard healthCard = new HealthCard(PowerMockito.mock(ICard.class));

    protected void checkEventContent(final AbstractHealthCardEvent event) {
        Assert.assertNotNull(event);
        Assert.assertEquals(healthCard, event.getHealthCard());
        Assert.assertEquals(cardReader, event.getCardReader());
    }

    protected void checkPresentEvent(AbstractHealthCardEvent event) {
        Assert.assertTrue(event instanceof AbstractHealthCardPresentEvent);
    }

    protected void checkAbsentEvent(AbstractHealthCardEvent event) {
        Assert.assertTrue(event instanceof AbstractHealthCardAbsentEvent);
    }
}

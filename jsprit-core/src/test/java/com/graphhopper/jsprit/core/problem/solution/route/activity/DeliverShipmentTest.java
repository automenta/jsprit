/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DeliverShipmentTest {

    private DeliverShipment deliver;

    @Before
    public void doBefore() {
        Shipment shipment = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.Builder.the().setId("pickupLoc").build())
            .setDeliveryLocation(Location.the("deliveryLoc"))
            .setPickupTimeWindow(TimeWindow.the(1., 2.))
            .setDeliveryTimeWindow(TimeWindow.the(3., 4.))
            .addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
        deliver = new DeliverShipment(shipment);
        deliver.startEarliest(shipment.getDeliveryTimeWindow().start);
        deliver.startLatest(shipment.getDeliveryTimeWindow().end);
    }

    @Test
    public void whenCallingCapacity_itShouldReturnCorrectCapacity() {
        assertEquals(-10, deliver.size().get(0));
        assertEquals(-100, deliver.size().get(1));
        assertEquals(-1000, deliver.size().get(2));
    }

    @Test
    public void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        assertEquals(3., deliver.startEarliest(), 0.01);
    }

    @Test
    public void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        assertEquals(4., deliver.startLatest(), 0.01);
    }

    @Test
    public void whenSettingArrTime_itShouldBeSetCorrectly() {
        deliver.arrTime(4.0);
        assertEquals(4., deliver.arrTime(), 0.01);
    }

    @Test
    public void whenSettingEndTime_itShouldBeSetCorrectly() {
        deliver.end(5.0);
        assertEquals(5., deliver.end(), 0.01);
    }

    @Test
    public void whenIniLocationId_itShouldBeSetCorrectly() {
        assertEquals("deliveryLoc", deliver.location().id);
    }

    @Test
    public void whenCopyingStart_itShouldBeDoneCorrectly() {
        DeliverShipment copy = (DeliverShipment) deliver.clone();
        assertEquals(3., copy.startEarliest(), 0.01);
        assertEquals(4., copy.startLatest(), 0.01);
        assertEquals("deliveryLoc", copy.location().id);
        assertEquals(-10, copy.size().get(0));
        assertEquals(-100, copy.size().get(1));
        assertEquals(-1000, copy.size().get(2));
        assertTrue(copy != deliver);
    }


    @Test
    public void whenGettingCapacity_itShouldReturnItCorrectly() {
        Shipment shipment = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.the().setId("pickLoc").build()).setDeliveryLocation(Location.the("delLoc"))
            .addSizeDimension(0, 10).addSizeDimension(1, 100).build();
        PickupShipment pick = new PickupShipment(shipment);
        assertEquals(10, pick.size().get(0));
        assertEquals(100, pick.size().get(1));
    }

}

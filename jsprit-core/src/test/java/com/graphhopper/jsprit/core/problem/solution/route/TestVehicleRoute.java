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
package com.graphhopper.jsprit.core.problem.solution.route;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl.NoDriver;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class TestVehicleRoute {

    private VehicleImpl vehicle;
    private NoDriver driver;

    @Before
    public void doBefore() {
        vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("loc")).setType(VehicleTypeImpl.Builder.the("yo").build()).build();
        driver = DriverImpl.noDriver();
    }

    @Test
    public void whenBuildingEmptyRouteCorrectly_go() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(VehicleImpl.get(), DriverImpl.noDriver()).build();
        assertTrue(route != null);
    }

    @Test
    public void whenBuildingEmptyRouteCorrectlyV2_go() {
        VehicleRoute route = VehicleRoute.emptyRoute();
        assertTrue(route != null);
    }

    @Test
    public void whenBuildingEmptyRoute_ActivityIteratorIteratesOverZeroActivities() {
        VehicleRoute route = VehicleRoute.emptyRoute();
        Iterator<AbstractActivity> iter = route.tourActivities().iterator();
        int count = 0;
        while (iter.hasNext()) {
            iter.next();
            count++;
        }
        assertEquals(0, count);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenBuildingRouteWithNulls_itThrowsException() {
        @SuppressWarnings("unused")
        VehicleRoute route = VehicleRoute.Builder.newInstance(null, null).build();
    }

    @Test
    public void whenBuildingANonEmptyTour2Times_tourIterIteratesOverActivitiesCorrectly() {
        VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
        routeBuilder.addService(Service.Builder.newInstance("2").sizeDimension(0, 30).location(Location.the("1")).build());
        VehicleRoute route = routeBuilder.build();

        {
            Iterator<AbstractActivity> iter = route.tourActivities().iterator();
            int count = 0;
            while (iter.hasNext()) {
                @SuppressWarnings("unused")
                AbstractActivity act = iter.next();
                count++;
            }
            assertEquals(1, count);
        }
        route.tourActivities().addActivity(ServiceActivity.newInstance(Service.Builder.newInstance("3").sizeDimension(0, 30).location(Location.the("1")).build()));
        Iterator<AbstractActivity> iter = route.tourActivities().iterator();
        int count = 0;
        while (iter.hasNext()) {
            @SuppressWarnings("unused")
            AbstractActivity act = iter.next();
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void whenBuildingANonEmptyTour_tourReverseIterIteratesOverActivitiesCorrectly() {
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).build();
        Iterator<AbstractActivity> iter = route.tourActivities().reverseActivityIterator();
        int count = 0;
        while (iter.hasNext()) {
            @SuppressWarnings("unused")
            AbstractActivity act = iter.next();
            count++;
        }
        assertEquals(0, count);
    }

    @Test
    public void whenBuildingANonEmptyTourV2_tourReverseIterIteratesOverActivitiesCorrectly() {
        VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
        routeBuilder.addService(Service.Builder.newInstance("2").sizeDimension(0, 30).location(Location.the("1")).build());
        VehicleRoute route = routeBuilder.build();
        Iterator<AbstractActivity> iter = route.tourActivities().reverseActivityIterator();
        int count = 0;
        while (iter.hasNext()) {
            @SuppressWarnings("unused")
            AbstractActivity act = iter.next();
            count++;
        }
        assertEquals(1, count);
    }

    @Test
    public void whenBuildingANonEmptyTour2Times_tourReverseIterIteratesOverActivitiesCorrectly() {
        VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
        routeBuilder.addService(Service.Builder.newInstance("2").sizeDimension(0, 30).location(Location.the("1")).build());
        routeBuilder.addService(Service.Builder.newInstance("3").sizeDimension(0, 30).location(Location.the("2")).build());
        VehicleRoute route = routeBuilder.build();
        {
            Iterator<AbstractActivity> iter = route.tourActivities().reverseActivityIterator();
            int count = 0;
            while (iter.hasNext()) {
                AbstractActivity act = iter.next();
                if (count == 0) {
                    assertEquals("2", act.location().id);
                }
                count++;
            }
            assertEquals(2, count);
        }
        Iterator<AbstractActivity> secondIter = route.tourActivities().reverseActivityIterator();
        int count = 0;
        while (secondIter.hasNext()) {
            AbstractActivity act = secondIter.next();
            if (count == 0) {
                assertEquals("2", act.location().id);
            }
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void whenBuildingRouteWithVehicleThatHasDifferentStartAndEndLocation_routeMustHaveCorrectStartLocation() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        assertTrue(vRoute.start.location().id.equals("start"));
    }

    @Test
    public void whenBuildingRouteWithVehicleThatHasDifferentStartAndEndLocation_routeMustHaveCorrectEndLocation() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        assertTrue(vRoute.end.location().id.equals("end"));
    }

    @Test
    public void whenBuildingRouteWithVehicleThatHasSameStartAndEndLocation_routeMustHaveCorrectStartLocation() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        assertTrue(vRoute.start.location().id.equals("start"));
    }

    @Test
    public void whenBuildingRouteWithVehicleThatHasSameStartAndEndLocation_routeMustHaveCorrectEndLocation() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("start")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        assertTrue(vRoute.end.location().id.equals("start"));
    }

    @Test
    public void whenBuildingRouteWithVehicleThatHasSameStartAndEndLocation_routeMustHaveCorrectStartLocationV2() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        assertTrue(vRoute.start.location().id.equals("start"));
    }

    @Test
    public void whenBuildingRouteWithVehicleThatHasSameStartAndEndLocation_routeMustHaveCorrectEndLocationV2() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("start")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        assertTrue(vRoute.end.location().id.equals("start"));
    }

    @Test
    public void whenBuildingRouteWithVehicleThatHasDifferentStartAndEndLocation_routeMustHaveCorrectDepartureTime() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        assertEquals(vRoute.getDepartureTime(), 100.0, 0.01);
        assertEquals(vRoute.start.end(), 100.0, 0.01);
    }

    @Test
    public void whenBuildingRouteWithVehicleThatHasDifferentStartAndEndLocation_routeMustHaveCorrectEndTime() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        assertEquals(200.0, vRoute.end.startLatest(), 0.01);
    }

    @Test
    public void whenSettingDepartureTimeInBetweenEarliestStartAndLatestArr_routeMustHaveCorrectDepartureTime() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        vRoute.setVehicleAndDepartureTime(vehicle, 150.0);
        assertEquals(vRoute.start.end(), 150.0, 0.01);
        assertEquals(vRoute.getDepartureTime(), 150.0, 0.01);
    }

    @Test
    public void whenSettingDepartureEarlierThanEarliestStart_routeMustHaveEarliestDepTimeAsDepTime() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        vRoute.setVehicleAndDepartureTime(vehicle, 50.0);
        assertEquals(vRoute.start.end(), 100.0, 0.01);
        assertEquals(vRoute.getDepartureTime(), 100.0, 0.01);
    }

    @Test
    public void whenSettingDepartureTimeLaterThanLatestArrival_routeMustHaveThisDepTime() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        vRoute.setVehicleAndDepartureTime(vehicle, 50.0);
        assertEquals(vRoute.start.end(), 100.0, 0.01);
        assertEquals(vRoute.getDepartureTime(), 100.0, 0.01);
    }

    @Test
    public void whenCreatingEmptyRoute_itMustReturnEmptyRoute() {
        @SuppressWarnings("unused")
        VehicleRoute route = VehicleRoute.emptyRoute();
        assertTrue(true);
    }

    @Test
    public void whenIniRouteWithNewVehicle_startLocationMustBeCorrect() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        Vehicle new_vehicle = VehicleImpl.Builder.newInstance("new_v").setEarliestStart(1000).setLatestArrival(2000).setStartLocation(Location.the("new_start")).setEndLocation(Location.the("new_end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        vRoute.setVehicleAndDepartureTime(new_vehicle, 50.0);
        assertEquals("new_start", vRoute.start.location().id);
    }

    @Test
    public void whenIniRouteWithNewVehicle_endLocationMustBeCorrect() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        Vehicle new_vehicle = VehicleImpl.Builder.newInstance("new_v").setEarliestStart(1000).setLatestArrival(2000).setStartLocation(Location.the("new_start")).setEndLocation(Location.the("new_end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        vRoute.setVehicleAndDepartureTime(new_vehicle, 50.0);
        assertEquals("new_end", vRoute.end.location().id);
    }

    @Test
    public void whenIniRouteWithNewVehicle_depTimeMustBeEarliestDepTimeOfNewVehicle() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        Vehicle new_vehicle = VehicleImpl.Builder.newInstance("new_v").setEarliestStart(1000).setLatestArrival(2000).setStartLocation(Location.the("new_start")).setEndLocation(Location.the("new_end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        vRoute.setVehicleAndDepartureTime(new_vehicle, 50.0);
        assertEquals(1000.0, vRoute.getDepartureTime(), 0.01);
    }

    @Test
    public void whenIniRouteWithNewVehicle_depTimeMustBeSetDepTime() {
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        Vehicle new_vehicle = VehicleImpl.Builder.newInstance("new_v").setEarliestStart(1000).setLatestArrival(2000).setStartLocation(Location.the("new_start")).setEndLocation(Location.the("new_end")).build();
        VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        vRoute.setVehicleAndDepartureTime(new_vehicle, 1500.0);
        assertEquals(1500.0, vRoute.getDepartureTime(), 0.01);
    }

    @Test
    public void whenAddingPickup_itShouldBeTreatedAsPickup() {

        Pickup pickup = Pickup.Builder.the("pick").location(Location.the("pickLoc")).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.the("startLoc")).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).addService(pickup).build();

        AbstractActivity act = route.activities().get(0);
        assertTrue(act.name().equals("pickup"));
        assertTrue(act instanceof PickupService);
        assertTrue(((JobActivity) act).job() instanceof Pickup);

    }

    @Test
    public void whenAddingPickup_itShouldBeAdded() {

        Pickup pickup = Pickup.Builder.the("pick").location(Location.the("pickLoc")).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.the("startLoc")).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).addPickup(pickup).build();

        AbstractActivity act = route.activities().get(0);
        assertTrue(act.name().equals("pickup"));
        assertTrue(act instanceof PickupService);
        assertTrue(((JobActivity) act).job() instanceof Pickup);

    }

    @Test
    public void whenAddingDelivery_itShouldBeTreatedAsDelivery() {

        Delivery delivery = Delivery.Builder.newInstance("delivery").location(Location.the("deliveryLoc")).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.the("startLoc")).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).addService(delivery).build();

        AbstractActivity act = route.activities().get(0);
        assertTrue(act.name().equals("delivery"));
        assertTrue(act instanceof DeliverService);
        assertTrue(((JobActivity) act).job() instanceof Delivery);

    }

    @Test
    public void whenAddingDelivery_itShouldBeAdded() {

        Delivery delivery = Delivery.Builder.newInstance("delivery").location(Location.the("deliveryLoc")).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.the("startLoc")).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).addDelivery(delivery).build();

        AbstractActivity act = route.activities().get(0);
        assertTrue(act.name().equals("delivery"));
        assertTrue(act instanceof DeliverService);
        assertTrue(((JobActivity) act).job() instanceof Delivery);

    }
}

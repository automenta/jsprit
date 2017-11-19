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
package com.graphhopper.jsprit.core.problem;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.*;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.v2;
import com.graphhopper.jsprit.core.util.TestUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class VehicleRoutingProblemTest {

    @Test
    public void whenBuildingWithInfiniteFleet_fleetSizeShouldBeInfinite() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        builder.setFleetSize(FleetSize.INFINITE);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.INFINITE, vrp.getFleetSize());
    }

    @Test
    public void whenBuildingWithFiniteFleet_fleetSizeShouldBeFinite() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        builder.setFleetSize(FleetSize.FINITE);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.FINITE, vrp.getFleetSize());
    }

    @Test
    public void whenBuildingWithFourVehicles_vrpShouldContainTheCorrectNuOfVehicles() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.the("start")).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.the("start")).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.the("start")).build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.the("start")).build();

        builder.addVehicle(v1).addVehicle(v2).addVehicle(v3).addVehicle(v4);

        VehicleRoutingProblem vrp = builder.build();
        assertEquals(4, vrp.vehicles().size());
        assertEquals(1, vrp.locations().size());

    }

    @Test
    public void whenAddingFourVehiclesAllAtOnce_vrpShouldContainTheCorrectNuOfVehicles() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.the("start")).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.the("start")).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.the("start")).build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.the("start")).build();

        builder.addAllVehicles(Arrays.asList(v1, v2, v3, v4));

        VehicleRoutingProblem vrp = builder.build();
        assertEquals(4, vrp.vehicles().size());

    }

    @Test
    public void whenBuildingWithFourVehiclesAndTwoTypes_vrpShouldContainTheCorrectNuOfTypes() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("type1").build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.the("type2").build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.the("yo")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.the("yo")).setType(type1).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.the("yo")).setType(type2).build();
        VehicleImpl v4 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.the("yo")).setType(type2).build();

        builder.addVehicle(v1).addVehicle(v2).addVehicle(v3).addVehicle(v4);

        VehicleRoutingProblem vrp = builder.build();
        assertEquals(2, vrp.types().size());

    }

    @Test
    public void whenShipmentsAreAdded_vrpShouldContainThem() {
        Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 10).setPickupLocation(Location.Builder.the().setId("foofoo").build()).setDeliveryLocation(Location.the("foo")).build();
        Shipment s2 = Shipment.Builder.newInstance("s2").addSizeDimension(0, 100).setPickupLocation(Location.Builder.the().setId("foofoo").build()).setDeliveryLocation(Location.the("foo")).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addJob(s);
        vrpBuilder.addJob(s2);
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.jobs().size());
        assertEquals(s, vrp.jobs().get("s"));
        assertEquals(s2, vrp.jobs().get("s2"));
        assertEquals(2,vrp.locations().size());
    }

    @Test
    public void whenServicesAreAdded_vrpShouldContainThem() {
        Service s1 = mock(Service.class);
        when(s1.id).thenReturn("s1");
        when(s1.location).thenReturn(Location.Builder.the().setIndex(1).build());
        Service s2 = mock(Service.class);
        when(s2.id).thenReturn("s2");
        when(s2.location).thenReturn(Location.Builder.the().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addJob(s1).addJob(s2);

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.jobs().size());
        assertEquals(s1, vrp.jobs().get("s1"));
        assertEquals(s2, vrp.jobs().get("s2"));
        assertEquals(1,vrp.locations().size());
    }


    @Test
    public void whenPickupsAreAdded_vrpShouldContainThem() {
        Pickup s1 = mock(Pickup.class);
        when(s1.id).thenReturn("s1");
        when(s1.location).thenReturn(Location.Builder.the().setIndex(1).build());
        Pickup s2 = mock(Pickup.class);
        when(s2.id).thenReturn("s2");
        when(s2.location).thenReturn(Location.Builder.the().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addJob(s1).addJob(s2);

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.jobs().size());
        assertEquals(s1, vrp.jobs().get("s1"));
        assertEquals(s2, vrp.jobs().get("s2"));
    }

    @Test
    public void whenPickupsAreAddedAllAtOnce_vrpShouldContainThem() {
        Pickup s1 = mock(Pickup.class);
        when(s1.id).thenReturn("s1");
        when(s1.location).thenReturn(Location.Builder.the().setIndex(1).build());
        Pickup s2 = mock(Pickup.class);
        when(s2.id).thenReturn("s2");
        when(s2.location).thenReturn(Location.Builder.the().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addAllJobs(Arrays.asList(s1, s2));

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.jobs().size());
        assertEquals(s1, vrp.jobs().get("s1"));
        assertEquals(s2, vrp.jobs().get("s2"));
    }

    @Test
    public void whenDelivieriesAreAdded_vrpShouldContainThem() {
        Delivery s1 = mock(Delivery.class);
        when(s1.id).thenReturn("s1");
        when(s1.size).thenReturn(Capacity.Builder.get().build());
        when(s1.location).thenReturn(Location.Builder.the().setIndex(1).build());
        Delivery s2 = mock(Delivery.class);
        when(s2.id).thenReturn("s2");
        when(s2.size).thenReturn(Capacity.Builder.get().build());
        when(s2.location).thenReturn(Location.Builder.the().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addJob(s1).addJob(s2);

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.jobs().size());
        assertEquals(s1, vrp.jobs().get("s1"));
        assertEquals(s2, vrp.jobs().get("s2"));
    }

    @Test
    public void whenDelivieriesAreAddedAllAtOnce_vrpShouldContainThem() {
        Delivery s1 = mock(Delivery.class);
        when(s1.id).thenReturn("s1");
        when(s1.size).thenReturn(Capacity.Builder.get().build());
        when(s1.location).thenReturn(Location.Builder.the().setIndex(1).build());
        Delivery s2 = mock(Delivery.class);
        when(s2.id).thenReturn("s2");
        when(s2.size).thenReturn(Capacity.Builder.get().build());
        when(s2.location).thenReturn(Location.Builder.the().setIndex(1).build());

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addAllJobs(Arrays.asList(s1, s2));

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.jobs().size());
        assertEquals(s1, vrp.jobs().get("s1"));
        assertEquals(s2, vrp.jobs().get("s2"));
    }

    @Test
    public void whenServicesAreAddedAllAtOnce_vrpShouldContainThem() {
        Service s1 = mock(Service.class);
        when(s1.id).thenReturn("s1");
        when(s1.location).thenReturn(Location.Builder.the().setIndex(1).build());
        Service s2 = mock(Service.class);
        when(s2.id).thenReturn("s2");
        when(s2.location).thenReturn(Location.Builder.the().setIndex(1).build());

        Collection<Service> services = new ArrayList<Service>();
        services.add(s1);
        services.add(s2);

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addAllJobs(services);

        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(2, vrp.jobs().size());
        assertEquals(s1, vrp.jobs().get("s1"));
        assertEquals(s2, vrp.jobs().get("s2"));
    }


    @Test
    public void whenSettingActivityCosts_vrpShouldContainIt() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        builder.setActivityCosts(new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(AbstractActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return 4.0;
            }

            @Override
            public double getActivityDuration(AbstractActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return tourAct.operationTime();
            }

        });

        VehicleRoutingProblem problem = builder.build();
        assertEquals(4.0, problem.activityCosts().getActivityCost(null, 0.0, null, null), 0.01);
    }

    @Test
    public void whenSettingRoutingCosts_vprShouldContainIt() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        builder.setRoutingCost(new AbstractForwardVehicleRoutingTransportCosts() {

            @Override
            public double distance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return 0;
            }

            @Override
            public double transportTime(Location from, Location to,
                                        double departureTime, Driver driver, Vehicle vehicle) {
                return 0;
            }

            @Override
            public double transportCost(Location from, Location to,
                                        double departureTime, Driver driver, Vehicle vehicle) {
                return 4.0;
            }
        });

        VehicleRoutingProblem problem = builder.build();
        assertEquals(4.0, problem.transportCosts().transportCost(loc(""), loc(""), 0.0, null, null), 0.01);
    }

    private Location loc(String i) {
        return Location.Builder.the().setId(i).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingVehiclesWithSameId_itShouldThrowException(){
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        VehicleType type = VehicleTypeImpl.Builder.the("type").build();
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("loc")).setType(type).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("loc")).setType(type).build();
        builder.addVehicle(vehicle1);
        builder.addVehicle(vehicle2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenBuildingProblemWithSameBreakId_itShouldThrowException(){
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        VehicleType type = VehicleTypeImpl.Builder.the("type").build();
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.the("loc")).setType(type)
            .setBreak(Break.Builder.newInstance("break").build())
            .build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.the("loc")).setType(type)
            .setBreak(Break.Builder.newInstance("break").build())
            .build();
        builder.addVehicle(vehicle1);
        builder.addVehicle(vehicle2);
        builder.setFleetSize(FleetSize.FINITE);
        builder.build();
    }

    @Test
    public void whenAddingAVehicle_getAddedVehicleTypesShouldReturnItsType() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        VehicleType type = VehicleTypeImpl.Builder.the("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("loc")).setType(type).build();
        builder.addVehicle(vehicle);

        assertEquals(1, builder.getAddedVehicleTypes().size());
        assertEquals(type, builder.getAddedVehicleTypes().iterator().next());


    }

    @Test
    public void whenAddingTwoVehicleWithSameType_getAddedVehicleTypesShouldReturnOnlyOneType() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        VehicleType type = VehicleTypeImpl.Builder.the("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("loc")).setType(type).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.the("loc")).setType(type).build();

        builder.addVehicle(vehicle);
        builder.addVehicle(vehicle2);

        assertEquals(1, builder.getAddedVehicleTypes().size());
        assertEquals(type, builder.getAddedVehicleTypes().iterator().next());
    }

    @Test
    public void whenAddingTwoVehicleWithDiffType_getAddedVehicleTypesShouldReturnTheseType() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        VehicleType type = VehicleTypeImpl.Builder.the("type").build();
        VehicleType type2 = VehicleTypeImpl.Builder.the("type2").build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("loc")).setType(type).build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.the("loc")).setType(type2).build();

        builder.addVehicle(vehicle);
        builder.addVehicle(vehicle2);

        assertEquals(2, builder.getAddedVehicleTypes().size());

    }


    @Test
    public void whenAddingVehicleWithDiffStartAndEnd_startLocationMustBeRegisteredInLocationMap() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start"))
            .setEndLocation(Location.the("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addVehicle(vehicle);
        assertTrue(vrpBuilder.locations().containsKey("start"));
    }

    @Test
    public void whenAddingVehicleWithDiffStartAndEnd_endLocationMustBeRegisteredInLocationMap() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start"))
            .setEndLocation(Location.the("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addVehicle(vehicle);
        assertTrue(vrpBuilder.locations().containsKey("end"));
    }

    @Test
    public void whenAddingInitialRoute_itShouldBeAddedCorrectly() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addInitialVehicleRoute(route);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertTrue(!vrp.initialVehicleRoutes().isEmpty());
    }

    @Test
    public void whenAddingInitialRoutes_theyShouldBeAddedCorrectly() {
        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute route1 = VehicleRoute.Builder.newInstance(vehicle1, DriverImpl.noDriver()).build();

        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).build();
        VehicleRoute route2 = VehicleRoute.Builder.newInstance(vehicle2, DriverImpl.noDriver()).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addInitialVehicleRoutes(Arrays.asList(route1, route2));

        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertEquals(2, vrp.initialVehicleRoutes().size());
        assertEquals(2,vrp.locations().size());
    }

    @Test
    public void whenAddingInitialRoute_locationOfVehicleMustBeMemorized() {
        Location start = TestUtils.loc("start", v2.the(0, 1));
        Location end = Location.the("end");
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(start)
            .setEndLocation(end).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addInitialVehicleRoute(route);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertThat(vrp.locations(),hasItem(start));
        assertThat(vrp.locations(),hasItem(end));
    }

    @Test
    public void whenAddingJobAndInitialRouteWithThatJobAfterwards_thisJobShouldNotBeInFinalJobMap() {
        Service service = Service.Builder.newInstance("myService").location(Location.the("loc")).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addJob(service);
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(TestUtils.loc("start", v2.the(0, 1)))
            .setEndLocation(Location.the("end")).build();
        VehicleRoute initialRoute = VehicleRoute.Builder.newInstance(vehicle).addService(service).build();
        vrpBuilder.addInitialVehicleRoute(initialRoute);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertFalse(vrp.jobs().containsKey("myService"));
        assertEquals(3,vrp.locations().size());
    }

    @Test
    public void whenAddingTwoJobs_theyShouldHaveProperIndeces() {
        Service service = Service.Builder.newInstance("myService").location(Location.the("loc")).build();
        Shipment shipment = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.Builder.the().setId("pick").build())
            .setDeliveryLocation(Location.the("del")).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addJob(service);
        vrpBuilder.addJob(shipment);
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, service.index());
        assertEquals(2, shipment.index());
        assertEquals(3,vrp.locations().size());

    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingTwoServicesWithTheSameId_itShouldThrowException() {
        Service service1 = Service.Builder.newInstance("myService").location(Location.the("loc")).build();
        Service service2 = Service.Builder.newInstance("myService").location(Location.the("loc")).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addJob(service1);
        vrpBuilder.addJob(service2);
        @SuppressWarnings("UnusedDeclaration") VehicleRoutingProblem vrp = vrpBuilder.build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAddingTwoShipmentsWithTheSameId_itShouldThrowException() {
        Shipment shipment1 = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.Builder.the().setId("pick").build())
            .setDeliveryLocation(Location.the("del")).build();
        Shipment shipment2 = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.Builder.the().setId("pick").build())
            .setDeliveryLocation(Location.the("del")).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addJob(shipment1);
        vrpBuilder.addJob(shipment2);
        @SuppressWarnings("UnusedDeclaration") VehicleRoutingProblem vrp = vrpBuilder.build();

    }

    @Test
    public void whenAddingTwoVehicles_theyShouldHaveProperIndices() {
        VehicleImpl veh1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("start", v2.the(0, 1)))
            .setEndLocation(Location.the("end")).build();
        VehicleImpl veh2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("start", v2.the(0, 1)))
            .setEndLocation(Location.the("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addVehicle(veh1);
        vrpBuilder.addVehicle(veh2);
        vrpBuilder.build();

        assertEquals(1, veh1.index());
        assertEquals(2, veh2.index());

    }

    @Test
    public void whenAddingTwoVehiclesWithSameTypeIdentifier_typeIdentifiersShouldHaveSameIndices() {
        VehicleImpl veh1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(TestUtils.loc("start", v2.the(0, 1)))
            .setEndLocation(Location.the("end")).build();
        VehicleImpl veh2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(TestUtils.loc("start", v2.the(0, 1)))
            .setEndLocation(Location.the("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addVehicle(veh1);
        vrpBuilder.addVehicle(veh2);
        vrpBuilder.build();

        assertEquals(1, veh1.vehicleType().index());
        assertEquals(1, veh2.vehicleType().index());

    }

    @Test
    public void whenAddingTwoVehiclesDifferentTypeIdentifier_typeIdentifiersShouldHaveDifferentIndices() {
        VehicleImpl veh1 = VehicleImpl.Builder.newInstance("v1")
            .setStartLocation(TestUtils.loc("start", v2.the(0, 1)))
            .setEndLocation(Location.the("end")).build();
        VehicleImpl veh2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(TestUtils.loc("startLoc", v2.the(0, 1)))
            .setEndLocation(Location.the("end")).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        vrpBuilder.addVehicle(veh1);
        vrpBuilder.addVehicle(veh2);
        vrpBuilder.build();

        assertEquals(1, veh1.vehicleType().index());
        assertEquals(2, veh2.vehicleType().index());

    }
}

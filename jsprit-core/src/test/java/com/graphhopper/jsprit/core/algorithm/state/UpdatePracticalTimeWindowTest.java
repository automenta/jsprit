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
package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.solution.route.ReverseRouteActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdatePracticalTimeWindowTest {

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private ReverseRouteActivityVisitor reverseActivityVisitor;

    private StateManager stateManager;

    private VehicleRoute route;

    @Before
    public void doBefore() {

        routingCosts = CostFactory.createManhattanCosts();
        activityCosts = new WaitingTimeCosts();

        VehicleRoutingProblem vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.FINITE);
        stateManager = new StateManager(vrpMock);

        reverseActivityVisitor = new ReverseRouteActivityVisitor();
        reverseActivityVisitor.addActivityVisitor(new UpdatePracticalTimeWindows(stateManager, routingCosts, activityCosts));

        Pickup pickup = Pickup.Builder.the("pick").location(Location.the("0,20")).timeWindowSet(TimeWindow.the(0, 30)).build();
        Delivery delivery = Delivery.Builder.newInstance("del").location(Location.the("20,20")).timeWindowSet(TimeWindow.the(10, 40)).build();
        Pickup pickup2 = Pickup.Builder.the("pick2").location(Location.the("20,0")).timeWindowSet(TimeWindow.the(20, 50)).build();

        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("0,0")).setType(mock(VehicleType.class)).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        final VehicleRoutingProblem vrp = vrpBuilder.addJob(pickup).addJob(pickup2).addJob(delivery).build();

        route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).setJobActivityFactory(vrp::copyAndGetActivities)
            .addService(pickup).addService(delivery).addService(pickup2).build();

        reverseActivityVisitor.visit(route);

    }

    @Test
    public void whenVehicleRouteHasPickupAndDeliveryAndPickup_latestStartTimeOfAct3MustBeCorrect() {
        assertEquals(50., route.activities().get(2).startLatest(), 0.01);
        assertEquals(50., stateManager.state(route.activities().get(2), InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void whenVehicleRouteHasPickupAndDeliveryAndPickup_latestStartTimeOfAct2MustBeCorrect() {
        assertEquals(40., route.activities().get(1).startLatest(), 0.01);
        assertEquals(30., stateManager.state(route.activities().get(1), InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void whenVehicleRouteHasPickupAndDeliveryAndPickup_latestStartTimeOfAct1MustBeCorrect() {
        assertEquals(30., route.activities().get(0).startLatest(), 0.01);
        assertEquals(10., stateManager.state(route.activities().get(0), InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

}

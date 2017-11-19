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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVariableCosts;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by schroeder on 02.07.14.
 */
public class TestRouteLevelServiceInsertionCostEstimator {

    private VehicleRoute route;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private StateManager stateManager;

    private ConstraintManager constraintManager;

    private VehicleRoutingProblem vrp;

    private JobActivityFactory activityFactory;

    @Before
    public void doBefore() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();

        routingCosts = CostFactory.createEuclideanCosts();
        vrpBuilder.setRoutingCost(routingCosts);

        activityCosts = new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(AbstractActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return Math.max(0., arrivalTime - tourAct.startLatest());
            }

            @Override
            public double getActivityDuration(AbstractActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return tourAct.operationTime();
            }

        };
        vrpBuilder.setActivityCosts(activityCosts);

        Service s1 = Service.Builder.newInstance("s1").location(Location.the("10,0")).timeWindowSet(TimeWindow.the(10., 10.)).build();
        Service s2 = Service.Builder.newInstance("s2").location(Location.the("20,0")).timeWindowSet(TimeWindow.the(20., 20.)).build();
        Service s3 = Service.Builder.newInstance("s3").location(Location.the("30,0")).timeWindowSet(TimeWindow.the(30., 30.)).build();
        vrpBuilder.addJob(s1).addJob(s2).addJob(s3);

        VehicleType type = VehicleTypeImpl.Builder.the("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.the("0,0")).setType(type).build();
        vrpBuilder.addVehicle(vehicle);
        vrp = vrpBuilder.build();

        vrp.activities(s1).get(0).startEarliest(10);
        vrp.activities(s1).get(0).startLatest(10);

        vrp.activities(s2).get(0).startEarliest(20);
        vrp.activities(s2).get(0).startLatest(20);

        vrp.activities(s3).get(0).startEarliest(30);
        vrp.activities(s3).get(0).startLatest(30);

        activityFactory = job -> vrp.copyAndGetActivities(job);
        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(activityFactory).addService(s1).addService(s2).addService(s3).build();

        VehicleRoutingProblem vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.INFINITE);
        stateManager = new StateManager(vrpMock);
        stateManager.addStateUpdater(new UpdateVariableCosts(activityCosts, routingCosts, stateManager));
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.emptyList());
        constraintManager = new ConstraintManager(vrp, stateManager);
    }

    @Test
    public void whenNewServiceNeedToBeInserted_itShouldReturnCorrectInsertionCosts() {
        final Service s4 = Service.Builder.newInstance("s4").location(Location.the("5,0")).timeWindowSet(TimeWindow.the(5., 5.)).build();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(job -> {
            List<JobActivity> acts = activityFactory.the(job);
            if (acts.isEmpty()) {
                acts.add(new PickupService(s4));
            }
            return acts;
        });
        InsertionData iData = routeInserter.getInsertionData(route, s4, route.vehicle(), route.getDepartureTime(), route.driver, Double.MAX_VALUE);
        assertEquals(0., iData.getInsertionCost(), 0.01);
    }

    @Test
    public void whenNewServiceNeedToBeInserted_itShouldReturnCorrectInsertionIndex() {
        final Service s4 = Service.Builder.newInstance("s4").location(Location.the("5,0")).timeWindowSet(TimeWindow.the(5., 5.)).build();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        final ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(job -> {
            List<JobActivity> acts = activityFactory.the(job);
            if (acts.isEmpty()) {
                acts.add(new PickupService(s4));
            }
            return acts;
        });
        InsertionData iData = routeInserter.getInsertionData(route, s4, route.vehicle(), route.getDepartureTime(), route.driver, Double.MAX_VALUE);
        assertEquals(0, iData.getDeliveryInsertionIndex(), 0.01);
    }

    @Test
    public void whenNewServiceWithServiceTimeNeedToBeInserted_itShouldReturnCorrectInsertionData() {
        final Service s4 = Service.Builder.newInstance("s4").serviceTime(10.).location(Location.the("5,0")).timeWindowSet(TimeWindow.the(5., 5.)).build();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(job -> {
            List<JobActivity> acts = activityFactory.the(job);
            if (acts.isEmpty()) {
                PickupService pickupService = new PickupService(s4);
                pickupService.startEarliest(5);
                pickupService.startLatest(5);
                acts.add(pickupService);
            }
            return acts;
        });
        InsertionData iData = routeInserter.getInsertionData(route, s4, route.vehicle(), route.getDepartureTime(), route.driver, Double.MAX_VALUE);
        assertEquals(0, iData.getDeliveryInsertionIndex(), 0.01);
        assertEquals(30., iData.getInsertionCost(), 0.01);
    }


    @Test
    public void whenNewServiceWithServiceTimeNeedToBeInsertedAndRouteIsEmpty_itShouldReturnCorrectInsertionData() {
        final Service s4 = Service.Builder.newInstance("s4").serviceTime(10.).location(Location.the("5,0")).timeWindowSet(TimeWindow.the(5., 5.)).build();
//        PickupActivity pickupService = new PickupService(s4);
        VehicleRoute emptyroute = VehicleRoute.emptyRoute();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(job -> {
            List<JobActivity> acts = activityFactory.the(job);
            if (acts.isEmpty()) {
                PickupService pickupService = new PickupService(s4);
                pickupService.startEarliest(5);
                pickupService.startLatest(5);
                acts.add(pickupService);
            }
            return acts;
        });
        InsertionData iData = routeInserter.getInsertionData(emptyroute, s4, route.vehicle(), route.getDepartureTime(), route.driver, Double.MAX_VALUE);
        assertEquals(0, iData.getDeliveryInsertionIndex(), 0.01);
        assertEquals(10., iData.getInsertionCost(), 0.01);
    }

    @Test
    public void whenNewServiceWithServiceTimeAndTWNeedToBeInsertedAndRouteIsEmpty_itShouldReturnCorrectInsertionData() {
        final Service s4 = Service.Builder.newInstance("s4").serviceTime(10.).location(Location.the("5,0")).timeWindowSet(TimeWindow.the(3., 3.)).build();
//        PickupActivity pickupService = new PickupService(s4);
        VehicleRoute emptyroute = VehicleRoute.emptyRoute();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(job -> {
            List<JobActivity> acts = activityFactory.the(job);
            if (acts.isEmpty()) {
                PickupService pickupService = new PickupService(s4);
                pickupService.startEarliest(3);
                pickupService.startLatest(3);
                acts.add(pickupService);
            }
            return acts;
        });
        InsertionData iData = routeInserter.getInsertionData(emptyroute, s4, route.vehicle(), route.getDepartureTime(), route.driver, Double.MAX_VALUE);
        assertEquals(0, iData.getDeliveryInsertionIndex(), 0.01);
        assertEquals(10. + 2., iData.getInsertionCost(), 0.01);
    }

}

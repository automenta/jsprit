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
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * unit tests to test route level insertion
 */
public class TestRouteLevelActivityInsertionCostEstimator {

    private VehicleRoute route;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private StateManager stateManager;

    @Before
    public void doBefore() {
        routingCosts = CostFactory.createEuclideanCosts();

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
        Service s1 = Service.Builder.newInstance("s1").location(Location.the("10,0")).timeWindowSet(TimeWindow.the(10., 10.)).build();
        Service s2 = Service.Builder.newInstance("s2").location(Location.the("20,0")).timeWindowSet(TimeWindow.the(20., 20.)).build();
        Service s3 = Service.Builder.newInstance("s3").location(Location.the("30,0")).timeWindowSet(TimeWindow.the(30., 30.)).build();

        VehicleType type = VehicleTypeImpl.Builder.the("type").build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.the("0,0")).setType(type).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        final VehicleRoutingProblem vrp = vrpBuilder.addJob(s1).addJob(s2).addJob(s3).build();

        vrp.activities(s1).get(0).startEarliest(10);
        vrp.activities(s1).get(0).startLatest(10);

        vrp.activities(s2).get(0).startEarliest(20);
        vrp.activities(s2).get(0).startLatest(20);

        vrp.activities(s3).get(0).startEarliest(30);
        vrp.activities(s3).get(0).startLatest(30);

        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp::copyAndGetActivities).addService(s1).addService(s2).addService(s3).build();

        stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateVariableCosts(activityCosts, routingCosts, stateManager));
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.emptyList());
    }

    @Test
    public void whenNewActInBetweenFirstAndSecond_and_forwardLookingIs0_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").location(Location.the("5,0")).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.vehicle(), route.driver, 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        double iCosts = estimator.getCosts(context, route.start, route.activities().get(0), pickupService, 0.);
        assertEquals(0., iCosts, 0.01);
    }

    @Test
    public void whenNewActWithTWInBetweenFirstAndSecond_and_forwardLookingIs0_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").location(Location.the("5,0")).timeWindowSet(TimeWindow.the(5., 5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.vehicle(), route.driver, 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        double iCosts = estimator.getCosts(context, route.start, route.activities().get(0), pickupService, 0.);
        assertEquals(0., iCosts, 0.01);
    }

    @Test
    public void whenNewActWithTWAndServiceTimeInBetweenFirstAndSecond_and_forwardLookingIs0_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").location(Location.the("5,0")).serviceTime(10.).timeWindowSet(TimeWindow.the(5., 5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        pickupService.startEarliest(5);
        pickupService.startLatest(5);

        JobInsertionContext context = new JobInsertionContext(route, s4, route.vehicle(), route.driver, 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        double iCosts = estimator.getCosts(context, route.start, route.activities().get(0), pickupService, 0.);
        double expectedTransportCosts = 0.;
        double expectedActivityCosts = 10.;
        assertEquals(expectedActivityCosts + expectedTransportCosts, iCosts, 0.01);
    }

    @Test
    public void whenNewActWithTWAndServiceTimeInBetweenFirstAndSecond_and_forwardLookingIs3_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").location(Location.the("5,0")).serviceTime(10.).timeWindowSet(TimeWindow.the(5., 5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.vehicle(), route.driver, 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(3);
        double iCosts = estimator.getCosts(context, route.start, route.activities().get(0), pickupService, 0.);
        double expectedTransportCosts = 0.;
        double expectedActivityCosts = 30.;
        assertEquals(expectedActivityCosts + expectedTransportCosts, iCosts, 0.01);
    }

    @Test
    public void whenNewActInBetweenSecondAndThird_and_forwardLookingIs0_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").location(Location.the("5,0")).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.vehicle(), route.driver, 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        double iCosts =
            estimator.getCosts(context, route.activities().get(0), route.activities().get(1), pickupService, 10.);
        double expectedTransportCosts = 10.;
        double expectedActivityCosts = 10.;
        assertEquals(expectedTransportCosts + expectedActivityCosts, iCosts, 0.01);
    }

    @Test
    public void whenNewActInBetweenSecondAndThird_and_forwardLookingIs3_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").location(Location.the("5,0")).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.vehicle(), route.driver, 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(3);
        double iCosts =
            estimator.getCosts(context, route.activities().get(0), route.activities().get(1), pickupService, 10.);
        double expectedTransportCosts = 10.;
        double expectedActivityCosts = 10. + 10.;
        assertEquals(expectedTransportCosts + expectedActivityCosts, iCosts, 0.01);
    }

    @Test
    public void whenNewActWithTWInBetweenSecondAndThird_and_forwardLookingIs3_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").location(Location.the("5,0")).timeWindowSet(TimeWindow.the(5., 5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        pickupService.startEarliest(5);
        pickupService.startLatest(5);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.vehicle(), route.driver, 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(3);
        double iCosts =
            estimator.getCosts(context, route.activities().get(0), route.activities().get(1), pickupService, 10.);
        double expectedTransportCosts = 10.;
        double expectedActivityCosts = 10. + 10. + 10.;
        assertEquals(expectedTransportCosts + expectedActivityCosts, iCosts, 0.01);
    }

}

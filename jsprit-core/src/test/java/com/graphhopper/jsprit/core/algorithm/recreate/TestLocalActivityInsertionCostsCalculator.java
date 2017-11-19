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
import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityTimes;
import com.graphhopper.jsprit.core.algorithm.state.UpdateFutureWaitingTimes;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVehicleDependentPracticalTimeWindows;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestLocalActivityInsertionCostsCalculator {

    VehicleRoutingTransportCosts tpCosts;

    VehicleRoutingActivityCosts actCosts;

    LocalActivityInsertionCostsCalculator calc;

    Vehicle vehicle;

    VehicleRoute route;

    JobInsertionContext jic;

    @Before
    public void doBefore() {

        vehicle = mock(Vehicle.class);
        route = mock(VehicleRoute.class);
        when(route.isEmpty()).thenReturn(false);
        when(route.vehicle()).thenReturn(vehicle);

        jic = mock(JobInsertionContext.class);
        when(jic.getRoute()).thenReturn(route);
        when(jic.getNewVehicle()).thenReturn(vehicle);
        when(vehicle.type()).thenReturn(VehicleTypeImpl.Builder.the("type").build());

        tpCosts = mock(VehicleRoutingTransportCosts.class);
        when(tpCosts.transportCost(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(2.0);
        when(tpCosts.transportTime(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);
        when(tpCosts.transportCost(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(3.0);
        when(tpCosts.transportTime(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(0.0);
        when(tpCosts.transportCost(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(3.0);
        when(tpCosts.transportTime(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);

        actCosts = new WaitingTimeCosts();
        calc = new LocalActivityInsertionCostsCalculator(tpCosts, actCosts, mock(StateManager.class));
    }

    private Location loc(String i) {
        return Location.Builder.the().setId(i).build();
    }

    @Test
    public void whenAddingServiceBetweenDiffStartAndEnd_costMustBeCorrect() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.the(0, 0))
            .setEndLocation(Location.the(20, 0))
            .build();
        Service s = Service.Builder.newInstance("s")
            .location(Location.the(10, 0))
            .build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get()
            .addVehicle(v)
            .addJob(s)
            .build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        JobInsertionContext jobInsertionContext =
            new JobInsertionContext(route, s, v, null, 0);
        LocalActivityInsertionCostsCalculator localActivityInsertionCostsCalculator =
            new LocalActivityInsertionCostsCalculator(
                vrp.transportCosts(),
                vrp.activityCosts(),
                new StateManager(vrp));
        double cost = localActivityInsertionCostsCalculator.getCosts(
            jobInsertionContext,
            new Start(v.start, 0, Double.MAX_VALUE),
            new End(v.end, 0, Double.MAX_VALUE),
            vrp.activities(s).get(0),
            0);
        assertEquals(20., cost, Math.ulp(20.));
    }

    @Test
    public void whenAddingShipmentBetweenDiffStartAndEnd_costMustBeCorrect() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.the(0, 0))
            .setEndLocation(Location.the(20, 0))
            .build();
        Shipment s = Shipment.Builder.newInstance("p")
            .setPickupLocation(Location.the(10, 0))
            .setDeliveryLocation(Location.the(10, 7.5))
            .build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get()
            .addVehicle(v)
            .addJob(s)
            .build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        JobInsertionContext jobInsertionContext =
            new JobInsertionContext(route, s, v, null, 0);
        LocalActivityInsertionCostsCalculator localActivityInsertionCostsCalculator =
            new LocalActivityInsertionCostsCalculator(
                vrp.transportCosts(),
                vrp.activityCosts(),
                new StateManager(vrp));
        double cost = localActivityInsertionCostsCalculator.getCosts(
            jobInsertionContext,
            new Start(v.start, 0, Double.MAX_VALUE),
            new End(v.end, 0, Double.MAX_VALUE),
            vrp.activities(s).get(0),
            0);
        assertEquals(20., cost, Math.ulp(20.));
        cost = localActivityInsertionCostsCalculator.getCosts(
            jobInsertionContext,
            vrp.activities(s).get(0),
            new End(v.end, 0, Double.MAX_VALUE),
            vrp.activities(s).get(1),
            0);
        assertEquals(10, cost, Math.ulp(10.));
    }

    @Test
    public void whenInsertingActBetweenTwoRouteActs_itCalcsMarginalTpCosts() {
        AbstractActivity prevAct = mock(AbstractActivity.class);
        when(prevAct.location()).thenReturn(loc("i"));
        when(prevAct.index()).thenReturn(1);
        AbstractActivity nextAct = mock(AbstractActivity.class);
        when(nextAct.location()).thenReturn(loc("j"));
        when(nextAct.index()).thenReturn(1);
        AbstractActivity newAct = mock(AbstractActivity.class);
        when(newAct.location()).thenReturn(loc("k"));
        when(newAct.index()).thenReturn(1);

        when(vehicle.isReturnToDepot()).thenReturn(true);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(4.0, costs, 0.01);
    }

    @Test
    public void whenInsertingActBetweenLastActAndEnd_itCalcsMarginalTpCosts() {
        AbstractActivity prevAct = mock(AbstractActivity.class);
        when(prevAct.location()).thenReturn(loc("i"));
        when(prevAct.index()).thenReturn(1);
        End nextAct = End.the("j", 0.0, 0.0);
        AbstractActivity newAct = mock(AbstractActivity.class);
        when(newAct.location()).thenReturn(loc("k"));
        when(newAct.index()).thenReturn(1);

        when(vehicle.isReturnToDepot()).thenReturn(true);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(4.0, costs, 0.01);
    }

    @Test
    public void whenInsertingActBetweenTwoRouteActsAndRouteIsOpen_itCalcsMarginalTpCosts() {
        AbstractActivity prevAct = mock(AbstractActivity.class);
        when(prevAct.location()).thenReturn(loc("i"));
        when(prevAct.index()).thenReturn(1);
        AbstractActivity nextAct = mock(AbstractActivity.class);
        when(nextAct.location()).thenReturn(loc("j"));
        when(nextAct.index()).thenReturn(1);
        AbstractActivity newAct = mock(AbstractActivity.class);
        when(newAct.location()).thenReturn(loc("k"));
        when(newAct.index()).thenReturn(1);

        when(vehicle.isReturnToDepot()).thenReturn(false);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(4.0, costs, 0.01);
    }

    @Test
    public void whenInsertingActBetweenLastActAndEndAndRouteIsOpen_itCalculatesTpCostsFromPrevToNewAct() {
        AbstractActivity prevAct = mock(AbstractActivity.class);
        when(prevAct.location()).thenReturn(loc("i"));
        when(prevAct.index()).thenReturn(1);
        End nextAct = End.the("j", 0.0, 0.0);
        AbstractActivity newAct = mock(AbstractActivity.class);
        when(newAct.location()).thenReturn(loc("k"));
        when(newAct.index()).thenReturn(1);

        when(vehicle.isReturnToDepot()).thenReturn(false);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(3.0, costs, 0.01);
    }

    @Test
    public void test() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();
        Service prevS = Service.Builder.newInstance("prev").location(Location.the(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").serviceTime(10).location(Location.the(60, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0)).timeWindowSet(TimeWindow.the(40, 80)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addJob(prevS).addJob(newS).addJob(nextS).addVehicle(v).build();

        AbstractActivity prevAct = vrp.activities(prevS).get(0);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(40);
        nextAct.startLatest(80);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).addService(prevS).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        VehicleRoutingProblem vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.INFINITE);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrpMock));
        calc.setSolutionCompletenessRatio(1.);

        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(50., c, 0.01);

		/*
        new: dist = 90 & wait = 0
		old: dist = 30 & wait = 10
		c = new - old = 90 - 40 = 50
		 */
    }

    @Test
    public void whenAddingNewBetweenStartAndAct_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();

        Service newS = Service.Builder.newInstance("new").serviceTime(10).location(Location.the(10, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0))
            .timeWindowSet(TimeWindow.the(40, 50)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addJob(newS).addJob(nextS).addVehicle(v).build();

        Start prevAct = new Start(Location.the(0, 0), 0, 100);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(40);
        nextAct.startLatest(50);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 0);
        assertEquals(-10., c, 0.01);
    }

    @Test
    public void whenAddingNewBetweenStartAndAct2_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.the(0, 0)).build();

        Service newS = Service.Builder.newInstance("new").serviceTime(10).location(Location.the(10, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0))
            .timeWindowSet(TimeWindow.the(140, 150)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addJob(newS).addJob(nextS).addVehicle(v2).build();

        Start prevAct = new Start(Location.the(0, 0), 0, 100);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(140);
        nextAct.startLatest(150);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v2).setJobActivityFactory(vrp.jobActivityFactory()).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v2, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 0);
        assertEquals(-10., c, 0.01);
    }

    @Test
    public void whenAddingNewInEmptyRoute_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();

        Service newS = Service.Builder.newInstance("new").serviceTime(10).location(Location.the(10, 0)).timeWindowSet(TimeWindow.the(100, 150)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addJob(newS).addVehicle(v).build();

        Start prevAct = new Start(Location.the(0, 0), 0, 100);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        newAct.startEarliest(100);
        newAct.startLatest(150);

        End nextAct = new End(Location.the(0, 0), 0, 100);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 0);
        assertEquals(110., c, 0.01);
    }

    @Test
    public void whenAddingNewBetweenTwoActs_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();

        Service prevS = Service.Builder.newInstance("prev").location(Location.the(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").serviceTime(10).location(Location.the(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0)).timeWindowSet(TimeWindow.the(40, 50)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addJob(prevS).addJob(newS).addJob(nextS).addVehicle(v).build();

        AbstractActivity prevAct = vrp.activities(prevS).get(0);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(40);
        nextAct.startLatest(50);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).addService(prevS).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(-10., c, 0.01);
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();

        Service prevS = Service.Builder.newInstance("prev").location(Location.the(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").serviceTime(10).timeWindowSet(TimeWindow.the(100, 120)).location(Location.the(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0)).timeWindowSet(TimeWindow.the(40, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addJob(prevS).addJob(newS).addJob(nextS).addVehicle(v).build();

        AbstractActivity prevAct = vrp.activities(prevS).get(0);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        newAct.startEarliest(100);
        newAct.startLatest(120);

        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(40);
        nextAct.startLatest(500);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).addService(prevS).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(0.5);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(35., c, 0.01);
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs2_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").location(Location.the(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").serviceTime(10).timeWindowSet(TimeWindow.the(100, 120)).location(Location.the(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0)).timeWindowSet(TimeWindow.the(40, 500)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").location(Location.the(40, 0)).timeWindowSet(TimeWindow.the(400, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addJob(afterNextS).addJob(prevS).addJob(newS).addJob(nextS).addVehicle(v).build();

        AbstractActivity prevAct = vrp.activities(prevS).get(0);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        newAct.startEarliest(100);
        newAct.startLatest(120);

        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(400);
        nextAct.startLatest(500);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).build();

        StateManager stateManager = getStateManager(vrp, route);

        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(-10., c, 0.01);
        //
        //old: dist: 0, waiting: 10 + 350 = 360
        //new: dist: 0, waiting: 80 + 270 = 350
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs3_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").location(Location.the(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").serviceTime(10).timeWindowSet(TimeWindow.the(100, 120)).location(Location.the(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0)).timeWindowSet(TimeWindow.the(40, 500)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").location(Location.the(40, 0)).timeWindowSet(TimeWindow.the(80, 500)).build();
        Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").location(Location.the(40, 0)).timeWindowSet(TimeWindow.the(100, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
            .addJob(afterNextS).addJob(afterAfterNextS).build();

        AbstractActivity prevAct = vrp.activities(prevS).get(0);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        newAct.startEarliest(100);
        newAct.startLatest(120);

        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(40);
        nextAct.startLatest(500);

        AbstractActivity afterNextAct = vrp.activities(afterNextS).get(0);
        afterNextAct.startEarliest(80);
        afterNextAct.startLatest(500);

        AbstractActivity afterAfterNextAct = vrp.activities(afterAfterNextS).get(0);
        afterAfterNextAct.startEarliest(100);
        afterAfterNextAct.startLatest(500);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();

        StateManager stateManager = getStateManager(vrp, route);

        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(20., c, 0.01);
        //start-delay = new - old = 120 - 40 = 80 > future waiting time savings = 30 + 20 + 10
        //ref: 10 + 50 + 20 = 80
        //new: 80 - 10 - 30 - 20 = 20
        /*
        w(new) + w(next) - w_old(next) - min{start_delay(next),future_waiting}
		 */
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs4_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").location(Location.the(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").serviceTime(10).timeWindowSet(TimeWindow.the(100, 120)).location(Location.the(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0)).timeWindowSet(TimeWindow.the(40, 500)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").location(Location.the(40, 0)).timeWindowSet(TimeWindow.the(80, 500)).build();
        Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").location(Location.the(50, 0)).timeWindowSet(TimeWindow.the(100, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
            .addJob(afterNextS).addJob(afterAfterNextS).build();

        AbstractActivity prevAct = vrp.activities(prevS).get(0);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        newAct.startEarliest(100);
        newAct.startLatest(120);

        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(40);
        nextAct.startLatest(500);

        AbstractActivity afterNextAct = vrp.activities(afterNextS).get(0);
        afterNextAct.startEarliest(80);
        afterNextAct.startLatest(500);

        AbstractActivity afterAfterNextAct = vrp.activities(afterAfterNextS).get(0);
        afterAfterNextAct.startEarliest(100);
        afterAfterNextAct.startLatest(500);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);

        StateManager stateManager = getStateManager(vrp, route);

        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(30., c, 0.01);
        //ref: 10 + 30 + 10 = 50
        //new: 50 - 50 = 0

		/*
        activity start time delay at next act = start-time-old - start-time-new is always bigger than subsequent waiting time savings
		 */
        /*
		old = 10 + 30 + 10 = 50
		new = 80 + 0 - 10 - min{80,40} = 30
		 */
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs4WithVarStart_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").location(Location.the(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").serviceTime(10).timeWindowSet(TimeWindow.the(100, 120)).location(Location.the(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0)).timeWindowSet(TimeWindow.the(40, 500)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").location(Location.the(40, 0)).timeWindowSet(TimeWindow.the(80, 500)).build();
        Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").location(Location.the(50, 0)).timeWindowSet(TimeWindow.the(100, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
            .addJob(afterNextS).addJob(afterAfterNextS).build();

        AbstractActivity prevAct = vrp.activities(prevS).get(0);
        AbstractActivity newAct = vrp.activities(newS).get(0);
        newAct.startEarliest(100);
        newAct.startLatest(120);

        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(40);
        nextAct.startLatest(500);

        AbstractActivity afterNextAct = vrp.activities(afterNextS).get(0);
        afterNextAct.startEarliest(80);
        afterNextAct.startLatest(500);

        AbstractActivity afterAfterNextAct = vrp.activities(afterAfterNextS).get(0);
        afterAfterNextAct.startEarliest(100);
        afterAfterNextAct.startLatest(500);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);

        StateManager stateManager = getStateManager(vrp, route);

        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(30., c, 0.01);
		/*
		activity start time delay at next act = start-time-old - start-time-new is always bigger than subsequent waiting time savings
		 */
		/*
		old = 10 + 30 + 10 = 50
		new = 80
		new - old = 80 - 40 = 40

		 */
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs3WithVarStart_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.the("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.the(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").location(Location.the(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").serviceTime(10).timeWindowSet(TimeWindow.the(50, 70)).location(Location.the(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").location(Location.the(30, 0)).timeWindowSet(TimeWindow.the(40, 70)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").location(Location.the(40, 0)).timeWindowSet(TimeWindow.the(50, 100)).build();
        Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").location(Location.the(50, 0)).timeWindowSet(TimeWindow.the(100, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
            .addJob(afterNextS).addJob(afterAfterNextS).build();

        AbstractActivity prevAct = vrp.activities(prevS).get(0);

        AbstractActivity newAct = vrp.activities(newS).get(0);
        newAct.startEarliest(50);
        newAct.startLatest(70);

        AbstractActivity nextAct = vrp.activities(nextS).get(0);
        nextAct.startEarliest(40);
        nextAct.startLatest(70);

        AbstractActivity afterNextAct = vrp.activities(afterNextS).get(0);
        afterNextAct.startEarliest(50);
        afterNextAct.startEarliest(100);

        AbstractActivity afterAfterNextAct = vrp.activities(afterAfterNextS).get(0);
        afterAfterNextAct.startEarliest(100);
        afterAfterNextAct.startEarliest(500);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.jobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);

        StateManager stateManager = getStateManager(vrp, route);
        stateManager.updateTimeWindowStates();
        stateManager.informInsertionStarts(Arrays.asList(route),new ArrayList<Job>());

        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(-10., c, 0.01);
		/*
		activity start time delay at next act = start-time-old - start-time-new is always bigger than subsequent waiting time savings
		 */
		/*
		old = 10 + 40 = 50
		new = 30 + 10 = 40
		 */
    }


    private StateManager getStateManager(VehicleRoutingProblem vrp, VehicleRoute route) {
        StateManager stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.transportCosts(), vrp.activityCosts()));
        stateManager.addStateUpdater(new UpdateVehicleDependentPracticalTimeWindows(stateManager, vrp.transportCosts(), actCosts));
        stateManager.addStateUpdater(new UpdateFutureWaitingTimes(stateManager, vrp.transportCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route), new ArrayList<Job>());
        return stateManager;
    }
}

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

package com.graphhopper.jsprit.core.algorithm;


import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import com.graphhopper.jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by schroeder on 22/07/15.
 */
public class VariableDepartureAndWaitingTime_IT {

    interface AlgorithmFactory {
        VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp);
    }

    VehicleRoutingActivityCosts activityCosts;

    AlgorithmFactory algorithmFactory;

    @Before
    public void doBefore() {
        activityCosts = new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(AbstractActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return vehicle.type().getVehicleCostParams().perWaitingTimeUnit * Math.max(0, tourAct.startEarliest() - arrivalTime);
            }

            @Override
            public double getActivityDuration(AbstractActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return tourAct.operationTime();
            }

        };
        algorithmFactory = new AlgorithmFactory() {
            @Override
            public VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp) {
                StateManager stateManager = new StateManager(vrp);
                ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

                return Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setObjectiveFunction(new SolutionCostCalculator() {
                        @Override
                        public double getCosts(VehicleRoutingProblemSolution solution) {
                            SolutionAnalyser sa = new SolutionAnalyser(vrp, solution, vrp.transportCosts());
                            return sa.getWaitingTime() + sa.getDistance();
                        }
                    })
                    .buildAlgorithm();
            }
        };
    }

    @Test
    public void plainSetupShouldWork() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").location(Location.the(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").location(Location.the(20, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get()
            .addJob(s1).addJob(s2).addVehicle(v)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .setRoutingCost(CostFactory.createManhattanCosts())
            .setActivityCosts(activityCosts)
            .build();
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(40., solution.cost());
    }

    @Test
    public void withTimeWindowsShouldWork() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").timeWindowSet(TimeWindow.the(1010, 1100)).location(Location.the(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").timeWindowSet(TimeWindow.the(1020, 1100)).location(Location.the(20, 0)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get()
            .addJob(s1).addJob(s2).addVehicle(v)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .setRoutingCost(CostFactory.createManhattanCosts())
            .setActivityCosts(activityCosts)
            .build();
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(40. + 1000., solution.cost());
    }


}

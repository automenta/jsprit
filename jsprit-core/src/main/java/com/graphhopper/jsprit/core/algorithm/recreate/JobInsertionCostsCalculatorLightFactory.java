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

import com.graphhopper.jsprit.core.algorithm.listener.VehicleRoutingAlgorithmListeners;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListener;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by schroeder on 11.12.14.
 */
public class JobInsertionCostsCalculatorLightFactory {

    /**
     * Returns standard insertion calculator, i.e. the calculator that identifies best insertion positions for the
     * jobs to be inserted. The position basically consists of the route and the according indices.
     *
     * @param vrp               vehicle routing problem
     * @param fleetManager      fleet manager
     * @param stateManager      state manager
     * @param constraintManager constraint manager
     * @return insertion calculator
     */
    public static JobInsertionCostsCalculatorLight createStandardCalculator(VehicleRoutingProblem vrp, VehicleFleetManager fleetManager, RouteAndActivityStateGetter stateManager, ConstraintManager constraintManager) {
        List<VehicleRoutingAlgorithmListeners.PrioritizedVRAListener> al = new ArrayList<>();
        List<InsertionListener> il = new ArrayList<>();
        JobInsertionCostsCalculatorBuilder builder = new JobInsertionCostsCalculatorBuilder(il, al);
        builder.setVehicleRoutingProblem(vrp).setConstraintManager(constraintManager).setStateManager(stateManager).setVehicleFleetManager(fleetManager);
        final JobInsertionCostsCalculator calculator = builder.build();
        return (unassignedJob, route, bestKnownCosts) -> calculator.getInsertionData(route, unassignedJob, AbstractInsertionStrategy.NO_NEW_VEHICLE_YET, AbstractInsertionStrategy.NO_NEW_DEPARTURE_TIME_YET, AbstractInsertionStrategy.NO_NEW_DRIVER_YET, bestKnownCosts);
    }

}

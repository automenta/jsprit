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

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.cost.ForwardTransportCost;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.util.ActivityTimeTracker;


/**
 * Updates total costs (i.e. transport and activity costs) at route and activity level.
 * <p>
 * <p>Thus it modifies <code>stateManager.getRouteState(route, StateTypes.COSTS)</code> and <br>
 * <code>stateManager.getActivityState(activity, StateTypes.COSTS)</code>
 */
public class UpdateVariableCosts implements ActivityVisitor, StateUpdater {

    private final VehicleRoutingActivityCosts activityCost;

    private final ForwardTransportCost transportCost;

    private final StateManager states;

    private double totalOperationCost;

    private VehicleRoute vehicleRoute;

    private AbstractActivity prevAct;

    private double startTimeAtPrevAct;

    private final ActivityTimeTracker timeTracker;

    /**
     * Updates total costs (i.e. transport and activity costs) at route and activity level.
     * <p>
     * <p>Thus it modifies <code>stateManager.getRouteState(route, StateTypes.COSTS)</code> and <br>
     * <code>stateManager.getActivityState(activity, StateTypes.COSTS)</code>
     *
     * @param activityCost
     * @param transportCost
     * @param states
     */
    public UpdateVariableCosts(VehicleRoutingActivityCosts activityCost, VehicleRoutingTransportCosts transportCost, StateManager states) {
        this.activityCost = activityCost;
        this.transportCost = transportCost;
        this.states = states;
        timeTracker = new ActivityTimeTracker(transportCost, activityCost);
    }

    public UpdateVariableCosts(VehicleRoutingActivityCosts activityCosts, VehicleRoutingTransportCosts transportCosts, StateManager stateManager, ActivityTimeTracker.ActivityPolicy activityPolicy) {
        this.activityCost = activityCosts;
        this.transportCost = transportCosts;
        this.states = stateManager;
        timeTracker = new ActivityTimeTracker(transportCosts, activityPolicy, activityCosts);
    }

    @Override
    public void begin(VehicleRoute route) {
        vehicleRoute = route;
        timeTracker.begin(route);
        prevAct = route.start;
        startTimeAtPrevAct = timeTracker.getActEndTime();
    }

    @Override
    public void visit(AbstractActivity act) {
        timeTracker.visit(act);

        double transportCost = this.transportCost.transportCost(prevAct.location(), act.location(), startTimeAtPrevAct, vehicleRoute.driver, vehicleRoute.vehicle());
        double actCost = activityCost.getActivityCost(act, timeTracker.getActArrTime(), vehicleRoute.driver, vehicleRoute.vehicle());

        totalOperationCost += transportCost;
        totalOperationCost += actCost;

        states.putInternalTypedActivityState(act, InternalStates.COSTS, totalOperationCost);

        prevAct = act;
        startTimeAtPrevAct = timeTracker.getActEndTime();
    }

    @Override
    public void finish() {
        timeTracker.finish();
        double transportCost = this.transportCost.transportCost(prevAct.location(), vehicleRoute.end.location(), startTimeAtPrevAct, vehicleRoute.driver, vehicleRoute.vehicle());
        double actCost = activityCost.getActivityCost(vehicleRoute.end, timeTracker.getActEndTime(), vehicleRoute.driver, vehicleRoute.vehicle());

        totalOperationCost += transportCost;
        totalOperationCost += actCost;

        states.putTypedInternalRouteState(vehicleRoute, InternalStates.COSTS, totalOperationCost);

        startTimeAtPrevAct = 0.0;
        prevAct = null;
        vehicleRoute = null;
        totalOperationCost = 0.0;
    }

}

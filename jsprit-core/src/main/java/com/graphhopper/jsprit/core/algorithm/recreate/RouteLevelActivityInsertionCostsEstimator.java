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

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Deprecated
class RouteLevelActivityInsertionCostsEstimator implements ActivityInsertionCostsCalculator {

    private final VehicleRoutingActivityCosts activityCosts;

    private final AuxilliaryCostCalculator auxilliaryPathCostCalculator;

    private final RouteAndActivityStateGetter stateManager;

    private int nuOfActivities2LookForward;

    public RouteLevelActivityInsertionCostsEstimator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts actCosts, RouteAndActivityStateGetter stateManager) {
        this.activityCosts = actCosts;
        this.stateManager = stateManager;
        auxilliaryPathCostCalculator = new AuxilliaryCostCalculator(routingCosts, activityCosts);
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, AbstractActivity prevAct, AbstractActivity nextAct, AbstractActivity newAct, double depTimeAtPrevAct) {
        List<AbstractActivity> path = new ArrayList<>();
        path.add(prevAct);
        path.add(newAct);
        path.add(nextAct);
        int actIndex;
        actIndex = prevAct instanceof Start ? 0 : iFacts.getRoute().tourActivities().activities().indexOf(nextAct);
        if (nuOfActivities2LookForward > 0 && !(nextAct instanceof End)) {
            path.addAll(getForwardLookingPath(iFacts.getRoute(), actIndex));
        }

		/*
         * calculates the path costs with new vehicle, c(forwardPath,newVehicle).
		 */
        double forwardPathCost_newVehicle = auxilliaryPathCostCalculator.costOfPath(path, depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        return forwardPathCost_newVehicle - (actCostsOld(iFacts.getRoute(), path.get(path.size() - 1)) - actCostsOld(iFacts.getRoute(), prevAct));
    }

    private double actCostsOld(VehicleRoute vehicleRoute, AbstractActivity act) {
        Double cost_at_act;
        cost_at_act = act instanceof End ? stateManager.getRouteState(vehicleRoute, InternalStates.COSTS, Double.class) : stateManager.state(act, InternalStates.COSTS, Double.class);
        if (cost_at_act == null) cost_at_act = 0.;
        return cost_at_act;
    }

    private Collection<AbstractActivity> getForwardLookingPath(VehicleRoute route, int actIndex) {
        List<AbstractActivity> forwardLookingPath = new ArrayList<>();
        int nuOfActsInPath = 0;
        int index = actIndex + 1;
        while (index < route.tourActivities().activities().size() && nuOfActsInPath < nuOfActivities2LookForward) {
            forwardLookingPath.add(route.tourActivities().activities().get(index));
            index++;
            nuOfActsInPath++;
        }
        if (nuOfActsInPath < nuOfActivities2LookForward) {
            forwardLookingPath.add(route.end);
        }
        return forwardLookingPath;
    }

    public void setForwardLooking(int nActivities) {
        this.nuOfActivities2LookForward = nActivities;
    }
}

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

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.constraint.SoftActivityConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;

public class VariableTransportCostCalculator implements SoftActivityConstraint {

    private final VehicleRoutingTransportCosts routingCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    public VariableTransportCostCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
        this.routingCosts = routingCosts;
        this.activityCosts = activityCosts;
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, AbstractActivity prevAct, AbstractActivity newAct, AbstractActivity nextAct, double depTimeAtPrevAct) {
        double tp_costs_prevAct_newAct = routingCosts.transportCost(prevAct.location(), newAct.location(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double tp_time_prevAct_newAct = routingCosts.transportTime(prevAct.location(), newAct.location(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());

        double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
        double newAct_endTime = Math.max(newAct_arrTime, newAct.startEarliest()) + activityCosts.getActivityDuration(newAct,newAct_arrTime,iFacts.getNewDriver(),iFacts.getNewVehicle());

        //open routes
        if (nextAct instanceof End) {
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                return tp_costs_prevAct_newAct;
            }
        }

        double tp_costs_newAct_nextAct = routingCosts.transportCost(newAct.location(), nextAct.location(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct;

        double oldCosts;
        if (iFacts.getRoute().isEmpty()) {
            double tp_costs_prevAct_nextAct = routingCosts.transportCost(prevAct.location(), nextAct.location(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
            oldCosts = tp_costs_prevAct_nextAct;
        } else {
            double tp_costs_prevAct_nextAct = routingCosts.transportCost(prevAct.location(), nextAct.location(), prevAct.end(), iFacts.getRoute().driver, iFacts.getRoute().vehicle());
            oldCosts = tp_costs_prevAct_nextAct;
        }
        return totalCosts - oldCosts;
    }

}

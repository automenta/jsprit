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
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Calculates activity insertion costs locally, i.e. by comparing the additional costs of insertion the new activity k between
 * activity i (prevAct) and j (nextAct).
 * Additional costs are then basically calculated as delta c = c_ik + c_kj - c_ij.
 * <p>
 * <p>Note once time has an effect on costs this class requires activity endTimes.
 *
 * @author stefan
 */
class LocalActivityInsertionCostsCalculator implements ActivityInsertionCostsCalculator {

    private final VehicleRoutingTransportCosts routingCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private final double activityCostsWeight = 1.;

    private double solutionCompletenessRatio = 1.;

    private final RouteAndActivityStateGetter stateManager;

    public LocalActivityInsertionCostsCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts actCosts, RouteAndActivityStateGetter stateManager) {
        this.routingCosts = routingCosts;
        this.activityCosts = actCosts;
        this.stateManager = stateManager;
    }

    @Override
    public double getCosts(JobInsertionContext iFacts, AbstractActivity prevAct, AbstractActivity nextAct, AbstractActivity newAct, double depTimeAtPrevAct) {

        double tp_costs_prevAct_newAct = routingCosts.transportCost(prevAct.location(), newAct.location(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double tp_time_prevAct_newAct = routingCosts.transportTime(prevAct.location(), newAct.location(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double newAct_arrTime = depTimeAtPrevAct + tp_time_prevAct_newAct;
        double newAct_endTime = Math.max(newAct_arrTime, newAct.startEarliest()) + activityCosts.getActivityDuration(newAct, newAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());

        double act_costs_newAct = activityCosts.getActivityCost(newAct, newAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());

        if (isEnd(nextAct) && !toDepot(iFacts.getNewVehicle())) return tp_costs_prevAct_newAct + solutionCompletenessRatio * activityCostsWeight * act_costs_newAct;

        double tp_costs_newAct_nextAct = routingCosts.transportCost(newAct.location(), nextAct.location(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double tp_time_newAct_nextAct = routingCosts.transportTime(newAct.location(), nextAct.location(), newAct_endTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
        double endTime_nextAct_new = Math.max(nextAct_arrTime, nextAct.startEarliest()) + activityCosts.getActivityDuration(nextAct, nextAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double act_costs_nextAct = activityCosts.getActivityCost(nextAct, nextAct_arrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());

        double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct + solutionCompletenessRatio * activityCostsWeight * (act_costs_newAct + act_costs_nextAct);

        double oldCosts = 0.;
        if (iFacts.getRoute().isEmpty()) {
            double tp_costs_prevAct_nextAct = 0.;
            if (newAct instanceof DeliverShipment)
                tp_costs_prevAct_nextAct = routingCosts.transportCost(prevAct.location(), nextAct.location(), depTimeAtPrevAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
            oldCosts += tp_costs_prevAct_nextAct;
        } else {
            double tp_costs_prevAct_nextAct = routingCosts.transportCost(prevAct.location(), nextAct.location(), prevAct.end(), iFacts.getRoute().driver, iFacts.getRoute().vehicle());
            double arrTime_nextAct = depTimeAtPrevAct + routingCosts.transportTime(prevAct.location(), nextAct.location(), prevAct.end(), iFacts.getRoute().driver, iFacts.getRoute().vehicle());
            double endTime_nextAct_old = Math.max(arrTime_nextAct, nextAct.startEarliest()) + activityCosts.getActivityDuration(nextAct, arrTime_nextAct, iFacts.getRoute().driver,iFacts.getRoute().vehicle());
            double actCost_nextAct = activityCosts.getActivityCost(nextAct, arrTime_nextAct, iFacts.getRoute().driver, iFacts.getRoute().vehicle());

            double endTimeDelay_nextAct = Math.max(0, endTime_nextAct_new - endTime_nextAct_old);
            Double futureWaiting = stateManager.state(nextAct, iFacts.getRoute().vehicle(), InternalStates.FUTURE_WAITING, Double.class);
            if (futureWaiting == null) futureWaiting = 0.;
            double waitingTime_savings_timeUnit = Math.min(futureWaiting, endTimeDelay_nextAct);
            double waitingTime_savings = waitingTime_savings_timeUnit * iFacts.getRoute().vehicle().type().getVehicleCostParams().perWaitingTimeUnit;
            oldCosts += solutionCompletenessRatio * activityCostsWeight * waitingTime_savings;
            oldCosts += tp_costs_prevAct_nextAct + solutionCompletenessRatio * activityCostsWeight * actCost_nextAct;
        }
        return totalCosts - oldCosts;
    }

    private static boolean toDepot(Vehicle newVehicle) {
        return newVehicle.isReturnToDepot();
    }

    private static boolean isEnd(AbstractActivity nextAct) {
        return nextAct instanceof End;
    }

    public void setSolutionCompletenessRatio(double solutionCompletenessRatio) {
        this.solutionCompletenessRatio = solutionCompletenessRatio;
    }
}

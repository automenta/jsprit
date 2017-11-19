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

package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;


/**
 * @author stefan
 */
public class VehicleDependentTimeWindowConstraints implements HardActivityConstraint {

    private final RouteAndActivityStateGetter states;

    private final VehicleRoutingTransportCosts routingCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    public VehicleDependentTimeWindowConstraints(RouteAndActivityStateGetter states, VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
        this.states = states;
        this.routingCosts = routingCosts;
        this.activityCosts = activityCosts;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, AbstractActivity prevAct, AbstractActivity newAct, AbstractActivity nextAct, double prevActDepTime) {
        double latestVehicleArrival = iFacts.getNewVehicle().latestArrival();
        Double latestArrTimeAtNextAct;
        Location nextActLocation;
        if (nextAct instanceof End) {
            latestArrTimeAtNextAct = latestVehicleArrival;
            nextActLocation = iFacts.getNewVehicle().end();
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                nextActLocation = newAct.location();
            }
        } else {
            latestArrTimeAtNextAct = states.state(nextAct, iFacts.getNewVehicle(), InternalStates.LATEST_OPERATION_START_TIME, Double.class);
            if (latestArrTimeAtNextAct == null) {//otherwise set it to theoretical_latest_operation_startTime
                latestArrTimeAtNextAct = nextAct.startLatest();
            }
            nextActLocation = nextAct.location();
        }

			/*
             * if latest arrival of vehicle (at its end) is smaller than earliest operation start times of activities,
			 * then vehicle can never conduct activities.
			 *
			 *     |--- vehicle's operation time ---|
			 *                        					|--- prevAct or newAct or nextAct ---|
			 */
        double newAct_theoreticalEarliestOperationStartTime = newAct.startEarliest();

        if (latestVehicleArrival < prevAct.startEarliest() ||
            latestVehicleArrival < newAct_theoreticalEarliestOperationStartTime ||
            latestVehicleArrival < nextAct.startEarliest()) {
            return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }
            /*
             * if the latest operation start-time of new activity is smaller than the earliest start of prev. activity,
			 * then
			 *
			 *                    |--- prevAct ---|
			 *  |--- newAct ---|
			 */
        if (newAct.startLatest() < prevAct.startEarliest()) {
            return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }

			/*
             *  |--- prevAct ---|
			 *                                          |- earliest arrival of vehicle
			 *                       |--- nextAct ---|
			 */
        double arrTimeAtNextOnDirectRouteWithNewVehicle = prevActDepTime + routingCosts.transportTime(prevAct.location(), nextActLocation, prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        if (arrTimeAtNextOnDirectRouteWithNewVehicle > latestArrTimeAtNextAct) {
            return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }

			/*
             *                     |--- newAct ---|
			 *  |--- nextAct ---|
			 */
        if (newAct.startEarliest() > nextAct.startLatest()) {
            return ConstraintsStatus.NOT_FULFILLED;
        }
        //			log.info("check insertion of " + newAct + " between " + prevAct + " and " + nextAct + ". prevActDepTime=" + prevActDepTime);
        double arrTimeAtNewAct = prevActDepTime + routingCosts.transportTime(prevAct.location(), newAct.location(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double activityDuration = activityCosts.getActivityDuration(newAct, arrTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double endTimeAtNewAct = Math.max(arrTimeAtNewAct, newAct.startEarliest())
                + activityDuration;
        double latestArrTimeAtNewAct =
            Math.min(newAct.startLatest(),
                latestArrTimeAtNextAct -
                    routingCosts.transportTimeReverse(newAct.location(), nextActLocation, latestArrTimeAtNextAct, iFacts.getNewDriver(), iFacts.getNewVehicle())
                    - activityDuration
            );

			/*
             *  |--- prevAct ---|
			 *                       		                 |--- vehicle's arrival @newAct
			 *        latest arrival of vehicle @newAct ---|
			 */
        if (arrTimeAtNewAct > latestArrTimeAtNewAct) {
            return ConstraintsStatus.NOT_FULFILLED;
        }

        if (nextAct instanceof End) {
            if (!iFacts.getNewVehicle().isReturnToDepot()) {
                return ConstraintsStatus.FULFILLED;
            }
        }
//			log.info(newAct + " arrTime=" + arrTimeAtNewAct);

        double arrTimeAtNextAct = endTimeAtNewAct + routingCosts.transportTime(newAct.location(), nextActLocation, endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());

			/*
             *  |--- newAct ---|
			 *                       		                 |--- vehicle's arrival @nextAct
			 *        latest arrival of vehicle @nextAct ---|
			 */
        return arrTimeAtNextAct > latestArrTimeAtNextAct ?
                ConstraintsStatus.NOT_FULFILLED : ConstraintsStatus.FULFILLED;
    }
}


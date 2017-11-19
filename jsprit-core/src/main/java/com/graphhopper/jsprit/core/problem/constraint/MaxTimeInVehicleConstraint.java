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

import com.graphhopper.jsprit.core.algorithm.state.State;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;

import java.util.Collections;
import java.util.Map;

/**
 * Created by schroeder on 15/09/16.
 */
public class MaxTimeInVehicleConstraint implements HardActivityConstraint {

    private final VehicleRoutingProblem vrp;

    private final TransportTime transportTime;

    private final VehicleRoutingActivityCosts activityCosts;

    private final State minSlackId;

    private final State openJobsId;

    private final StateManager stateManager;

    public MaxTimeInVehicleConstraint(TransportTime transportTime, VehicleRoutingActivityCosts activityCosts, State minSlackId, StateManager stateManager, VehicleRoutingProblem vrp, State openJobsId) {
        this.transportTime = transportTime;
        this.minSlackId = minSlackId;
        this.stateManager = stateManager;
        this.activityCosts = activityCosts;
        this.vrp = vrp;
        this.openJobsId = openJobsId;
    }

    @Override
    public ConstraintsStatus fulfilled(final JobInsertionContext iFacts, AbstractActivity prevAct, AbstractActivity newAct, AbstractActivity nextAct, double prevActDepTime) {
        boolean newActIsPickup = newAct instanceof PickupActivity;
        boolean newActIsDelivery = newAct instanceof DeliveryActivity;

        /*
        1. check whether insertion of new shipment satisfies own max-in-vehicle-constraint
        2. check whether insertion of new shipment satisfies all other max-in-vehicle-constraints
         */
        //************ 1. check whether insertion of new shipment satisfies own max-in-vehicle-constraint
        double newActArrival = prevActDepTime + transportTime.transportTime(prevAct.location(),newAct.location(),prevActDepTime,iFacts.getNewDriver(),iFacts.getNewVehicle());
        double newActStart = Math.max(newActArrival, newAct.startEarliest());
        double newActDeparture = newActStart + activityCosts.getActivityDuration(newAct, newActArrival, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double nextActArrival = newActDeparture + transportTime.transportTime(newAct.location(),nextAct.location(),newActDeparture,iFacts.getNewDriver(),iFacts.getNewVehicle());
        double nextActStart = Math.max(nextActArrival,nextAct.startEarliest());
        if(newAct instanceof DeliveryActivity){
            double pickupEnd;
            pickupEnd = iFacts.getAssociatedActivities().size() == 1 ? iFacts.getNewDepTime() : iFacts.getRelatedActivityContext().getEndTime();
            double timeInVehicle = newActStart - pickupEnd;
            double maxTimeInVehicle = ((JobActivity)newAct).job().vehicleTimeInMax();
            if(timeInVehicle > maxTimeInVehicle) return ConstraintsStatus.NOT_FULFILLED;

        }
        else if(newActIsPickup){
            if(iFacts.getAssociatedActivities().size() == 1){
                double maxTimeInVehicle = ((JobActivity)newAct).job().vehicleTimeInMax();
                //ToDo - estimate in vehicle time of pickups here - This seems to trickier than I thought
                double nextActDeparture = nextActStart + activityCosts.getActivityDuration(nextAct, nextActArrival, iFacts.getNewDriver(), iFacts.getNewVehicle());
//                if(!nextAct instanceof End)
                double timeToEnd = 0; //newAct.end + tt(newAct,nextAct) + t@nextAct + t_to_end
                if(timeToEnd > maxTimeInVehicle) return ConstraintsStatus.NOT_FULFILLED;
            }
        }

        //************ 2. check whether insertion of new shipment satisfies all other max-in-vehicle-constraints

        double minSlack = Double.MAX_VALUE;
        if (!(nextAct instanceof End)) {
            minSlack = stateManager.state(nextAct, iFacts.getNewVehicle(), minSlackId, Double.class);
        }
        double directArrTimeNextAct = prevActDepTime + transportTime.transportTime(prevAct.location(), nextAct.location(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
        double directNextActStart = Math.max(directArrTimeNextAct, nextAct.startEarliest());
        double additionalTimeOfNewAct = (nextActStart - prevActDepTime) - (directNextActStart - prevActDepTime);
        if (additionalTimeOfNewAct > minSlack) {
            return newActIsPickup ? ConstraintsStatus.NOT_FULFILLED : ConstraintsStatus.NOT_FULFILLED;
        }
        if (newActIsDelivery) {
            Map<Job, Double> openJobsAtNext;
            openJobsAtNext = nextAct instanceof End ? stateManager.getRouteState(iFacts.getRoute(), iFacts.getNewVehicle(), openJobsId, Map.class) : stateManager.state(nextAct, iFacts.getNewVehicle(), openJobsId, Map.class);
            if (openJobsAtNext == null) openJobsAtNext = Collections.emptyMap();
            for (Map.Entry<Job, Double> jobDoubleEntry : openJobsAtNext.entrySet()) {
                double slack = jobDoubleEntry.getValue();
                double additionalTimeOfNewJob = additionalTimeOfNewAct;
                if (jobDoubleEntry.getKey() instanceof Shipment) {
                    Map<Job, Double> openJobsAtNextOfPickup = Collections.emptyMap();
                    AbstractActivity nextAfterPickup;
                    nextAfterPickup = iFacts.getAssociatedActivities().size() == 1 && !iFacts.getRoute().isEmpty() ? iFacts.getRoute().activities().get(0) : iFacts.getRoute().activities().get(iFacts.getRelatedActivityContext().getInsertionIndex());
                    if (nextAfterPickup != null)
                        openJobsAtNextOfPickup = stateManager.state(nextAfterPickup, iFacts.getNewVehicle(), openJobsId, Map.class);
                    if (openJobsAtNextOfPickup.containsKey(jobDoubleEntry.getKey())) {
                        AbstractActivity pickupAct = iFacts.getAssociatedActivities().get(0);
                        double pickupActArrTime = iFacts.getRelatedActivityContext().getArrivalTime();
                        double pickupActEndTime = startOf(pickupAct, pickupActArrTime) + activityCosts.getActivityDuration(pickupAct, pickupActArrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
                        double nextAfterPickupArr = pickupActEndTime + transportTime.transportTime(pickupAct.location(), nextAfterPickup.location(), pickupActArrTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
                        additionalTimeOfNewJob += startOf(nextAfterPickup, nextAfterPickupArr) - startOf(nextAfterPickup, nextAfterPickup.arrTime());
                    }
                }
                if (additionalTimeOfNewJob > slack) {
                    return ConstraintsStatus.NOT_FULFILLED;
                }
            }
        }
        return ConstraintsStatus.FULFILLED;
    }

    private static double startOf(AbstractActivity act, double arrTime) {
        return Math.max(arrTime, act.startEarliest());
    }

}

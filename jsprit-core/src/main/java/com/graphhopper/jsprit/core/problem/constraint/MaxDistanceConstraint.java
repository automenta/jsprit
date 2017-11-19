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
import com.graphhopper.jsprit.core.problem.Indexed;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.Collection;
import java.util.Map;

/**
 * Created by schroeder on 11/10/16.
 */
public class MaxDistanceConstraint implements HardActivityConstraint {

    private final StateManager stateManager;

    private final State distanceId;

    private final TransportDistance distanceCalculator;

    private Double[] maxDistances;

    public MaxDistanceConstraint(StateManager stateManager, State distanceId, TransportDistance distanceCalculator, Map<Vehicle, Double> maxDistancePerVehicleMap) {
        this.stateManager = stateManager;
        this.distanceId = distanceId;
        this.distanceCalculator = distanceCalculator;
        makeArray(maxDistancePerVehicleMap);
    }

    private void makeArray(Map<Vehicle, Double> maxDistances) {
        int maxIndex = getMaxIndex(maxDistances.keySet());
        this.maxDistances = new Double[maxIndex + 1];
        for (Map.Entry<Vehicle, Double> vehicleDoubleEntry : maxDistances.entrySet()) {
            this.maxDistances[(vehicleDoubleEntry.getKey()).index()] = vehicleDoubleEntry.getValue();
        }
    }

    private static int getMaxIndex(Iterable<Vehicle> vehicles) {
        int index = 0;
        for (Vehicle v : vehicles) {
            if (v.index() > index) index = v.index();
        }
        return index;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, AbstractActivity prevAct, AbstractActivity newAct, AbstractActivity nextAct, double prevActDepTime) {
        if (!hasMaxDistance(iFacts.getNewVehicle())) return ConstraintsStatus.FULFILLED;
        Double currentDistance = 0d;
        boolean routeIsEmpty = iFacts.getRoute().isEmpty();
        if (!routeIsEmpty) {
            currentDistance = stateManager.getRouteState(iFacts.getRoute(), iFacts.getNewVehicle(), distanceId, Double.class);
        }
        double maxDistance = getMaxDistance(iFacts.getNewVehicle());
        if (currentDistance > maxDistance) return ConstraintsStatus.NOT_FULFILLED_BREAK;

        double distancePrevAct2NewAct = distanceCalculator.distance(prevAct.location(), newAct.location(), iFacts.getNewDepTime(), iFacts.getNewVehicle());
        double distanceNewAct2nextAct = distanceCalculator.distance(newAct.location(), nextAct.location(), iFacts.getNewDepTime(), iFacts.getNewVehicle());
        double distancePrevAct2NextAct = distanceCalculator.distance(prevAct.location(), nextAct.location(), prevActDepTime, iFacts.getNewVehicle());
        if (prevAct instanceof Start && nextAct instanceof End) distancePrevAct2NextAct = 0;
        if (nextAct instanceof End && !iFacts.getNewVehicle().isReturnToDepot()) {
            distanceNewAct2nextAct = 0;
            distancePrevAct2NextAct = 0;
        }
        double additionalDistance = distancePrevAct2NewAct + distanceNewAct2nextAct - distancePrevAct2NextAct;
        if (currentDistance + additionalDistance > maxDistance) return ConstraintsStatus.NOT_FULFILLED;


        double additionalDistanceOfPickup = 0;
        if (newAct instanceof DeliverShipment) {
            int iIndexOfPickup = iFacts.getRelatedActivityContext().getInsertionIndex();
            AbstractActivity pickup = iFacts.getAssociatedActivities().get(0);
            AbstractActivity actBeforePickup;
            actBeforePickup = iIndexOfPickup > 0 ? iFacts.getRoute().activities().get(iIndexOfPickup - 1) : new Start(iFacts.getNewVehicle().start(), 0, Double.MAX_VALUE);
            AbstractActivity actAfterPickup;
            actAfterPickup = iIndexOfPickup < iFacts.getRoute().activities().size() ? iFacts.getRoute().activities().get(iIndexOfPickup) : nextAct;
            double distanceActBeforePickup2Pickup = distanceCalculator.distance(actBeforePickup.location(), pickup.location(), actBeforePickup.end(), iFacts.getNewVehicle());
            double distancePickup2ActAfterPickup = distanceCalculator.distance(pickup.location(), actAfterPickup.location(), iFacts.getRelatedActivityContext().getEndTime(), iFacts.getNewVehicle());
            double distanceBeforePickup2AfterPickup = distanceCalculator.distance(actBeforePickup.location(), actAfterPickup.location(), actBeforePickup.end(), iFacts.getNewVehicle());
            if (routeIsEmpty) distanceBeforePickup2AfterPickup = 0;
            if (actAfterPickup instanceof End && !iFacts.getNewVehicle().isReturnToDepot()) {
                distancePickup2ActAfterPickup = 0;
                distanceBeforePickup2AfterPickup = 0;
            }
            additionalDistanceOfPickup = distanceActBeforePickup2Pickup + distancePickup2ActAfterPickup - distanceBeforePickup2AfterPickup;
        }


        if (currentDistance + additionalDistance + additionalDistanceOfPickup > maxDistance) {
            return ConstraintsStatus.NOT_FULFILLED;
        }

        return ConstraintsStatus.FULFILLED;
    }

    private boolean hasMaxDistance(Indexed newVehicle) {
        if (newVehicle.index() >= this.maxDistances.length) return false;
        return this.maxDistances[newVehicle.index()] != null;
    }

    private double getMaxDistance(Indexed newVehicle) {
        Double maxDistance = this.maxDistances[newVehicle.index()];
        if (maxDistance == null) return Double.MAX_VALUE;
        return maxDistance;
    }
}

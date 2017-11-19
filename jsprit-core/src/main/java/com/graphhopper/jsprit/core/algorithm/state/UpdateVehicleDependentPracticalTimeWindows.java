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
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.RouteVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class UpdateVehicleDependentPracticalTimeWindows implements RouteVisitor, StateUpdater {

    @Override
    public void visit(VehicleRoute route) {
        begin(route);
        Iterator<AbstractActivity> revIterator = route.tourActivities().reverseActivityIterator();
        while (revIterator.hasNext()) {
            visit(revIterator.next());
        }
        finish();
    }

    public interface VehiclesToUpdate {

        Collection<Vehicle> get(VehicleRoute route);

    }

    private VehiclesToUpdate vehiclesToUpdate = route -> Collections.singletonList(route.vehicle());

    private final StateManager stateManager;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private VehicleRoute route;

    private final double[] latest_arrTimes_at_prevAct;

    private final Location[] location_of_prevAct;

    private Collection<Vehicle> vehicles;

    public UpdateVehicleDependentPracticalTimeWindows(StateManager stateManager, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts activityCosts) {
        this.stateManager = stateManager;
        this.transportCosts = tpCosts;
        this.activityCosts = activityCosts;
        latest_arrTimes_at_prevAct = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        location_of_prevAct = new Location[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
    }

    public void setVehiclesToUpdate(VehiclesToUpdate vehiclesToUpdate) {
        this.vehiclesToUpdate = vehiclesToUpdate;
    }


    public void begin(VehicleRoute route) {
        this.route = route;
        vehicles = vehiclesToUpdate.get(route);
        for (Vehicle vehicle : vehicles) {
            latest_arrTimes_at_prevAct[vehicle.vehicleType().index()] = vehicle.latestArrival();
            Location location = vehicle.end();
            if(!vehicle.isReturnToDepot()){
                location = route.end.location();
            }
            location_of_prevAct[vehicle.vehicleType().index()] = location;
        }
    }


    public void visit(AbstractActivity activity) {
        for (Vehicle vehicle : vehicles) {
            double latestArrTimeAtPrevAct = latest_arrTimes_at_prevAct[vehicle.vehicleType().index()];
            Location prevLocation = location_of_prevAct[vehicle.vehicleType().index()];
            double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transportCosts.transportTimeReverse(activity.location(), prevLocation,
                latestArrTimeAtPrevAct, route.driver, vehicle) - activityCosts.getActivityDuration(activity, latestArrTimeAtPrevAct, route.driver, route.vehicle());
            double latestArrivalTime = Math.min(activity.startLatest(), potentialLatestArrivalTimeAtCurrAct);
            if (latestArrivalTime < activity.startEarliest()) {
                stateManager.putTypedInternalRouteState(route, vehicle, InternalStates.SWITCH_NOT_FEASIBLE, true);
            }
            stateManager.putInternalTypedActivityState(activity, vehicle, InternalStates.LATEST_OPERATION_START_TIME, latestArrivalTime);
            latest_arrTimes_at_prevAct[vehicle.vehicleType().index()] = latestArrivalTime;
            location_of_prevAct[vehicle.vehicleType().index()] = activity.location();
        }
    }


    public void finish() {
    }

}


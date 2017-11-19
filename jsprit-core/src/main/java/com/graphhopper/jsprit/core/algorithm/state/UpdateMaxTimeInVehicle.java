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
import com.graphhopper.jsprit.core.problem.cost.TransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.*;

/**
 * Created by schroeder on 15/09/16.
 */
public class UpdateMaxTimeInVehicle implements StateUpdater, ActivityVisitor{

    private final Map<Integer, Map<Job, Double>> openPickupEndTimesPerVehicle = new HashMap<>();

    private final Map<Integer, Map<AbstractActivity, Double>> slackTimesPerVehicle = new HashMap<>();

    private final Map<Integer, Map<AbstractActivity, Double>> actStartTimesPerVehicle = new HashMap<>();

    private VehicleRoute route;

    private final StateManager stateManager;

    private final State minSlackId;

    private final State openJobsId;

    private final double[] prevActEndTimes;

    private final Location[] prevActLocations;

    private Collection<Vehicle> vehicles;

    private final TransportTime transportTime;

    private final VehicleRoutingActivityCosts activityCosts;

    private UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate vehiclesToUpdate = route -> Collections.singletonList(route.vehicle());


    public UpdateMaxTimeInVehicle(StateManager stateManager, State slackTimeId, TransportTime transportTime, VehicleRoutingActivityCosts activityCosts, State openJobsId) {
        this.stateManager = stateManager;
        this.minSlackId = slackTimeId;
        this.openJobsId = openJobsId;
        this.transportTime = transportTime;
        prevActEndTimes = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        prevActLocations = new Location[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        this.activityCosts = activityCosts;
    }


    public void setVehiclesToUpdate(UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate vehiclesToUpdate) {
        this.vehiclesToUpdate = vehiclesToUpdate;
    }


    @Override
    public void begin(VehicleRoute route) {
        openPickupEndTimesPerVehicle.clear();
        slackTimesPerVehicle.clear();
        actStartTimesPerVehicle.clear();
        vehicles = vehiclesToUpdate.get(route);
        this.route = route;
        for(Vehicle v : vehicles){
            int vehicleIndex = v.vehicleType().index();
            openPickupEndTimesPerVehicle.put(vehicleIndex, new HashMap<>());
            slackTimesPerVehicle.put(vehicleIndex, new HashMap<>());
            actStartTimesPerVehicle.put(vehicleIndex, new HashMap<>());
            prevActEndTimes[vehicleIndex] = v.earliestDeparture();
            prevActLocations[vehicleIndex] = v.start();
        }
    }

    @Override
    public void visit(AbstractActivity activity) {
        double maxTime = getMaxTimeInVehicle(activity);

        for(Vehicle v : vehicles) {
            int vehicleIndex = v.vehicleType().index();
            Location prevActLocation = prevActLocations[vehicleIndex];
            double prevActEndTime = prevActEndTimes[v.vehicleType().index()];
            double activityArrival = prevActEndTimes[v.vehicleType().index()] + transportTime.transportTime(prevActLocation,activity.location(),prevActEndTime, route.driver,v);
            double activityStart = Math.max(activityArrival,activity.startEarliest());
            memorizeActStart(activity,v,activityStart);
            double activityEnd = activityStart + activityCosts.getActivityDuration(activity, activityArrival, route.driver, v);
            Map<Job, Double> openPickups = openPickupEndTimesPerVehicle.get(vehicleIndex);
            if (activity instanceof ServiceActivity || activity instanceof PickupActivity) {
                openPickups.put(((JobActivity) activity).job(), activityEnd);
            } else if (activity instanceof DeliveryActivity) {
                Job job = ((JobActivity) activity).job();
                double pickupEnd;
                if (openPickups.containsKey(job)) {
                    pickupEnd = openPickups.get(job);
                    openPickups.remove(job);
                } else pickupEnd = v.earliestDeparture();
                double slackTime = maxTime - (activityStart - pickupEnd);
                slackTimesPerVehicle.get(vehicleIndex).put(activity, slackTime);
            }
            prevActLocations[vehicleIndex] = activity.location();
            prevActEndTimes[vehicleIndex] = activityEnd;
        }

    }

    private static double getMaxTimeInVehicle(AbstractActivity activity) {
        double maxTime = Double.MAX_VALUE;
        if(activity instanceof JobActivity){
            maxTime = ((JobActivity) activity).job().vehicleTimeInMax();
        }
        return maxTime;
    }

    private void memorizeActStart(AbstractActivity activity, Vehicle v, double activityStart) {
        actStartTimesPerVehicle.get(v.vehicleType().index()).put(activity, activityStart);
    }

    @Override
    public void finish() {
        for(Vehicle v : vehicles) {
            int vehicleIndex = v.vehicleType().index();

            //!!! open routes !!!
            double routeEnd;
            routeEnd = !v.isReturnToDepot() ? prevActEndTimes[vehicleIndex] : prevActEndTimes[vehicleIndex] + transportTime.transportTime(prevActLocations[vehicleIndex], v.end(), prevActEndTimes[vehicleIndex], route.driver, v);

            Map<Job, Double> openDeliveries = new HashMap<>();
            for (Job job : openPickupEndTimesPerVehicle.get(vehicleIndex).keySet()) {
                double actEndTime = openPickupEndTimesPerVehicle.get(vehicleIndex).get(job);
                double slackTime = job.vehicleTimeInMax() - (routeEnd - actEndTime);
                openDeliveries.put(job, slackTime);
            }

            double minSlackTimeAtEnd = minSlackTime(openDeliveries);
            stateManager.putRouteState(route, v, minSlackId, minSlackTimeAtEnd);
            stateManager.putRouteState(route, v, openJobsId, new HashMap<>(openDeliveries));
            List<AbstractActivity> acts = new ArrayList<>(this.route.activities());
            Collections.reverse(acts);
            for (AbstractActivity act : acts) {
                Job job = ((JobActivity) act).job();
                if (act instanceof ServiceActivity || act instanceof PickupActivity) {
                    openDeliveries.remove(job);
                    double minSlackTime = minSlackTime(openDeliveries);
//                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, openJobsId, new HashMap<>(openDeliveries));
                    stateManager.putActivityState(act, v, minSlackId, minSlackTime);
                } else {
                    if (slackTimesPerVehicle.get(vehicleIndex).containsKey(act)) {
                        double slackTime = slackTimesPerVehicle.get(vehicleIndex).get(act);
                        openDeliveries.put(job, slackTime);
                    }
                    double minSlackTime = minSlackTime(openDeliveries);
//                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, openJobsId, new HashMap<>(openDeliveries));
                    stateManager.putActivityState(act, v, minSlackId, minSlackTime);
                }
            }
        }
    }

    public void finish(List<AbstractActivity> activities, Job ignore) {
        for (Vehicle v : vehicles) {
            int vehicleIndex = v.vehicleType().index();

            //!!! open routes !!!
            double routeEnd;
            routeEnd = !v.isReturnToDepot() ? prevActEndTimes[vehicleIndex] : prevActEndTimes[vehicleIndex] + transportTime.transportTime(prevActLocations[vehicleIndex], v.end(), prevActEndTimes[vehicleIndex], route.driver, v);

            Map<Job, Double> openDeliveries = new HashMap<>();
            for (Job job : openPickupEndTimesPerVehicle.get(vehicleIndex).keySet()) {
                if (job == ignore) continue;
                double actEndTime = openPickupEndTimesPerVehicle.get(vehicleIndex).get(job);
                double slackTime = job.vehicleTimeInMax() - (routeEnd - actEndTime);
                openDeliveries.put(job, slackTime);
            }

            double minSlackTimeAtEnd = minSlackTime(openDeliveries);
            stateManager.putRouteState(route, v, minSlackId, routeEnd + minSlackTimeAtEnd);
            List<AbstractActivity> acts = new ArrayList<>(activities);
            Collections.reverse(acts);
            for (AbstractActivity act : acts) {
                Job job = ((JobActivity) act).job();
                if (act instanceof ServiceActivity || act instanceof PickupActivity) {
                    openDeliveries.remove(job);
                    double minSlackTime = minSlackTime(openDeliveries);
                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, minSlackId, latestStart);
                } else {
                    if (slackTimesPerVehicle.get(vehicleIndex).containsKey(act)) {
                        double slackTime = slackTimesPerVehicle.get(vehicleIndex).get(act);
                        openDeliveries.put(job, slackTime);
                    }
                    double minSlackTime = minSlackTime(openDeliveries);
                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, minSlackId, latestStart);
                }
            }
        }
    }

    private double actStart(AbstractActivity act, Vehicle v) {
        return actStartTimesPerVehicle.get(v.vehicleType().index()).get(act);
    }

    private static double minSlackTime(Map<Job, Double> openDeliveries) {
        double min = Double.MAX_VALUE;
        for(Double value : openDeliveries.values()){
           if(value < min) min = value;
        }
        return min;
    }
}

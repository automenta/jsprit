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
/**
 *
 */
package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;


/**
 * @author stefan schroeder
 */
public class EuclideanCosts extends AbstractForwardVehicleRoutingTransportCosts {

    public final int speed = 1;

    public final double detourFactor = 1.0;

    @Override
    public String toString() {
        return "[name=crowFlyCosts]";
    }

    @Override
    public double transportCost(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
        double distance = distance(from, to);
        if (vehicle != null) {
            VehicleType t = vehicle.type();
            if (t != null) {
                return distance * t.getVehicleCostParams().perDistanceUnit;
            }
        }
        return distance;
    }

    double distance(Location fromLocation, Location toLocation) {
        return distance(fromLocation.coord, toLocation.coord);
    }

    double distance(v2 from, v2 to) {
        try {
            return EuclideanDistanceCalculator.calculateDistance(from, to) * detourFactor;
        } catch (NullPointerException e) {
            throw new NullPointerException("cannot calculate euclidean distance. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
        }
    }

    @Override
    public double transportTime(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
        return distance(from, to) / speed;
    }

    @Override
    public double distance(Location from, Location to, double departureTime, Vehicle vehicle) {
            return distance(from, to);
    }
}

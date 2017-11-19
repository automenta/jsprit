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
package com.graphhopper.jsprit.core.util;


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.AbstractForwardVehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;

/**
 * @author stefan schroeder
 */

public class ManhattanCosts extends AbstractForwardVehicleRoutingTransportCosts {

    public final double speed = 1;

    private Locations locations;

    public ManhattanCosts(Locations locations) {
        this.locations = locations;
    }

    public ManhattanCosts() {

    }

    @Override
    public double transportCost(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
        double distance;
        try {
            distance = calculateDistance(from, to);
        } catch (NullPointerException e) {
            throw new NullPointerException("cannot calculate euclidean distance. coordinates are missing. either add coordinates or use another transport-cost-calculator.");
        }
        double costs = distance;
        if (vehicle != null) {
            VehicleType t = vehicle.type();
            if (t != null) {
                costs = distance * t.getVehicleCostParams().perDistanceUnit;
            }
        }
        return costs;
    }

    @Override
    public double transportTime(Location from, Location to, double time, Driver driver, Vehicle vehicle) {
        return calculateDistance(from, to) / speed;
    }

    private double calculateDistance(Location fromLocation, Location toLocation) {
        v2 from = null;
        v2 to = null;
        if (fromLocation.coord != null && toLocation.coord != null) {
            from = fromLocation.coord;
            to = toLocation.coord;
        } else if (locations != null) {
            from = locations.coord(fromLocation.id);
            to = locations.coord(toLocation.id);
        }
        if (from == null || to == null) throw new NullPointerException();
        return calculateDistance(from, to);
    }

    private static double calculateDistance(v2 from, v2 to) {
        return Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
    }

    @Override
    public double distance(Location from, Location to, double departureTime, Vehicle vehicle) {
        return calculateDistance(from, to);
    }
}

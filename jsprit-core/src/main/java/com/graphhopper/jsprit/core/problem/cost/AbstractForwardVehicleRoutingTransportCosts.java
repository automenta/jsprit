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
package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public abstract class AbstractForwardVehicleRoutingTransportCosts implements VehicleRoutingTransportCosts {
    
    @Override
    public abstract double distance(Location from, Location to, double departureTime, Vehicle vehicle);

    @Override
    public abstract double transportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle);

    @Override
    public abstract double transportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle);

    @Override
    public double transportTimeReverse(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
        return transportTime(from, to, arrivalTime, driver, vehicle);
    }

    @Override
    public double transportCostReverse(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
        return transportCost(from, to, arrivalTime, driver, vehicle);
    }

}

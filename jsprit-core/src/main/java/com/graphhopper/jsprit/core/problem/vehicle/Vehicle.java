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
package com.graphhopper.jsprit.core.problem.vehicle;

import com.graphhopper.jsprit.core.problem.HasId;
import com.graphhopper.jsprit.core.problem.Indexed;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.job.Break;

/**
 * Basic interface for vehicle-data.
 *
 * @author schroeder
 */
public interface Vehicle extends HasId, Indexed {

    /**
     * Returns the earliest departure of vehicle which should be the lower bound of this vehicle's departure times.
     *
     * @return earliest departure time
     */
    double earliestDeparture();

    /**
     * Returns the latest arrival time at this vehicle's end-location which should be the upper bound of this vehicle's arrival times at end-location.
     *
     * @return latest arrival time of this vehicle
     */
    double latestArrival();

    /**
     * Returns the {@link VehicleType} of this vehicle.
     *
     * @return {@link VehicleType} of this vehicle
     */
    VehicleType type();

    /**
     * Returns the id of this vehicle.
     *
     * @return id
     */
    @Override
    String id();

    /**
     * Returns true if vehicle returns to depot, false otherwise.
     *
     * @return true if isReturnToDepot
     */
    boolean isReturnToDepot();

    Location start();

    Location end();

    VehicleTypeKey vehicleType();

    Skills skills();

    /**
     * @return User-specific domain data associated with the vehicle
     */
    Object data();

    Break aBreak();
    // Switch to this as soon as we switct to Java 8:
    // default Object getUserData() {
    // return null;
    // };
}

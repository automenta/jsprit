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




import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;



class InfiniteVehicles implements VehicleFleetManager {

    private static final Logger logger = LoggerFactory.getLogger(InfiniteVehicles.class);

    private final Map<VehicleTypeKey, Vehicle> types = new HashMap<>();

//	private List<VehicleTypeKey> sortedTypes = new ArrayList<VehicleTypeKey>();

    public InfiniteVehicles(Collection<Vehicle> vehicles) {
        extractTypes(vehicles);
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=infiniteVehicle]";
    }

    private void extractTypes(Iterable<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
//            VehicleTypeKey typeKey = new VehicleTypeKey(v.getType().getTypeId(), v.getStartLocation().getId(), v.getEndLocation().getId(), v.getEarliestDeparture(), v.getLatestArrival(), v.getSkills(), v.isReturnToDepot());
            types.put(v.vehicleType(), v);
//			sortedTypes.add(typeKey);
        }
    }


    @Override
    public void lock(Vehicle vehicle) {

    }

    @Override
    public void unlock(Vehicle vehicle) {

    }


    @Override
    public boolean isLocked(Vehicle vehicle) {
        return false;
    }

    @Override
    public void unlockAll() {

    }

    @Override
    public Collection<Vehicle> vehiclesAvailable() {
        return types.values();
    }

    @Override
    public Collection<Vehicle> vehiclesAvailable(Vehicle withoutThisType) {
        Collection<Vehicle> vehicles = new ArrayList<>();
        VehicleTypeKey thisKey = new VehicleTypeKey(withoutThisType.type().type(), withoutThisType.start().id, withoutThisType.end().id, withoutThisType.earliestDeparture(), withoutThisType.latestArrival(), withoutThisType.skills(), withoutThisType.isReturnToDepot());
        for (Map.Entry<VehicleTypeKey, Vehicle> vehicleTypeKeyVehicleEntry : types.entrySet()) {
            if (!(vehicleTypeKeyVehicleEntry.getKey()).equals(thisKey)) {
                vehicles.add(vehicleTypeKeyVehicleEntry.getValue());
            }
        }
        return vehicles;
    }

    @Override
    public Vehicle vehicleAvailable(VehicleTypeKey vehicleTypeIdentifier) {
        return types.get(vehicleTypeIdentifier);
    }

}

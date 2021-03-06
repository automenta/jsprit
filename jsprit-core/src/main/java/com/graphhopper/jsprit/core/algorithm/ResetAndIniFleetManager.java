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
package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.ArrayList;
import java.util.Collection;


public class ResetAndIniFleetManager implements InsertionStartsListener {

    private final VehicleFleetManager vehicleFleetManager;

    public ResetAndIniFleetManager(VehicleFleetManager vehicleFleetManager) {
        this.vehicleFleetManager = vehicleFleetManager;
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        vehicleFleetManager.unlockAll();
        Iterable<VehicleRoute> routes = new ArrayList<>(vehicleRoutes);
        for (VehicleRoute route : routes) {
            vehicleFleetManager.lock(route.vehicle());
        }
    }

    @Override
    public String toString() {
        return "[name=resetAndIniFleetManager]";
    }
}

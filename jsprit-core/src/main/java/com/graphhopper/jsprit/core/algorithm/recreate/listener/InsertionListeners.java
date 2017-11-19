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
package com.graphhopper.jsprit.core.algorithm.recreate.listener;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;


public class InsertionListeners {

    private final Collection<InsertionListener> listeners = new CopyOnWriteArrayList<>();

    /** for fast iteration, updated after listeners changed */
    private InsertionListener[] listenersArray = new InsertionListener[0];

    public Collection<InsertionListener> getListeners() {
        return listeners;
    }

    public void informJobInserted(Job insertedJob, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
        for (InsertionListener l : listenersArray) {
            if (l instanceof JobInsertedListener) {
                ((JobInsertedListener) l).informJobInserted(insertedJob, inRoute, additionalCosts, additionalTime);
            }
        }
    }

    public void informVehicleSwitched(VehicleRoute route, Vehicle oldVehicle, Vehicle newVehicle) {
        for (InsertionListener l : listenersArray) {
            if (l instanceof VehicleSwitchedListener) {
                ((VehicleSwitchedListener) l).vehicleSwitched(route, oldVehicle, newVehicle);
            }
        }
    }

    public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
        for (InsertionListener l : listenersArray) {
            if (l instanceof BeforeJobInsertionListener) {
                ((BeforeJobInsertionListener) l).informBeforeJobInsertion(job, data, route);
            }
        }
    }

    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        for (InsertionListener l : listenersArray) {
            if (l instanceof InsertionStartsListener) {
                ((InsertionStartsListener) l).informInsertionStarts(vehicleRoutes, unassignedJobs);
            }
        }
    }

    public void informInsertionEndsListeners(Collection<VehicleRoute> vehicleRoutes) {
        for (InsertionListener l : listenersArray) {
            if (l instanceof InsertionEndsListener) {
                ((InsertionEndsListener) l).informInsertionEnds(vehicleRoutes);
            }
        }
    }

    public void informJobUnassignedListeners(Job unassigned, Collection<String> reasons) {
        for (InsertionListener l : listenersArray) {
            if (l instanceof JobUnassignedListener) {
                ((JobUnassignedListener) l).informJobUnassigned(unassigned, reasons);
            }
        }
    }

    public void addListener(InsertionListener insertionListener) {
        if (listeners.add(insertionListener))
            commit();
    }

    public void removeListener(InsertionListener insertionListener) {

        if (listeners.remove(insertionListener))
            commit();
    }

    private void commit() {
        this.listenersArray = listeners.toArray(new InsertionListener[listeners.size()]);
    }

//    public void addAllListeners(Iterable<InsertionListener> listeners) {
//        for (InsertionListener l : listeners) addListener(l);
//    }

}

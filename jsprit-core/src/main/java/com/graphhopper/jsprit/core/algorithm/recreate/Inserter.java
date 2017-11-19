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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListeners;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;

import java.util.List;

class Inserter {

    interface JobInsertionHandler {

        void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route);

        void setNextHandler(JobInsertionHandler handler);

    }

    static class JobExceptionHandler implements JobInsertionHandler {

        @Override
        public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
            throw new IllegalStateException("job insertion is not supported. Do not know job type.");
        }

        @Override
        public void setNextHandler(JobInsertionHandler handler) {

        }

    }

    class ServiceInsertionHandler implements JobInsertionHandler {



        private JobInsertionHandler delegator = new JobExceptionHandler();

        private final VehicleRoutingProblem vehicleRoutingProblem;

        public ServiceInsertionHandler(VehicleRoutingProblem vehicleRoutingProblem) {
            this.vehicleRoutingProblem = vehicleRoutingProblem;
        }

        @Override
        public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
            if (job instanceof Service) {
                route.setVehicleAndDepartureTime(iData.getSelectedVehicle(), iData.getVehicleDepartureTime());
                if (!iData.getSelectedVehicle().isReturnToDepot()) {
                    if (iData.getDeliveryInsertionIndex() >= route.tourActivities().activities().size()) {
                        setEndLocation(route, (Service) job);
                    }
                }
                AbstractActivity activity = vehicleRoutingProblem.copyAndGetActivities(job).get(0);
                route.tourActivities().addActivity(iData.getDeliveryInsertionIndex(), activity);
            } else delegator.handleJobInsertion(job, iData, route);
        }

        private void setEndLocation(VehicleRoute route, Service service) {
            route.end.location(service.location);
        }

        @Override
        public void setNextHandler(JobInsertionHandler jobInsertionHandler) {
            this.delegator = jobInsertionHandler;
        }

    }

    class ShipmentInsertionHandler implements JobInsertionHandler {

//        private final VehicleRoutingProblem vehicleRoutingProblem;

        //private final TourShipmentActivityFactory activityFactory = new DefaultShipmentActivityFactory();

        private JobInsertionHandler delegator = new JobExceptionHandler();

        public ShipmentInsertionHandler() {

        }

        @Override
        public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
            if (job instanceof Shipment) {
                List<JobActivity> acts = vehicleRoutingProblem.copyAndGetActivities(job);
                AbstractActivity pickupShipment = acts.get(0);
                AbstractActivity deliverShipment = acts.get(1);
                route.setVehicleAndDepartureTime(iData.getSelectedVehicle(), iData.getVehicleDepartureTime());
                if (!iData.getSelectedVehicle().isReturnToDepot()) {
                    if (iData.getDeliveryInsertionIndex() >= route.activities().size()) {
                        setEndLocation(route, (Shipment) job);
                    }
                }
                route.tourActivities().addActivity(iData.getDeliveryInsertionIndex(), deliverShipment);
                route.tourActivities().addActivity(iData.getPickupInsertionIndex(), pickupShipment);
            } else delegator.handleJobInsertion(job, iData, route);
        }

        private void setEndLocation(VehicleRoute route, Shipment shipment) {
            route.end.location(shipment.getDeliveryLocation());
        }

        @Override
        public void setNextHandler(JobInsertionHandler jobInsertionHandler) {
            this.delegator = jobInsertionHandler;
        }

    }

    private final InsertionListeners insertionListeners;

    private final JobInsertionHandler jobInsertionHandler;

    private VehicleRoutingProblem vehicleRoutingProblem;

    private final TourActivityFactory activityFactory = new DefaultTourActivityFactory();

    public Inserter(InsertionListeners insertionListeners, VehicleRoutingProblem vehicleRoutingProblem) {
        this.insertionListeners = insertionListeners;
        this.vehicleRoutingProblem = vehicleRoutingProblem;
        jobInsertionHandler = new ServiceInsertionHandler(vehicleRoutingProblem);
        jobInsertionHandler.setNextHandler(new ShipmentInsertionHandler());
    }

    public void insertJob(Job job, InsertionData insertionData, VehicleRoute vehicleRoute) {
        insertionListeners.informBeforeJobInsertion(job, insertionData, vehicleRoute);

        if (insertionData == null || (insertionData instanceof NoInsertionFound))
            throw new IllegalStateException("insertionData null. cannot insert job.");
        if (job == null) throw new IllegalStateException("cannot insert null-job");
        if (!(vehicleRoute.vehicle().id().equals(insertionData.getSelectedVehicle().id()))) {
            insertionListeners.informVehicleSwitched(vehicleRoute, vehicleRoute.vehicle(), insertionData.getSelectedVehicle());
            vehicleRoute.setVehicleAndDepartureTime(insertionData.getSelectedVehicle(), insertionData.getVehicleDepartureTime());
        }
        jobInsertionHandler.handleJobInsertion(job, insertionData, vehicleRoute);

        insertionListeners.informJobInserted(job, vehicleRoute, insertionData.getInsertionCost(), insertionData.getAdditionalTime());
    }
}

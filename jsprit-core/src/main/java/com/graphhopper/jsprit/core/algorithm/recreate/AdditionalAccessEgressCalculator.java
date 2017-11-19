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

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

/**
 * Estimates additional access/egress costs when operating route with a new vehicle that has different start/end-location.
 * <p>
 * <p>If two vehicles have the same start/end-location and departure-time .getCosts(...) must return zero.
 *
 * @author schroeder
 */
class AdditionalAccessEgressCalculator {

    private final VehicleRoutingTransportCosts routingCosts;

    /**
     * Constructs the estimator that estimates additional access/egress costs when operating route with a new vehicle that has different start/end-location.
     * <p>
     * <p>If two vehicles have the same start/end-location and departure-time .getCosts(...) must return zero.
     *
     * @author schroeder
     */
    public AdditionalAccessEgressCalculator(VehicleRoutingTransportCosts routingCosts) {
        this.routingCosts = routingCosts;
    }

    public double getCosts(JobInsertionContext insertionContext) {
        double delta_access = 0.0;
        double delta_egress = 0.0;
        VehicleRoute currentRoute = insertionContext.getRoute();
        Vehicle newVehicle = insertionContext.getNewVehicle();
        Driver newDriver = insertionContext.getNewDriver();
        double newVehicleDepartureTime = insertionContext.getNewDepTime();
        if (!currentRoute.isEmpty()) {
            double accessTransportCostNew = routingCosts.transportCost(newVehicle.start(), currentRoute.activities().get(0).location(), newVehicleDepartureTime, newDriver, newVehicle);
            double accessTransportCostOld = routingCosts.transportCost(currentRoute.start.location(), currentRoute.activities().get(0).location(), currentRoute.getDepartureTime(), currentRoute.driver, currentRoute.vehicle());

            delta_access = accessTransportCostNew - accessTransportCostOld;

            if (newVehicle.isReturnToDepot()) {
                AbstractActivity lastActivityBeforeEndOfRoute = currentRoute.activities().get(currentRoute.activities().size() - 1);
                double lastActivityEndTimeWithOldVehicleAndDepartureTime = lastActivityBeforeEndOfRoute.end();
                double lastActivityEndTimeEstimationWithNewVehicleAndNewDepartureTime = Math.max(0.0, lastActivityEndTimeWithOldVehicleAndDepartureTime + (newVehicleDepartureTime - currentRoute.getDepartureTime()));
                double egressTransportCostNew = routingCosts.transportCost(lastActivityBeforeEndOfRoute.location(), newVehicle.end(), lastActivityEndTimeEstimationWithNewVehicleAndNewDepartureTime, newDriver, newVehicle);
                double egressTransportCostOld = routingCosts.transportCost(lastActivityBeforeEndOfRoute.location(), currentRoute.end.location(), lastActivityEndTimeWithOldVehicleAndDepartureTime, currentRoute.driver, currentRoute.vehicle());

                delta_egress = egressTransportCostNew - egressTransportCostOld;
            }
        }
        return delta_access + delta_egress;
    }

}

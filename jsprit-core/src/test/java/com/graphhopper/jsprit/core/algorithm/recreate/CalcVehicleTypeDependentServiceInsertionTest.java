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

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class CalcVehicleTypeDependentServiceInsertionTest {

    Vehicle veh1;
    Vehicle veh2;
    VehicleFleetManager fleetManager;
    Service service;
    VehicleRoute vehicleRoute;

    @Before
    public void doBefore() {
        veh1 = mock(Vehicle.class);
        veh2 = mock(Vehicle.class);
        when(veh1.type()).thenReturn(VehicleTypeImpl.Builder.the("type1").build());
        when(veh2.type()).thenReturn(VehicleTypeImpl.Builder.the("type2").build());
        when(veh1.start()).thenReturn(Location.the("loc1"));
        when(veh2.start()).thenReturn(Location.the("loc2"));
        fleetManager = mock(VehicleFleetManager.class);
        service = mock(Service.class);
        vehicleRoute = mock(VehicleRoute.class);

        when(fleetManager.vehiclesAvailable()).thenReturn(Arrays.asList(veh1, veh2));

        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.get().addDimension(0, 10).build());
        when(veh1.type()).thenReturn(type);

        when(veh2.type()).thenReturn(type);

        when(service.size()).thenReturn(Capacity.Builder.get().build());
        when(service.timeWindow()).thenReturn(TimeWindow.the(0.0, Double.MAX_VALUE));

        when(vehicleRoute.driver()).thenReturn(null);
        when(vehicleRoute.vehicle()).thenReturn(VehicleImpl.get());
    }

    @Test
    public void whenHaving2Vehicle_calcInsertionOfCheapest() {
        JobInsertionCostsCalculator calc = mock(JobInsertionCostsCalculator.class);
        InsertionData iDataVeh1 = new InsertionData(10.0, InsertionData.NO_INDEX, 1, veh1, null);
        InsertionData iDataVeh2 = new InsertionData(20.0, InsertionData.NO_INDEX, 1, veh2, null);
        when(calc.getInsertionData(vehicleRoute, service, veh1, veh1.earliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh1);
        when(calc.getInsertionData(vehicleRoute, service, veh2, veh2.earliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh2);
        when(calc.getInsertionData(vehicleRoute, service, veh2, veh2.earliestDeparture(), null, 10.0)).thenReturn(iDataVeh2);
        VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
        when(vrp.initialVehicleRoutes()).thenReturn(Set.of());
        VehicleTypeDependentJobInsertionCalculator insertion = new VehicleTypeDependentJobInsertionCalculator(vrp, fleetManager, calc);
        InsertionData iData = insertion.getInsertionData(vehicleRoute, service, null, 0.0, null, Double.MAX_VALUE);
        assertThat(iData.getSelectedVehicle(), is(veh1));

    }

    @Test
    public void whenHaving2Vehicle_calcInsertionOfCheapest2() {
        JobInsertionCostsCalculator calc = mock(JobInsertionCostsCalculator.class);
        InsertionData iDataVeh1 = new InsertionData(20.0, InsertionData.NO_INDEX, 1, veh1, null);
        InsertionData iDataVeh2 = new InsertionData(10.0, InsertionData.NO_INDEX, 1, veh2, null);
        when(calc.getInsertionData(vehicleRoute, service, veh1, veh1.earliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh1);
        when(calc.getInsertionData(vehicleRoute, service, veh2, veh2.earliestDeparture(), null, Double.MAX_VALUE)).thenReturn(iDataVeh2);
        when(calc.getInsertionData(vehicleRoute, service, veh2, veh2.earliestDeparture(), null, 20.0)).thenReturn(iDataVeh2);
        VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
        when(vrp.initialVehicleRoutes()).thenReturn(Set.of());
        VehicleTypeDependentJobInsertionCalculator insertion = new VehicleTypeDependentJobInsertionCalculator(vrp, fleetManager, calc);
        InsertionData iData = insertion.getInsertionData(vehicleRoute, service, null, 0.0, null, Double.MAX_VALUE);
        assertThat(iData.getSelectedVehicle(), is(veh2));

    }
}

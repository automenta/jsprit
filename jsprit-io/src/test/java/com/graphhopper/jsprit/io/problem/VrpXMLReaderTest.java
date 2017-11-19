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
package com.graphhopper.jsprit.io.problem;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;


public class VrpXMLReaderTest {

    private InputStream inputStream;

    @Before
    public void doBefore() {
        inputStream = getClass().getResourceAsStream("finiteVrpForReaderTest.xml");
    }

    @Test
    public void shouldReadNameOfService() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Service s = (Service) vrp.jobs().get("1");
        assertTrue(s.name.equals("cleaning"));
    }

    @Test
    public void shouldReadNameOfShipment() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertTrue(s.name().equals("deliver-smth"));
    }

    @Test
    public void whenReadingVrp_problemTypeIsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.FINITE, vrp.getFleetSize());
    }

    @Test
    public void whenReadingVrp_vehiclesAreReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(5, vrp.vehicles().size());
        assertTrue(idsInCollection(Arrays.asList("v1", "v2"), vrp.vehicles()));
    }

    @Test
    public void whenReadingVrp_vehiclesAreReadCorrectly2() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v1 = getVehicle("v1", vrp.vehicles());
        assertEquals(20, v1.type().getCapacityDimensions().get(0));
        assertEquals(100.0, v1.start().coord.x, 0.01);
        assertEquals(0.0, v1.earliestDeparture(), 0.01);
        assertEquals("depotLoc2", v1.start().id);
        assertNotNull(v1.type());
        assertEquals("vehType", v1.type().type());
        assertNotNull(v1.start());
        assertEquals(1, v1.start().index);
        assertEquals(1000.0, v1.latestArrival(), 0.01);
    }

    @Test
    public void whenReadingVehicles_skill1ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v1 = getVehicle("v1", vrp.vehicles());
        assertTrue(v1.skills().containsSkill("skill1"));
    }

    @Test
    public void whenReadingVehicles_skill2ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v1 = getVehicle("v1", vrp.vehicles());
        assertTrue(v1.skills().containsSkill("skill2"));
    }

    @Test
    public void whenReadingVehicles_nuSkillsShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v1 = getVehicle("v1", vrp.vehicles());
        assertEquals(2, v1.skills().values().size());
    }

    @Test
    public void whenReadingVehicles_nuSkillsOfV2ShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v2", vrp.vehicles());
        assertEquals(0, v.skills().values().size());
    }

    private Vehicle getVehicle(String string, Collection<Vehicle> vehicles) {
        for (Vehicle v : vehicles) if (string.equals(v.id())) return v;
        return null;
    }

    private boolean idsInCollection(List<String> asList, Collection<Vehicle> vehicles) {
        List<String> ids = new ArrayList<String>(asList);
        for (Vehicle v : vehicles) {
            if (ids.contains(v.id())) ids.remove(v.id());
        }
        return ids.isEmpty();
    }

    @Test
    public void whenReadingVrp_vehicleTypesAreReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(3, vrp.types().size());
    }

    @Test
    public void whenReadingVrpWithInfiniteSize_itReadsCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.FINITE, vrp.getFleetSize());
    }

    @Test
    public void whenReadingJobs_nuOfJobsIsReadThemCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(4, vrp.jobs().size());
    }

    @Test
    public void whenReadingServices_itReadsThemCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        int servCounter = 0;
        for (Job j : vrp.jobs().values()) {
            if (j instanceof Service) servCounter++;
        }
        assertEquals(2, servCounter);
    }

    @Test
    public void whenReadingService1_skill1ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Service s = (Service) vrp.jobs().get("1");
        assertTrue(s.skills.containsSkill("skill1"));
    }

    @Test
    public void whenReadingService1_skill2ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Service s = (Service) vrp.jobs().get("1");
        assertTrue(s.skills.containsSkill("skill2"));
    }

    @Test
    public void whenReadingService1_nuSkillsShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Service s = (Service) vrp.jobs().get("1");
        assertEquals(2, s.skills.values().size());
    }

    @Test
    public void whenReadingService2_nuSkillsOfV2ShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Service s = (Service) vrp.jobs().get("2");
        assertEquals(0, s.skills.values().size());
    }

    @Test
    public void whenReadingShipments_itReadsThemCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        int shipCounter = 0;
        for (Job j : vrp.jobs().values()) {
            if (j instanceof Shipment) shipCounter++;
        }
        assertEquals(2, shipCounter);
    }

    @Test
    public void whenReadingShipment3_skill1ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertTrue(s.skillsRequired().containsSkill("skill1"));
    }

    @Test
    public void whenReadingShipment3_skill2ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertTrue(s.skillsRequired().containsSkill("skill2"));
    }

    @Test
    public void whenReadingShipment3_nuSkillsShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals(2, s.skillsRequired().values().size());
    }

    @Test
    public void whenReadingShipment4_nuSkillsOfV2ShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("4");
        assertEquals(0, s.skillsRequired().values().size());
    }

    @Test
    public void whenReadingServices_capOfService1IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Service s1 = (Service) vrp.jobs().get("1");
        assertEquals(1, s1.size.get(0));
    }

    @Test
    public void whenReadingServices_durationOfService1IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Service s1 = (Service) vrp.jobs().get("1");
        assertEquals(10.0, s1.serviceTime, 0.01);
    }

    @Test
    public void whenReadingServices_twOfService1IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Service s1 = (Service) vrp.jobs().get("1");
        assertEquals(0.0, s1.timeWindow().start, 0.01);
        assertEquals(4000.0, s1.timeWindow().end, 0.01);
    }

    @Test
    public void whenReadingServices_typeOfService1IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Service s1 = (Service) vrp.jobs().get("1");
        assertEquals("service", s1.type);
    }

    @Test
    public void whenReadingFile_v2MustNotReturnToDepot() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v2", vrp.vehicles());
        assertFalse(v.isReturnToDepot());
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectStartLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.vehicles());
        assertEquals("startLoc", v3.start().id);
        assertNotNull(v3.end());
        assertEquals(4, v3.end().index);
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectEndLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.vehicles());
        assertEquals("endLoc", v3.end().id);
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectEndLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.vehicles());
        assertEquals(1000.0, v3.end().coord.x, 0.01);
        assertEquals(2000.0, v3.end().coord.y, 0.01);
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectStartLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.vehicles());
        assertEquals(10.0, v3.start().coord.x, 0.01);
        assertEquals(100.0, v3.start().coord.y, 0.01);
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.vehicles());
        assertEquals(10.0, v3.start().coord.x, 0.01);
        assertEquals(100.0, v3.start().coord.y, 0.01);
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectLocationId() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.vehicles());
        assertEquals("startLoc", v3.start().id);
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectStartLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.vehicles());
        assertEquals("startLoc", v.start().id);
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectEndLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.vehicles());
        assertEquals("endLoc", v.end().id);
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectEndLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.vehicles());
        assertEquals(1000.0, v.end().coord.x, 0.01);
        assertEquals(2000.0, v.end().coord.y, 0.01);
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectStartLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.vehicles());
        assertEquals(10.0, v.start().coord.x, 0.01);
        assertEquals(100.0, v.start().coord.y, 0.01);
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.vehicles());
        assertEquals(10.0, v.start().coord.x, 0.01);
        assertEquals(100.0, v.start().coord.y, 0.01);
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectLocationId() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.vehicles());
        assertEquals("startLoc", v.start().id);
    }

    @Test
    public void whenReadingJobs_capOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals(10, s.size().get(0));
    }

    @Test
    public void whenReadingJobs_pickupServiceTimeOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals(10.0, s.getPickupServiceTime(), 0.01);
    }

    @Test
    public void whenReadingJobs_pickupTimeWindowOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals(1000.0, s.getPickupTimeWindow().start, 0.01);
        assertEquals(4000.0, s.getPickupTimeWindow().end, 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryTimeWindowOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals(6000.0, s.getDeliveryTimeWindow().start, 0.01);
        assertEquals(10000.0, s.getDeliveryTimeWindow().end, 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryServiceTimeOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals(100.0, s.getDeliveryServiceTime(), 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryCoordShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals(10.0, s.getDeliveryLocation().coord.x, 0.01);
        assertEquals(0.0, s.getDeliveryLocation().coord.y, 0.01);
    }

    @Test
    public void whenReadingJobs_pickupCoordShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals(10.0, s.getPickupLocation().coord.x, 0.01);
        assertEquals(10.0, s.getPickupLocation().coord.y, 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryIdShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals("i(9,9)", s.getDeliveryLocation().id);
    }

    @Test
    public void whenReadingJobs_pickupIdShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("3");
        assertEquals("i(3,9)", s.getPickupLocation().id);
    }

    @Test
    public void whenReadingJobs_pickupLocationIdShipment4IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("4");
        assertEquals("[x=10.0][y=10.0]", s.getPickupLocation().id);
    }

    @Test
    public void whenReadingJobs_deliveryLocationIdShipment4IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("4");
        assertEquals("[x=10.0][y=0.0]", s.getDeliveryLocation().id);
    }

    @Test
    public void whenReadingJobs_pickupServiceTimeOfShipment4IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("4");
        assertEquals(0.0, s.getPickupServiceTime(), 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryServiceTimeOfShipment4IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Shipment s = (Shipment) vrp.jobs().get("4");
        assertEquals(100.0, s.getDeliveryServiceTime(), 0.01);
    }

    @Test
    public void whenReadingFile_v5AndItsTypeHasTheCorrectCapacityDimensionValues() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v5", vrp.vehicles());
        assertEquals(100, v.type().getCapacityDimensions().get(0));
        assertEquals(1000, v.type().getCapacityDimensions().get(1));
        assertEquals(10000, v.type().getCapacityDimensions().get(2));
        assertEquals(0, v.type().getCapacityDimensions().get(3));
        assertEquals(0, v.type().getCapacityDimensions().get(5));
        assertEquals(100000, v.type().getCapacityDimensions().get(10));
    }

    @Test
    public void whenReadingInitialRouteWithShipment4_thisShipmentShouldNotAppearInJobMap() { //since it is not part of the problem anymore
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder).read(getClass().getResourceAsStream("finiteVrpWithInitialSolutionForReaderTest.xml"));
        VehicleRoutingProblem vrp = builder.build();
        assertFalse(vrp.jobs().containsKey("4"));
    }

    @Test
    public void whenReadingInitialRouteWithDepTime10_departureTimeOfRouteShouldBeReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder).read(getClass().getResourceAsStream("finiteVrpWithInitialSolutionForReaderTest.xml"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(10., vrp.initialVehicleRoutes().iterator().next().getDepartureTime(), 0.01);
    }

    @Test
    public void whenReadingInitialRoute_nuInitialRoutesShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(getClass().getResourceAsStream("finiteVrpWithInitialSolutionForReaderTest.xml"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(1, vrp.initialVehicleRoutes().size());
    }

    @Test
    public void whenReadingInitialRoute_nuActivitiesShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(builder, null).read(getClass().getResourceAsStream("finiteVrpWithInitialSolutionForReaderTest.xml"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(2, vrp.initialVehicleRoutes().iterator().next().activities().size());
    }

    @Test
    public void testRead_ifReaderIsCalled_itReadsSuccessfullyV2() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        ArrayList<VehicleRoutingProblemSolution> solutions = new ArrayList<VehicleRoutingProblemSolution>();
        new VrpXMLReader(vrpBuilder, solutions).read(getClass().getResourceAsStream("finiteVrpWithShipmentsAndSolution.xml"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertEquals(4, vrp.jobs().size());
        assertEquals(1, solutions.size());

        assertEquals(1, solutions.get(0).routes.size());
        List<AbstractActivity> activities = solutions.get(0).routes.iterator().next().tourActivities().activities();
        assertEquals(4, activities.size());
        assertTrue(activities.get(0) instanceof PickupService);
        assertTrue(activities.get(1) instanceof PickupService);
        assertTrue(activities.get(2) instanceof PickupShipment);
        assertTrue(activities.get(3) instanceof DeliverShipment);
    }

    @Test
    public void testRead_ifReaderIsCalled_itReadsSuccessfully() {
        new VrpXMLReader(VehicleRoutingProblem.Builder.get(), new ArrayList<VehicleRoutingProblemSolution>()).read(getClass().getResourceAsStream("lui-shen-solution.xml"));
        assertTrue(true);
    }


    @Test
    public void unassignedJobShouldBeRead() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        ArrayList<VehicleRoutingProblemSolution> solutions = new ArrayList<VehicleRoutingProblemSolution>();
        new VrpXMLReader(vrpBuilder, solutions).read(getClass().getResourceAsStream("finiteVrpWithShipmentsAndSolution.xml"));

        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        assertEquals(1, solution.jobsUnassigned.size());
        assertEquals("4", solution.jobsUnassigned.iterator().next().id());
    }
}

//

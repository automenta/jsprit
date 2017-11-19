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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.v2;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class VrpXMLWriterTest {

    @Test
    public void whenWritingServices_itWritesThemCorrectly() {
        VehicleRoutingProblem.Builder builder = twoVehicleTypesAndImpls();

        Service s1 = Service.Builder.newInstance("1").sizeDimension(0, 1).location(TestUtils.loc("loc")).serviceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").sizeDimension(0, 1).location(TestUtils.loc("loc2")).serviceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        assertEquals(2, readVrp.jobs().size());

        Service s1_read = (Service) vrp.jobs().get("1");
        assertEquals("1", s1_read.id);
        Assert.assertEquals("loc", s1_read.location.id);
        assertEquals("service", s1_read.type);
        assertEquals(2.0, s1_read.serviceTime, 0.01);
    }

    @Test
    public void shouldWriteNameOfService() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        Service s1 = Service.Builder.newInstance("1").name("cleaning").sizeDimension(0, 1).location(TestUtils.loc("loc")).serviceTime(2.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        Service s1_read = (Service) readVrp.jobs().get("1");
        assertTrue(s1_read.name.equals("cleaning"));
    }

    @Test
    public void shouldWriteNameOfShipment() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        Location pickLocation = Location.Builder.the().setId("pick").setIndex(1).build();
        Shipment s1 = Shipment.Builder.newInstance("1").setName("cleaning")
            .setPickupLocation(pickLocation)
            .setDeliveryLocation(TestUtils.loc("del")).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        Shipment s1_read = (Shipment) readVrp.jobs().get("1");
        assertTrue(s1_read.name().equals("cleaning"));
        Assert.assertEquals(1, s1_read.getPickupLocation().index);
    }

    @Test
    public void whenWritingServicesWithSeveralCapacityDimensions_itWritesThemCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        Service s1 = Service.Builder.newInstance("1")
            .sizeDimension(0, 20)
            .sizeDimension(1, 200)
            .location(TestUtils.loc("loc")).serviceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").sizeDimension(0, 1).location(TestUtils.loc("loc2")).serviceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        assertEquals(2, readVrp.jobs().size());

        Service s1_read = (Service) vrp.jobs().get("1");

        assertEquals(2, s1_read.size.dim());
        assertEquals(20, s1_read.size.get(0));
        assertEquals(200, s1_read.size.get(1));

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationIdsOfS1() {
        VehicleRoutingProblem.Builder builder = twoVehicleTypesAndImpls();

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(Location.Builder.the().setId("pickLoc").build())
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.the(1, 2))
            .setDeliveryTimeWindow(TimeWindow.the(3, 4)).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.the().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.the(5, 6))
            .setDeliveryTimeWindow(TimeWindow.the(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        assertEquals(2, readVrp.jobs().size());

        assertEquals("pickLoc", ((Shipment) readVrp.jobs().get("1")).getPickupLocation().id);
        assertEquals("delLoc", ((Shipment) readVrp.jobs().get("1")).getDeliveryLocation().id);

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenPickupTimeWindowsOfS1() {
        VehicleRoutingProblem.Builder builder = twoVehicleTypesAndImpls();

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(Location.Builder.the().setId("pickLoc").build())
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.the(1, 2))
            .setDeliveryTimeWindow(TimeWindow.the(3, 4)).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.the().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.the(5, 6))
            .setDeliveryTimeWindow(TimeWindow.the(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        assertEquals(2, readVrp.jobs().size());

        assertEquals(1.0, ((Shipment) readVrp.jobs().get("1")).getPickupTimeWindow().start, 0.01);
        assertEquals(2.0, ((Shipment) readVrp.jobs().get("1")).getPickupTimeWindow().end, 0.01);


    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenDeliveryTimeWindowsOfS1() {
        VehicleRoutingProblem.Builder builder = twoVehicleTypesAndImpls();

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(Location.Builder.the().setId("pickLoc").build())
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.the(1, 2))
            .setDeliveryTimeWindow(TimeWindow.the(3, 4)).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.the().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.the(5, 6))
            .setDeliveryTimeWindow(TimeWindow.the(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        assertEquals(2, readVrp.jobs().size());

        assertEquals(3.0, ((Shipment) readVrp.jobs().get("1")).getDeliveryTimeWindow().start, 0.01);
        assertEquals(4.0, ((Shipment) readVrp.jobs().get("1")).getDeliveryTimeWindow().end, 0.01);

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenDeliveryServiceTimeOfS1() {
        VehicleRoutingProblem.Builder builder = twoVehicleTypesAndImpls();

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(Location.Builder.the().setId("pickLoc").build())
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.the(1, 2))
            .setDeliveryTimeWindow(TimeWindow.the(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.the().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.the(5, 6))
            .setDeliveryTimeWindow(TimeWindow.the(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        assertEquals(2, readVrp.jobs().size());

        assertEquals(100.0, ((Shipment) readVrp.jobs().get("1")).getPickupServiceTime(), 0.01);
        assertEquals(50.0, ((Shipment) readVrp.jobs().get("1")).getDeliveryServiceTime(), 0.01);

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationIdOfS1() {
        VehicleRoutingProblem.Builder builder = twoVehicleTypesAndImpls();

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(TestUtils.loc(v2.the(1, 2))).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.the(1, 2))
            .setDeliveryTimeWindow(TimeWindow.the(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.the().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.the(5, 6))
            .setDeliveryTimeWindow(TimeWindow.the(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        assertEquals(2, readVrp.jobs().size());

        assertEquals("[x=1.0][y=2.0]", ((Shipment) readVrp.jobs().get("1")).getPickupLocation().id);
    }

    @Test
    public void whenWritingVehicles_vehShouldHave3Skills() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").addSkill("SKILL5").addSkill("skill1").addSkill("Skill2")
            .setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v);

        VehicleRoutingProblem vrp = builder.build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        Vehicle veh1 = getVehicle("v1", readVrp);

        assertEquals(3, veh1.skills().values().size());
        assertTrue(veh1.skills().containsSkill("skill5"));
        assertTrue(veh1.skills().containsSkill("skill1"));
        assertTrue(veh1.skills().containsSkill("skill2"));
    }

    @Test
    public void whenWritingVehicles_vehShouldHave0Skills() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v);

        VehicleRoutingProblem vrp = builder.build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        Vehicle veh = getVehicle("v1", readVrp);

        assertEquals(0, veh.skills().values().size());
    }

    private Vehicle getVehicle(String v1, VehicleRoutingProblem readVrp) {
        for (Vehicle v : readVrp.vehicles()) {
            if (v.id().equals(v1)) return v;
        }
        return null;
    }

    @Test
    public void whenWritingShipments_shipmentShouldHaveCorrectNuSkills() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        Shipment s = Shipment.Builder.newInstance("1").addRequiredSkill("skill1").addRequiredSkill("skill2").addRequiredSkill("skill3")
            .addSizeDimension(0, 10)
            .setPickupLocation(TestUtils.loc(v2.the(1, 2)))
            .setDeliveryLocation(TestUtils.loc("delLoc", v2.the(5, 6)))
            .setPickupTimeWindow(TimeWindow.the(1, 2))
            .setDeliveryTimeWindow(TimeWindow.the(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();

        VehicleRoutingProblem vrp = builder.addJob(s).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        assertEquals(3, readVrp.jobs().get("1").skillsRequired().values().size());
        assertTrue(readVrp.jobs().get("1").skillsRequired().containsSkill("skill1"));
        assertTrue(readVrp.jobs().get("1").skillsRequired().containsSkill("skill2"));
        assertTrue(readVrp.jobs().get("1").skillsRequired().containsSkill("skill3"));
    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationCoordinateOfS1() {
        VehicleRoutingProblem.Builder builder = twoVehicleTypesAndImpls();

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10).setPickupLocation(TestUtils.loc(v2.the(1, 2)))
            .setDeliveryLocation(TestUtils.loc("delLoc", v2.the(5, 6)))
            .setPickupTimeWindow(TimeWindow.the(1, 2))
            .setDeliveryTimeWindow(TimeWindow.the(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.the().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation"))
            .setPickupTimeWindow(TimeWindow.the(5, 6))
            .setDeliveryTimeWindow(TimeWindow.the(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);
        assertEquals(2, readVrp.jobs().size());

        assertEquals(1.0, ((Shipment) readVrp.jobs().get("1")).getPickupLocation().coord.x, 0.01);
        assertEquals(2.0, ((Shipment) readVrp.jobs().get("1")).getPickupLocation().coord.y, 0.01);

        assertEquals(5.0, ((Shipment) readVrp.jobs().get("1")).getDeliveryLocation().coord.x, 0.01);
        assertEquals(6.0, ((Shipment) readVrp.jobs().get("1")).getDeliveryLocation().coord.y, 0.01);
    }

    @Test
    public void whenWritingShipmentWithSeveralCapacityDimension_itShouldWriteAndReadItCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        Shipment s1 = Shipment.Builder.newInstance("1")
            .setPickupLocation(TestUtils.loc(v2.the(1, 2)))
            .setDeliveryLocation(TestUtils.loc("delLoc", v2.the(5, 6)))
            .setPickupTimeWindow(TimeWindow.the(1, 2))
            .setDeliveryTimeWindow(TimeWindow.the(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50)
            .addSizeDimension(0, 10)
            .addSizeDimension(2, 100)
            .build();

        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.the().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.the(5, 6))
            .setDeliveryTimeWindow(TimeWindow.the(7, 8)).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        assertEquals(3, (readVrp.jobs().get("1")).size().dim());
        assertEquals(10, (readVrp.jobs().get("1")).size().get(0));
        assertEquals(0, (readVrp.jobs().get("1")).size().get(1));
        assertEquals(100, (readVrp.jobs().get("1")).size().get(2));

        assertEquals(1, (readVrp.jobs().get("2")).size().dim());
        assertEquals(20, (readVrp.jobs().get("2")).size().get(0));
    }

    @Test
    public void whenWritingVehicleV1_itsStartLocationMustBeWrittenCorrectly() {
        VehicleRoutingProblem.Builder builder = twoVehicleTypesAndImpls();

        Service s1 = Service.Builder.newInstance("1").sizeDimension(0, 1).location(TestUtils.loc("loc")).serviceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").sizeDimension(0, 1).location(TestUtils.loc("loc2")).serviceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        Vehicle v = getVehicle("v1", readVrp.vehicles());
        assertEquals("loc", v.start().id);
        assertEquals("loc", v.end().id);

    }

    private VehicleRoutingProblem.Builder twoVehicleTypesAndImpls() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.the("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);
        return builder;
    }

    @Test
    public void whenWritingService_itShouldContain_bothSkills() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        //skill names are case-insensitive
        Service s = Service.Builder.newInstance("1").skillRequired("skill1").skillRequired("SKILL2").sizeDimension(0, 1)
            .location(TestUtils.loc("loc")).serviceTime(2.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s).build();

        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        assertEquals(2, readVrp.jobs().get("1").skillsRequired().values().size());
        assertTrue(readVrp.jobs().get("1").skillsRequired().containsSkill("skill1"));
        assertTrue(readVrp.jobs().get("1").skillsRequired().containsSkill("skill2"));
    }


    @Test
    public void whenWritingVehicleV1_itDoesNotReturnToDepotMustBeWrittenCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.the("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc"))
            .setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").sizeDimension(0, 1).location(TestUtils.loc("loc")).serviceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").sizeDimension(0, 1).location(TestUtils.loc("loc2")).serviceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        Vehicle v = getVehicle("v1", readVrp.vehicles());
        assertFalse(v.isReturnToDepot());
    }

    @Test
    public void whenWritingVehicleV1_readingAgainAssignsCorrectType() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.the("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").sizeDimension(0, 1).location(TestUtils.loc("loc")).serviceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").sizeDimension(0, 1).location(TestUtils.loc("loc2")).serviceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        Vehicle v = getVehicle("v1", readVrp.vehicles());
        assertEquals("vehType", v.type().type());
    }

    @Test
    public void whenWritingVehicleV2_readingAgainAssignsCorrectType() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.the("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").sizeDimension(0, 1).location(TestUtils.loc("loc")).serviceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").sizeDimension(0, 1).location(TestUtils.loc("loc2")).serviceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        Vehicle v = getVehicle("v2", readVrp.vehicles());
        assertEquals("vehType2", v.type().type());
        assertEquals(200, v.type().getCapacityDimensions().get(0));

    }

    @Test
    public void whenWritingVehicleV2_readingItsLocationsAgainReturnsCorrectLocations() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.the("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false)
            .setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(TestUtils.loc("startLoc", com.graphhopper.jsprit.core.util.v2.the(1, 2)))
            .setEndLocation(TestUtils.loc("endLoc", com.graphhopper.jsprit.core.util.v2.the(4, 5))).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").sizeDimension(0, 1).location(TestUtils.loc("loc")).serviceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").sizeDimension(0, 1).location(TestUtils.loc("loc2")).serviceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        Vehicle v = getVehicle("v2", readVrp.vehicles());
        assertEquals("startLoc", v.start().id);
        assertEquals("endLoc", v.end().id);
        assertEquals(1.0, v.start().coord.x, 0.01);
        assertEquals(2.0, v.start().coord.y, 0.01);
        assertEquals(4.0, v.end().coord.x, 0.01);
        assertEquals(5.0, v.end().coord.y, 0.01);
    }


    @Test
    public void whenWritingVehicleWithSeveralCapacityDimensions_itShouldBeWrittenAndRereadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.the("type")
            .addCapacityDimension(0, 100)
            .addCapacityDimension(1, 1000)
            .addCapacityDimension(2, 10000)
            .build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(TestUtils.loc("startLoc", com.graphhopper.jsprit.core.util.v2.the(1, 2)))
            .setEndLocation(TestUtils.loc("endLoc", com.graphhopper.jsprit.core.util.v2.the(4, 5))).setType(type2).build();
        builder.addVehicle(v2);

        VehicleRoutingProblem vrp = builder.build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        Vehicle v = getVehicle("v", readVrp.vehicles());
        assertEquals(3, v.type().getCapacityDimensions().dim());
        assertEquals(100, v.type().getCapacityDimensions().get(0));
        assertEquals(1000, v.type().getCapacityDimensions().get(1));
        assertEquals(10000, v.type().getCapacityDimensions().get(2));
    }

    @Test
    public void whenWritingVehicleWithSeveralCapacityDimensions_itShouldBeWrittenAndRereadCorrectlyV2() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.the("type")
            .addCapacityDimension(0, 100)
            .addCapacityDimension(1, 1000)
            .addCapacityDimension(10, 10000)
            .build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(TestUtils.loc("startLoc", com.graphhopper.jsprit.core.util.v2.the(1, 2)))
            .setEndLocation(TestUtils.loc("endLoc", com.graphhopper.jsprit.core.util.v2.the(4, 5))).setType(type2).build();
        builder.addVehicle(v2);

        VehicleRoutingProblem vrp = builder.build();
        VehicleRoutingProblem readVrp = writeAndRereadXml(vrp);

        Vehicle v = getVehicle("v", readVrp.vehicles());
        assertEquals(11, v.type().getCapacityDimensions().dim());
        assertEquals(0, v.type().getCapacityDimensions().get(9));
        assertEquals(10000, v.type().getCapacityDimensions().get(10));
    }

    private Vehicle getVehicle(String string, Collection<Vehicle> vehicles) {
        for (Vehicle v : vehicles) if (string.equals(v.id())) return v;
        return null;
    }

    @Test
    public void solutionWithoutUnassignedJobsShouldBeWrittenCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v1);

        Service s1 = Service.Builder.newInstance("1").sizeDimension(0, 1).location(TestUtils.loc("loc")).serviceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").sizeDimension(0, 1).location(TestUtils.loc("loc2")).serviceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v1).addService(s1).addService(s2).build();
        List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
        routes.add(route);
        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, 10.);
        List<VehicleRoutingProblemSolution> solutions = new ArrayList<VehicleRoutingProblemSolution>();
        solutions.add(solution);

        List<VehicleRoutingProblemSolution> solutionsToRead = writeAndRereadXmlWithSolutions(vrp, solutions);

        assertEquals(1, solutionsToRead.size());
        assertEquals(10., Solutions.bestOf(solutionsToRead).cost(), 0.01);
        assertTrue(Solutions.bestOf(solutionsToRead).jobsUnassigned.isEmpty());
    }

    @Test
    public void solutionWithUnassignedJobsShouldBeWrittenCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v1);

        Service s1 = Service.Builder.newInstance("1").sizeDimension(0, 1).location(TestUtils.loc("loc")).serviceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").sizeDimension(0, 1).location(TestUtils.loc("loc2")).serviceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v1).addService(s1).build();
        List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
        routes.add(route);
        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, 10.);
        solution.jobsUnassigned.add(s2);
        List<VehicleRoutingProblemSolution> solutions = new ArrayList<VehicleRoutingProblemSolution>();
        solutions.add(solution);

        List<VehicleRoutingProblemSolution> solutionsToRead = writeAndRereadXmlWithSolutions(vrp, solutions);

        assertEquals(1, solutionsToRead.size());
        assertEquals(10., Solutions.bestOf(solutionsToRead).cost(), 0.01);
        assertEquals(1, Solutions.bestOf(solutionsToRead).jobsUnassigned.size());
        assertEquals("2", Solutions.bestOf(solutionsToRead).jobsUnassigned.iterator().next().id());
    }

    @Test
    public void outputStreamAndFileContentsAreEqual() {
        VehicleRoutingProblem.Builder builder = twoVehicleTypesAndImpls();
        VehicleRoutingProblem vrp = builder.build();

        VrpXMLWriter vrpXMLWriter = new VrpXMLWriter(vrp, null);
        ByteArrayOutputStream os = (ByteArrayOutputStream) vrpXMLWriter.write();

        String outputStringFromFile = new String(os.toByteArray());
        String outputStringFromStream = new VrpXMLWriter(vrp, null).write().toString();

        assertEquals(outputStringFromFile, outputStringFromStream);

    }

    private VehicleRoutingProblem writeAndRereadXml(VehicleRoutingProblem vrp) {
        VrpXMLWriter vrpXMLWriter = new VrpXMLWriter(vrp, null);
        ByteArrayOutputStream os = (ByteArrayOutputStream) vrpXMLWriter.write();
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(vrpToReadBuilder, null).read(is);
        return vrpToReadBuilder.build();
    }

    private List<VehicleRoutingProblemSolution> writeAndRereadXmlWithSolutions(VehicleRoutingProblem vrp, List<VehicleRoutingProblemSolution> solutions) {
        VrpXMLWriter vrpXMLWriter = new VrpXMLWriter(vrp, solutions);
        ByteArrayOutputStream os = (ByteArrayOutputStream) vrpXMLWriter.write();
        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.get();
        List<VehicleRoutingProblemSolution> solutionsToRead = new ArrayList<VehicleRoutingProblemSolution>();
        new VrpXMLReader(vrpToReadBuilder, solutionsToRead).read(is);
        return solutionsToRead;
    }

}

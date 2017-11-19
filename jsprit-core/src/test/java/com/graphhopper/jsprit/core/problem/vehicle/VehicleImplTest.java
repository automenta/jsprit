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


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class VehicleImplTest {


    @Test(expected = IllegalArgumentException.class)
    public void whenVehicleIsBuiltWithoutSettingNeitherLocationNorCoord_itThrowsAnIllegalStateException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").build();
    }


    @Test
    public void whenAddingDriverBreak_itShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("type").build();
        Break aBreak = Break.Builder.newInstance("break").timeWindowSet(TimeWindow.the(100, 200)).serviceTime(30).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start"))
            .setType(type1).setEndLocation(Location.the("start"))
            .setBreak(aBreak).build();
        assertNotNull(v.aBreak());
        assertEquals(100., v.aBreak().timeWindow().start, 0.1);
        assertEquals(200., v.aBreak().timeWindow().end, 0.1);
        assertEquals(30., v.aBreak().serviceTime, 0.1);
    }


    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("type").build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setType(type1).setEndLocation(Location.the("start"))
            .addSkill("drill").addSkill("screwdriver").build();
        assertTrue(v.skills().containsSkill("drill"));
        assertTrue(v.skills().containsSkill("drill"));
        assertTrue(v.skills().containsSkill("screwdriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("type").build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setType(type1).setEndLocation(Location.the("start"))
            .addSkill("drill").addSkill("screwdriver").build();
        assertTrue(v.skills().containsSkill("drill"));
        assertTrue(v.skills().containsSkill("dRill"));
        assertTrue(v.skills().containsSkill("ScrewDriver"));
    }


    @Test
    public void whenVehicleIsBuiltToReturnToDepot_itShouldReturnToDepot() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(true).setStartLocation(Location.the("loc")).build();
        assertTrue(v.isReturnToDepot());
    }

    @Test
    public void whenVehicleIsBuiltToNotReturnToDepot_itShouldNotReturnToDepot() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(false).setStartLocation(Location.the("loc")).build();
        assertFalse(v.isReturnToDepot());
    }

    @Test
    public void whenVehicleIsBuiltWithLocation_itShouldHvTheCorrectLocation() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("loc")).build();
        assertEquals("loc", v.start().id);
    }

    @Test
    public void whenVehicleIsBuiltWithCoord_itShouldHvTheCorrectCoord() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1, 2)).build();
        assertEquals(1.0, v.start().coord.x, 0.01);
        assertEquals(2.0, v.start().coord.y, 0.01);
    }

    @Test
    public void whenVehicleIsBuiltAndEarliestStartIsNotSet_itShouldSetTheDefaultOfZero() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1, 2)).build();
        assertEquals(0.0, v.earliestDeparture(), 0.01);
    }

    @Test
    public void whenVehicleIsBuiltAndEarliestStartSet_itShouldBeSetCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setEarliestStart(10.0).setStartLocation(Location.the(1, 2)).build();
        assertEquals(10.0, v.earliestDeparture(), 0.01);
    }

    @Test
    public void whenVehicleIsBuiltAndLatestArrivalIsNotSet_itShouldSetDefaultOfDoubleMaxValue() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1, 2)).build();
        assertEquals(Double.MAX_VALUE, v.latestArrival(), 0.01);
    }

    @Test
    public void whenVehicleIsBuiltAndLatestArrivalIsSet_itShouldBeSetCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setLatestArrival(30.0).setStartLocation(Location.the(1, 2)).build();
        assertEquals(30.0, v.latestArrival(), 0.01);
    }

    @Test
    public void whenNoVehicleIsCreate_itShouldHvTheCorrectId() {
        Vehicle v = VehicleImpl.get();
        assertEquals("noVehicle", v.id());
    }

    @Test
    public void whenStartLocationIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("startLoc")).build();
        assertEquals("startLoc", v.start().id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenStartLocationIsNull_itThrowsException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(null)).build();
    }

    @Test
    public void whenStartLocationCoordIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1, 2)).build();
        assertEquals(1.0, v.start().coord.x, 0.01);
        assertEquals(2.0, v.start().coord.y, 0.01);
    }

    @Test
    public void whenEndLocationIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("startLoc")).setEndLocation(Location.the("endLoc")).build();
        assertEquals("startLoc", v.start().id);
        assertEquals("endLoc", v.end().id);
    }

    @Test
    public void whenEndLocationCoordIsSet_itIsDoneCorrectly() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("startLoc")).setEndLocation(Location.the(1, 2)).build();
        assertEquals(1.0, v.end().coord.x, 0.01);
        assertEquals(2.0, v.end().coord.y, 0.01);
    }


    @Test
    public void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationIdMustBeEqualToStartLocationId() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("startLoc")).build();
        assertEquals("startLoc", v.end().id);
    }

    @Test
    public void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationCoordMustBeEqualToStartLocationCoord() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("startLoc")).build();
        assertEquals(v.end().coord, v.start().coord);
    }

    @Test
    public void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationCoordMustBeEqualToStartLocationCoordV2() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1.0, 2.0)).build();
        assertEquals(v.end().coord, v.start().coord);
    }

    @Test
    public void whenEndLocationCoordinateIsSetButNoId_idMustBeCoordToString() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1.0, 2.0)).setEndLocation(Location.the(3.0, 4.0)).build();
        assertEquals(v.end().coord.toString(), v.end().id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenEndLocationIdIsSpecifiedANDReturnToDepotIsFalse_itShouldThrowException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1.0, 2.0)).setEndLocation(Location.the("endLoc")).setReturnToDepot(false).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenEndLocationCoordIsSpecifiedANDReturnToDepotIsFalse_itShouldThrowException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1.0, 2.0)).setEndLocation(Location.the(3, 4)).setReturnToDepot(false).build();
    }

    @Test
    public void whenEndLocationCoordIsNotSpecifiedANDReturnToDepotIsFalse_endLocationCoordMustBeStartLocationCoord() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1.0, 2.0)).setReturnToDepot(false).build();
        assertEquals(v.start().coord, v.end().coord);
    }

    @Test
    public void whenEndLocationIdIsNotSpecifiedANDReturnToDepotIsFalse_endLocationIdMustBeStartLocationId() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the(1.0, 2.0)).setReturnToDepot(false).build();
        assertEquals(v.start().coord.toString(), v.end().id);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenStartAndEndAreUnequalANDReturnToDepotIsFalse_itShouldThrowException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("end")).setReturnToDepot(false).build();
    }

    @Test
    public void whenStartAndEndAreEqualANDReturnToDepotIsFalse_itShouldThrowException() {
        @SuppressWarnings("unused")
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("start")).setReturnToDepot(false).build();
        assertTrue(true);
    }

    @Test
    public void whenTwoVehiclesHaveTheSameId_theyShouldBeEqual() {
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("start")).setReturnToDepot(false).build();
        Vehicle v2 = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setEndLocation(Location.the("start")).setReturnToDepot(false).build();
        assertTrue(v.equals(v2));
    }


    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("type").build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.the("start")).setType(type1).setEndLocation(Location.the("start"))
            .addSkill("drill").build();
        assertFalse(v.skills().containsSkill("ScrewDriver"));
    }

    @Test
    public void whenSettingUserData_itIsAssociatedWithTheVehicle() {
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.the("type").build();
        Vehicle one = VehicleImpl.Builder.newInstance("v").setType(type1)
            .setStartLocation(Location.the("start")).setUserData(new HashMap<String, Object>()).build();
        Vehicle two = VehicleImpl.Builder.newInstance("v").setType(type1)
            .setStartLocation(Location.the("start")).setUserData(42).build();
        Vehicle three = VehicleImpl.Builder.newInstance("v").setType(type1)
            .setStartLocation(Location.the("start")).build();

        assertTrue(one.data() instanceof Map);
        assertEquals(42, two.data());
        assertNull(three.data());
    }

}

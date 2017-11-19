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
package com.graphhopper.jsprit.core.problem.job;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.*;

public class ServiceTest {

    @Test
    public void whenTwoServicesHaveTheSameId_theirReferencesShouldBeUnEqual() {
        Service one = Service.Builder.newInstance("service").sizeDimension(0, 10).location(Location.the("foo")).build();
        Service two = Service.Builder.newInstance("service").sizeDimension(0, 10).location(Location.the("fo")).build();

        assertTrue(one != two);
    }

    @Test
    public void whenTwoServicesHaveTheSameId_theyShouldBeEqual() {
        Service one = Service.Builder.newInstance("service").sizeDimension(0, 10).location(Location.the("foo")).build();
        Service two = Service.Builder.newInstance("service").sizeDimension(0, 10).location(Location.the("fo")).build();

        assertTrue(one.equals(two));
    }

    @Test
    public void noName() {
        Set<Service> serviceSet = new HashSet<Service>();
        Service one = Service.Builder.newInstance("service").sizeDimension(0, 10).location(Location.the("foo")).build();
        Service two = Service.Builder.newInstance("service").sizeDimension(0, 10).location(Location.the("fo")).build();
        serviceSet.add(one);
        //		assertTrue(serviceSet.contains(two));
        serviceSet.remove(two);
        assertTrue(serviceSet.isEmpty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenCapacityDimValueIsNegative_throwIllegalStateExpception() {
        @SuppressWarnings("unused")
        Service s = Service.Builder.newInstance("s").location(Location.the("foo")).sizeDimension(0, -10).build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        Service one = Service.Builder.newInstance("s").location(Location.the("foofoo"))
            .sizeDimension(0, 2)
            .sizeDimension(1, 4)
            .build();
        assertEquals(2, one.size.dim());
    }

    @Test
    public void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        Service one = Service.Builder.newInstance("s").location(Location.the("foofoo"))
            .build();
        assertEquals(1, one.size.dim());
        assertEquals(0, one.size.get(0));
    }

    @Test
    public void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        Service one = Service.Builder.newInstance("s").sizeDimension(0, 1).location(Location.the("foofoo"))
            .build();
        assertEquals(1, one.size.dim());
        assertEquals(1, one.size.get(0));
    }

    @Test
    public void whenCallingForNewInstanceOfBuilder_itShouldReturnBuilderCorrectly() {
        Service.Builder builder = Service.Builder.newInstance("s");
        assertNotNull(builder);
    }

    @Test
    public void whenSettingNoType_itShouldReturn_service() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc")).build();
        assertEquals("service", s.type);
    }

    @Test
    public void whenSettingLocation_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc")).build();
        assertEquals("loc", s.location.id);
        assertEquals("loc", s.location.id);
    }

    @Test
    public void whenSettingLocation_itShouldWork() {
        Service s = Service.Builder.newInstance("s").location(Location.Builder.the().setId("loc").build()).build();
        assertEquals("loc", s.location.id);
        assertEquals("loc", s.location.id);
    }


    @Test
    public void whenSettingLocationCoord_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").location(Location.the(1, 2)).build();
        assertEquals(1.0, s.location.coord.x, 0.01);
        assertEquals(2.0, s.location.coord.y, 0.01);
        assertEquals(1.0, s.location.coord.x,0.01);
        assertEquals(2.0, s.location.coord.y,0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingNeitherLocationIdNorCoord_throwsException() {
        @SuppressWarnings("unused")
        Service s = Service.Builder.newInstance("s").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenServiceTimeSmallerZero_throwIllegalStateException() {
        @SuppressWarnings("unused")
        Service s = Service.Builder.newInstance("s").location(Location.the("loc")).serviceTime(-1).build();
    }

    @Test
    public void whenSettingServiceTime_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc")).serviceTime(1).build();
        assertEquals(1.0, s.serviceTime, 0.01);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenTimeWindowIsNull_throwException() {
        @SuppressWarnings("unused")
        Service s = Service.Builder.newInstance("s").location(Location.the("loc")).timeWindowSet(null).build();
    }

    @Test
    public void whenSettingTimeWindow_itShouldBeSetCorrectly() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc")).timeWindowSet(TimeWindow.the(1.0, 2.0)).build();
        assertEquals(1.0, s.timeWindow().start, 0.01);
        assertEquals(2.0, s.timeWindow().end, 0.01);
    }

    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .skillRequired("drill").skillRequired("screwdriver").build();
        assertTrue(s.skills.containsSkill("drill"));
        assertTrue(s.skills.containsSkill("drill"));
        assertTrue(s.skills.containsSkill("ScrewDriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .skillRequired("DriLl").skillRequired("screwDriver").build();
        assertTrue(s.skills.containsSkill("drill"));
        assertTrue(s.skills.containsSkill("drilL"));
    }

    @Test
    public void whenAddingSeveralTimeWindows_itShouldBeSetCorrectly(){
        TimeWindow tw1 = TimeWindow.the(1.0, 2.0);
        TimeWindow tw2 = TimeWindow.the(3.0, 5.0);
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .timeWindowAdd(tw1)
            .timeWindowAdd(tw2)
            .build();
        assertEquals(2, s.timeWindows.size());
        assertThat(s.timeWindows,hasItem(is(tw1)));
        assertThat(s.timeWindows,hasItem(is(tw2)));
    }

    @Test
    public void whenAddingTimeWindow_itShouldBeSetCorrectly(){
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .timeWindowAdd(TimeWindow.the(1.0, 2.0)).build();
        assertEquals(1.0, s.timeWindow().start, 0.01);
        assertEquals(2.0, s.timeWindow().end, 0.01);
    }




    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .skillRequired("screwDriver").build();
        assertFalse(s.skills.containsSkill("drill"));
        assertFalse(s.skills.containsSkill("drilL"));
    }

    @Test
    public void nameShouldBeAssigned() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .name("name").build();
        assertEquals("name", s.name);
    }

    @Test
    public void shouldKnowMultipleTimeWindows() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .timeWindowAdd(TimeWindow.the(0., 10.)).timeWindowAdd(TimeWindow.the(20., 30.))
            .name("name").build();
        assertEquals(2, s.timeWindows.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenMultipleTWOverlap_throwEx() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .timeWindowAdd(TimeWindow.the(0., 10.))
            .timeWindowAdd(TimeWindow.the(5., 30.))
            .name("name").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenMultipleTWOverlap2_throwEx() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .timeWindowAdd(TimeWindow.the(20., 30.))
            .timeWindowAdd(TimeWindow.the(0., 25.))
            .name("name").build();
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly(){
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .setPriority(1).build();
        Assert.assertEquals(1, s.priority);
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly2(){
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .setPriority(3).build();
        Assert.assertEquals(3, s.priority);
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly3() {
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .setPriority(10).build();
        Assert.assertEquals(10, s.priority);
    }

    @Test
    public void whenNotSettingPriorities_defaultShouldBe2(){
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .build();
        Assert.assertEquals(2, s.priority);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException(){
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .setPriority(30).build();

    }

    @Test(expected = IllegalArgumentException.class)
    public void whenSettingIncorrectPriorities_itShouldThrowException2(){
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .setPriority(0).build();

    }

    @Test(expected = UnsupportedOperationException.class)
    public void whenAddingMaxTimeInVehicle_itShouldThrowEx(){
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .setMaxTimeInVehicle(10)
            .build();
    }

    @Test
    public void whenNotAddingMaxTimeInVehicle_itShouldBeDefault(){
        Service s = Service.Builder.newInstance("s").location(Location.the("loc"))
            .build();
        Assert.assertEquals(Double.MAX_VALUE, s.maxTimeInVehicle,0.001);
    }


    @Test
    public void whenSettingUserData_itIsAssociatedWithTheJob() {
        Service one = Service.Builder.newInstance("s").location(Location.the("loc"))
            .userData(new HashMap<String, Object>()).build();
        Service two = Service.Builder.newInstance("s2").location(Location.the("loc")).userData(42)
            .build();
        Service three = Service.Builder.newInstance("s3").location(Location.the("loc")).build();

        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }
}

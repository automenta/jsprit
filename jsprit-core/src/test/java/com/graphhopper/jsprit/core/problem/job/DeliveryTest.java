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
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class DeliveryTest {

    @Test(expected = IllegalArgumentException.class)
    public void whenNeitherLocationIdNorCoordIsSet_itThrowsException() {
        Delivery.Builder.newInstance("p").build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        Delivery one = Delivery.Builder.newInstance("s").location(Location.the("foofoo"))
            .sizeDimension(0, 2)
            .sizeDimension(1, 4)
            .build();
        assertEquals(2, one.size.dim());
        assertEquals(2, one.size.get(0));
        assertEquals(4, one.size.get(1));

    }

    @Test
    public void whenPickupIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        Delivery one = Delivery.Builder.newInstance("s").location(Location.the("foofoo"))
            .build();
        assertEquals(1, one.size.dim());
        assertEquals(0, one.size.get(0));
    }

    @Test
    public void whenPickupIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        Delivery one = Delivery.Builder.newInstance("s").sizeDimension(0, 1).location(Location.the("foofoo"))
            .build();
        assertEquals(1, one.size.dim());
        assertEquals(1, one.size.get(0));
    }

    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        Delivery s = Delivery.Builder.newInstance("s").location(Location.the("loc"))
            .skillRequired("drill").skillRequired("screwdriver").build();
        assertTrue(s.skills.containsSkill("drill"));
        assertTrue(s.skills.containsSkill("ScrewDriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        Delivery s = Delivery.Builder.newInstance("s").location(Location.the("loc"))
            .skillRequired("DriLl").skillRequired("screwDriver").build();
        assertTrue(s.skills.containsSkill("drill"));
        assertTrue(s.skills.containsSkill("drilL"));
    }

    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        Delivery s = Delivery.Builder.newInstance("s").location(Location.the("loc"))
            .skillRequired("screwDriver").build();
        assertFalse(s.skills.containsSkill("drill"));
        assertFalse(s.skills.containsSkill("drilL"));
    }

    @Test
    public void nameShouldBeAssigned() {
        Delivery s = Delivery.Builder.newInstance("s").location(Location.the("loc"))
            .name("name").build();
        assertEquals("name", s.name);
    }

    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly(){
        Delivery s = Delivery.Builder.newInstance("s").location(Location.the("loc"))
            .setPriority(3).build();
        Assert.assertEquals(3, s.priority);
    }

    @Test
    public void whenNotSettingPriorities_defaultShouldBe(){
        Delivery s = Delivery.Builder.newInstance("s").location(Location.the("loc"))
            .build();
        Assert.assertEquals(2, s.priority);
    }

    @Test
    public void whenAddingMaxTimeInVehicle_itShouldBeSet(){
        Delivery s = Delivery.Builder.newInstance("s").location(Location.the("loc"))
            .setMaxTimeInVehicle(10)
            .build();
        Assert.assertEquals(10, s.maxTimeInVehicle,0.001);
    }

    @Test
    public void whenNotAddingMaxTimeInVehicle_itShouldBeDefault(){
        Delivery s = Delivery.Builder.newInstance("s").location(Location.the("loc"))
            .build();
        Assert.assertEquals(Double.MAX_VALUE, s.maxTimeInVehicle,0.001);
    }


    @Test
    public void whenSettingUserData_itIsAssociatedWithTheJob() {
        Delivery one = Delivery.Builder.newInstance("s").location(Location.the("loc"))
            .userData(new HashMap<String, Object>()).build();
        Delivery two = Delivery.Builder.newInstance("s2").location(Location.the("loc")).userData(42)
            .build();
        Delivery three = Delivery.Builder.newInstance("s3").location(Location.the("loc")).build();

        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }
}

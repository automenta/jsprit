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

public class PickupTest {

    @Test(expected = IllegalArgumentException.class)
    public void whenNeitherLocationIdNorCoordIsSet_itThrowsException() {
        Pickup.Builder.the("p").build();
    }

    @Test
    public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo() {
        Pickup one = Pickup.Builder.the("s").location(Location.the("foofoo"))
            .sizeDimension(0, 2)
            .sizeDimension(1, 4)
            .build();
        assertEquals(2, one.size.dim());
        assertEquals(2, one.size.get(0));
        assertEquals(4, one.size.get(1));

    }

    @Test
    public void whenPickupIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero() {
        Pickup one = Pickup.Builder.the("s").location(Location.the("foofoo"))
            .build();
        assertEquals(1, one.size.dim());
        assertEquals(0, one.size.get(0));
    }

    @Test
    public void whenPickupIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly() {
        Pickup one = Pickup.Builder.the("s").sizeDimension(0, 1).location(Location.the("foofoo"))
            .build();
        assertEquals(1, one.size.dim());
        assertEquals(1, one.size.get(0));
    }

    @Test
    public void whenAddingSkills_theyShouldBeAddedCorrectly() {
        Pickup s = Pickup.Builder.the("s").location(Location.the("loc"))
            .skillRequired("drill").skillRequired("screwdriver").build();
        assertTrue(s.skills.containsSkill("drill"));
        assertTrue(s.skills.containsSkill("drill"));
        assertTrue(s.skills.containsSkill("ScrewDriver"));
    }

    @Test
    public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly() {
        Pickup s = Pickup.Builder.the("s").location(Location.the("loc"))
            .skillRequired("DriLl").skillRequired("screwDriver").build();
        assertTrue(s.skills.containsSkill("drill"));
        assertTrue(s.skills.containsSkill("drilL"));
    }

    @Test
    public void whenAddingSkillsCaseSensV2_theyShouldBeAddedCorrectly() {
        Pickup s = Pickup.Builder.the("s").location(Location.the("loc"))
            .skillRequired("screwDriver").build();
        assertFalse(s.skills.containsSkill("drill"));
        assertFalse(s.skills.containsSkill("drilL"));
    }

    @Test
    public void nameShouldBeAssigned() {
        Pickup s = Pickup.Builder.the("s").location(Location.the("loc"))
            .name("name").build();
        assertEquals("name", s.name);
    }


    @Test
    public void whenSettingPriorities_itShouldBeSetCorrectly(){
        Pickup s = Pickup.Builder.the("s").location(Location.the("loc"))
            .setPriority(3).build();
        Assert.assertEquals(3, s.priority);
    }

    @Test
    public void whenNotSettingPriorities_defaultShouldBe(){
        Pickup s = Pickup.Builder.the("s").location(Location.the("loc"))
            .build();
        Assert.assertEquals(2, s.priority);
    }

    @Test
    public void whenSettingUserData_itIsAssociatedWithTheJob() {
        Pickup one = Pickup.Builder.the("s").location(Location.the("loc"))
            .userData(new HashMap<String, Object>()).build();
        Pickup two = Pickup.Builder.the("s2").location(Location.the("loc")).userData(42).build();
        Pickup three = Pickup.Builder.the("s3").location(Location.the("loc")).build();

        assertTrue(one.getUserData() instanceof Map);
        assertEquals(42, two.getUserData());
        assertNull(three.getUserData());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void whenAddingMaxTimeInVehicle_itShouldThrowEx(){
        Pickup s = Pickup.Builder.the("s").location(Location.the("loc"))
            .setMaxTimeInVehicle(10)
            .build();
    }

    @Test
    public void whenNotAddingMaxTimeInVehicle_itShouldBeDefault(){
        Pickup s = Pickup.Builder.the("s").location(Location.the("loc"))
            .build();
        Assert.assertEquals(Double.MAX_VALUE, s.maxTimeInVehicle,0.001);
    }

}

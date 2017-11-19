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

package com.graphhopper.jsprit.core.problem;

import com.graphhopper.jsprit.core.util.v2;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by schroeder on 16.12.14.
 */
public class LocationTest {

    @Test
    public void whenIndexSet_buildLocation() {
        Location l = Location.Builder.the().setIndex(1).build();
        Assert.assertEquals(1, l.index);
        Assert.assertTrue(true);
    }

    @Test
    public void whenNameSet_buildLocation() {
        Location l = Location.Builder.the().setName("mystreet 6a").setIndex(1).build();
        Assert.assertEquals("mystreet 6a", l.name());
    }

    @Test
    public void whenIndexSetWitFactory_returnCorrectLocation() {
        Location l = Location.the(1);
        Assert.assertEquals(1, l.index);
        Assert.assertTrue(true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenIndexSmallerZero_throwException() {
        Location l = Location.Builder.the().setIndex(-1).build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenCoordinateAndIdAndIndexNotSet_throwException() {
        Location l = Location.Builder.the().build();
    }

    @Test
    public void whenIdSet_build() {
        Location l = Location.Builder.the().setId("id").build();
        Assert.assertEquals("id", l.id);
        Assert.assertTrue(true);
    }

    @Test
    public void whenIdSetWithFactory_returnCorrectLocation() {
        Location l = Location.the("id");
        Assert.assertEquals("id", l.id);
        Assert.assertTrue(true);
    }

    @Test
    public void whenCoordinateSet_build() {
        Location l = Location.Builder.the().setCoord(v2.the(10, 20)).build();
        Assert.assertEquals(10., l.coord.x, 0.001);
        Assert.assertEquals(20., l.coord.y, 0.001);
        Assert.assertTrue(true);
    }

    @Test
    public void whenCoordinateSetWithFactory_returnCorrectLocation() {
        //        Location l = Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10,20)).build();
        Location l = Location.the(10, 20);
        Assert.assertEquals(10., l.coord.x, 0.001);
        Assert.assertEquals(20., l.coord.y, 0.001);
        Assert.assertTrue(true);
    }


    @Test
    public void whenSettingUserData_itIsAssociatedWithTheLocation() {
        Location one = Location.Builder.the().setCoord(v2.the(10, 20))
            .setData(new HashMap<String, Object>()).build();
        Location two = Location.Builder.the().setIndex(1).setData(42).build();
        Location three = Location.Builder.the().setIndex(2).build();

        assertTrue(one.data instanceof Map);
        assertEquals(42, two.data);
        assertNull(three.data);
    }

}

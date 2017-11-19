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

package com.graphhopper.jsprit.core.problem.solution.route.activity;

import org.junit.Test;

/**
 * Created by schroeder on 18/12/15.
 */
public class TimeWindowsTest {

    @Test(expected = IllegalArgumentException.class)
    public void overlappingTW_shouldThrowException(){
        TimeWindows tws = new TimeWindows();
        tws.add(TimeWindow.the(50, 100));
        tws.add(TimeWindow.the(90,150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void overlappingTW2_shouldThrowException(){
        TimeWindows tws = new TimeWindows();
        tws.add(TimeWindow.the(50, 100));
        tws.add(TimeWindow.the(40,150));
    }

    @Test(expected = IllegalArgumentException.class)
    public void overlappingTW3_shouldThrowException(){
        TimeWindows tws = new TimeWindows();
        tws.add(TimeWindow.the(50, 100));
        tws.add(TimeWindow.the(50, 100));
    }
}

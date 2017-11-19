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

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by schroeder on 26/05/15.
 */
public class TimeWindows extends ArrayList<TimeWindow>  {

    public TimeWindows() {
        super(1);
    }

    public TimeWindows(TimeWindow... x) {
        super(x.length);
        Collections.addAll(this, x);
    }

    //public final Collection<TimeWindow> timeWindows = new ArrayList<TimeWindow>();

    @Override
    public boolean add(TimeWindow timeWindow){
        this.forEach(tw -> {
            double tws = tw.start;
            double TWS = timeWindow.start;
            double twe = tw.end;
            if (TWS > tws && TWS < twe) {
                throw new IllegalArgumentException("time-windows cannot overlap each other. overlap: " + tw + ", " + timeWindow);
            }
            double TWE = timeWindow.end;
            if (TWE > tws && TWE < twe) {
                throw new IllegalArgumentException("time-windows cannot overlap each other. overlap: " + tw + ", " + timeWindow);
            }
            if (TWS <= tws && TWE >= twe) {
                throw new IllegalArgumentException("time-windows cannot overlap each other. overlap: " + tw + ", " + timeWindow);
            }
        });
        return super.add(timeWindow);
    }

    @Override
    public String toString() {
        int size = this.size();
        StringBuilder sb = new StringBuilder(size * 32);
        for (int i = 0; i < size; i++) {
            sb.append("[timeWindow=").append(this.get(i)).append(']');
        }
        return sb.toString();
    }
}

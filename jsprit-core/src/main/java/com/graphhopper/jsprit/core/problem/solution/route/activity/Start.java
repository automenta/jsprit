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

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Indexed;
import com.graphhopper.jsprit.core.problem.Location;

public final class Start extends AbstractActivity implements Indexed {

    @Deprecated
    public final static String ACTIVITY_NAME = "start";

    private final static Capacity capacity = Capacity.Builder.get().build();

    public static Start newInstance(String locationId, double theoreticalStart, double theoreticalEnd) {
        return new Start(locationId, theoreticalStart, theoreticalEnd);
    }

    public static Start copyOf(Start start) {
        return new Start(start);
    }

//    private String locationId;

    private double theoretical_earliestOperationStartTime;

    private double theoretical_latestOperationStartTime;

    private double endTime;

    private double arrTime;

    private Location location;

    private Start(String locationId, double theoreticalStart, double theoreticalEnd) {
        if (locationId != null) this.location = Location.Builder.the().setId(locationId).build();
        this.theoretical_earliestOperationStartTime = theoreticalStart;
        this.theoretical_latestOperationStartTime = theoreticalEnd;
        this.endTime = theoreticalStart;
        index(-1);
    }

    public Start(Location location, double theoreticalStart, double theoreticalEnd) {
        this.location = location;
        this.theoretical_earliestOperationStartTime = theoreticalStart;
        this.theoretical_latestOperationStartTime = theoreticalEnd;
        this.endTime = theoreticalStart;
        index(-1);
    }

    private Start(Start start) {
        this.location = start.location();
        theoretical_earliestOperationStartTime = start.startEarliest();
        theoretical_latestOperationStartTime = start.startLatest();
        endTime = start.end();
        index(-1);
    }

    @Override
    public double startEarliest() {
        return theoretical_earliestOperationStartTime;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public double startLatest() {
        return theoretical_latestOperationStartTime;
    }


    @Override
    public void startEarliest(double time) {
        this.theoretical_earliestOperationStartTime = time;
    }

    @Override
    public void startLatest(double time) {
        this.theoretical_latestOperationStartTime = time;
    }

    @Override
    public Location location() {
        return location;
    }

    @Override
    public double operationTime() {
        return 0.0;
    }

    @Override
    public String toString() {
        return "[type=" + name() + "][location=" + location
            + "][twStart=" + Activities.round(theoretical_earliestOperationStartTime)
            + "][twEnd=" + Activities.round(theoretical_latestOperationStartTime) + ']';
    }

    @Override
    public String name() {
        return "start";
    }

    @Override
    public double arrTime() {
        return arrTime;
    }

    @Override
    public double end() {
        return endTime;
    }

    @Override
    public void arrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    @Override
    public void end(double endTime) {
        this.endTime = endTime;
    }

    @Override
    public Start clone() {
        return new Start(this);
    }

    @Override
    public Capacity size() {
        return capacity;
    }

}

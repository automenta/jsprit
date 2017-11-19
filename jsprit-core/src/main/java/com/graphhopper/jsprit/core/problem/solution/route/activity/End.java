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

public final class End extends AbstractActivity implements Indexed {

    public static End the(String locationId, double earliestArrival, double latestArrival) {
        return new End(locationId, earliestArrival, latestArrival);
    }

    public static End copyOf(End end) {
        return new End(end);
    }

    private final static Capacity capacity = Capacity.Builder.get().build();


    private double endTime = -1;


    private double theoretical_earliestOperationStartTime;

    private double theoretical_latestOperationStartTime;

    private double arrTime;

    private Location location;

    @Override
    public void startEarliest(double theoreticalEarliestOperationStartTime) {
        theoretical_earliestOperationStartTime = theoreticalEarliestOperationStartTime;
    }

    @Override
    public void startLatest(double theoreticalLatestOperationStartTime) {
        theoretical_latestOperationStartTime = theoreticalLatestOperationStartTime;
    }

    public End(Location location, double theoreticalStart, double theoreticalEnd) {
        this.location = location;
        theoretical_earliestOperationStartTime = theoreticalStart;
        theoretical_latestOperationStartTime = theoreticalEnd;
        endTime = theoreticalEnd;
        index(-2);
    }

    public End(String locationId, double theoreticalStart, double theoreticalEnd) {
        if (locationId != null) this.location = Location.Builder.the().setId(locationId).build();
        theoretical_earliestOperationStartTime = theoreticalStart;
        theoretical_latestOperationStartTime = theoreticalEnd;
        endTime = theoreticalEnd;
        index(-2);
    }

    public End(End end) {
        this.location = end.location();
//		this.locationId = end.getLocation().getId();
        theoretical_earliestOperationStartTime = end.startEarliest();
        theoretical_latestOperationStartTime = end.startLatest();
        arrTime = end.arrTime();
        endTime = end.end();
        index(-2);
    }

    @Override
    public double startEarliest() {
        return theoretical_earliestOperationStartTime;
    }

    @Override
    public double startLatest() {
        return theoretical_latestOperationStartTime;
    }

    @Override
    public double end() {
        return endTime;
    }

    @Override
    public void end(double endTime) {
        this.endTime = endTime;
    }

    public void location(Location location) {
        this.location = location;
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
        return "end";
    }

    @Override
    public double arrTime() {
        return this.arrTime;
    }

    @Override
    public void arrTime(double arrTime) {
        this.arrTime = arrTime;

    }

    @Override
    public AbstractActivity clone() {
        return new End(this);
    }

    @Override
    public Capacity size() {
        return capacity;
    }

}

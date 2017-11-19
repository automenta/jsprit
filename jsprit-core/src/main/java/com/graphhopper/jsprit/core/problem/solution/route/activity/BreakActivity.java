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

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Service;

public class BreakActivity extends JobActivity {

    public static int counter;

    public double arrTime;

    public double endTime;

    private Location location;

    private double duration;

    /**
     * @return the arrTime
     */
    @Override
    public double arrTime() {
        return arrTime;
    }

    /**
     * @param arrTime the arrTime to set
     */
    @Override
    public void arrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    /**
     * @return the endTime
     */
    @Override
    public double end() {
        return endTime;
    }

    /**
     * @param endTime the endTime to set
     */
    @Override
    public void end(double endTime) {
        this.endTime = endTime;
    }

    public static BreakActivity copyOf(BreakActivity breakActivity) {
        return new BreakActivity(breakActivity);
    }

    public static BreakActivity the(Break aBreak) {
        return new BreakActivity(aBreak);
    }

    private final Break aBreak;

    private double earliest;

    private double latest = Double.MAX_VALUE;

    protected BreakActivity(Break aBreak) {
        counter++;
        this.aBreak = aBreak;
        this.duration = aBreak.serviceTime;
    }

    protected BreakActivity(BreakActivity breakActivity) {
        counter++;
        this.aBreak = (Break) breakActivity.job();
        this.arrTime = breakActivity.arrTime();
        this.endTime = breakActivity.end();
        this.location = breakActivity.location();
        index(breakActivity.index());
        this.earliest = breakActivity.startEarliest();
        this.latest = breakActivity.startLatest();
        this.duration = breakActivity.operationTime();
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aBreak == null) ? 0 : aBreak.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BreakActivity other = (BreakActivity) obj;
        return aBreak == null ? other.aBreak == null : aBreak.equals(other.aBreak);
    }

    @Override
    public double startEarliest() {
        return earliest;
    }

    @Override
    public double startLatest() {
        return latest;
    }

    @Override
    public double operationTime() {
        return duration;
    }

    public void operationTime(double duration){
        this.duration = duration;
    }

    @Override
    public Location location() {
        return location;
    }

    public void location(Location breakLocation) {
        this.location = breakLocation;
    }

    @Override
    public Service job() {
        return aBreak;
    }


    @Override
    public String toString() {
        return "[type=" + name() + "][location=" + location()
            + "][size=" + size()
            + "][twStart=" + Activities.round(startEarliest())
            + "][twEnd=" + Activities.round(startLatest()) + ']';
    }

    @Override
    public void startEarliest(double earliest) {
        this.earliest = earliest;
    }

    @Override
    public void startLatest(double latest) {
        this.latest = latest;
    }

    @Override
    public String name() {
        return aBreak.type;
    }

    @Override
    public BreakActivity clone() {
        return new BreakActivity(this);
    }

    @Override
    public Capacity size() {
        return aBreak.size;
    }


}

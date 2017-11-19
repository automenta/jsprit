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


/**
 * Created by schroeder on 14.07.14.
 */
public abstract class AbstractActivity {

    private int index;

    public int index() {
        return index;
    }

    protected void index(int index) {
        this.index = index;
    }

    @Override
    public  abstract AbstractActivity clone();

        /**
     * Returns end-time of this activity.
     *
     * @return end time
     */
    public  abstract double end();

    /**
     * Returns the name of this activity.
     *
     * @return name
     */
    abstract public String name();    /**
     * Returns the theoretical earliest operation start time, which is the time that is just allowed
     * (not earlier) to start this activity, that is for example <code>service.getTimeWindow().getStart()</code>.
     *
     * @return earliest start time
     */
    abstract public double startEarliest();

    /**
     * Returns the theoretical latest operation start time, which is the time that is just allowed
     * (not later) to start this activity, that is for example <code>service.getTimeWindow().getEnd()</code>.
     *
     * @return latest start time
     */
    abstract public double startLatest();

    /**
     * Returns the arrival-time of this activity.
     *
     * @return arrival time
     */
    abstract public double arrTime();

        /**
     * Returns the capacity-demand of that activity, in terms of what needs to be loaded or unloaded at
     * this activity.
     *
     * @return capacity
     */
    abstract public Capacity size();

    /**
     * Returns location.
     *
     * @return location
     */
    abstract public Location location();


    /**
     * Returns the operation-time this activity takes.
     * <p>
     * <p>Note that this is not necessarily the duration of this activity, but the
     * service time a pickup/delivery actually takes, that is for example <code>service.getServiceTime()</code>.
     *
     * @return operation time
     */
    abstract public double operationTime();



    /**
     * Sets the arrival time of that activity.
     *
     * @param arrTime
     */
    abstract public void arrTime(double arrTime);


    abstract public void startEarliest(double earliest);

    abstract public void startLatest(double latest);

    /**
     * Sets the end-time of this activity.
     *
     * @param endTime
     */
    abstract public void end(double endTime);
}

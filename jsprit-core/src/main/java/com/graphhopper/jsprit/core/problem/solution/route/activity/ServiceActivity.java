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
import com.graphhopper.jsprit.core.problem.job.Service;

public class ServiceActivity extends JobActivity {

    public double arrTime;

    public double endTime;

    private double theoreticalEarliest;

    private double theoreticalLatest;

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

    public static ServiceActivity copyOf(ServiceActivity serviceActivity) {
        return new ServiceActivity(serviceActivity);
    }

    public static ServiceActivity newInstance(Service service) {
        return new ServiceActivity(service);
    }


    private final Service service;

    protected ServiceActivity(Service service) {
        this.service = service;
    }

    protected ServiceActivity(ServiceActivity serviceActivity) {
        this.service = serviceActivity.job();
        this.arrTime = serviceActivity.arrTime();
        this.endTime = serviceActivity.end();
        index(serviceActivity.index());
        this.theoreticalEarliest = serviceActivity.startEarliest();
        this.theoreticalLatest = serviceActivity.startLatest();
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((service == null) ? 0 : service.hashCode());
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
        ServiceActivity other = (ServiceActivity) obj;
        return service == null ? other.service == null : service.equals(other.service);
    }

    @Override
    public double startEarliest() {
        return theoreticalEarliest;
    }

    @Override
    public double startLatest() {
        return theoreticalLatest;
    }

    @Override
    public void startEarliest(double earliest) {
        theoreticalEarliest = earliest;
    }

    @Override
    public void startLatest(double latest) {
        theoreticalLatest = latest;
    }

    @Override
    public double operationTime() {
        return service.serviceTime;
    }

    @Override
    public Location location() {
        return service.location;
    }


    @Override
    public Service job() {
        return service;
    }


    @Override
    public String toString() {
        return "[type=" + name() + "][locationId=" + location().id
            + "][size=" + size()
            + "][twStart=" + Activities.round(startEarliest())
            + "][twEnd=" + Activities.round(startLatest()) + ']';
    }

    @Override
    public String name() {
        return service.type;
    }

    @Override
    public ServiceActivity clone() {
        return new ServiceActivity(this);
    }

    @Override
    public Capacity size() {
        return service.size;
    }


}

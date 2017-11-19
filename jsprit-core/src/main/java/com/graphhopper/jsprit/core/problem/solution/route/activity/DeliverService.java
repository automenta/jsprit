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
import com.graphhopper.jsprit.core.problem.job.Delivery;

public final class DeliverService extends DeliveryActivity {

    private final Delivery delivery;

    private final Capacity capacity;

    private double arrTime;

    private double endTime;

    private double theoreticalEarliest;

    private double theoreticalLatest = Double.MAX_VALUE;

    public DeliverService(Delivery delivery) {
        this.delivery = delivery;
        capacity = Capacity.invert(delivery.size());
    }

    private DeliverService(DeliverService deliveryActivity) {
        this.delivery = deliveryActivity.job();
        this.arrTime = deliveryActivity.arrTime();
        this.endTime = deliveryActivity.end();
        capacity = deliveryActivity.size();
        index(deliveryActivity.index());
        this.theoreticalEarliest = deliveryActivity.startEarliest();
        this.theoreticalLatest = deliveryActivity.startLatest();
    }

    @Override
    public String name() {
        return delivery.type;
    }

    @Override
    public Location location() {
        return delivery.location;
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
    public double startEarliest() {
        return theoreticalEarliest;
    }

    @Override
    public double startLatest() {
        return theoreticalLatest;
    }

    @Override
    public double operationTime() {
        return delivery.serviceTime;
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
    public DeliverService clone() {
        return new DeliverService(this);
    }

    @Override
    public Delivery job() {
        return delivery;
    }

    public String toString() {
        return "[type=" + name() + "][locationId=" + location().id
            + "][size=" + size()
            + "][twStart=" + Activities.round(startEarliest())
            + "][twEnd=" + Activities.round(startLatest()) + ']';
    }

    @Override
    public Capacity size() {
        return capacity;
    }
}

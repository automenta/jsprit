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

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

/**
 * Created by schroeder on 15/10/15.
 */
class VersionedInsertionData {

    private final InsertionData iData;

    private final VehicleRoute route;

    private final int version;

    public VersionedInsertionData(InsertionData iData, int version, VehicleRoute route) {
        this.iData = iData;
        this.version = version;
        this.route = route;
    }

    public InsertionData getiData() {
        return iData;
    }

    public int getVersion() {
        return version;
    }

    public VehicleRoute getRoute() {
        return route;
    }
}

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

/**
 * Created by schroeder on 19/05/15.
 */
class InsertActivityListener implements EventListener {

    @Override
    public void inform(Event event) {
        if (event instanceof InsertActivity) {
            InsertActivity insertActivity = (InsertActivity) event;
            if (!insertActivity.getNewVehicle().isReturnToDepot()) {
                if (insertActivity.getIndex() >= insertActivity.getVehicleRoute().activities().size()) {
                    insertActivity.getVehicleRoute().end.location(insertActivity.getActivity().location());
                }
            }
            insertActivity.getVehicleRoute().tourActivities().addActivity(insertActivity.getIndex(), ((InsertActivity) event).getActivity());
        }
    }

}

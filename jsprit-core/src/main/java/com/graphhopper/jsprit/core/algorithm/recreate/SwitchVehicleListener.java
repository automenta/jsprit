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

import com.graphhopper.jsprit.core.problem.job.Break;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by schroeder on 19/05/15.
 */
class SwitchVehicleListener implements EventListener {

    private static final Logger logger = LoggerFactory.getLogger(SwitchVehicleListener.class);

    @Override
    public void inform(Event event) {
        if (event instanceof SwitchVehicle) {
            SwitchVehicle switchVehicle = (SwitchVehicle) event;
            if (vehiclesDifferent((SwitchVehicle) event)) {
                logger.trace("switch vehicle ({} to {})",((SwitchVehicle) event).getRoute().vehicle().id(),((SwitchVehicle) event).getVehicle().id());
                Break aBreak = ((SwitchVehicle) event).getRoute().vehicle().aBreak();
                if (aBreak != null) {
                    boolean removed = ((SwitchVehicle) event).getRoute().tourActivities().removeJob(aBreak);
                    if (removed) logger.trace("remove {}", aBreak.id);
                }
            }
            switchVehicle.getRoute().setVehicleAndDepartureTime(switchVehicle.getVehicle(), ((SwitchVehicle) event).getDepartureTime());
        }
    }

    private static boolean vehiclesDifferent(SwitchVehicle event) {
        return !event.getRoute().vehicle().id().equals(event.getVehicle().id());
    }
}

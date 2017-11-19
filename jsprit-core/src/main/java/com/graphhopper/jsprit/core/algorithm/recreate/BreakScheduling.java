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

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by schroeder on 07/04/16.
 */
public class BreakScheduling implements InsertionStartsListener,JobInsertedListener, RuinListener {

    private final static Logger logger = LoggerFactory.getLogger(BreakScheduling.class);

    private final StateManager stateManager;

    private final BreakInsertionCalculator breakInsertionCalculator;

    private final EventListeners eventListeners;

    private final Collection<VehicleRoute> modifiedRoutes = new HashSet<>();

    public BreakScheduling(VehicleRoutingProblem vrp, StateManager stateManager, ConstraintManager constraintManager) {
        this.stateManager = stateManager;
        this.breakInsertionCalculator = new BreakInsertionCalculator(vrp.transportCosts(),vrp.activityCosts(),new LocalActivityInsertionCostsCalculator(vrp.transportCosts(),vrp.activityCosts(),stateManager),constraintManager);
        this.breakInsertionCalculator.setJobActivityFactory(vrp.jobActivityFactory());
        eventListeners = new EventListeners();
    }

    @Override
    public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
        Break aBreak = inRoute.vehicle().aBreak();
        if(aBreak != null){
            boolean removed = inRoute.tourActivities().removeJob(aBreak);
            if(removed){
                logger.trace("ruin: {}", aBreak.id);
                stateManager.removed(aBreak,inRoute);
                stateManager.reCalculateStates(inRoute);
            }
            if(inRoute.end.arrTime() > aBreak.timeWindow().end){
                InsertionData iData = breakInsertionCalculator.getInsertionData(inRoute, aBreak, inRoute.vehicle(), inRoute.getDepartureTime(), inRoute.driver, Double.MAX_VALUE);
                if(!(iData instanceof InsertionData.NoInsertionFound)){
                    logger.trace("insert: [jobId={}]{}", aBreak.id, iData);
                    for(Event e : iData.getEvents()){
                        eventListeners.inform(e);
                    }
                    stateManager.informJobInserted(aBreak,inRoute,0,0);
                }
            }
        }
    }

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {

    }

    @Override
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        for(VehicleRoute route : routes){
            Break aBreak = route.vehicle().aBreak();
            boolean removed = route.tourActivities().removeJob(aBreak);
            if(removed) logger.trace("ruin: {}", aBreak.id);
        }
        Collection<Break> breaks = new ArrayList<>();
        for (Job j : unassignedJobs) {
            if (j instanceof Break) {
                breaks.add((Break) j);
            }
        }
        for(Break b : breaks){ unassignedJobs.remove(b); }
    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {
        if(fromRoute.vehicle().aBreak() != null) modifiedRoutes.add(fromRoute);
    }

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        for(VehicleRoute route : vehicleRoutes){
            Break aBreak = route.vehicle().aBreak();
            if(aBreak != null && !route.tourActivities().servesJob(aBreak)){
                if(route.end.arrTime() > aBreak.timeWindow().end){
                    InsertionData iData = breakInsertionCalculator.getInsertionData(route, aBreak, route.vehicle(), route.getDepartureTime(), route.driver, Double.MAX_VALUE);
                    if(!(iData instanceof InsertionData.NoInsertionFound)){
                        logger.trace("insert: [jobId={}]{}", aBreak.id, iData);
                        for(Event e : iData.getEvents()){
                            eventListeners.inform(e);
                        }
                        stateManager.informJobInserted(aBreak,route,0,0);
                    }
                }
            }
        }

    }
}

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
package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.driver.DriverImpl;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.NoiseMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


/**
 * Ruin strategy that ruins current solution randomly. I.e.
 * customer are removed randomly from current solution.
 *
 * @author stefan schroeder
 */

public final class RuinWorst extends AbstractRuinStrategy {

    private static final Logger logger = LoggerFactory.getLogger(RuinWorst.class);

    private NoiseMaker noiseMaker = () -> 0;

    public void setNoiseMaker(NoiseMaker noiseMaker) {
        this.noiseMaker = noiseMaker;
    }

    public RuinWorst(VehicleRoutingProblem vrp, final int initialNumberJobsToRemove) {
        super(vrp);
        setRuinShareFactory(() -> initialNumberJobsToRemove);
        logger.debug("initialise {}", this);
    }

    /**
     * Removes a fraction of jobs from vehicleRoutes.
     * <p>
     * <p>The number of jobs is calculated as follows: Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined).
     */
    @Override
    public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        List<Job> unassignedJobs = new ArrayList<>();
        int nOfJobs2BeRemoved = getRuinShareFactory().createNumberToBeRemoved();
        ruin(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs);
        return unassignedJobs;
    }

    private void ruin(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved, Collection<Job> unassignedJobs) {
        Deque<Job> availableJobs = new LinkedList<>(vrp.jobs().values());
        int toRemove = nOfJobs2BeRemoved;
        while (toRemove > 0) {
            Job worst = getWorst(vehicleRoutes);
            if (worst == null) break;
            if (removeJob(worst, vehicleRoutes)) {
                availableJobs.remove(worst);
                unassignedJobs.add(worst);
            }
            toRemove--;
        }
    }

    private Job getWorst(Iterable<VehicleRoute> copied) {
        Job worst = null;
        double bestSavings = Double.NEGATIVE_INFINITY;

        Map<Job, Double> savingsMap = new LinkedHashMap<>();
        for (VehicleRoute route : copied) {
            savingsMap.clear();
            if (route.isEmpty()) continue;
            AbstractActivity actBefore = route.start;
            AbstractActivity actToEval = null;
            for (AbstractActivity act : route.activities()) {
                if (actToEval == null) {
                    actToEval = act;
                    continue;
                }
                double savings = savings(route, actBefore, actToEval, act);
                Job job = ((JobActivity) actToEval).job();
                if (!savingsMap.containsKey(job)) {
                    savingsMap.put(job, savings);
                } else {
                    double s = savingsMap.get(job);
                    savingsMap.put(job, s + savings);
                }
                actBefore = actToEval;
                actToEval = act;
            }
            double savings = savings(route, actBefore, actToEval, route.end);
            Job job = ((JobActivity) actToEval).job();
            if (!savingsMap.containsKey(job)) {
                savingsMap.put(job, savings);
            } else {
                double s = savingsMap.get(job);
                savingsMap.put(job, s + savings);
            }
            //getCounts best
            for (Map.Entry<Job,Double> x: savingsMap.entrySet()) {
                double s = x.getValue();
                if (s > bestSavings) {
                    bestSavings = s;
                    worst = x.getKey();
                }
            }
        }
        return worst;
    }

    private double savings(VehicleRoute route, AbstractActivity actBefore, AbstractActivity actToEval, AbstractActivity act) {
        double savings = c(actBefore, actToEval, route.vehicle()) + c(actToEval, act, route.vehicle()) - c(actBefore, act, route.vehicle());
        return Math.max(0, savings + noiseMaker.makeNoise());
    }

    private double c(AbstractActivity from, AbstractActivity to, Vehicle vehicle) {
        return vrp.transportCosts().transportCost(from.location(), to.location(), from.end(), DriverImpl.noDriver(), vehicle);
    }

    @Override
    public String toString() {
        return "[name=worstRuin]";
    }

}

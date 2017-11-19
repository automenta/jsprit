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


package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionStrategy;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.InitialSolutionFactory;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public final class InsertionInitialSolutionFactory implements InitialSolutionFactory {

    private static final Logger logger = LoggerFactory.getLogger(InsertionInitialSolutionFactory.class);

    private final InsertionStrategy insertion;

    private final SolutionCostCalculator solutionCostsCalculator;

    public InsertionInitialSolutionFactory(InsertionStrategy insertionStrategy, SolutionCostCalculator solutionCostCalculator) {
        this.insertion = insertionStrategy;
        this.solutionCostsCalculator = solutionCostCalculator;
    }

    @Override
    public VehicleRoutingProblemSolution solution(final VehicleRoutingProblem vrp) {
        logger.info("create initial solution");
        Set<VehicleRoute> vehicleRoutes = vrp.initialVehicleRoutes();
        Collection<Job> badJobs = insertion.insertJobs(vehicleRoutes, jobsUnassigned(vrp));
        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(vehicleRoutes, badJobs, Double.MAX_VALUE);
        double costs = solutionCostsCalculator.getCosts(solution);
        solution.setCost(costs);
        return solution;
    }

    private static Collection<Job> jobsUnassigned(VehicleRoutingProblem vrp) {
        return vrp.jobs().values();

//        List<Job> jobs = new ArrayList<>();
////        for (Vehicle v : vrp.getVehicles()) {
////            if (v.getBreak() != null) jobs.add(v.getBreak());
////        }
//        return jobs;
    }

}

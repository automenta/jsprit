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
package com.graphhopper.jsprit.core.problem.solution;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.ArrayList;
import java.util.Collection;


/**
 * Contains the solution of a vehicle routing problem and its corresponding costs.
 *
 * @author stefan schroeder
 */
public class VehicleRoutingProblemSolution {

    /**
     * Makes a deep copy of the solution to be copied.
     *
     * @param solution2copy solution to be copied
     * @return solution
     */
    public static VehicleRoutingProblemSolution copyOf(VehicleRoutingProblemSolution solution2copy) {
        return new VehicleRoutingProblemSolution(solution2copy);
    }

    public final Collection<VehicleRoute> routes;

    public final Collection<Job> jobsUnassigned;

    private double cost;

    private VehicleRoutingProblemSolution(VehicleRoutingProblemSolution solution) {
        if (solution.routes!=null) {
            routes = new ArrayList<>(solution.routes.size());
            for (VehicleRoute r : solution.routes) {
                VehicleRoute route = VehicleRoute.copyOf(r);
                routes.add(route);
            }
        } else {
            routes = new ArrayList(0);
        }
        this.cost = solution.cost();

        jobsUnassigned = solution.jobsUnassigned;
    }

    /**
     * Constructs a solution with a number of {@link VehicleRoute}s and their corresponding aggregate cost value.
     *
     * @param routes routes being part of the solution
     * @param cost   total costs of solution
     */
    public VehicleRoutingProblemSolution(Collection<VehicleRoute> routes, double cost) {
        this.routes = routes;
        assert(routes!=null);
        this.cost = cost;
        jobsUnassigned = new ArrayList<>();
    }

    /**
     * Constructs a solution with a number of {@link VehicleRoute}s, bad jobs and their corresponding aggregate cost value.
     *
     * @param routes         routes being part of the solution
     * @param jobsUnassigned jobs that could not be assigned to any vehicle
     * @param cost           total costs of solution
     */
    public VehicleRoutingProblemSolution(Collection<VehicleRoute> routes, Collection<Job> jobsUnassigned, double cost) {
        this.routes = routes;
        assert(routes!=null);
        this.jobsUnassigned = jobsUnassigned;
        this.cost = cost;
    }

    /**
     * Returns a collection of vehicle-routes.
     *
     * @return collection of vehicle-routes
     */
    public final Collection<VehicleRoute> routes() {
        return routes;
    }

    /**
     * Returns cost of this solution.
     *
     * @return costs
     */
    public double cost() {
        return cost;
    }

    /**
     * Sets the costs of this solution.
     *
     * @param cost the cost to assigned to this solution
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Returns bad jobs, i.e. jobs that are not assigned to any vehicle route.
     *
     * @return bad jobs
     */
    public final Collection<Job> jobsUnassigned() {
        return jobsUnassigned;
    }

    @Override
    public String toString() {
        return "[costs=" + cost + "][routes=" + routes.size() + "][unassigned=" + jobsUnassigned.size() + ']';
    }
}

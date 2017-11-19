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
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class VehicleRoutingProblemSolutionTest {

    @Test
    public void whenCreatingSolutionWithTwoRoutes_solutionShouldContainTheseRoutes() {
        VehicleRoute r1 = mock(VehicleRoute.class);
        VehicleRoute r2 = mock(VehicleRoute.class);

        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(Set.of(r1, r2), 0.0);
        assertEquals(2, sol.routes.size());
    }

    @Test
    public void whenSettingSolutionCostsTo10_solutionCostsShouldBe10() {
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(new HashSet(), 10.0);
        assertEquals(10.0, sol.cost(), 0.01);
    }

    @Test
    public void whenCreatingSolWithCostsOf10AndSettingCostsAfterwardsTo20_solutionCostsShouldBe20() {
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(new HashSet(), 10.0);
        sol.setCost(20.0);
        assertEquals(20.0, sol.cost(), 0.01);
    }

    @Test
    public void sizeOfBadJobsShouldBeCorrect() {
        Job badJob = mock(Job.class);
        List<Job> badJobs = new ArrayList<Job>();
        badJobs.add(badJob);
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(new HashSet(), badJobs, 10.0);
        assertEquals(1, sol.jobsUnassigned.size());
    }

    @Test
    public void sizeOfBadJobsShouldBeCorrect_2() {
        Job badJob = mock(Job.class);
        List<Job> badJobs = new ArrayList<Job>();
        badJobs.add(badJob);
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(new HashSet(), 10.0);
        sol.jobsUnassigned.addAll(badJobs);
        assertEquals(1, sol.jobsUnassigned.size());
    }

    @Test
    public void badJobsShouldBeCorrect() {
        Job badJob = mock(Job.class);
        List<Job> badJobs = new ArrayList<Job>();
        badJobs.add(badJob);
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(new HashSet(), badJobs, 10.0);
        Assert.assertEquals(badJob, sol.jobsUnassigned.iterator().next());
    }

    @Test
    public void badJobsShouldBeCorrect_2() {
        Job badJob = mock(Job.class);
        List<Job> badJobs = new ArrayList<Job>();
        badJobs.add(badJob);
        VehicleRoutingProblemSolution sol = new VehicleRoutingProblemSolution(new HashSet(), 10.0);
        sol.jobsUnassigned.addAll(badJobs);
        Assert.assertEquals(badJob, sol.jobsUnassigned.iterator().next());
    }

}

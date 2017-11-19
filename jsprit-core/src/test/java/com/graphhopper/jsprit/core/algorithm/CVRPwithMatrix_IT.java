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

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class CVRPwithMatrix_IT {

    private int index;


    @Test
    public void whenReturnToDepot_itShouldWorkWithMatrix() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        new ChristofidesReader(vrpBuilder).setJobType(JobType.DELIVERY).read(getClass().getResourceAsStream("vrpnc1.txt"));
        VehicleRoutingProblem vrp_ = vrpBuilder.build();
        VehicleRoutingProblem vrp = createVrpWithLocationIndecesAndMatrix(vrp_, true);
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.FAST_REGRET,"true").buildAlgorithm();
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        Assert.assertEquals(530.0, Solutions.bestOf(solutions).cost(), 50.0);
        assertEquals(5, Solutions.bestOf(solutions).routes.size());
    }

    @Test
    public void whenNotReturnToDepot_itShouldWorkWithMatrix() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        new ChristofidesReader(vrpBuilder).setJobType(JobType.DELIVERY).read(getClass().getResourceAsStream("vrpnc1.txt"));
        VehicleRoutingProblem vrp_ = vrpBuilder.build();
        VehicleRoutingProblem vrp = createVrpWithLocationIndecesAndMatrix(vrp_, false);
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.FAST_REGRET,"true").buildAlgorithm();
        try {
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            assertTrue(true);
        } catch (Exception e) {
            assertFalse(true);
        }
    }

    @Test
    public void whenCalcTimeWithSolutionAnalyser_itShouldWork() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        new ChristofidesReader(vrpBuilder).setJobType(JobType.DELIVERY).read(getClass().getResourceAsStream("vrpnc1.txt"));
        VehicleRoutingProblem vrp_ = vrpBuilder.build();
        final VehicleRoutingProblem vrp = createVrpWithLocationIndecesAndMatrix(vrp_, false);
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.FAST_REGRET,"true").buildAlgorithm();
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        SolutionAnalyser sa = new SolutionAnalyser(vrp, Solutions.bestOf(solutions), vrp.transportCosts());
    }


    private VehicleRoutingProblem createVrpWithLocationIndecesAndMatrix(VehicleRoutingProblem vrp_, boolean return_to_depot) {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        List<Location> locations = new ArrayList<Location>();
        for (Vehicle v : vrp_.vehicles()) {
            Location l = Location.Builder.the().setIndex(getIndex()).setId(v.start().id)
                .setCoord(v.start().coord).build();
            VehicleImpl.Builder newVehicleBuilder = VehicleImpl.Builder.newInstance(v.id()).setType(v.type())
                .setEarliestStart(v.earliestDeparture()).setLatestArrival(v.latestArrival())
                .setStartLocation(l).setReturnToDepot(return_to_depot);
            VehicleImpl newVehicle = newVehicleBuilder.build();
            vrpBuilder.addVehicle(newVehicle);
            locations.add(l);
        }
        for (Job j : vrp_.jobs().values()) {
            Service s = (Service) j;
            Location l = Location.Builder.the().setIndex(getIndex())
                .setId(s.location.id).setCoord(s.location.coord).build();
            Service newService = Service.Builder.newInstance(s.id).serviceTime(s.serviceTime)
                .sizeDimension(0, s.size.get(0))
                .location(l).build();
            vrpBuilder.addJob(newService);
            locations.add(l);
        }
        FastVehicleRoutingTransportCostsMatrix.Builder matrixBuilder = FastVehicleRoutingTransportCostsMatrix.Builder.get(locations.size(), true);
        for (Location from : locations) {
            for (Location to : locations) {
                double distance = EuclideanDistanceCalculator.calculateDistance(from.coord, to.coord);
                matrixBuilder.addTransportDistance(from.index, to.index, distance);
                matrixBuilder.addTransportTime(from.index, to.index, distance);
            }
        }
        vrpBuilder.setRoutingCost(matrixBuilder.build());
        return vrpBuilder.build();
    }


    public int getIndex() {
        int i = index;
        index++;
        return i;
    }
}

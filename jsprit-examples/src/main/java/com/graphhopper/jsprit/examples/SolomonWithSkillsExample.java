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

package com.graphhopper.jsprit.examples;


import com.graphhopper.jsprit.analysis.toolbox.Plotter;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.instance.reader.SolomonReader;
import com.graphhopper.jsprit.io.problem.VrpXMLWriter;

import java.util.Collection;

public class SolomonWithSkillsExample {

    public static void main(String[] args) {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        new SolomonReader(vrpBuilder).read("input/C101_solomon.txt");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        //y >= 50 skill1 otherwise skill2
        //two vehicles: v1 - skill1 #5; v2 - skill2 #6
        Vehicle solomonVehicle = vrp.vehicles().iterator().next();
        VehicleType newType = solomonVehicle.type();
        VehicleRoutingProblem.Builder skillProblemBuilder = VehicleRoutingProblem.Builder.get();
        for (int i = 0; i < 5; i++) {
            VehicleImpl skill1Vehicle = VehicleImpl.Builder.newInstance("skill1_vehicle_" + i).addSkill("skill1")
                .setStartLocation(Location.Builder.the().setId(solomonVehicle.start().id).setCoord(solomonVehicle.start().coord).build())
                .setEarliestStart(solomonVehicle.earliestDeparture())
                .setType(newType).build();
            VehicleImpl skill2Vehicle = VehicleImpl.Builder.newInstance("skill2_vehicle_" + i).addSkill("skill2")
                .setStartLocation(Location.Builder.the().setId(solomonVehicle.start().id)
                    .setCoord(solomonVehicle.start().coord).build())
                .setEarliestStart(solomonVehicle.earliestDeparture())
                .setType(newType).build();
            skillProblemBuilder.addVehicle(skill1Vehicle).addVehicle(skill2Vehicle);
        }
        for (Job job : vrp.jobs().values()) {
            Service service = (Service) job;
            Service.Builder skillServiceBuilder;
            if (service.location.coord.y < 50.) {
                skillServiceBuilder = Service.Builder.newInstance(service.id + "_skill2").serviceTime(service.serviceTime)
                    .location(Location.Builder.the().setId(service.location.id)
                        .setCoord(service.location.coord).build()).timeWindowSet(service.timeWindow())
                    .sizeDimension(0, service.size.get(0));
                skillServiceBuilder.skillRequired("skill2");
            } else {
                skillServiceBuilder = Service.Builder.newInstance(service.id + "_skill1").serviceTime(service.serviceTime)
                    .location(
                        Location.Builder.the().setId(service.location.id)
                            .setCoord(service.location.coord).build()
                    ).timeWindowSet(service.timeWindow())
                    .sizeDimension(0, service.size.get(0));
                skillServiceBuilder.skillRequired("skill1");
            }
            skillProblemBuilder.addJob(skillServiceBuilder.build());
        }
        skillProblemBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        VehicleRoutingProblem skillProblem = skillProblemBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(skillProblem).buildAlgorithm();

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(skillProblem, solution, SolutionPrinter.Print.VERBOSE);

        new Plotter(skillProblem, solution).plot("output/skill_solution", "solomon_with_skills");

        new VrpXMLWriter(skillProblem, solutions).write("output/solomon_with_skills");
    }
}

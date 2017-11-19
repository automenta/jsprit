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

import com.graphhopper.jsprit.analysis.toolbox.GraphStreamViewer;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;

import java.util.Collection;

/**
 * customers (id,x,y,demand)
 * 1 22 22 18
 * 2 36 26 26
 * 3 21 45 11
 * 4 45 35 30
 * 5 55 20 21
 * 6 33 34 19
 * 7 50 50 15
 * 8 55 45 16
 * 9 26 59 29
 * 10 40 66 26
 * 11 55 65 37
 * 12 35 51 16
 * 13 62 35 12
 * 14 62 57 31
 * 15 62 24 8
 * 16 21 36 19
 * 17 33 44 20
 * 18 9 56 13
 * 19 62 48 15
 * 20 66 14 22
 * <p>
 * vehicles (id,cap,fixed costs, perDistance, #vehicles) at location (40,40)
 * 1 120 1000 1.0 2
 * 2 160 1500 1.1 1
 * 3 300 3500 1.4 1
 *
 * @author schroeder
 */
public class HVRPExample {


    public static void main(String[] args) {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();

        //add customers
        vrpBuilder.addJob(Service.Builder.newInstance("1").sizeDimension(0, 18).location(Location.the(22, 22)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("2").sizeDimension(0, 26).location(Location.the(36, 26)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("3").sizeDimension(0, 11).location(Location.the(21, 45)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("4").sizeDimension(0, 30).location(Location.the(45, 35)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("5").sizeDimension(0, 21).location(Location.the(55, 20)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("6").sizeDimension(0, 19).location(Location.the(33, 34)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("7").sizeDimension(0, 15).location(Location.the(50, 50)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("8").sizeDimension(0, 16).location(Location.the(55, 45)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("9").sizeDimension(0, 29).location(Location.the(26, 59)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("10").sizeDimension(0, 26).location(Location.the(40, 66)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("11").sizeDimension(0, 37).location(Location.the(55, 56)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("12").sizeDimension(0, 16).location(Location.the(35, 51)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("13").sizeDimension(0, 12).location(Location.the(62, 35)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("14").sizeDimension(0, 31).location(Location.the(62, 57)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("15").sizeDimension(0, 8).location(Location.the(62, 24)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("16").sizeDimension(0, 19).location(Location.the(21, 36)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("17").sizeDimension(0, 20).location(Location.the(33, 44)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("18").sizeDimension(0, 13).location(Location.the(9, 56)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("19").sizeDimension(0, 15).location(Location.the(62, 48)).build());
        vrpBuilder.addJob(Service.Builder.newInstance("20").sizeDimension(0, 22).location(Location.the(66, 14)).build());


        //add vehicle - finite fleet
        //2xtype1
        VehicleType type1 = VehicleTypeImpl.Builder.the("type_1").addCapacityDimension(0, 120).setCostPerDistance(1.0).build();
        VehicleImpl vehicle1_1 = VehicleImpl.Builder.newInstance("1_1").setStartLocation(Location.the(40, 40)).setType(type1).build();
        vrpBuilder.addVehicle(vehicle1_1);
        VehicleImpl vehicle1_2 = VehicleImpl.Builder.newInstance("1_2").setStartLocation(Location.the(40, 40)).setType(type1).build();
        vrpBuilder.addVehicle(vehicle1_2);
        //1xtype2
        VehicleType type2 = VehicleTypeImpl.Builder.the("type_2").addCapacityDimension(0, 160).setCostPerDistance(1.1).build();
        VehicleImpl vehicle2_1 = VehicleImpl.Builder.newInstance("2_1").setStartLocation(Location.the(40, 40)).setType(type2).build();
        vrpBuilder.addVehicle(vehicle2_1);
        //1xtype3
        VehicleType type3 = VehicleTypeImpl.Builder.the("type_3").addCapacityDimension(0, 300).setCostPerDistance(1.3).build();
        VehicleImpl vehicle3_1 = VehicleImpl.Builder.newInstance("3_1").setStartLocation(Location.the(40, 40)).setType(type3).build();
        vrpBuilder.addVehicle(vehicle3_1);

        //add penaltyVehicles to allow invalid solutions temporarily
//		vrpBuilder.addPenaltyVehicles(5, 1000);

        //set fleetsize finite
        vrpBuilder.setFleetSize(FleetSize.FINITE);

        //build problem
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);

        SolutionPrinter.print(vrp, best, SolutionPrinter.Print.VERBOSE);

        new GraphStreamViewer(vrp, best).setRenderDelay(100).display();

    }

}

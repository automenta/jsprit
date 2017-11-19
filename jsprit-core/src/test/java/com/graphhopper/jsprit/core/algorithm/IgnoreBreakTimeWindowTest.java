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
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 08/01/16.
 */
public class IgnoreBreakTimeWindowTest {

    @Test
    public void doNotIgnoreBreakTW(){
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.the("vehicleType");
        VehicleType vehicleType = vehicleTypeBuilder.setCostPerWaitingTime(0.8).build();

		/*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */

        VehicleImpl vehicle2;
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("v2");
        vehicleBuilder.setStartLocation(Location.the(0, 0));
        vehicleBuilder.setType(vehicleType);
        vehicleBuilder.setEarliestStart(10).setLatestArrival(50);
        vehicleBuilder.setBreak(Break.Builder.newInstance("lunch").timeWindowSet(TimeWindow.the(14, 14)).serviceTime(1.).build());
        vehicle2 = vehicleBuilder.build();
        /*
         * build services at the required locations, each with a capacity-demand of 1.
		 */


        Service service4 = Service.Builder.newInstance("2").location(Location.the(0, 0))
            .serviceTime(1.).timeWindowSet(TimeWindow.the(17,17)).build();

        Service service5 = Service.Builder.newInstance("3").location(Location.the(0, 0))
            .serviceTime(1.).timeWindowSet(TimeWindow.the(18, 18)).build();

        Service service7 = Service.Builder.newInstance("4").location(Location.the(0, 0))
            .serviceTime(1.).timeWindowSet(TimeWindow.the(10, 10)).build();

        Service service8 = Service.Builder.newInstance("5").location(Location.the(0, 0))
            .serviceTime(1.).timeWindowSet(TimeWindow.the(12, 12)).build();

        Service service10 = Service.Builder.newInstance("6").location(Location.the(0, 0))
            .serviceTime(1.).timeWindowSet(TimeWindow.the(16, 16)).build();

        Service service11 = Service.Builder.newInstance("7").location(Location.the(0, 0))
            .serviceTime(1.).timeWindowSet(TimeWindow.the(13, 13)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.get()
            .addVehicle(vehicle2)
            .addJob(service4)
            .addJob(service5).addJob(service7)
            .addJob(service8).addJob(service10).addJob(service11)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(50);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());


        Assert.assertTrue(breakShouldBeTime(solution));
    }

    private boolean breakShouldBeTime(VehicleRoutingProblemSolution solution) {
        boolean inTime = true;
        for(AbstractActivity act : solution.routes.iterator().next().activities()){
            if(act instanceof BreakActivity){
                if(act.end() < ((BreakActivity) act).job().timeWindow().start){
                    inTime = false;
                }
                if(act.arrTime() > ((BreakActivity) act).job().timeWindow().end){
                    inTime = false;
                }
            }
        }
        return inTime;
    }
}

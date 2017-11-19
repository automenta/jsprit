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
import com.graphhopper.jsprit.core.algorithm.state.State;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.v2;
import com.graphhopper.jsprit.core.util.EuclideanDistanceCalculator;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.io.problem.VrpXMLReader;

import java.util.Collection;

//import jsprit.core.problem.solution.route.state.StateFactory; //v1.3.1

public class AdditionalDistanceConstraintExample {

    static class DistanceUpdater implements StateUpdater, ActivityVisitor {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costMatrix;

        //        private final StateFactory.StateId distanceStateId;    //v1.3.1
        private final State distanceState; //head of development - upcoming release

        private VehicleRoute vehicleRoute;

        private double distance;

        private AbstractActivity prevAct;

        //        public DistanceUpdater(StateFactory.StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix costMatrix) { //v1.3.1
        public DistanceUpdater(State distanceState, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) { //head of development - upcoming release (v1.4)
            this.costMatrix = transportCosts;
            this.stateManager = stateManager;
            this.distanceState = distanceState;
        }

        @Override
        public void begin(VehicleRoute vehicleRoute) {
            distance = 0.;
            prevAct = vehicleRoute.start;
            this.vehicleRoute = vehicleRoute;
        }

        @Override
        public void visit(AbstractActivity abstractActivity) {
            distance += getDistance(prevAct, abstractActivity);
            prevAct = abstractActivity;
        }

        @Override
        public void finish() {
            distance += getDistance(prevAct, vehicleRoute.end);
//            stateManager.putTypedRouteState(vehicleRoute,distanceStateId,Double.class,distance); //v1.3.1
            stateManager.putRouteState(vehicleRoute, distanceState, distance); //head of development - upcoming release (v1.4)
        }

        double getDistance(AbstractActivity from, AbstractActivity to) {
            return costMatrix.getDistance(from.location().id, to.location().id);
        }
    }

    static class DistanceConstraint implements HardActivityConstraint {

        private final StateManager stateManager;

        private final VehicleRoutingTransportCostsMatrix costsMatrix;

        private final double maxDistance;

        //        private final StateFactory.StateId distanceStateId; //v1.3.1
        private final State distanceState; //head of development - upcoming release (v1.4)

        //        DistanceConstraint(double maxDistance, StateFactory.StateId distanceStateId, StateManager stateManager, VehicleRoutingTransportCostsMatrix costsMatrix) { //v1.3.1
        DistanceConstraint(double maxDistance, State distanceState, StateManager stateManager, VehicleRoutingTransportCostsMatrix transportCosts) { //head of development - upcoming release (v1.4)
            this.costsMatrix = transportCosts;
            this.maxDistance = maxDistance;
            this.stateManager = stateManager;
            this.distanceState = distanceState;
        }

        @Override
        public ConstraintsStatus fulfilled(JobInsertionContext context, AbstractActivity prevAct, AbstractActivity newAct, AbstractActivity nextAct, double v) {
            double additionalDistance = getDistance(prevAct, newAct) + getDistance(newAct, nextAct) - getDistance(prevAct, nextAct);
            Double routeDistance = stateManager.getRouteState(context.getRoute(), distanceState, Double.class);
            if (routeDistance == null) routeDistance = 0.;
            double newRouteDistance = routeDistance + additionalDistance;
            if (newRouteDistance > maxDistance) {
                return ConstraintsStatus.NOT_FULFILLED;
            } else return ConstraintsStatus.FULFILLED;
        }

        double getDistance(AbstractActivity from, AbstractActivity to) {
            return costsMatrix.getDistance(from.location().id, to.location().id);
        }

    }

    public static void main(String[] args) {

        //route length 618
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.get();
        new VrpXMLReader(vrpBuilder).read("input/pickups_and_deliveries_solomon_r101_withoutTWs.xml");
        //builds a matrix based on euclidean distances; t_ij = euclidean(i,j) / 2; d_ij = euclidean(i,j);
        VehicleRoutingTransportCostsMatrix costMatrix = createMatrix(vrpBuilder);
        vrpBuilder.setRoutingCost(costMatrix);
        VehicleRoutingProblem vrp = vrpBuilder.build();


        StateManager stateManager = new StateManager(vrp); //head of development - upcoming release (v1.4)

        State distanceState = stateManager.createStateId("distance"); //head of development - upcoming release (v1.4)
        stateManager.addStateUpdater(new DistanceUpdater(distanceState, stateManager, costMatrix));

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new DistanceConstraint(120., distanceState, stateManager, costMatrix), ConstraintManager.Priority.CRITICAL);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setStateAndConstraintManager(stateManager,constraintManager)
            .buildAlgorithm();
//        vra.setMaxIterations(250); //v1.3.1
        vra.setMaxIterations(250); //head of development - upcoming release (v1.4)

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);

        new Plotter(vrp, Solutions.bestOf(solutions)).plot("output/plot", "plot");
    }

    private static VehicleRoutingTransportCostsMatrix createMatrix(VehicleRoutingProblem.Builder vrpBuilder) {
        VehicleRoutingTransportCostsMatrix.Builder matrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        for (String from : vrpBuilder.locations().keySet()) {
            for (String to : vrpBuilder.locations().keySet()) {
                v2 fromCoord = vrpBuilder.locations().get(from);
                v2 toCoord = vrpBuilder.locations().get(to);
                double distance = EuclideanDistanceCalculator.calculateDistance(fromCoord, toCoord);
                matrixBuilder.addTransportDistance(from, to, distance);
                matrixBuilder.addTransportTime(from, to, (distance / 2.));
            }
        }
        return matrixBuilder.build();
    }


}

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
package com.graphhopper.jsprit.instance.reader;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Test;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class BelhaizaReaderTest {

	@Test
	public void whenReadingBelhaizaInstance_nuOfCustomersIsCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(100,vrp.jobs().values().size());
	}

	private String getPath() {
		URL resource = getClass().getClassLoader().getResource("cm101.txt");
		if(resource == null) throw new IllegalStateException("file C101_solomon.txt does not exist");
		return resource.getPath();
	}

	@Test
	public void whenReadingBelhaizaInstance_fleetSizeIsInfinite(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.INFINITE,vrp.getFleetSize());
	}

	@Test
	public void whenReadingBelhaizaInstance_vehicleCapacitiesAreCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		for(Vehicle v : vrp.vehicles()){
			assertEquals(200,v.type().getCapacityDimensions().get(0));
		}
	}

	@Test
	public void whenReadingBelhaizaInstance_vehicleLocationsAreCorrect_and_correspondToDepotLocation(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		for(Vehicle v : vrp.vehicles()){
            assertEquals(40.0, v.start().coord.x,0.01);
			assertEquals(50.0, v.start().coord.y,0.01);
		}
	}

	@Test
	public void whenReadingBelhaizaInstance_demandOfCustomerOneIsCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(10,vrp.jobs().get("1").size().get(0));
	}

	@Test
	public void whenReadingBelhaizaInstance_serviceDurationOfCustomerTwoIsCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(90, ((Service) vrp.jobs().get("2")).serviceTime,0.1);
	}

	@Test
	public void noTimeWindowsShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
        assertEquals(5, ((Service) vrp.jobs().get("1")).timeWindows.size());
	}

	@Test
	public void noTimeWindowsShouldBeCorrect2(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
        assertEquals(10, ((Service) vrp.jobs().get("2")).timeWindows.size());
	}

	@Test
	public void firstTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
        assertEquals(20., ((Service) vrp.jobs().get("1")).timeWindows.iterator().next().start,0.1);
        assertEquals(31., ((Service) vrp.jobs().get("1")).timeWindows.iterator().next().end,0.1);
	}

	@Test
	public void secondTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
        List<TimeWindow> timeWindows = new ArrayList<TimeWindow>(((Service) vrp.jobs().get("1")).timeWindows);
		assertEquals(118., timeWindows.get(1).start,0.1);
        assertEquals(148., timeWindows.get(1).end,0.1);
	}

	@Test
	public void thirdTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
        List<TimeWindow> timeWindows = new ArrayList<TimeWindow>(((Service) vrp.jobs().get("1")).timeWindows);
		assertEquals(235., timeWindows.get(2).start,0.1);
        assertEquals(258., timeWindows.get(2).end,0.1);
	}

	@Test
	public void fourthTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
        List<TimeWindow> timeWindows = new ArrayList<TimeWindow>(((Service) vrp.jobs().get("1")).timeWindows);
		assertEquals(343., timeWindows.get(3).start,0.1);
        assertEquals(355., timeWindows.get(3).end,0.1);
	}

	@Test
	public void fifthTimeWindowShouldBeCorrect(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		VehicleRoutingProblem vrp = builder.build();
        List<TimeWindow> timeWindows = new ArrayList<TimeWindow>(((Service) vrp.jobs().get("1")).timeWindows);
		assertEquals(441., timeWindows.get(4).start,0.1);
        assertEquals(457., timeWindows.get(4).end,0.1);
	}

	@Test
	public void testAlgo(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.get();
		new BelhaizaReader(builder).read(getPath());
		builder.setFleetSize(FleetSize.FINITE);
		VehicleRoutingProblem vrp = builder.build();

//		VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);

//		VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(vrp);

		Jsprit.Builder vraBuilder = Jsprit.Builder.newInstance(vrp);
		vraBuilder.setProperty(Jsprit.Strategy.CLUSTER_REGRET, "0.25");
		vraBuilder.setProperty(Jsprit.Strategy.RADIAL_REGRET, "0.25");
		vraBuilder.setProperty(Jsprit.Strategy.RANDOM_REGRET, "0.");
		vraBuilder.setProperty(Jsprit.Strategy.WORST_REGRET, "0.25");
		vraBuilder.setProperty(Jsprit.Parameter.THRESHOLD_INI, "0.05");
		VehicleRoutingAlgorithm algorithm = vraBuilder.buildAlgorithm();
		algorithm.setMaxIterations(5000);
//		VariationCoefficientTermination variation_coefficient = new VariationCoefficientTermination(200, 0.005);
//		algorithm.setPrematureAlgorithmTermination(variation_coefficient);
//		algorithm.addListener(variation_coefficient);

//		vra.setMaxIterations(5000);
		VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

		SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
	}

}

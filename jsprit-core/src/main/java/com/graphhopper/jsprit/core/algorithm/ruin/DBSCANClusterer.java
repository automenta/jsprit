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
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.ForwardTransportCost;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import com.graphhopper.jsprit.core.util.RandomUtils;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;

import java.util.*;

/**
 * Created by schroeder on 04/02/15.
 */
public class DBSCANClusterer {

    private static class LocationWrapper implements Clusterable {

        private static int objCounter;

        public final Job job;

        public final List<Location> locations;

        private final int id;

        public LocationWrapper(Job job, List<Location> locations) {
            this.locations = locations;
            objCounter++;
            this.job = job;
            this.id = objCounter;
        }

//        private List<Location> getLocations(Job job){
//            List<Location> locs = new ArrayList<Location>();
//            if(job instanceof Service) {
//                locs.add(((Service) job).getLocation());
//            }
//            else if(job instanceof Shipment){
//                locs.add(((Shipment) job).getPickupLocation());
//                locs.add(((Shipment) job).getDeliveryLocation());
//            }
//            return locs;
//        }

        public List<Location> locations() {
            return locations;
        }

        @Override
        public double[] getPoint() {
            return new double[]{id};
        }

        public Job job() {
            return job;
        }
    }

    private static class MyDistance implements DistanceMeasure {

        private final Map<Integer, LocationWrapper> locations;

        private final VehicleRoutingTransportCosts costs;

        public MyDistance(Iterable<LocationWrapper> locations, VehicleRoutingTransportCosts costs) {
            this.locations = new HashMap<>();
            for (LocationWrapper lw : locations) {
                this.locations.put((int) Math.round(lw.getPoint()[0]), lw);
            }
            this.costs = costs;
        }

        @Override
        public double compute(double[] a, double[] b) {
            LocationWrapper l1 = locations.get((int) Math.round(a[0]));
            LocationWrapper l2 = locations.get((int) Math.round(b[0]));
            int count = 0;
            double sum = 0;
            List<Location> locations1 = l1.locations;
            for (int i = 0, locations1Size = locations1.size(); i < locations1Size; i++) {
                Location loc_1 = locations1.get(i);
                List<Location> locations2 = l2.locations;
                for (int i1 = 0, locations2Size = locations2.size(); i1 < locations2Size; i1++) {
                    Location loc_2 = locations2.get(i1);
                    sum += costs.transportCost(loc_1, loc_2, 0, null, null);
                    count++;
                }
            }
            return sum / count;
        }
    }

    private final VehicleRoutingTransportCosts costs;

    private int minNoOfJobsInCluster = 1;

    private final int noDistanceSamples = 10;

    private double epsFactor = 0.8;

    private Double epsDistance;

    private Random random = RandomNumberGeneration.getRandom();

    public void setRandom(Random random) {
        this.random = random;
    }

    public DBSCANClusterer(VehicleRoutingTransportCosts costs) {
        this.costs = costs;
    }

    public void setMinPts(int pts) {
        this.minNoOfJobsInCluster = pts;
    }

    public void setEpsFactor(double epsFactor) {
        this.epsFactor = epsFactor;
    }

    public void setEpsDistance(double epsDistance) {
        this.epsDistance = epsDistance;
    }

    public List<List<Job>> getClusters(VehicleRoute route) {
        List<LocationWrapper> locations = getLocationWrappers(route);
        List<Cluster<LocationWrapper>> clusterResults = getClusters(route, locations);
        return makeList(clusterResults);
    }

    private static List<LocationWrapper> getLocationWrappers(VehicleRoute route) {
        List<LocationWrapper> locations = new ArrayList<>(route.tourActivities().jobs().size());
        Map<Job, ArrayList<Location>> jobs2locations = new HashMap<>();
        for (AbstractActivity act : route.activities()) {
            if (act instanceof JobActivity) {
                jobs2locations.computeIfAbsent(((JobActivity) act).job(), (x)->new ArrayList<>()).add(act.location());
            }
        }
        for (Map.Entry<Job, ArrayList<Location>> jobListEntry : jobs2locations.entrySet()) {
            locations.add(new LocationWrapper(jobListEntry.getKey(), jobListEntry.getValue()));
        }
        return locations;
    }

    private List<Cluster<LocationWrapper>> getClusters(VehicleRoute route, List<LocationWrapper> locations) {
        double sampledDistance;
        sampledDistance = epsDistance != null ? epsDistance : Math.max(0, sample(costs, route));
        org.apache.commons.math3.ml.clustering.DBSCANClusterer<LocationWrapper> clusterer = new org.apache.commons.math3.ml.clustering.DBSCANClusterer<>(sampledDistance, minNoOfJobsInCluster, new MyDistance(locations, costs));
        return clusterer.cluster(locations);
    }

    private static List<List<Job>> makeList(Iterable<Cluster<LocationWrapper>> clusterResults) {
        List<List<Job>> l = new ArrayList<>();
        for (Cluster<LocationWrapper> c : clusterResults) {
            List<Job> l_ = getJobList(c);
            l.add(l_);
        }
        return l;
    }

    private static List<Job> getJobList(Cluster<LocationWrapper> c) {
        List<Job> l_ = new ArrayList<>();
        if (c == null) return l_;
        for (LocationWrapper lw : c.getPoints()) {
            l_.add(lw.job);
        }
        return l_;
    }

    public List<Job> getRandomCluster(VehicleRoute route) {
        if (route.isEmpty()) return Collections.emptyList();
        List<LocationWrapper> locations = getLocationWrappers(route);
        List<Cluster<LocationWrapper>> clusterResults = getClusters(route, locations);
        if (clusterResults.isEmpty()) return Collections.emptyList();
        Cluster<LocationWrapper> randomCluster = RandomUtils.nextItem(clusterResults, random);
        return getJobList(randomCluster);
    }

    private double sample(ForwardTransportCost costs, VehicleRoute r) {
        double min = Double.MAX_VALUE;
        double sum = 0;
        for (int i = 0; i < noDistanceSamples; i++) {
            AbstractActivity act1 = RandomUtils.nextItem(r.activities(), random);
            AbstractActivity act2 = RandomUtils.nextItem(r.activities(), random);
            double dist = costs.transportCost(act1.location(), act2.location(),
                0., null, r.vehicle());
            if (dist < min) min = dist;
            sum += dist;
        }
        double avg = sum / ((double) noDistanceSamples);
        return (avg - min) * epsFactor;
    }

}

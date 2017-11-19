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

import com.graphhopper.jsprit.core.algorithm.ruin.distance.JobDistance;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by schroeder on 07/01/15.
 */
class JobNeighborhoodsOptimized implements JobNeighborhoods {

    static class ArrayIterator implements Iterator<Job> {

        private final int noItems;

        private final int[] itemArray;

        private final Job[] jobs;

        private int index;

        public ArrayIterator(int noItems, int[] itemArray, Job[] jobs) {
            this.noItems = noItems;
            this.itemArray = itemArray;
            this.jobs = jobs;
        }

        @Override
        public boolean hasNext() {
            return index < noItems && index < itemArray.length;
        }

        @Override
        public Job next() {
            Job job = jobs[itemArray[index]];
            index++;
            return job;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(JobNeighborhoodsOptimized.class);

    private final VehicleRoutingProblem vrp;

    private final int[][] neighbors;

    private final Job[] jobs;

    private final JobDistance jobDistance;

    private final int capacity;

    private double maxDistance;

    public JobNeighborhoodsOptimized(VehicleRoutingProblem vrp, JobDistance jobDistance, int capacity) {
        this.vrp = vrp;
        this.jobDistance = jobDistance;
        this.capacity = capacity;
        neighbors = new int[vrp.jobsInclusiveInitialJobsInRoutes().size()+1][capacity];
        jobs = new Job[vrp.jobsInclusiveInitialJobsInRoutes().size()+1];
        logger.debug("initialize {}", this);
    }

    @Override
    public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo) {
        int[] neighbors = this.neighbors[neighborTo.index()-1];
        return new ArrayIterator(nNeighbors,neighbors,jobs);
    }

    @Override
    public void initialise() {
        logger.debug("calculates distances from EACH job to EACH job --> n^2={} calculations, but 'only' {} are cached.", Math.pow(vrp.jobs().values().size(), 2), (vrp.jobs().values().size() * capacity));
        if (capacity == 0) return;
        calculateDistancesFromJob2Job();
    }

    @Override
    public double getMaxDistance() {
        return maxDistance;
    }

    private void calculateDistancesFromJob2Job() {
        logger.debug("pre-process distances between locations ...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (Job job_i : vrp.jobsInclusiveInitialJobsInRoutes().values()) {
            jobs[job_i.index()] = job_i;
            List<ReferencedJob> jobList = new ArrayList<>(vrp.jobsInclusiveInitialJobsInRoutes().values().size());
            for (Job job_j : vrp.jobsInclusiveInitialJobsInRoutes().values()) {
                if (job_i == job_j) continue;
                double distance = jobDistance.getDistance(job_i, job_j);
                if (distance > maxDistance) maxDistance = distance;
                ReferencedJob referencedJob = new ReferencedJob(job_j, distance);
                jobList.add(referencedJob);
            }
            jobList.sort(COMPARATOR);
            int[] jobIndices = new int[capacity];
            for(int index=0;index<capacity;index++){
                jobIndices[index] = jobList.get(index).job.index();
            }
            neighbors[job_i.index()-1] = jobIndices;
        }
        stopWatch.stop();
        logger.debug("pre-processing comp-time: {}", stopWatch);
    }

    private static final Comparator<ReferencedJob> COMPARATOR = Comparator.comparingDouble(o -> o.distance);


    @Override
    public String toString() {
        return "[name=neighborhoodWithCapRestriction][capacity=" + capacity + ']';
    }

}

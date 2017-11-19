package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

public class ClusterRuinStrategyFactory implements RuinStrategyFactory  {

    private final int initialNumberJobsToRemove;
    private final JobNeighborhoods jobNeighborhoods;

    public ClusterRuinStrategyFactory(int initialNumberJobsToRemove, JobNeighborhoods jobNeighborhoods) {
        this.initialNumberJobsToRemove = initialNumberJobsToRemove;
        this.jobNeighborhoods = jobNeighborhoods;
    }

    @Override
    public RuinStrategy createStrategy(VehicleRoutingProblem vrp) {
        return new RuinClusters(vrp, initialNumberJobsToRemove, jobNeighborhoods);
    }
}
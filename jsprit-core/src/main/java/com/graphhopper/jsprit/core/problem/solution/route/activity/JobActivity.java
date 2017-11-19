package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Indexed;
import com.graphhopper.jsprit.core.problem.job.Job;

/**
 * Basic interface of job-activies.
 * <p>
 * <p>A job activity is related to a {@link Job}.
 *
 * @author schroeder
 */
abstract public class JobActivity extends AbstractActivity implements Indexed {

    /**
     * Returns the job that is involved with this activity.
     *
     * @return job
     */
    abstract public Job job();

    @Override
    abstract public JobActivity clone();

}

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

package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobUnassignedListener;
import com.graphhopper.jsprit.core.problem.job.Job;
import org.apache.commons.math3.stat.Frequency;


import java.util.*;

/**
 * Created by schroeder on 06/02/17.
 */
public class UnassignedJobReasonTracker implements JobUnassignedListener {

    public static String getMostLikelyFailedConstraintName(Frequency failedConstraintNamesFrequency) {
        if (failedConstraintNamesFrequency == null) return "no reason found";
        long maxCount = 0;
        Comparable<?> mostLikely = null;
        Iterator<Map.Entry<Comparable<?>, Long>> entryIterator = failedConstraintNamesFrequency.entrySetIterator();
        while (entryIterator.hasNext()) {
            Map.Entry<Comparable<?>, Long> entry = entryIterator.next();
            long ev = entry.getValue();
            if (ev > maxCount) {
                maxCount = ev;
                mostLikely = entry.getKey();
            }
        }
        java.util.
        return mostLikely.toString();
    }

    final Map<String, Frequency> failedConstraintNamesFrequencyMapping = new HashMap<>();

    static final String[] codesToHumanReadableReason = new String[]{
        /* 0 */ null,
        /* 1 */ "cannot serve required skill",
        /* 2 */ "cannot be visited within time window",
        /* 3 */ "does not fit into any vehicle due to capacity",
        /* 4 */ "cannot be assigned due to max distance constraint of vehicle"
    };

    static final Map<String, Integer> failedConstraintNamesToCode = Map.of(
            "HardSkillConstraint", 1,
            "VehicleDependentTimeWindowConstraints", 2,
            "ServiceLoadRouteLevelConstraint", 3,
            "PickupAndDeliverShipmentLoadActivityLevelConstraint", 3,
            "ServiceLoadActivityLevelConstraint", 3,
            "MaxDistanceConstraint", 4
    );

//    final Collection<String> failedConstraintNamesToBeIgnored = new HashSet<>();

    public UnassignedJobReasonTracker() {


    }

//    public void ignore(String simpleNameOfConstraint) {
//        failedConstraintNamesToBeIgnored.add(simpleNameOfConstraint);
//    }

    @Override
    public void informJobUnassigned(Job unassigned, Collection<String> failedConstraintNames) {
        String uid = unassigned.id();
        Frequency m = this.failedConstraintNamesFrequencyMapping
                .computeIfAbsent(uid, (x) -> new Frequency());
        for (String r : failedConstraintNames) {
//            if (failedConstraintNamesToBeIgnored.contains(r)) continue;
            m.addValue(r);
        }
    }

//    public void put(String simpleNameOfFailedConstraint, int code, String reason) {
//        if (code <= 20)
//            throw new IllegalArgumentException("first 20 codes are reserved internally. choose a code > 20");
//        codesToHumanReadableReason.put(code, reason);
//        if (failedConstraintNamesToCode.containsKey(simpleNameOfFailedConstraint)) {
//            throw new IllegalArgumentException(simpleNameOfFailedConstraint + " already assigned to code and reason");
//        } else failedConstraintNamesToCode.put(simpleNameOfFailedConstraint, code);
//    }

//    /**
//     * For each job id, it returns frequency distribution of failed constraints (simple name of constraint) in an unmodifiable map.
//     *
//     * @return
//     */
//    @Deprecated
//    public Map<String, Frequency> getReasons() {
//        return Collections.unmodifiableMap(failedConstraintNamesFrequencyMapping);
//    }

//    /**
//     * For each job id, it returns frequency distribution of failed constraints (simple name of constraint) in an unmodifiable map.
//     *
//     * @return
//     */
//    public Map<String, Frequency> getFailedConstraintNamesFrequencyMapping() {
//        return Collections.unmodifiableMap(failedConstraintNamesFrequencyMapping);
//    }

//    /**
//     * Returns an unmodifiable map of codes and reason pairs.
//     *
//     * @return
//     */
//    public Map<Integer, String> getCodesToReason() {
//        return Collections.unmodifiableMap(codesToHumanReadableReason);
//    }

//    /**
//     * Returns an unmodifiable map of constraint names (simple name of constraint) and reason code pairs.
//     *
//     * @return
//     */
//    public Map<String, Integer> getFailedConstraintNamesToCode() {
//        return Collections.unmodifiableMap(failedConstraintNamesToCode);
//    }

//    public int getCode(String failedConstraintName) {
//        return toCode(failedConstraintName);
//    }

//    public String getHumanReadableReason(int code) {
//        return getCodesToReason().get(code);
//    }
//
//    public String getHumanReadableReason(String failedConstraintName) {
//        return getCodesToReason().get(getCode(failedConstraintName));
//    }

    /**
     * Returns the most likely reason code i.e. the reason (failed constraint) being observed most often.
     * <p>
     * 1 --> "cannot serve required skill
     * 2 --> "cannot be visited within time window"
     * 3 --> "does not fit into any vehicle due to capacity"
     * 4 --> "cannot be assigned due to max distance constraint of vehicle"
     *
     * @param jobId
     * @return
     */
    public int getMostLikelyReasonCode(String jobId) {

        Frequency reasons = this.failedConstraintNamesFrequencyMapping.get(jobId);
        if (reasons == null)
            return -1;

        return toCode(getMostLikelyFailedConstraintName(reasons));
    }

//    /**
//     * Returns the most likely reason i.e. the reason (failed constraint) being observed most often.
//     *
//     * @param jobId
//     * @return
//     */
//    public String getMostLikelyReason(String jobId) {
//        if (!this.failedConstraintNamesFrequencyMapping.containsKey(jobId)) return "no reason found";
//        Frequency reasons = this.failedConstraintNamesFrequencyMapping.get(jobId);
//        String mostLikelyReason = getMostLikelyFailedConstraintName(reasons);
//        int code = toCode(mostLikelyReason);
//        return code == -1 ? mostLikelyReason : codesToHumanReadableReason.get(code);
//    }

    private int toCode(String mostLikelyReason) {
        return failedConstraintNamesToCode.getOrDefault(mostLikelyReason, -1);
    }


}

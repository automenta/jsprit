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

package com.graphhopper.jsprit.core.algorithm.state;

public class InternalStates {


    public final static State MAXLOAD = new StateFactory.StateImpl("max_load", 0);

    public final static State LOAD = new StateFactory.StateImpl("load", 1);

    public final static State COSTS = new StateFactory.StateImpl("costs", 2);

    public final static State LOAD_AT_BEGINNING = new StateFactory.StateImpl("load_at_beginning", 3);

    public final static State LOAD_AT_END = new StateFactory.StateImpl("load_at_end", 4);

    public final static State DURATION = new StateFactory.StateImpl("duration", 5);

    public final static State LATEST_OPERATION_START_TIME = new StateFactory.StateImpl("latest_operation_start_time", 6);

    public final static State EARLIEST_OPERATION_START_TIME = new StateFactory.StateImpl("earliest_operation_start_time", 7);

    public final static State FUTURE_MAXLOAD = new StateFactory.StateImpl("future_max_load", 8);

    public final static State PAST_MAXLOAD = new StateFactory.StateImpl("past_max_load", 9);

    public static final State SKILLS = new StateFactory.StateImpl("skills", 10);

    public static final State WAITING = new StateFactory.StateImpl("waiting", 11);

    public static final State TIME_SLACK = new StateFactory.StateImpl("time_slack", 12);

    public static final State FUTURE_WAITING = new StateFactory.StateImpl("future_waiting", 13);

    public static final State EARLIEST_WITHOUT_WAITING = new StateFactory.StateImpl("earliest_without_waiting", 14);

    public static final State SWITCH_NOT_FEASIBLE = new StateFactory.StateImpl("switch_not_feasible", 15);
}

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

import java.util.Arrays;
import java.util.List;

/**
 * Created by schroeder on 28.07.14.
 */
class StateFactory {

    final static List<String> reservedIds = Arrays.asList("max_load", "load", "costs", "load_at_beginning", "load_at_end", "duration", "latest_operation_start_time", "earliest_operation_start_time"
        , "future_max_load", "past_max_load", "skills");


    static State createId(String name) {
        if (reservedIds.contains(name)) {
            throwReservedIdException(name);
        }
        return new StateImpl(name, -1);
    }

    static State createId(String name, int index) {
        if (reservedIds.contains(name)) throwReservedIdException(name);
        if (index < 10) throwReservedIdException(name);
        return new StateImpl(name, index);
    }


    static boolean isReservedId(String stateId) {
        return reservedIds.contains(stateId);
    }

    static boolean isReservedId(State state) {
        return reservedIds.contains(state.toString());
    }

    static void throwReservedIdException(String name) {
        throw new IllegalStateException("state-id with name '" + name + "' cannot be created. it is already reserved internally.");
    }


    static class StateImpl implements State {

        private final int index;

        @Override
        public int index() {
            return index;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StateImpl other = (StateImpl) obj;
            return name == null ? other.name == null : name.equals(other.name);
        }

        private final String name;

        public StateImpl(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public String toString() {
            return name;
        }
    }
}

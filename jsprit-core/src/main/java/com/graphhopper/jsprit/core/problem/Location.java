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

package com.graphhopper.jsprit.core.problem;

import com.graphhopper.jsprit.core.util.v2;

/**
 * Created by schroeder on 16.12.14.
 */
public final class Location implements Indexed, HasId {

    private final int hash;

    /**
     * Factory method (and shortcut) for creating a location object just with x and y coordinates.
     *
     * @param x coordinate
     * @param y coordinate
     * @return location
     */
    public static Location the(double x, double y) {
        return Location.Builder.the().setCoord(v2.the(x, y)).build();
    }

    /**
     * Factory method (and shortcut) for creating location object just with id
     *
     * @param id location id
     * @return location
     */
    public static Location the(String id) {
        return Location.Builder.the().setId(id).build();
    }

    /**
     * Factory method (and shortcut) for creating location object just with location index
     *
     * @param index
     * @return
     */
    public static Location the(int index) {
        return Location.Builder.the().setIndex(index).build();
    }

    public static class Builder {

        private String id;

        private int index = Location.NO_INDEX;

        private v2 coord;

        private String name = "";

        private Object data;

        public static Builder the() {
            return new Builder();
        }

        /**
         * Sets user specific domain data associated with the object.
         * <p>
         * <p>
         * The user data is a black box for the framework, it only stores it,
         * but never interacts with it in any way.
         * </p>
         *
         * @param data any object holding the domain specific user data
         *                 associated with the object.
         * @return builder
         */
        public Builder setData(Object data) {
            this.data = data;
            return this;
        }

        /**
         * Sets location index
         *
         * @param index
         * @return the builder
         */
        public Builder setIndex(int index) {
            if (index < 0) throw new IllegalArgumentException("index must be >= 0");
            this.index = index;
            return this;
        }

        /**
         * Sets coordinate of location
         *
         * @param coord
         * @return
         */
        public Builder setCoord(v2 coord) {
            this.coord = coord;
            return this;
        }

        /**
         * Sets location id
         *
         * @param id
         * @return
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Adds name, e.g. street name, to location
         *
         * @param name
         * @return
         */
        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Location build() {
            if (id == null && coord == null) {
                if (index == -1) throw new IllegalArgumentException("either id or coordinate or index must be set");
            }
            if (coord != null && id == null) {
                this.id = coord.toString();
            }
            if (index != -1 && id == null) {
                this.id = Integer.toString(index);
            }
            return new Location(this);
        }

    }

    public final static int NO_INDEX = -1;

    public final int index;

    public final v2 coord;

    public final String id;

    public final String name;

    public final Object data;

    private Location(Builder builder) {
        this.data = builder.data;
        this.index = builder.index;
        this.coord = builder.coord;
        this.id = builder.id;
        this.name = builder.name;

         int hash = index;
        hash = 31 * hash + (coord != null ? coord.hashCode() : 0);
        hash = 31 * hash + (id != null ? id.hashCode() : 0);
        this.hash = hash;
    }

    /**
     * @return User-specific domain data associated by the job
     */
    public final Object data() {
        return data;
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final int index() {
        return index;
    }

    public final v2 coord() {
        return coord;
    }

    public final String name() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location) || hash != o.hashCode()) return false;

        Location location = (Location) o;

        if (index != location.index) return false;
        if (coord != null ? !coord.equals(location.coord) : location.coord != null) return false;
        return id != null ? id.equals(location.id) : location.id == null;
    }

    @Override
    public final int hashCode() {
       return hash;
    }

    @Override
    public String toString() {
        return "[id=" + id + "][index=" + index + "][coordinate=" + coord + ']';
    }
}

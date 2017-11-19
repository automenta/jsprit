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
package com.graphhopper.jsprit.core.problem.vehicle;

import com.graphhopper.jsprit.core.problem.AbstractVehicle;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.job.Break;


/**
 * Implementation of {@link Vehicle}.
 *
 * @author stefan schroeder
 */

public class VehicleImpl extends AbstractVehicle {


    private final int hash;

    /**
     * Extension of {@link VehicleImpl} representing an unspecified vehicle with the id 'noVehicle'
     * (to avoid null).
     *
     * @author schroeder
     */
    public static class NoVehicle extends AbstractVehicle {

        private final String id = "noVehicle";

        private final VehicleType type = VehicleTypeImpl.Builder.the("noType").build();

        public NoVehicle() {
        }

        @Override
        public double earliestDeparture() {
            return 0;
        }

        @Override
        public double latestArrival() {
            return 0;
        }

        @Override
        public final VehicleType type() {
            return type;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public boolean isReturnToDepot() {
            return false;
        }

        @Override
        public Location start() {
            return null;
        }

        @Override
        public Location end() {
            return null;
        }

        @Override
        public Skills skills() {
            return null;
        }


        @Override
        public Break aBreak() {
            return null;
        }

    }

    /**
     * Builder that builds the vehicle.
     * <p>
     * <p>By default, earliestDepartureTime is 0.0, latestDepartureTime is Double.MAX_VALUE,
     * it returns to the depot and its {@link VehicleType} is the DefaultType with typeId equal to 'default'
     * and a capacity of 0.
     *
     * @author stefan
     */
    public static class Builder {

//        static final Logger log = LoggerFactory.getLogger(Builder.class.getName());

        private final String id;

        private double earliestStart;

        private double latestArrival = Double.MAX_VALUE;

        private boolean returnToDepot = true;

        private VehicleType type = VehicleTypeImpl.Builder.the("default").build();

        private final Skills.Builder skillBuilder = Skills.Builder.newInstance();

        private Skills skills;

        private Location startLocation;

        private Location endLocation;

        private Break aBreak;

        private Object userData;

        private Builder(String id) {
            this.id = id;
        }

        /**
         * Sets the {@link VehicleType}.<br>
         *
         * @param type the type to be set
         * @return this builder
         * @throws IllegalArgumentException if type is null
         */
        public Builder setType(VehicleType type) {
            if (type == null) throw new IllegalArgumentException("type cannot be null.");
            this.type = type;
            return this;
        }

        /**
         * Sets user specific domain data associated with the object.
         * <p>
         * <p>
         * The user data is a black box for the framework, it only stores it,
         * but never interacts with it in any way.
         * </p>
         *
         * @param userData any object holding the domain specific user data
         *                 associated with the object.
         * @return builder
         */
        public Builder setUserData(Object userData) {
            this.userData = userData;
            return this;
        }

        /**
         * Sets the flag whether the vehicle must return to depot or not.
         * <p>
         * <p>
         * If returnToDepot is true, the vehicle must return to specified
         * end-location. If you omit specifying the end-location, vehicle
         * returns to start-location (that must to be set). If you specify it,
         * it returns to specified end-location.
         * <p>
         * <p>
         * If returnToDepot is false, the end-location of the vehicle is
         * endogenous.
         *
         * @param returnToDepot
         *            true if vehicle need to return to depot, otherwise false
         * @return this builder
         */
        public Builder setReturnToDepot(boolean returnToDepot) {
            this.returnToDepot = returnToDepot;
            return this;
        }

        /**
         * Sets start location.
         *
         * @param startLocation start location
         * @return start location
         */
        public Builder setStartLocation(Location startLocation) {
            this.startLocation = startLocation;
            return this;
        }

        public Builder setEndLocation(Location endLocation) {
            this.endLocation = endLocation;
            return this;
        }

        /**
         * Sets earliest-start of vehicle which should be the lower bound of the vehicle's departure times.
         *
         * @param earliest_startTime the earliest start time / departure time of the vehicle at its start location
         * @return this builder
         */
        public Builder setEarliestStart(double earliest_startTime) {
            if (earliest_startTime < 0)
                throw new IllegalArgumentException("earliest start of vehicle " + id + " must not be negative");
            this.earliestStart = earliest_startTime;
            return this;
        }

        /**
         * Sets the latest arrival at vehicle's end-location which is the upper bound of the vehicle's arrival times.
         *
         * @param latest_arrTime the latest arrival time of the vehicle at its end location
         * @return this builder
         */
        public Builder setLatestArrival(double latest_arrTime) {
            if (latest_arrTime < 0)
                throw new IllegalArgumentException("latest arrival time of vehicle " + id + " must not be negative");
            this.latestArrival = latest_arrTime;
            return this;
        }

        public Builder addSkill(String skill) {
            skillBuilder.addSkill(skill);
            return this;
        }

        /**
         * Builds and returns the vehicle.
         * <p>
         * <p>if {@link VehicleType} is not set, default vehicle-type is set with id="default" and
         * capacity=0
         * <p>
         * <p>if startLocationId || locationId is null (=> startLocationCoordinate || locationCoordinate must be set) then startLocationId=startLocationCoordinate.toString()
         * and locationId=locationCoordinate.toString() [coord.toString() --> [x=x_val][y=y_val])
         * <p>if endLocationId is null and endLocationCoordinate is set then endLocationId=endLocationCoordinate.toString()
         * <p>if endLocationId==null AND endLocationCoordinate==null then endLocationId=startLocationId AND endLocationCoord=startLocationCoord
         * Thus endLocationId can never be null even returnToDepot is false.
         *
         * @return vehicle
         * @throws IllegalArgumentException if both locationId and locationCoord is not set or (endLocationCoord!=null AND returnToDepot=false)
         *                               or (endLocationId!=null AND returnToDepot=false)
         */
        public VehicleImpl build() {
            if (latestArrival < earliestStart)
                throw new IllegalArgumentException("latest arrival of vehicle " + id + " must not be smaller than its start time");
            if (startLocation != null && endLocation != null) {
                if (!startLocation.id.equals(endLocation.id) && !returnToDepot)
                    throw new IllegalArgumentException("this must not be. you specified both endLocationId and open-routes. this is contradictory. <br>" +
                        "if you set endLocation, returnToDepot must be true. if returnToDepot is false, endLocationCoord must not be specified.");
            }
            if (startLocation != null && endLocation == null) {
                endLocation = startLocation;
            }
            if (startLocation == null && endLocation == null)
                throw new IllegalArgumentException("vehicle requires startLocation. but neither locationId nor locationCoord nor startLocationId nor startLocationCoord has been set");
            skills = skillBuilder.build();
            return new VehicleImpl(this);
        }

        /**
         * Returns new instance of vehicle builder.
         *
         * @param vehicleId the id of the vehicle which must be a unique identifier among all vehicles
         * @return vehicle builder
         */
        public static Builder newInstance(String vehicleId) {
            return new Builder(vehicleId);
        }

        public Builder addSkills(Skills skills) {
            this.skillBuilder.addAllSkills(skills.values());
            return this;
        }

        public Builder setBreak(Break aBreak) {
            this.aBreak = aBreak;
            return this;
        }
    }

    /**
     * Returns empty/noVehicle which is a vehicle having no capacity, no type and no reasonable id.
     * <p>
     * <p>NoVehicle has id="noVehicle" and extends {@link VehicleImpl}
     *
     * @return emptyVehicle
     */
    public static Vehicle get() {
        return new NoVehicle();
    }

    public final String id;

    public final VehicleType type;

    public final double earliestDeparture;

    public final double latestArrival;

    public final boolean returnToDepot;

    public final Skills skills;

    public final Location end;

    public final Location start;

    public final Break aBreak;

    private VehicleImpl(Builder builder) {
        data(builder.userData);
        id = builder.id;
        type = builder.type;
        earliestDeparture = builder.earliestStart;
        latestArrival = builder.latestArrival;
        returnToDepot = builder.returnToDepot;
        skills = builder.skills;
        end = builder.endLocation;
        start = builder.startLocation;
        aBreak = builder.aBreak;
        //        setVehicleIdentifier(new VehicleTypeKey(type.getTypeId(),startLocation.getId(),endLocation.getId(),earliestDeparture,latestArrival,skills));
        vehicleType(new VehicleTypeKey(type.type(), start.id, end.id, earliestDeparture, latestArrival, skills, returnToDepot));

        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ((id == null) ? 0 : id.hashCode());
        hash = prime * hash + type.hashCode();
        this.hash = hash;
    }

    /**
     * Returns String with attributes of this vehicle
     * <p>
     * <p>String has the following format [attr1=val1][attr2=val2]...[attrn=valn]
     */
    @Override
    public String toString() {
        return "[id=" + id + ']' +
            "[type=" + type + ']' +
            "[startLocation=" + start + ']' +
            "[endLocation=" + end + ']' +
            "[isReturnToDepot=" + returnToDepot + ']' +
            "[skills=" + skills + ']';
    }


    @Override
    public final double earliestDeparture() {
        return earliestDeparture;
    }

    @Override
    public final double latestArrival() {
        return latestArrival;
    }

    @Override
    public final VehicleType type() {
        return type;
    }

    @Override
    public final String id() {
        return id;
    }

    @Override
    public final boolean isReturnToDepot() {
        return returnToDepot;
    }

    @Override
    public final Location start() {
        return start;
    }

    @Override
    public final Location end() {
        return end;
    }

    @Override
    public final Skills skills() {
        return skills;
    }

    @Override
    public final Break aBreak() {
        return aBreak;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return hash;
    }

    /**
     * Two vehicles are equal if they have the same id and if their types are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VehicleImpl other = (VehicleImpl) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return type == null ? other.type == null : type.equals(other.type);
    }

}


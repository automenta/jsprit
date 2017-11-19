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

import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeKey;
import com.graphhopper.jsprit.core.util.v2;
import com.graphhopper.jsprit.core.util.CrowFlyCosts;
import com.graphhopper.jsprit.core.util.Locations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;



/**
 * Contains and defines the vehicle routing problem.
 * <p>
 * <p>A routing problem is defined as jobs, vehicles, costs and constraints.
 * <p>
 * <p> To construct the problem, use VehicleRoutingProblem.Builder. Get an instance of this by using the static method VehicleRoutingProblem.Builder.newInstance().
 * <p>
 * <p>By default, fleetSize is INFINITE, transport-costs are calculated as euclidean-distance (CrowFlyCosts),
 * and activity-costs are set to zero.
 *
 * @author stefan schroeder
 */
public class VehicleRoutingProblem {

    /**
     * Builder to build the routing-problem.
     *
     * @author stefan schroeder
     */
    public static class Builder {


        /**
         * Returns a new instance of this builder.
         *
         * @return builder
         */
        public static Builder get() {
            return new Builder();
        }

        private VehicleRoutingTransportCosts transportCosts;

        private VehicleRoutingActivityCosts activityCosts = new WaitingTimeCosts();

        private final Map<String, Job> jobs = new LinkedHashMap<>();

        private final Map<String, Job> tentativeJobs = new LinkedHashMap<>();

        private final Set<Job> jobsInInitialRoutes = new HashSet<>();

        private final Map<String, v2> tentative_coordinates = new HashMap<>();

        private FleetSize fleetSize = FleetSize.INFINITE;

        private final Set<VehicleType> vehicleTypes = new HashSet<>();

        private final Set<VehicleRoute> initialRoutes = new LinkedHashSet();

        private final Set<Vehicle> uniqueVehicles = new LinkedHashSet<>();

        private final Set<String> addedVehicleIds = new LinkedHashSet<>();

//        private final boolean hasBreaks;

        private JobActivityFactory jobActivityFactory = new JobActivityFactory() {

            @Override
            public List<JobActivity> the(Job job) {
                List<JobActivity> acts = new ArrayList<>(2);
                if( job instanceof Break){
                    acts.add(BreakActivity.the((Break) job));
                }
                else if (job instanceof Service) {
                    acts.add(serviceActivityFactory.createActivity((Service) job));
                } else if (job instanceof Shipment) {
                    acts.add(shipmentActivityFactory.createPickup((Shipment) job));
                    acts.add(shipmentActivityFactory.createDelivery((Shipment) job));
                }
                return acts;
            }

        };

        private int vehicleIndexCounter = 1;

        private int activityIndexCounter = 1;

        private int vehicleTypeIdIndexCounter = 1;

        private final Map<VehicleTypeKey, Integer> typeKeyIndices = new HashMap<>();

        private final Map<Job, List<JobActivity>> activityMap = new HashMap<>();

        private final TourShipmentActivityFactory shipmentActivityFactory = new DefaultShipmentActivityFactory();

        private final TourActivityFactory serviceActivityFactory = new DefaultTourActivityFactory();

        private void incActivityIndexCounter() {
            activityIndexCounter++;
        }

        private void incVehicleTypeIdIndexCounter() {
            vehicleTypeIdIndexCounter++;
        }

        private final Set<Location> allLocations = new LinkedHashSet<>();

        /**
         * Returns the unmodifiable map of collected locations (mapped by their location-id).
         *
         * @return map with locations
         */
        public Map<String, v2> locations() {
            return Collections.unmodifiableMap(tentative_coordinates);
        }



        /**
         * Returns the locations collected SO FAR by this builder.
         * <p>
         * <p>Locations are cached when adding a shipment, service, depot, vehicle.
         *
         * @return locations
         */
        public Locations getLocations() {
            return tentative_coordinates::get;
        }

        /**
         * Sets routing costs.
         *
         * @param costs the routingCosts
         * @return builder
         * @see VehicleRoutingTransportCosts
         */
        public Builder setRoutingCost(VehicleRoutingTransportCosts costs) {
            this.transportCosts = costs;
            return this;
        }


        public Builder setJobActivityFactory(JobActivityFactory jobActivityFactory) {
            this.jobActivityFactory = jobActivityFactory;
            return this;
        }

        /**
         * Sets the type of fleetSize.
         * <p>
         * <p>FleetSize is either FleetSize.INFINITE or FleetSize.FINITE. By default it is FleetSize.INFINITE.
         *
         * @param fleetSize the fleet size used in this problem. it can either be FleetSize.INFINITE or FleetSize.FINITE
         * @return this builder
         */
        public Builder setFleetSize(FleetSize fleetSize) {
            this.fleetSize = fleetSize;
            return this;
        }

        /**
         * Adds a job which is either a service or a shipment.
         * <p>
         * <p>Note that job.getId() must be unique, i.e. no job (either it is a shipment or a service) is allowed to have an already allocated id.
         *
         * @param job job to be added
         * @return this builder
         * @throws IllegalStateException if job is neither a shipment nor a service, or jobId has already been added.
         *
         */
        public Builder addJob(Job job) {
            if (!(job instanceof AbstractJob)) throw new IllegalArgumentException("job must be of type AbstractJob");
            if (tentativeJobs.containsKey(job.id()))
                throw new IllegalArgumentException("vehicle routing problem already contains a service or shipment with id " + job.id() + ". make sure you use unique ids for all services and shipments");
            if (!(job instanceof Service || job instanceof Shipment))
                throw new IllegalArgumentException("job must be either a service or a shipment");
            tentativeJobs.put(job.id(), job);
            addLocationToTentativeLocations(job);
            return this;
        }

        private void addLocationToTentativeLocations(Job job) {
            if (job instanceof Service) {
                Location location = ((Service) job).location;
//                tentative_coordinates.put(location.getId(), location.getCoordinate());
                addLocationToTentativeLocations(location);
            } else if (job instanceof Shipment) {
                Shipment shipment = (Shipment) job;
                Location pickupLocation = shipment.getPickupLocation();
                addLocationToTentativeLocations(pickupLocation);
//                tentative_coordinates.put(pickupLocation.getId(), pickupLocation.getCoordinate());
                Location deliveryLocation = shipment.getDeliveryLocation();
                addLocationToTentativeLocations(deliveryLocation);
//                tentative_coordinates.put(deliveryLocation.getId(), deliveryLocation.getCoordinate());
            }
        }

        private void addLocationToTentativeLocations(Location location) {
            if (allLocations.add(location))
                tentative_coordinates.put(location.id, location.coord);
        }

        private void addJobToFinalJobMapAndCreateActivities(Job job) {
            if (job instanceof Service) {
                Service service = (Service) job;
                addService(service);
            } else if (job instanceof Shipment) {
                Shipment shipment = (Shipment) job;
                addShipment(shipment);
            }
            List<JobActivity> jobActs = jobActivityFactory.the(job);
            for (JobActivity act : jobActs) {
                act.index(activityIndexCounter);
                incActivityIndexCounter();
            }
            activityMap.put(job, (List)jobActs);
        }

        private boolean addBreaksToActivityMap() {
            boolean hasBreaks = false;
            Collection<String> uniqueBreakIds = new HashSet<>();
            for (Vehicle v : uniqueVehicles) {
                if (v.aBreak() != null) {
                    if (!uniqueBreakIds.add(v.aBreak().id))
                        throw new IllegalArgumentException("problem already contains a vehicle break with id " + v.aBreak().id + ". choose unique ids for each vehicle break.");
                    hasBreaks = true;

                    List<JobActivity> breakActivities = jobActivityFactory.the(v.aBreak());
                    if(breakActivities.isEmpty())
                        throw new IllegalArgumentException("at least one activity for break needs to be created by activityFactory");

                    for(AbstractActivity act : breakActivities){
                        act.index(activityIndexCounter);
                        incActivityIndexCounter();
                    }
                    activityMap.put(v.aBreak(), (List)breakActivities);
                }
            }
            return hasBreaks;
        }

        /**
         * Adds an initial vehicle route.
         *
         * @param route initial route
         * @return the builder
         */
        public Builder addInitialVehicleRoute(VehicleRoute route) {
            if(!addedVehicleIds.contains(route.vehicle().id())){
                addVehicle((AbstractVehicle) route.vehicle());
            }
            for (AbstractActivity act : route.activities()) {
                AbstractActivity abstractAct = act;
                abstractAct.index(activityIndexCounter);
                incActivityIndexCounter();
                if (act instanceof JobActivity) {
                    Job job = ((JobActivity) act).job();
                    if (jobsInInitialRoutes.add(job)) {
                        addLocationToTentativeLocations(job);
                        registerJobAndActivity((JobActivity) abstractAct, job);
                    }
                }
            }
            initialRoutes.add(route);
            return this;
        }



        private void registerJobAndActivity(JobActivity abstractAct, Job job) {
            activityMap.computeIfAbsent(job, (k)->new ArrayList()).add(abstractAct);
        }

        /**
         * Adds a collection of initial vehicle routes.
         *
         * @param routes initial routes
         * @return the builder
         */
        public Builder addInitialVehicleRoutes(Iterable<VehicleRoute> routes) {
            for (VehicleRoute r : routes) {
                addInitialVehicleRoute(r);
            }
            return this;
        }

        private void addShipment(Job job) {
            if (jobs.putIfAbsent(job.id(), job)!=null) {
                logger.warn("job {} already in job list. overrides existing job.", job);
            }
            addLocationToTentativeLocations(job);
//            tentative_coordinates.put(job.getPickupLocation().getId(), job.getPickupLocation().getCoordinate());
//            tentative_coordinates.put(job.getDeliveryLocation().getId(), job.getDeliveryLocation().getCoordinate());
        }

        /**
         * Adds a vehicle.
         *
         * @param vehicle vehicle to be added
         * @return this builder
         * */
        public Builder addVehicle(Vehicle vehicle) {
            if (!(vehicle instanceof AbstractVehicle))
                throw new IllegalArgumentException("vehicle must be an AbstractVehicle");
            return addVehicle((AbstractVehicle) vehicle);
        }

        /**
         * Adds a vehicle.
         *
         * @param vehicle vehicle to be added
         * @return this builder
         */
        public Builder addVehicle(AbstractVehicle vehicle) {
            if(!addedVehicleIds.add(vehicle.id())){
                throw new IllegalArgumentException("problem already contains a vehicle with id " + vehicle.id() + ". choose unique ids for each vehicle.");
            }

            if (!uniqueVehicles.contains(vehicle)) {
                vehicle.setIndex(vehicleIndexCounter);
                incVehicleIndexCounter();
            }
            if (typeKeyIndices.containsKey(vehicle.vehicleType())) {
                vehicle.vehicleType().setIndex(typeKeyIndices.get(vehicle.vehicleType()));
            } else {
                vehicle.vehicleType().setIndex(vehicleTypeIdIndexCounter);
                typeKeyIndices.put(vehicle.vehicleType(), vehicleTypeIdIndexCounter);
                incVehicleTypeIdIndexCounter();
            }
            uniqueVehicles.add(vehicle);

            vehicleTypes.add(vehicle.type());

            String startLocationId = vehicle.start().id;
            addLocationToTentativeLocations(vehicle.start());
//            tentative_coordinates.put(startLocationId, vehicle.getStartLocation().getCoordinate());
            if (!vehicle.end().id.equals(startLocationId)) {
                addLocationToTentativeLocations(vehicle.end());
//                tentative_coordinates.put(vehicle.getEndLocation().getId(), vehicle.getEndLocation().getCoordinate());
            }
            return this;
        }

        private void incVehicleIndexCounter() {
            vehicleIndexCounter++;
        }

        /**
         * Sets the activity-costs.
         * <p>
         * <p>By default it is set to zero.
         *
         * @param activityCosts activity costs of the problem
         * @return this builder
         * @see VehicleRoutingActivityCosts
         */
        public Builder setActivityCosts(VehicleRoutingActivityCosts activityCosts) {
            this.activityCosts = activityCosts;
            return this;
        }

        /**
         * Builds the {@link VehicleRoutingProblem}.
         * <p>
         * <p>If {@link VehicleRoutingTransportCosts} are not set, {@link CrowFlyCosts} is used.
         *
         * @return {@link VehicleRoutingProblem}
         */
        public VehicleRoutingProblem build() {
            if (transportCosts == null) {
                transportCosts = new CrowFlyCosts(getLocations());
            }
            for (Job job : tentativeJobs.values()) {
                if (!jobsInInitialRoutes.contains(job)) {
                    addJobToFinalJobMapAndCreateActivities(job);
                }
            }
            
            int jobIndexCounter = 1;
            for (Job job : jobs.values()) {
                ((AbstractJob)job).setIndex(jobIndexCounter++);
            }
            for (Job job : jobsInInitialRoutes) {
                ((AbstractJob)job).setIndex(jobIndexCounter++);
            }
            
            boolean hasBreaks = addBreaksToActivityMap();
            if (hasBreaks && fleetSize == FleetSize.INFINITE)
                throw new UnsupportedOperationException("breaks are not yet supported when dealing with infinite fleet. either set it to finite or omit breaks.");
            return new VehicleRoutingProblem(this);
        }

        public Builder addLocation(String locationId, v2 coordinate) {
            tentative_coordinates.put(locationId, coordinate);
            return this;
        }

        /**
         * Adds a collection of jobs.
         *
         * @param jobs which is a collection of jobs that subclasses Job
         * @return this builder
         */
        public Builder addAllJobs(Iterable<? extends Job> jobs) {
            for (Job j : jobs) {
                addJob(j);
            }
            return this;
        }


        /**
         * Adds a collection of vehicles.
         *
         * @param vehicles vehicles to be added
         * @return this builder
         */
        public Builder addAllVehicles(Iterable<? extends Vehicle> vehicles) {
            for (Vehicle v : vehicles) {
                addVehicle(v);
            }
            return this;
        }

        /**
         * Gets an unmodifiable collection of already added vehicles.
         *
         * @return collection of vehicles
         */
        public Collection<Vehicle> getAddedVehicles() {
            return Collections.unmodifiableCollection(uniqueVehicles);
        }

        /**
         * Gets an unmodifiable collection of already added vehicle-types.
         *
         * @return collection of vehicle-types
         */
        public Collection<VehicleType> getAddedVehicleTypes() {
            return Collections.unmodifiableCollection(vehicleTypes);
        }

        /**
         * Returns an unmodifiable collection of already added jobs.
         *
         * @return collection of jobs
         */
        public Collection<Job> getAddedJobs() {
            return Collections.unmodifiableCollection(tentativeJobs.values());
        }

        private Builder addService(Service service) {
//            tentative_coordinates.put(service.getLocation().getId(), service.getLocation().getCoordinate());
            addLocationToTentativeLocations(service);
            if (jobs.containsKey(service.id)) {
                logger.warn("service {} already in job list. overrides existing job.", service);
            }
            jobs.put(service.id, service);
            return this;
        }


    }

    /**
     * Enum that characterizes the fleet-size.
     *
     * @author sschroeder
     */
    public enum FleetSize {
        FINITE, INFINITE
    }

    /**
     * logger logging for this class
     */
    private final static Logger logger = LoggerFactory.getLogger(VehicleRoutingProblem.class);

    /**
     * contains transportation costs, i.e. the costs traveling from location A to B
     */
    private final VehicleRoutingTransportCosts transportCosts;

    /**
     * contains activity costs, i.e. the costs imposed by an activity
     */
    private final VehicleRoutingActivityCosts activityCosts;

    /**
     * map of jobs, stored by jobId
     */
    private final Map<String, Job> jobs;

    private final Map<String, Job> allJobs;
    /**
     * Collection that contains available vehicles.
     */
    private final Collection<Vehicle> vehicles;

    /**
     * Collection that contains all available types.
     */
    private final Collection<VehicleType> vehicleTypes;


    private final Collection<VehicleRoute> initialVehicleRoutes;

    private final Collection<Location> allLocations;

    /**
     * An enum that indicates type of fleetSize. By default, it is INFINTE
     */
    private final FleetSize fleetSize;

    private final Map<Job, List<JobActivity>> activityMap;

    private final int nuActivities;

    private final JobActivityFactory jobActivityFactory = this::copyAndGetActivities;

    private VehicleRoutingProblem(Builder builder) {
        this.jobs = builder.jobs;
        this.fleetSize = builder.fleetSize;
        this.vehicles = builder.uniqueVehicles;
        this.vehicleTypes = builder.vehicleTypes;
        this.initialVehicleRoutes = builder.initialRoutes;
        this.transportCosts = builder.transportCosts;
        this.activityCosts = builder.activityCosts;
        this.activityMap = builder.activityMap;
        this.nuActivities = builder.activityIndexCounter;
        this.allLocations = builder.allLocations;
        this.allJobs = builder.tentativeJobs;
        logger.info("setup problem: {}", this);
    }


    @Override
    public String toString() {
        return "[fleetSize=" + fleetSize + "][#jobs=" + jobs.size() + "][#vehicles=" + vehicles.size() + "][#vehicleTypes=" + vehicleTypes.size() + "][" +
            "transportCost=" + transportCosts + "][activityCosts=" + activityCosts + ']';
    }

    /**
     * Returns type of fleetSize, either INFINITE or FINITE.
     * <p>
     * <p>By default, it is INFINITE.
     *
     * @return either FleetSize.INFINITE or FleetSize.FINITE
     */
    public FleetSize getFleetSize() {
        return fleetSize;
    }

    /**
     * Returns the unmodifiable job map.
     *
     * @return unmodifiable jobMap
     */
    public Map<String, Job> jobs() {
        return Collections.unmodifiableMap(jobs);
    }

    public Map<String, Job> jobsInclusiveInitialJobsInRoutes(){
        return Collections.unmodifiableMap(allJobs);
    }
    /**
     * Returns a copy of initial vehicle routes.
     *
     * @return copied collection of initial vehicle routes
     */
    public Set<VehicleRoute> initialVehicleRoutes() {
        Set<VehicleRoute> copiedInitialRoutes = new HashSet<>(initialVehicleRoutes.size());
        for (VehicleRoute route : initialVehicleRoutes) {
            copiedInitialRoutes.add(VehicleRoute.copyOf(route));
        }
        return copiedInitialRoutes;
    }

    /**
     * Returns the entire, unmodifiable collection of types.
     *
     * @return unmodifiable collection of types
     * @see com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl
     */
    public Collection<VehicleType> types() {
        return Collections.unmodifiableCollection(vehicleTypes);
    }


    /**
     * Returns the entire, unmodifiable collection of vehicles.
     *
     * @return unmodifiable collection of vehicles
     * @see Vehicle
     */
    public Collection<Vehicle> vehicles() {
        return Collections.unmodifiableCollection(vehicles);
    }

    /**
     * Returns routing costs.
     *
     * @return routingCosts
     * @see VehicleRoutingTransportCosts
     */
    public VehicleRoutingTransportCosts transportCosts() {
        return transportCosts;
    }

    /**
     * Returns activityCosts.
     */
    public VehicleRoutingActivityCosts activityCosts() {
        return activityCosts;
    }

    public Collection<Location> locations(){
        return allLocations;
    }
    /**
     * @param job for which the corresponding activities needs to be returned
     * @return associated activities
     */
    public List<AbstractActivity> activities(Job job) {
        return Collections.unmodifiableList(activityMap.get(job));
    }

//    public Map<Job,List<AbstractActivity>> getActivityMap() { return Collections.unmodifiableMap(activityMap); }

    /**
     * @return total number of activities
     */
    public int activitiesCount() {
        return nuActivities;
    }

    /**
     * @return factory that creates the activities associated to a job
     */
    public JobActivityFactory jobActivityFactory() {
        return jobActivityFactory;
    }

    /**
     * @param job for which the corresponding activities needs to be returned
     * @return a copy of the activities that are associated to the specified job
     */
    public List<JobActivity>  copyAndGetActivities(Job job) {

        List<JobActivity> j = activityMap.get(job);
        if (j!=null) {
            int jj = j.size();
            List<JobActivity> acts = new ArrayList<>(jj);
            for (int i = 0, jSize = jj; i < jSize; i++) {
                acts.add(j.get(i).clone());
            }
            return acts;
        } else {
            return new ArrayList(1);
        }

    }

}

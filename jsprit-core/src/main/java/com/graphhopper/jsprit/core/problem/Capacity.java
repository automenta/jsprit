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

import java.util.Arrays;

/**
 * Capacity with an arbitrary number of capacity-dimension.
 * <p>
 * <p>Note that this assumes the the values of each capacity dimension can be added up and subtracted
 *
 * @author schroeder
 */
public class Capacity {

    private final int hash;

    /**
     * Adds up two capacities, i.e. sums up each and every capacity dimension, and returns the resulting Capacity.
     * <p>
     * <p>Note that this assumes that capacity dimension can be added up.
     *
     * @param cap1 capacity to be added up
     * @param cap2 capacity to be added up
     * @return new capacity
     * @throws NullPointerException if one of the args is null
     */
    public static Capacity addup(Capacity cap1, Capacity cap2) {
        if (cap1 == null || cap2 == null)
            throw new NullPointerException();
        Capacity.Builder capacityBuilder = Capacity.Builder.get();
        int max = Math.max(cap1.dim(), cap2.dim());
        for (int i = 0; i < max; i++) {
            capacityBuilder.addDimension(i, cap1.get(i) + cap2.get(i));
        }
        return capacityBuilder.build();
    }

    /**
     * Subtracts cap2subtract from cap and returns the resulting Capacity.
     *
     * @param cap          capacity to be subtracted from
     * @param cap2subtract capacity to subtract
     * @return new capacity
     * @throws NullPointerException  if one of the args is null
     * @throws IllegalStateException if number of capacityDimensions of cap1 and cap2 are different (i.e. <code>cap1.getNuOfDimension() != cap2.getNuOfDimension()</code>).
     */
    public static Capacity subtract(Capacity cap, Capacity cap2subtract) {
        if (cap == null || cap2subtract == null) throw new NullPointerException("arguments must not be null");
        Capacity.Builder capacityBuilder = Capacity.Builder.get();
        int max = Math.max(cap.dim(), cap2subtract.dim());
        for (int i = 0; i < max; i++) {
            int dimValue = cap.get(i) - cap2subtract.get(i);
            capacityBuilder.addDimension(i, dimValue);
        }
        return capacityBuilder.build();
    }

    /**
     * Returns the inverted capacity, i.e. it multiplies all capacity dimensions with -1.
     *
     * @param cap2invert capacity to be inverted
     * @return inverted capacity
     * @throws NullPointerException if one of the args is null
     */
    public static Capacity invert(Capacity cap2invert) {
        if (cap2invert == null) throw new NullPointerException("arguments must not be null");
        Capacity.Builder capacityBuilder = Capacity.Builder.get();
        int d = cap2invert.dim();
        for (int i = 0; i < d; i++) {
            int dimValue = cap2invert.get(i) * -1;
            capacityBuilder.addDimension(i, dimValue);
        }
        return capacityBuilder.build();
    }

    /**
     * Divides every dimension of numerator capacity by the corresponding dimension of denominator capacity,
     * , and averages each quotient.
     * <p>
     * <p>If both nominator.get(i) and denominator.get(i) equal to 0, dimension i is ignored.
     * <p>If both capacities are have only dimensions with dimensionVal=0, it returns 0.0
     *
     * @param numerator   the numerator
     * @param denominator the denominator
     * @return quotient
     * @throws IllegalStateException if numerator.get(i) != 0 and denominator.get(i) == 0
     */
    public static double divide(Capacity numerator, Capacity denominator) {
        int nuOfDimensions = 0;
        double sumQuotients = 0.0;
        int max = Math.max(numerator.dim(), denominator.dim());
        for (int index = 0; index < max; index++) {
            int ni = numerator.get(index);
            int di = denominator.get(index);
            if (ni != 0 && di == 0) {
                throw new IllegalArgumentException("numerator > 0 and denominator = 0. cannot divide by 0");
            } else if (ni == 0 && di == 0) {
                continue;
            } else {
                nuOfDimensions++;
                sumQuotients += ((double) ni) / di;
            }
        }
        return nuOfDimensions > 0 ? sumQuotients / nuOfDimensions : 0.0;
    }

//    /**
//     * Makes a deep copy of Capacity.
//     *
//     * @param capacity capacity to be copied
//     * @return copy
//     */
//    public static Capacity copyOf(Capacity capacity) {
//        if (capacity == null) return null;
//        return new Capacity(capacity);
//    }

    /**
     * Builder that builds Capacity
     *
     * @author schroeder
     */
    public static class Builder {

        /**
         * default is 1 dimension with size of zero
         */
        private int[] dimensions = new int[1];

        /**
         * Returns a new instance of Capacity with one dimension and a value/size of 0
         *
         * @return this builder
         */
        public static Builder get() {
            return new Builder();
        }

        Builder() {
        }

        /**
         * add capacity dimension
         * <p>
         * <p>Note that it automatically resizes dimensions according to index, i.e. if index=7 there are 8 dimensions.
         * New dimensions then are initialized with 0
         *
         * @param index    dimensionIndex
         * @param dimValue dimensionValue
         * @return this builder
         */
        public Builder addDimension(int index, int dimValue) {
            if (index < dimensions.length) {
                dimensions[index] = dimValue;
            } else {
                int requiredSize = index + 1;
                int[] newDimensions = new int[requiredSize];
                copy(dimensions, newDimensions);
                newDimensions[index] = dimValue;
                this.dimensions = newDimensions;
            }
            return this;
        }

        private void copy(int[] from, int[] to) {
            System.arraycopy(from, 0, to, 0, dimensions.length);
        }

        /**
         * Builds an immutable Capacity and returns it.
         *
         * @return Capacity
         */
        public Capacity build() {
            return new Capacity(this);
        }


    }

    private final int[] dimensions;

//    /**
//     * copy constructor
//     *
//     * @param capacity capacity to be copied
//     */
//    Capacity(Capacity capacity) {
//        this.dimensions = new int[capacity.dim()];
//        for (int i = 0; i < capacity.dim(); i++) {
//            this.dimensions[i] = capacity.get(i);
//        }
//        this.hash = Arrays.hashCode(dimensions);
//    }

    private Capacity(Builder builder) {
        this.dimensions = builder.dimensions;
        this.hash = Arrays.hashCode(dimensions);
    }

    /**
     * Returns the number of specified capacity dimensions.
     *
     * @return noDimensions
     */
    public final int dim() {
        return dimensions.length;
    }


    /**
     * Returns value of capacity-dimension with specified index.
     * <p>
     * <p>If capacity dimension does not exist, it returns 0 (rather than IndexOutOfBoundsException).
     *
     * @param index dimension index of the capacity value to be retrieved
     * @return the according dimension value
     */
    public final int get(int index) {
        return index < dimensions.length ? dimensions[index] : 0;
    }

    /**
     * Returns true if this capacity is less or equal than the capacity toCompare, i.e. if none of the capacity dimensions > than the corresponding dimension in toCompare.
     *
     * @param toCompare the capacity to compare
     * @return true if this capacity is less or equal than toCompare
     * @throws NullPointerException if one of the args is null
     */
    public boolean lessOrEq(Capacity toCompare) {
//        if (toCompare == null) throw new NullPointerException();
        //int d = this.dim();
        int[] cc = this.dimensions;
        int[] dd = toCompare.dimensions;
        int cLen = cc.length;
        int dLen = dd.length;
        if (cLen!=dLen) return cLen < dLen;
        for (int i = 0; i < cLen; i++) {
            if (cc[i] > dd[i]) return false;
        }
        return true;
    }

    /**
     * Returns true if this capacity is greater or equal than the capacity toCompare
     *
     * @param toCompare the capacity to compare
     * @return true if this capacity is greater or equal than toCompare
     * @throws NullPointerException if one of the args is null
     */
    public boolean greaterOrEq(Capacity toCompare) {
//        if (toCompare == null) throw new NullPointerException();
        //int d = Math.max(this.dim(), toCompare.dim());
        int[] cc = this.dimensions;
        int[] dd = toCompare.dimensions;
        int cLen = cc.length;
        int dLen = dd.length;
        if (cLen!=dLen) return cLen > dLen;
        for (int i = 0; i < cLen; i++) {
            if (cc[i] < dd[i]) return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder("[noDimensions=" + dim() + ']');
        for (int i = 0; i < dim(); i++) {
            string.append("[[dimIndex=").append(i).append("][dimValue=").append(dimensions[i]).append("]]");
        }
        return string.toString();
    }

    /**
     * Return the maximum, i.e. the maximum of each capacity dimension.
     *
     * @param cap1 first capacity to compare
     * @param cap2 second capacity to compare
     * @return capacity maximum of each capacity dimension
     */
    public static Capacity max(Capacity cap1, Capacity cap2) {
//        if (cap1 == null || cap2 == null) throw new IllegalArgumentException("arg must not be null");
        Capacity.Builder toReturnBuilder = Capacity.Builder.get();
        int max = Math.max(cap1.dim(), cap2.dim());
        for (int i = 0; i < max; i++) {
            toReturnBuilder.addDimension(i, Math.max(cap1.get(i), cap2.get(i)));
        }
        return toReturnBuilder.build();
    }

    public static Capacity min(Capacity cap1, Capacity cap2) {
//        if (cap1 == null || cap2 == null) throw new IllegalArgumentException("arg must not be null");
        Capacity.Builder toReturnBuilder = Capacity.Builder.get();
        int max = Math.max(cap1.dim(), cap2.dim());
        for (int i = 0; i < max; i++) {
            toReturnBuilder.addDimension(i, Math.min(cap1.get(i), cap2.get(i)));
        }
        return toReturnBuilder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Capacity)) return false;

        Capacity capacity = (Capacity) o;

        return hash == capacity.hash && Arrays.equals(dimensions, capacity.dimensions);
    }

    @Override
    public final int hashCode() {
        return hash;
    }
}

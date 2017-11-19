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

/** 2D point/vector/coordinate/tuple */
public final class v2 {

    public static v2 the(double x, double y) {
        return new v2(x, y);
    }

    public final double x;
    public final double y;
    public final int hash;

    public v2(double x, double y) {
        this.x = x;
        this.y = y;
        this.hash = Double.hashCode(x) + 31 * Double.hashCode(y);
    }

    @Override
    public String toString() {
        return "[x=" + x + "][y=" + y + ']';
    }

    @Override
    public final int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if ((!(obj instanceof v2)) || hash!=obj.hashCode()) return false;
        v2 other = (v2) obj;
        return Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x) && Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y);
    }

}

/*
    Copyright (c) 2005,2006,2007, OpenMI Association
    "http://www.openmi.org/"

    This file is part of org.openmi.standard.jar

    org.openmi.standard.jar is free software; you can redistribute it and/or
    modify it under the terms of the Lesser GNU General Public License as
    published by the Free Software Foundation; either version 3 of the License,
    or (at your option) any later version.

    org.openmi.standard.jar is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    Lesser GNU General Public License for more details.

    You should have received a copy of the Lesser GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.openmi.standard;


/**
 * Quantity interface
 */
public interface IQuantity {

    /**
     * Value(Set)Type for Quantity.
     */
    enum ValueType
    {
        /**
         * ValueSet for the IQuantity contains scalars.
         */
        Scalar(1),
        /**
         * ValueSet for the Quantity contains vectors.
         */
        Vector(2);

        private final int value;

        /**
         * Minimal constructor to use enum with int flag.
         *
         * @param value The integer flag value
         */
        ValueType(int value)
        {
            this.value = value;
        }

        /**
         * int flag value to a given enum base type.
         *
         * @return The integer flag value
         */
        public int getValue()
        {
            return value;
        }
    }

    /**
     * Identifier
     */
    String getID();

    /**
     * Additional descriptive information
     */
    String getDescription();

    /**
     * Quantity's value type (vector, scalar or ...)
     */
    ValueType getValueType();

    /**
     * Quantity's Dimension
     */
    IDimension getDimension();

    /**
     * Unit
     */
    IUnit getUnit();

}

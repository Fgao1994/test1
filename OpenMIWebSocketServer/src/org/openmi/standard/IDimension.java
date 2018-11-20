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
 * Dimension interface
 */
public interface IDimension {

    /**
     * Enumeration for base dimensions
     */
    public enum DimensionBase {
        /**
         * Base dimension length.
         */
        Length(0),

        /**
         * Base dimension mass.
         */
        Mass(1),

        /**
         * Base dimension time.
         */
        Time(2),

        /**
         * Base dimension electric current.
         */
        ElectricCurrent(3),

        /**
         * Base dimension temperature.
         */
        Temperature(4),

        /**
         * Base dimension amount of substance.
         */
        AmountOfSubstance(5),

        /**
         * Base dimension luminous intensity.
         */
        LuminousIntensity(6),

        /**
         * Base dimension currency.
         */
        Currency(7);

        private final int value;


        /**
         * Minimal constructor to use enum with int flag.
         *
         * @param value The integer flag value
         */
        private DimensionBase(int value) {
            this.value = value;
        }


        /**
         * int flag value to a given enum base type.
         *
         * @return The integer flag value
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Returns the power for the requested dimension
     * <p/>EXAMPLE: <p/>For a quantity such as flow, which may have the unit
     * m3/s, the GetPower method must work as follows:
     * <p/>myDimension.GetPower(DimensionBase.AmountOfSubstance) -->returns 0
     * <p/>myDimension.GetPower(DimensionBase.Currency) --> returns 0
     * <p/>myDimension.GetPower(DimensionBase.ElectricCurrent) --> returns 0
     * <p/>myDimension.GetPower(DimensionBase.Length) --> returns 3
     * <p/>myDimension.GetPower(DimensionBase.LuminousIntensity) --> returns 0
     * <p/>myDimension.GetPower(DimensionBase.Mass) --> returns 0
     * <p/>myDimension.GetPower(DimensionBase.Temperature) --> returns 0
     * <p/>myDimension.GetPower(DimensionBase.Time) --> returns -1
     *
     * @param baseQuantity The requested dimension.
     * @return The power of the requested dimension.
     */
    double getPower(DimensionBase baseQuantity);

    /**
     * Check if a Dimension instance equals to another Dimension instance.
     *
     * @param otherDimension Dimension instance to compare with.
     * @return True if the dimensions are equal.
     */
    boolean equals(IDimension otherDimension);

}
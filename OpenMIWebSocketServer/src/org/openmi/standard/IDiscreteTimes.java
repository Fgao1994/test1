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
 * Within and outside modelling exercises, many situations occur where "raw"
 * data is desired at the (discrete) time stamp as it is available in the source
 * component. A typical example is the comparison of computation results with
 * monitoring data, or a computational core that wants to adhere to the time
 * stepping of its data source. To keep the values fixed to the discrete times
 * as they are available in the source component, the IDiscreteTimes interface
 * has been defined. This interface can provide a list of time stamps for which
 * values of a quantity on an element set are available.
 * <p/>
 * <p/>Note that the IDiscreteTimes interface is an optional interface to
 * provide more detailed information on the temporal discretization of available
 * data. It is not required to implement the IDiscreteTimes interface in order
 * to claim OpenMI compliance for a Component. However, if the IDiscreteTimes
 * interface is implemented it must be implemented according to the definitions
 * given below.
 */
public interface IDiscreteTimes {

    /**
     * Returns true if the component can provide discrete times for the specific
     * exchange item defined by the arguments quantity and elementSet
     */
    boolean hasDiscreteTimes(IQuantity quantity, IElementSet elementSet);

    /**
     * Returns the number of discrete time steps for a specific combination of
     * ElementSet and Quantity
     */
    int getDiscreteTimesCount(IQuantity quantity, IElementSet elementSet);

    /**
     * Get n-th discrete time stamp or time span for a specific combination of
     * ElementSet and Quantity. This method must accept values of
     * discreteTimeIndex in the interval [0, GetDiscreteTimesCount - 1]. If the
     * discreteTimeIndex is outside this interval an exception must be thrown.
     *
     * @param quantity          The quantity.
     * @param elementSet        The element.
     * @param discreteTimeIndex index of timeStep.
     * @return Discrete time stamp or time span.
     */
    ITime getDiscreteTime(IQuantity quantity, IElementSet elementSet, int discreteTimeIndex);
}


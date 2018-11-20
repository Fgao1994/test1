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
 * DataOperation interface
 */
public interface IDataOperation {

    /**
     * Intializes the data operation.
     *
     * @param properties Arguments for initialization
     * @deprecated This method should not be a part of the standard since it is
     *             not required to be invoked by any outside component. However,
     *             in order to avoid changing the standard it will remain in the
     *             IDataOperation interface. It is recommended simply to make an
     *             empty implementation of this method.
     */
    @Deprecated
    void initialize(IArgument[] properties);

    /**
     * Identification String for the data operation.
     * <p/>Two or more data operations provided by one OutputExchangeItem may
     * not have the same ID.
     * <p/>EXAMPLE: <p/>"Mean value", "Max value", "Spatially averaged",
     * "Accumulated", "linear conversion"
     */
    String getID();

    /**
     * Number of arguments for this data operation
     */
    int getArgumentCount();

    /**
     * Gets the argument Object (instance of class implementing IArgument) as
     * identified by the argumentIndex parameter.
     *
     * @param argumentIndex <p/>The index-number of the requested
     *                      DataOperation(indexing starts from zero) <p/>This
     *                      method must accept values of argumentIndex in the
     *                      interval [0, ArgumentCount - 1]. If the
     *                      argumentIndex is outside this interval an exception
     *                      must be thrown..
     * @return The Argument as identified by argumentIndex.
     */
    IArgument getArgument(int argumentIndex);

    /**
     * Validates a specific combination of InputExchangeItem, OutputExchangeItem
     * and a selection of DataOperations. If this combination is valid true
     * should be returned otherwise false should be returned.
     *
     * @param inputExchangeItem      The input exchange item.
     * @param outputExchangeItem     The output exchange item.
     * @param selectedDataOperations The already selected data operations.
     * @return True if the combination of InputExchangeItem, OutputExchangeItem,
     *         and the array of dataOperations provided in the methods argument
     *         is valid, otherwise false.
     */
    boolean isValid(IInputExchangeItem inputExchangeItem, IOutputExchangeItem outputExchangeItem,
                    IDataOperation[] selectedDataOperations);
}


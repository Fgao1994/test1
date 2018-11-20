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

//TODO: write some more xml comments..
/**
 * Link interface
 */
public interface ILink {

    /**
     * Identification String
     */
    String getID();

    /**
     * Additional descriptive information
     */
    String getDescription();

    /**
     * Number of data operations
     */
    int getDataOperationsCount();

    /**
     * Get the data operation with index DataOperationIndex. If this method is
     * invoked with a dataOperaionIndex, which is outside the interval
     * [0,DataOperaionCount] an exception must be thrown.
     *
     * @return DataOperation according to the argument: dataOperationIndex.
     */
    IDataOperation getDataOperation(int dataOperationIndex);

    /**
     * Target quantity
     */
    IQuantity getTargetQuantity();

    /**
     * Target elementset
     */
    IElementSet getTargetElementSet();

    /**
     * Source elementset
     */
    IElementSet getSourceElementSet();


    /**
     * Souce linkable component
     */
    ILinkableComponent getSourceComponent();


    /**
     * Source quantity
     */
    IQuantity getSourceQuantity();


    /**
     * Target linkable component
     */
    ILinkableComponent getTargetComponent();

}
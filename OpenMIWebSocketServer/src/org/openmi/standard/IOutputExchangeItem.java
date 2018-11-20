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
 * An IOutputExchangeItem describes an output item that can be delivered by a
 * LinkableComponent. The item describes on which elementset a quantity can be
 * provided.
 * <p/>An output exchange item may provide data operations
 * (interpolation in time, spatial interpolation etc.) that can be performed on
 * the output exchange item before the values are delivered to the target
 * ILinkableComponent
 */

public interface IOutputExchangeItem extends IExchangeItem {

    /**
     * The number of data operations that can be performed on the output
     * quantity/elemenset.
     */
    int getDataOperationCount();

    /**
     * Get one of the data operations that can be performed on the output
     * quantity/elemenset.
     *
     * @param dataOperationIndex The index for the data operation [0,
     *                           DataOperationCount-1].
     * @return The data operation for index dataOperationIndex.
     */
    IDataOperation getDataOperation(int dataOperationIndex);

}

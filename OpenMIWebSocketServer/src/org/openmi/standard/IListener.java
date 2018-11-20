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
 * Listener interface
 */
public interface IListener {
    
    /**
     * Method called when event is raised
     *
     * @param event Event that has been raised.
     */
    void onEvent(IEvent event);

    /**
     * Get number of accepted event types
     *
     * @return Number of accepted event types.
     */
    int getAcceptedEventTypeCount();

    /**
     * Get accepted event type with index acceptedEventTypeIndex.
     * <p/>If this method is invoked with an argument that is outside the
     * interval [0, numberOfAcceptedEventsTypes], where numberOfAcceptedEventsTypes
     * is the values obtained through the method GetAcceptedEventTypeCount(), an
     * exception must be thrown.
     *
     * @param acceptedEventTypeIndex index in accepted event types.
     * @return Accepted event type.
     */
    IEvent.EventType getAcceptedEventType(int acceptedEventTypeIndex);

}
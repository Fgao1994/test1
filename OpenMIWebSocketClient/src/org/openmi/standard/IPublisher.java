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
 * Publisher interface
 */

public interface IPublisher {

    /**
     * Subscribes a listener
     *
     * @param listener  The listener.
     * @param eventType The event type.
     */
    void subscribe(IListener listener, IEvent.EventType eventType);

    /**
     * Unsubscribes a listener
     *
     * @param listener  The listener.
     * @param eventType The event type.
     */
    void unSubscribe(IListener listener, IEvent.EventType eventType);

    /**
     * Sends an event to all subscribed listeners
     *
     * @param event The event.
     */
    void sendEvent(IEvent event);

    /**
     * Get number of published event types
     *
     * @return Number of provided event types.
     */
    int getPublishedEventTypeCount();

    /**
     * Get provided event type with index providedEventTypeIndex
     *
     * @param providedEventTypeIndex index in provided event types.
     * @return Provided event type.
     */
    IEvent.EventType getPublishedEventType(int providedEventTypeIndex);

}
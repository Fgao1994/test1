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
 * Within modern software systems, events are often applied for all types of
 * messaging. Within OpenMI a lightweight event mechanism is applied, using a
 * generic Event interface and an enumeration of event types
 * (OpenMI.Standard.EventType) to allow the implementation of generic tools that
 * perform monitoring tasks such as logging, tracing, or online visualization.
 * Linkable components must generate events to which other linkable components
 * or tools can subscribe. In this way, it becomes possible to implement these
 * generic tools without requiring any knowledge of the specific tools in the
 * components themselves. By adopting the OpenMI event types, system developers
 * can use those tools without additional effort. Note that the event mechanism
 * should not be used to pass data sets. Data sets should be retrieved through
 * the GetValues() call.
 *
 * The event mechanism is also used to facilitate pausing and resuming of
 * the computation thread, as the computation process of an entire model chain
 * is rather autonomous and not controlled by any master controller. Once a
 * component receives the thread, it must send an event, so listeners (e.g. a
 * GUI) can grab and hold the thread, and thus pause the computation by not
 * returning control. In normal conditions, the control is returned so the
 * component can continue its computation. Of course the computation is also
 * controlled at the level that triggers the first component of the chain by
 * means of a GetValues()-call. Stop firing those calls will also result in a
 * paused system, although it may take a while before an entire call stack
 * completes its processing activity.
 */

public interface IEvent {

    /**
     * Enumeration for event types.
     */
    public enum EventType {
        /**
         * Warning.
         */
        Warning(0),
        /**
         * Informative. Any type of information.
         */
        Informative(1),
        /**
         * Value out of range. If a LinkableComponent receives values through
         * the GetValues method, which are detected by the receiving component
         * as out-of-range an OutOfRange event must be send. Alternatively, if
         * the component cannot proceed with the received value or if proceeding
         * with the received value will make the component unstable or make the
         * component generate erroneous results and exception can be thrown.
         */
        ValueOutOfRange(2),
        /**
         * Global progress. Indicates progress as percentage of global time
         * horizon. It is not mandatory for LinkableComponent to provide this
         * event type.
         */
        GlobalProgress(3),
        /**
         * Timestep progress. Indicates progress as % for the current time step.
         * It is not mandatory for LinkableComponent to provide this event
         * type.
         */
        TimeStepProgress(4),
        /**
         * Data changed. Events of this event type must be send at least once
         * during each period when the LinkableComponent hold the thread if the
         * internal state of the component has changed.
         */
        DataChanged(5),
        /**
         * Target before GetValues call. Immediately before a LinkableComponent
         * invokes the GetValues method in another LinkableComponent an event of
         * type TargetBeforeGetValuesCall must be send.
         */
        TargetBeforeGetValuesCall(6),
        /**
         * Source after GetValues call. Immediately when the GetValues method is
         * invoked in a LinkableComponent this component must send an event of
         * type SourceAfterGetValuesCall
         */
        SourceAfterGetValuesCall(7),
        /**
         * Source before GetValues return. Immediately before a
         * LinkableComponent in which the GetValues method has been invoked
         * returns the thread to the calling component an event of type
         * SourceBeforeGetValuesReturn must be send.
         */
        SourceBeforeGetValuesReturn(8),
        /**
         * Target after GetValues return. Immediately after a LinkableComponent
         * which has invoked the GetValues method in another LinkableComponent
         * receives the thread back from this component (after this component
         * returns the values) an event of type TargetAfterGetValuesReturn must
         * be send.
         */
        TargetAfterGetValuesReturn(9),
        /**
         * Other. Any other event that is found useful to implement.
         */
        Other(10);

        private final int value;


        /**
         * Minimal constructor to use enum with int flag.
         *
         * @param value The integer flag value
         */
        EventType(int value) {
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


        public EventType[] toArray() {
            return EventType.values();
        }
    }

    /**
     * Type of event
     */
    EventType getType();

    /**
     * Additional descriptive information
     */
    String getDescription();

    /**
     * ILinkableComponent that generated the event.
     */
    ILinkableComponent getSender();

    /**
     * Current SimulationTime ("-" if not applicable)
     */
    ITimeStamp getSimulationTime();

    /**
     * Get the value of a Key=Value pair, containing additional information on the event.
     * This method must throw an exception if the key is not recognized.
     */
    Object getAttribute(String key);

}
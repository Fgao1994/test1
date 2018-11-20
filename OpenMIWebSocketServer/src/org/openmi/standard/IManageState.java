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
 * Manage State Interface. (To be implemented optionally, in addition to the
 * linkable component interface.)
 */
public interface IManageState {

    /**
     * Store the linkable component's current State.
     *
     * @return State identifier.
     */
    String keepCurrentState();

    /**
     * Restores the state identified by the parameter stateID. If the state
     * identifier identified by stateID is not known by the linkable component
     * an exception should be trown.
     *
     * @param stateID State identifier.
     */
    void restoreState(String stateID);

    /**
     * Clears a state from the linkable component's memory. If the state
     * identifier identified by stateID is not known by the linkable component
     * an exception should be thrown.
     *
     * @param stateID State identifier.
     */
    void clearState(String stateID);

}

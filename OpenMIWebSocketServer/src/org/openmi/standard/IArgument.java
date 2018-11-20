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
 * The IArgument interface defines a key - value pair. If the property ReadOnly
 * is false the value is edible otherwise it is read-only.
 */
public interface IArgument {
   
    /**
     * The key (String) in key-value pair.
     */
    String getKey();

    /**
     * Get the value (String) in key-value pair.
     */
    String getValue();

    /**
     * Set the value (String) in key-value pair.
     * <p/>If the ReadOnly property is true and the property is attempted to be
     * changed from outside an exception must be thrown.
     *
     * @param value The value to be set
     */
    void setValue(String value);

    /**
     * Defines whether the Values property may be edited from outside.
     */
    boolean isReadOnly();

    /**
     * Description of the key-value pair.
     */
    String getDescription();
}



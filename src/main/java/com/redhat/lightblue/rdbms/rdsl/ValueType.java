/*
 Copyright 2013 Red Hat, Inc. and/or its affiliates.

 This file is part of lightblue.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redhat.lightblue.rdbms.rdsl;

/**
 * Enumeration that describes the type of a value used in a script
 *
 * <ul>
 * <li>primitive: any java value object, numbers, string, date, etc.</li>
 * <li>lob: Large binary object. Can be an array of bytes or char</li>
 * <li>list: An instance of ValueList.</li>
 * <li>map: An instance of ValueMap</li>
 * <ul>
 */
public enum ValueType {
  primitive, lob, list, map 
}


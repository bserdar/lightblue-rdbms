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

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Util;

/**
 * MapValue adapter backed by a hashmap
 */
public class TempVarMapValueAdapter implements MapValue, TempVarResolver {

    private Map<String,Value> map;

    /**
     * Default ctor, empty map
     */
    public TempVarMapValueAdapter() {
        this(new HashMap());
    }

    /**
     * Sets the underlying map
     */
    public TempVarMapValueAdapter(Map<String,Value> map) {
        this.map=map;
    }

    /**
     * Copy ctor
     */
    public TempVarMapValueAdapter(MapValue v) {
        this();
        if(v!=null&&!v.isEmpty()) {
            for(Iterator<String> itr=v.getNames();itr.hasNext();) {
                String name=itr.next();
                Value value=v.getValue(name);
                if(value.getType()==ValueType.map) {
                    map.put(name,new Value(new TempVarMapValueAdapter(value.getMapValue())));
                } else if(value.getType()==ValueType.list) {
                    map.put(name,new Value(new TempVarListValueAdapter(value.getListValue())));
                } else {
                    map.put(name,value);
                }
            }
        }
    }
    
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Iterator<String> getNames() {
        return map.keySet().iterator();
    }

    @Override
    public Value getValue(String name) {
        return map.get(name);
    }

    public void put(String name,Value value) {
        map.put(name,value);
    }

    @Override
    public Value get(Path name) {
        if(name.numSegments()==0) {
            return new Value(this);
        } else {
            Value element;
            element=map.get(name.head(0));
            if(name.numSegments()>1) {
                if(element instanceof TempVarResolver) {
                    return ((TempVarResolver)element).get(name.suffix(-1));
                } else {
                    throw Error.get(ScriptErrors.ERR_INVALID_DEREFERENCE);
                } 
            } else {
                return element;
            }
        }   
    }

    @Override
    public void set(Path name,Value v) {
        if(name.numSegments()==0) {
            throw new IllegalArgumentException("Cannot set this");
        } else {
            String head=name.head(0);
            Value currentElement=map.get(head);
            if(name.numSegments()>1) {
                Path suffix=name.suffix(-1);
                // currentElement must be an object or array
                if(currentElement.getValue() instanceof TempVarResolver) {
                    ((TempVarResolver)currentElement.getValue()).set(suffix,v);
                } else if(currentElement==null||currentElement.getValue()==null) {
                    // Null value. Replace with an object or array
                    if(Util.isNumber(suffix.head(0))) {
                        currentElement=new Value(new TempVarListValueAdapter());
                    } else {
                        currentElement=new Value(new TempVarMapValueAdapter());
                    }
                    map.put(head,currentElement);
                    ((TempVarResolver)currentElement.getValue()).set(suffix,v);
                } else {
                    throw Error.get(ScriptErrors.ERR_INVALID_DEREFERENCE,name.toString());
                }
            } else {
                // We are setting current element
                if(v.getType()==ValueType.primitive||
                   v.getType()==ValueType.lob) {
                    map.put(head,v);
                } else if(v.getType()==ValueType.map) {
                    // Copy
                    map.put(head,new Value(new TempVarMapValueAdapter(v.getMapValue())));
                } else {
                    map.put(head,new Value(new TempVarListValueAdapter(v.getListValue())));
                }
            }
        }
    }

    @Override
    public String toString() {
        return map.toString();
    }
}

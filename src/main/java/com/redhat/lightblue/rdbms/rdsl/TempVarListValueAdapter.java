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
import java.util.List;
import java.util.ArrayList;

import com.redhat.lightblue.util.Path;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Util;

/**
 * ListValue adapter backed by an array list.
 */
public class TempVarListValueAdapter implements ListValue, TempVarResolver {

    private List<Value> list;

    /**
     * Default ctor, empty list
     */
    public TempVarListValueAdapter() {
        this(new ArrayList<Value>());
    }

    /**
     * Sets the underlying list
     */
    public TempVarListValueAdapter(List<Value> list) {
        this.list=list;
    }

    /**
     * Copy ctor
     */
    public TempVarListValueAdapter(ListValue v) {
        this();
        if(v!=null&&!v.isEmpty()) {
            for(Iterator<Value> itr=v.getValues();itr.hasNext();) {
                Value value=itr.next();
                if(value.getType()==ValueType.map) {
                    list.add(new Value(new TempVarMapValueAdapter(value.getMapValue())));
                } else if(value.getType()==ValueType.list) {
                    list.add(new Value(new TempVarListValueAdapter(value.getListValue())));
                } else {
                    list.add(value);
                }
            }
        }
    }
    
    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<Value> getValues() {
        return list.iterator();
    }

    public void set(int index,Value v) {
        list.set(index,v);
    }

    public Value get(int index) {
        return list.get(index);
    }

    public int size() {
        return list.size();
    }

    @Override
    public Value get(Path name) {
        if(name.numSegments()==0) {
            return new Value(this);
        } else {
            int index;
            try {
                index=Integer.valueOf(name.head(0)).intValue();
            } catch (Exception e) {
                throw Error.get(ScriptErrors.ERR_INVALID_ARRAY_INDEX,name.toString());
            }
            Value element;
            try {
                element=list.get(index);
            } catch (Exception e) {
                throw Error.get(ScriptErrors.ERR_INVALID_ARRAY_INDEX,name.toString());
            }
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
             int index;
            try {
                index=Integer.valueOf(name.head(0)).intValue();
            } catch (Exception e) {
                throw Error.get(ScriptErrors.ERR_INVALID_ARRAY_INDEX,name.toString());
            }
            while(list.size()<=index) {
                list.add(Value.NULL_VALUE);
            }
            Value currentElement=list.get(index);
            if(name.numSegments()>1) {
                Path suffix=name.suffix(-1);
                // currentElement must be an object or array
                if(currentElement.getValue() instanceof TempVarResolver) {
                    ((TempVarResolver)currentElement.getValue()).set(suffix,v);
                } else if(currentElement.getValue()==null) {
                    // Null value. Replace with an object or array
                    if(Util.isNumber(suffix.head(0))) {
                        currentElement=new Value(new TempVarListValueAdapter());
                    } else {
                        currentElement=new Value(new TempVarMapValueAdapter());
                    }
                    list.set(index,currentElement);
                    ((TempVarResolver)currentElement.getValue()).set(suffix,v);
                } else {
                    throw Error.get(ScriptErrors.ERR_INVALID_DEREFERENCE,name.toString());
                }
            } else {
                // We are setting current element
                if(v.getType()==ValueType.primitive||
                   v.getType()==ValueType.lob) {
                    list.set(index,v);
                } else if(v.getType()==ValueType.map) {
                    // Copy
                    list.set(index,new Value(new TempVarMapValueAdapter(v.getMapValue())));
                } else {
                    list.set(index,new Value(new TempVarListValueAdapter(v.getListValue())));
                }
            }
        }
    }
}

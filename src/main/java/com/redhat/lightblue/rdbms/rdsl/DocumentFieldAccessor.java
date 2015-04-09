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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import com.redhat.lightblue.metadata.FieldTreeNode;
import com.redhat.lightblue.metadata.ArrayField;
import com.redhat.lightblue.metadata.ObjectField;
import com.redhat.lightblue.metadata.ObjectArrayElement;
import com.redhat.lightblue.metadata.SimpleArrayElement;
import com.redhat.lightblue.metadata.ArrayElement;
import com.redhat.lightblue.metadata.Fields;
import com.redhat.lightblue.metadata.EntityMetadata;

import com.redhat.lightblue.metadata.types.BinaryType;

import com.redhat.lightblue.util.JsonDoc;
import com.redhat.lightblue.util.Error;
import com.redhat.lightblue.util.Path;

/**
 * This variable accessor returns the fields of a document as Value
 * objects, and allows callers to set values.
 */
public class DocumentFieldAccessor implements VariableAccessor {

    private final JsonDoc doc;
    private final EntityMetadata md;

    /**
     * Constructs the document accessor to access the given doc
     */
    public DocumentFieldAccessor(EntityMetadata md,JsonDoc doc) {
        this.doc=doc;
        this.md=md;
    }

    /**
     * Utility function that returns a value for a document node using
     * its metadata. The metadata is used to return a Value object
     * with the correct type. It is optional, if metadata is not
     * given, the value is returned with the best matching type
     * (ListValue for array, MapValue for object, and primitive for
     * everything else)
     */
    public static Value getValueForField(FieldTreeNode nodeMd,JsonNode node) {
        if(node instanceof ArrayNode) {
            if(nodeMd instanceof ArrayField || nodeMd==null)
                return new Value(new JsonArrayAdapter((ArrayNode)node,(ArrayField)nodeMd));
            else
                throw Error.get(ScriptErrors.ERR_INCONSISTENT_DATA,nodeMd.getType().getName()+"/"+node.getClass().getName());
        } else if(node instanceof ObjectNode) {
            if(nodeMd instanceof ObjectField||
               nodeMd instanceof ObjectArrayElement||
               nodeMd==null)
                return new Value(new JsonObjectAdapter((ObjectNode)node,(ObjectField)nodeMd));
            else
                throw Error.get(ScriptErrors.ERR_INCONSISTENT_DATA,nodeMd.getType().getName()+"/"+node.getClass().getName());
        } else if(node != null) {
            if(nodeMd!=null) {
                if(nodeMd.getType() instanceof BinaryType) {
                    return new Value(ValueType.lob,nodeMd.getType().fromJson(node));
                } else {
                    return new Value(ValueType.primitive,nodeMd.getType().fromJson(node));
                }
            } else {
                // Node is not an array or object, but there is no nodeMd. This is a primitive type
                return Value.toValue(node);
            }
        } else {
            // node is null
            if(nodeMd==null) {
                return Value.NULL_VALUE;
            } else {
                if(nodeMd instanceof ArrayField) {
                    return new Value(ValueType.list,null);
                } else if(nodeMd instanceof ObjectField||
                          nodeMd instanceof ObjectArrayElement) {
                    return new Value(ValueType.map,null);
                } else if(nodeMd.getType() instanceof BinaryType) {
                    return new Value(ValueType.lob,null);
                } else {
                    return new Value(ValueType.primitive,null);
                }
            }
        }
    }

    @Override
    public Value getVarValue(Path var) {
        if(var.isEmpty())
            return new Value(new JsonObjectAdapter((ObjectNode)doc.getRoot(),md.getEntitySchema().getFieldTreeRoot()));
        FieldTreeNode nodeMd=md.resolve(var);
        JsonNode node=doc.get(var);
        return getValueForField(nodeMd,node);
    }

    @Override
    public ValueType getVarType(Path var) {
        if(var.isEmpty())
            return ValueType.map;
        FieldTreeNode nodeMd=md.resolve(var);
        if(nodeMd instanceof ArrayField) {
            return ValueType.list;
        } else if(nodeMd instanceof ObjectField ||
                  nodeMd instanceof ObjectArrayElement) {
            return ValueType.map;
        } else if(nodeMd.getType() instanceof BinaryType) {
            return ValueType.lob;
        } else {
            return ValueType.primitive;
        }
    }

    @Override
    public void setVarValue(Path var,Value value) {
        FieldTreeNode nodeMd=md.resolve(var);
        setVarValue(var,value,nodeMd);
    }

    private void setVarValue(Path var,Value value,FieldTreeNode nodeMd) {
        if(nodeMd instanceof ArrayField) {
            // We expect the value to be a list
            if(value.getType()==ValueType.list) {
                ListValue lv=value.getListValue();
                if(lv==null) {
                    doc.modify(var,null,true);
                } else {
                    ArrayNode arrayNode=JsonNodeFactory.instance.arrayNode();
                    doc.modify(var,arrayNode,true);
                    setValue(var,arrayNode,lv,((ArrayField)nodeMd).getElement());
                }
            } else
                throw Error.get(ScriptErrors.ERR_INCOMPATIBLE_ASSIGNMENT,var.toString());
        } else if(nodeMd instanceof ObjectField||
                  nodeMd instanceof ObjectArrayElement) { 
            // We expect the value to be a map
            if(value.getType()==ValueType.map) {
                MapValue mv=value.getMapValue();
                if(mv==null) {
                    doc.modify(var,null,false);
                } else {
                    ObjectNode objectNode=JsonNodeFactory.instance.objectNode();
                    doc.modify(var,objectNode,true);
                    Fields fields;
                    if(nodeMd instanceof ObjectField)
                        fields=((ObjectField)nodeMd).getFields();
                    else
                        fields=((ObjectArrayElement)nodeMd).getFields();
                    setValue(var,objectNode,mv,fields);
                }
            } else
                throw Error.get(ScriptErrors.ERR_INCOMPATIBLE_ASSIGNMENT,var.toString());
        } else if(nodeMd.getType() instanceof BinaryType) {
            // We expect a lob 
            if(value.getType()==ValueType.lob) {
                Object lob=value.getValue();
                if(lob==null) {
                    doc.modify(var,null,false);
                } else {
                    doc.modify(var,nodeMd.getType().toJson(JsonNodeFactory.instance,lob),true);
                }
            } else 
                throw Error.get(ScriptErrors.ERR_INCOMPATIBLE_ASSIGNMENT,var.toString());
        } else {
            // A primitive value.
            Object v=value.getValue();
            if(v==null) {
                doc.modify(var,null,false);
            } else {
                doc.modify(var,nodeMd.getType().toJson(JsonNodeFactory.instance,v),true);
            }
        }
    }

    private void setValue(Path arrayVar,ArrayNode arrayNode,ListValue lv,ArrayElement elMd) {
        if(!lv.isEmpty()) {
            JsonNodeFactory factory=JsonNodeFactory.instance;
            if(elMd instanceof SimpleArrayElement) {
                for(Iterator<Value> itr=lv.getValues();itr.hasNext();) {
                    Value value=itr.next();
                    if(elMd.getType() instanceof BinaryType) {
                        if(value.getType()==ValueType.lob) {
                            Object lob=value.getValue();
                            if(lob==null) {
                                arrayNode.add(factory.nullNode());
                            } else {
                                arrayNode.add(elMd.getType().toJson(factory,lob));
                            }
                        } else {
                            throw Error.get(ScriptErrors.ERR_INCOMPATIBLE_ARRAY_ELEMENT,value.getType().toString());
                        }
                    } else {
                        if(value.getValue()==null) {
                            arrayNode.add(factory.nullNode());
                        } else {
                            arrayNode.add(elMd.getType().toJson(factory,value.getValue()));
                        }
                    }
                }
            } else if(elMd instanceof ObjectArrayElement) {
                for(Iterator<Value> itr=lv.getValues();itr.hasNext();) {
                    Value value=itr.next();
                    if(value.getType()==ValueType.map) {
                        MapValue map=value.getMapValue();
                        JsonNode newNode=map==null?factory.nullNode():factory.objectNode();
                        if(map!=null) {
                            Path elemVar=new Path(arrayVar,new Path(Integer.toString(arrayNode.size())));
                            setValue(elemVar,(ObjectNode)newNode,map,((ObjectArrayElement)elMd).getFields());
                        }
                        arrayNode.add(newNode);
                    } else {
                        throw Error.get(ScriptErrors.ERR_INCOMPATIBLE_ARRAY_ELEMENT,value.getType().toString());
                    }
                } 
            } else
                throw new UnsupportedOperationException(elMd.getClass().getName());
        }
    }
    
    private void setValue(Path objectVar,ObjectNode objectNode,MapValue mv,Fields fields) {
        if(!mv.isEmpty()) {
            for(Iterator<String> itr=mv.getNames();itr.hasNext();) {
                String name=itr.next();
                Path fieldName=new Path(objectVar,new Path(name));
                FieldTreeNode fieldMd=fields.getField(name);
                if(fieldMd==null)
                    throw Error.get(ScriptErrors.ERR_VAR_NOT_DOCUMENT_PART,fieldName.toString());
                setVarValue(fieldName,mv.getValue(name),fieldMd);
            }
        }
    }

    @Override
    public String toString() {
        return doc.toString();
    }
}


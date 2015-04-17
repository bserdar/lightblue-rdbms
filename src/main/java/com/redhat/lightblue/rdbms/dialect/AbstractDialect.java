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
package com.redhat.lightblue.rdbms.dialect;

import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Types;
import java.sql.Blob;
import java.sql.Clob;

import java.math.BigDecimal;
import java.math.BigInteger;

import java.util.Date;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Calendar;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;

import com.redhat.lightblue.util.Error;

import com.redhat.lightblue.rdbms.rdsl.Value;
import com.redhat.lightblue.rdbms.rdsl.ValueType;

public abstract class AbstractDialect implements Dialect {

    private static final class JDBCSqlTypeMap {
        final String name;
        final int type;

        public JDBCSqlTypeMap(String name,int type) {
            this.name=name.toUpperCase();
            this.type=type;
        }
    }

    /**
     * Keep this list ordered alphabetically
     */
    private static final JDBCSqlTypeMap JDBCSQLTYPEMAP[]={
        new JDBCSqlTypeMap("ARRAY",Types.ARRAY),
        new JDBCSqlTypeMap("BIGINT",Types.BIGINT),
        new JDBCSqlTypeMap("BINARY",Types.BINARY),
        new JDBCSqlTypeMap("BIT",Types.BIT),
        new JDBCSqlTypeMap("BLOB",Types.BLOB),
        new JDBCSqlTypeMap("BOOLEAN",Types.BOOLEAN),
        new JDBCSqlTypeMap("CHAR",Types.CHAR),
        new JDBCSqlTypeMap("CLOB",Types.CLOB),
        new JDBCSqlTypeMap("DATALINK",Types.DATALINK),
        new JDBCSqlTypeMap("DATE",Types.DATE),
        new JDBCSqlTypeMap("DECIMAL",Types.DECIMAL),
        new JDBCSqlTypeMap("DISTINCT",Types.DISTINCT),
        new JDBCSqlTypeMap("DOUBLE",Types.DOUBLE),
        new JDBCSqlTypeMap("FLOAT",Types.FLOAT),
        new JDBCSqlTypeMap("INTEGER",Types.INTEGER),
        new JDBCSqlTypeMap("JAVA_OBJECT",Types.JAVA_OBJECT),
        new JDBCSqlTypeMap("LONGNVARCHAR",Types.LONGNVARCHAR),
        new JDBCSqlTypeMap("LONGVARBINARY",Types.LONGVARBINARY),
        new JDBCSqlTypeMap("LONGVARCHAR",Types.LONGVARCHAR),
        new JDBCSqlTypeMap("NCHAR",Types.NCHAR),
        new JDBCSqlTypeMap("NCLOB",Types.NCLOB),
        new JDBCSqlTypeMap("NULL",Types.NULL),
        new JDBCSqlTypeMap("NUMERIC",Types.NUMERIC),
        new JDBCSqlTypeMap("NVARCHAR",Types.NVARCHAR),
        new JDBCSqlTypeMap("OTHER",Types.OTHER),
        new JDBCSqlTypeMap("REAL",Types.REAL),
        new JDBCSqlTypeMap("REF",Types.REF),
        new JDBCSqlTypeMap("ROWID",Types.ROWID),
        new JDBCSqlTypeMap("SMALLINT",Types.SMALLINT),
        new JDBCSqlTypeMap("SQLXML",Types.SQLXML),
        new JDBCSqlTypeMap("STRUCT",Types.STRUCT),
        new JDBCSqlTypeMap("TIME",Types.TIME),
        new JDBCSqlTypeMap("TIMESTAMP",Types.TIMESTAMP),
        new JDBCSqlTypeMap("TINYINT",Types.TINYINT),
        new JDBCSqlTypeMap("VARBINARY",Types.VARBINARY),
        new JDBCSqlTypeMap("VARCHAR",Types.VARCHAR) };


    /**
     * This basic implementation ignores the sql type information for
     * most cases, and uses the appropriate setParameter method based
     * on the Java class type of 'value'. If 'value' is null and sqlType is
     * given, the PreparedStatement.setNull method is called,
     * otherwise null value is set by using setObject()
     */
    @Override
    public void setParameter(PreparedStatement stmt,int index,Integer sqlType,Object value) {
        try {
            if(value==null) {
                if(sqlType==null)
                    stmt.setObject(index,null);
                else
                    stmt.setNull(index,sqlType);
            } else if(value instanceof BigDecimal) {
                stmt.setBigDecimal(index,(BigDecimal)value);
            } else if(value instanceof BigInteger) {
                stmt.setString(index,value.toString());
            } else if(value instanceof Double) {
                stmt.setDouble(index,(Double)value);
            } else if(value instanceof Float) {
                stmt.setFloat(index,(Float)value);
            } else if(value instanceof Long) {
                stmt.setLong(index,(Long)value);
            } else if(value instanceof Integer) {
                stmt.setInt(index,(Integer)value);
            } else if(value instanceof Short) {
                stmt.setShort(index,(Short)value);
            } else if(value instanceof Byte) {
                stmt.setByte(index,(Byte)value);
            } else if(value instanceof Character) {
                char[] x=new char[1];
                x[0]=(Character)value;
                stmt.setString(index,new String(x));
            } else if(value instanceof Timestamp) {
                stmt.setTimestamp(index,(Timestamp)value);
            } else if(value instanceof Time) {
                stmt.setTime(index,(Time)value);
            } else if(value instanceof Date) {
                if(value instanceof java.sql.Date)
                    stmt.setDate(index,(java.sql.Date)value);
                else
                    stmt.setDate(index,new java.sql.Date( ((Date)value).getTime()) );
            } else if(value instanceof Calendar) {
                stmt.setDate(index,new java.sql.Date( ((Calendar)value).getTime().getTime()));
            } else if(value instanceof String) {
                stmt.setString(index,(String)value);
            } else if(value instanceof Boolean) {
                stmt.setBoolean(index,(Boolean)value);
            } else if(value instanceof byte[]) {
                stmt.setBinaryStream(index,new ByteArrayInputStream((byte[])value));
            } else if(value instanceof char[]) {
                stmt.setCharacterStream(index,new CharArrayReader((char[])value));
            } else 
                throw Error.get(DialectErrors.ERR_CANNOT_BIND,value.getClass().getName());
        } catch (Error e) {
            throw e;
        } catch (Exception x) {
            throw Error.get(DialectErrors.ERR_CANNOT_BIND,x.toString());
        }        
    }

    /**
     * The default implementation returns JDBC types from java.sql.Types
     */
    @Override
    public Integer parseType(String type) {
        return parseJDBCType(type);
    }

    @Override
    public void registerOutParameter(CallableStatement stmt,int index,int sqlType) {
        try {
            stmt.registerOutParameter(index,sqlType);
        } catch (Exception e) {
            throw Error.get(DialectErrors.ERR_CANNOT_REGISTER_OUT_PARAM,Integer.toString(sqlType));
        }
    }

    @Override
    public Value getOutValue(CallableStatement stmt,int index,int sqlType) {
        try {
            switch(sqlType) {
            case Types.NUMERIC:
            case Types.INTEGER:
            case Types.BIGINT: return new Value(ValueType.primitive,new BigInteger(stmt.getString(index)));
            case Types.BOOLEAN:
            case Types.BIT: return new Value(stmt.getBoolean(index));
            case Types.ROWID:
            case Types.NCHAR:
            case Types.CHAR:
            case Types.NVARCHAR:
            case Types.VARCHAR : return new Value(stmt.getString(index));
            case Types.REAL:
            case Types.DOUBLE:
            case Types.FLOAT: return new Value(stmt.getDouble(index));
            case Types.DATE: return new Value(ValueType.primitive,stmt.getDate(index));
            case Types.TIME: return new Value(ValueType.primitive,stmt.getTime(index));
            case Types.TIMESTAMP: return new Value(ValueType.primitive,stmt.getTimestamp(index));
            case Types.SMALLINT:
            case Types.TINYINT: return new Value(stmt.getInt(index));
            case Types.DECIMAL: return new Value(ValueType.primitive,stmt.getBigDecimal(index));
            case Types.BINARY: 
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY: 
                Blob blob=stmt.getBlob(index);
                if(blob!=null) {
                    Value v=new Value(ValueType.lob,blob.getBytes(1,(int)blob.length()));
                    blob.free();
                    return v;
                } else
                    return Value.NULL_VALUE;
            case Types.CLOB:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCLOB:
                Clob clob=stmt.getClob(index);
                if(clob!=null) {
                    Value v=new Value(ValueType.lob,clob.getSubString(1,(int)clob.length()));
                    clob.free();
                    return v;
                } else
                    return Value.NULL_VALUE;
            case Types.ARRAY:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"array");
            case Types.DATALINK:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"datalink");
            case Types.DISTINCT:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"distinct");
            case Types.JAVA_OBJECT:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"java_object");
            case Types.NULL:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"null");
            case Types.OTHER:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"other");
            case Types.REF:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"ref");
            case Types.SQLXML: throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"sqlxml");
            case Types.STRUCT: throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"struct");
            }
        } catch (Error e) {
            throw e;
        } catch (Exception x) {
            throw Error.get(DialectErrors.ERR_CANNOT_BIND,x.toString());
        }
        throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,Integer.toString(sqlType));
    }

    @Override
    public Value getResultSetValue(ResultSet rs,int index) {
        int sqlType=-1;
        try {
            sqlType=rs.getMetaData().getColumnType(index);
            switch(sqlType) {
            case Types.NUMERIC:
            case Types.INTEGER:
            case Types.BIGINT: return new Value(ValueType.primitive,new BigInteger(rs.getString(index)));
            case Types.BOOLEAN:
            case Types.BIT: return new Value(rs.getBoolean(index));
            case Types.ROWID:
            case Types.NCHAR:
            case Types.CHAR:
            case Types.NVARCHAR:
            case Types.VARCHAR : return new Value(rs.getString(index));
            case Types.REAL:
            case Types.DOUBLE:
            case Types.FLOAT: return new Value(rs.getDouble(index));
            case Types.DATE: return new Value(ValueType.primitive,rs.getDate(index));
            case Types.TIME: return new Value(ValueType.primitive,rs.getTime(index));
            case Types.TIMESTAMP: return new Value(ValueType.primitive,rs.getTimestamp(index));
            case Types.SMALLINT:
            case Types.TINYINT: return new Value(rs.getInt(index));
            case Types.DECIMAL: return new Value(ValueType.primitive,rs.getBigDecimal(index));
            case Types.BINARY: 
            case Types.BLOB:
            case Types.LONGVARBINARY:
            case Types.VARBINARY: 
                Blob blob=rs.getBlob(index);
                if(blob!=null) {
                    Value v=new Value(ValueType.lob,blob.getBytes(1,(int)blob.length()));
                    blob.free();
                    return v;
                } else
                    return Value.NULL_VALUE;
            case Types.CLOB:
            case Types.LONGNVARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCLOB:
                Clob clob=rs.getClob(index);
                if(clob!=null) {
                    Value v=new Value(ValueType.lob,clob.getSubString(1,(int)clob.length()));
                    clob.free();
                    return v;
                } else
                    return Value.NULL_VALUE;
            case Types.ARRAY:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"array");
            case Types.DATALINK:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"datalink");
            case Types.DISTINCT:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"distinct");
            case Types.JAVA_OBJECT:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"java_object");
            case Types.NULL:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"null");
            case Types.OTHER:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"other");
            case Types.REF:throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"ref");
            case Types.SQLXML: throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"sqlxml");
            case Types.STRUCT: throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,"struct");            
            }
        } catch (Error e) {
            throw e;
        } catch (Exception x) {
            throw Error.get(DialectErrors.ERR_CANNOT_BIND,x.toString());
        }
        throw Error.get(DialectErrors.ERR_UNSUPPORTED_TYPE,Integer.toString(sqlType));
    }

    /**
     * Parses a JDBC sql type from string
     *
     * @param s The type string
     *
     * @return null if s is null, or is not a valid type. Returns the JDBC type if s is a valid type
     */
    public static Integer parseJDBCType(String s) {
        if(s!=null) {
            int ix=Arrays.binarySearch(JDBCSQLTYPEMAP,new JDBCSqlTypeMap(s,0),new Comparator<JDBCSqlTypeMap>() {
                    public int compare(JDBCSqlTypeMap o1,JDBCSqlTypeMap o2) {
                        return o1.name.compareTo(o2.name);
                    }
                });
            if(ix>=0)
                return JDBCSQLTYPEMAP[ix].type;
        }
        return null;
    }
}


package org.mouraf.goodwill.store;

import org.apache.thrift.protocol.TType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Describe a TField
 */
public class ThriftField
{
    private String name;
    private byte type;
    private Integer position;
    private String sqlType;
    private Integer sqlLength;
    private String description;

    /* Keys for the JSON representation */
    public static final String JSON_THRIFT_FIELD_NAME = "name";
    public static final String JSON_THRIFT_FIELD_TYPE = "type";
    public static final String JSON_THRIFT_FIELD_POSITION = "position";
    public static final String JSON_THRIFT_FIELD_DESCRIPTION = "description";
    // When storing the thrifts in a database/datawarehouse, use these fields to describe
    // the associated sql type and length.
    // Given the large range of data types (Netezza, Oracle, MySQL, ...), no enforcement
    // is performed (plain Strings).
    public static final String JSON_THRIFT_FIELD_SQL_KEY = "sql";
    public static final String JSON_THRIFT_FIELD_SQL_TYPE = "type";
    public static final String JSON_THRIFT_FIELD_SQL_LENGTH = "length";

    /* Human readable representation of Thrift internal types */
    private static final String TTYPE_STRING = "string";
    private static final String TTYPE_I64 = "i64";
    private static final String TTYPE_I32 = "i32";
    private static final String TTYPE_I16 = "i16";
    private static final String TTYPE_BYTE = "i8";
    private static final String TTYPE_BOOL = "bool";
    private static final String TTYPE_DOUBLE = "double";

    public ThriftField(
        String name,
        String typeString,
        Integer position
    )
    {
        this(name, typeString, position, null, null, null);
    }

    public ThriftField(
        String name,
        String typeString,
        Integer position,
        String description
    )
    {
        this(name, typeString, position, description, null, null);
    }

    public ThriftField(
        String name,
        String typeString,
        Integer position,
        String sqlType,
        Integer sqlLength
    )
    {
        this(name, typeString, position, null, sqlType, sqlLength);
    }

    public ThriftField(
        String name,
        String typeString,
        Integer position,
        String description,
        String sqlType,
        Integer sqlLength
    )
    {
        if (name == null) {
            throw new IllegalArgumentException("ThriftField name can't be null");
        }
        this.name = name;

        this.type = ttypeFromString(typeString);

        if (position == null) {
            throw new IllegalArgumentException("ThriftField position can't be null");
        }
        this.position = position;

        // Optional fields
        this.description = description;
        this.sqlType = sqlType;
        this.sqlLength = sqlLength;
    }

    public ThriftField(
        JSONObject thriftItemJSONObject
    ) throws JSONException
    {
        this(thriftItemJSONObject.getString(JSON_THRIFT_FIELD_NAME),
            thriftItemJSONObject.getString(JSON_THRIFT_FIELD_TYPE),
            thriftItemJSONObject.getInt(JSON_THRIFT_FIELD_POSITION),
            thriftItemJSONObject.has(JSON_THRIFT_FIELD_DESCRIPTION) ?
                thriftItemJSONObject.getString(JSON_THRIFT_FIELD_DESCRIPTION) : null,
            thriftItemJSONObject.has(JSON_THRIFT_FIELD_SQL_KEY) ?
                (thriftItemJSONObject.getJSONObject(JSON_THRIFT_FIELD_SQL_KEY).has(JSON_THRIFT_FIELD_SQL_TYPE) ?
                    thriftItemJSONObject.getJSONObject(JSON_THRIFT_FIELD_SQL_KEY).getString(JSON_THRIFT_FIELD_SQL_TYPE) : null) : null,
            thriftItemJSONObject.has(JSON_THRIFT_FIELD_SQL_KEY) ?
                (thriftItemJSONObject.getJSONObject(JSON_THRIFT_FIELD_SQL_KEY).has(JSON_THRIFT_FIELD_SQL_LENGTH) ?
                    thriftItemJSONObject.getJSONObject(JSON_THRIFT_FIELD_SQL_KEY).getInt(JSON_THRIFT_FIELD_SQL_LENGTH) : null) : null);
    }

    /**
     * Lookup a TType associated with a human readable string
     *
     * @param type human readable string
     * @return the TType associated to the type
     */
    private byte ttypeFromString(String type)
    {
        if (type.equals(TTYPE_STRING)) {
            return TType.STRING;
        }
        else if (type.equals(TTYPE_I64)) {
            return TType.I64;
        }
        else if (type.equals(TTYPE_I32)) {
            return TType.I32;
        }
        else if (type.equals(TTYPE_I16)) {
            return TType.I16;
        }
        else if (type.equals(TTYPE_BYTE)) {
            return TType.BYTE;
        }
        else if (type.equals(TTYPE_BOOL)) {
            return TType.BOOL;
        }
        else if (type.equals(TTYPE_DOUBLE)) {
            return TType.DOUBLE;
        }
        else {
            throw new IllegalArgumentException(String.format("%s not a valid TType", type));
        }
    }

    /**
     * Return a human readable string representing a TType
     *
     * @param b TField type
     * @return human readable representation of the type b
     */
    public static String typeStringfromTType(byte b)
    {
        switch (b) {
            case TType.STRING:
                return TTYPE_STRING;
            case TType.I64:
                return TTYPE_I64;
            case TType.I32:
                return TTYPE_I32;
            case TType.I16:
                return TTYPE_I16;
            case TType.BYTE:
                return TTYPE_BYTE;
            case TType.BOOL:
                return TTYPE_BOOL;
            case TType.DOUBLE:
                return TTYPE_DOUBLE;
            default:
                throw new IllegalArgumentException(String.format("%d not a valid TType", b));
        }
    }

    /**
     * Return the field position of the described ThriftField in the associated ThriftType.
     *
     * @return the field position
     */
    public Integer getPosition()
    {
        return position;
    }

    @Override
    public String toString()
    {
        try {
            return toJSON().toString();
        }
        catch (JSONException e) {
            return "ThriftField{" +
                JSON_THRIFT_FIELD_NAME + "='" + name + '\'' +
                ", " + JSON_THRIFT_FIELD_TYPE + "='" + ThriftField.typeStringfromTType(type) + '\'' +
                ", " + JSON_THRIFT_FIELD_POSITION + "=" + position +
                ", " + JSON_THRIFT_FIELD_SQL_TYPE + "='" + sqlType + '\'' +
                ", " + JSON_THRIFT_FIELD_SQL_LENGTH + "=" + sqlLength +
                ", " + JSON_THRIFT_FIELD_DESCRIPTION + "=" + description +
                '}';
        }
    }

    /**
     * Create a JSON representation of the ThriftField. It will always contain the name, type and position.
     * Description and SQL attributes are however optional.
     *
     * @return JSONObject containing all fields
     * @throws JSONException if a serialization exception occurs
     */
    public JSONObject toJSON() throws JSONException
    {
        JSONObject tFieldJSON = new JSONObject()
            .put(JSON_THRIFT_FIELD_NAME, name)
            .put(JSON_THRIFT_FIELD_TYPE, ThriftField.typeStringfromTType(type))
            .put(JSON_THRIFT_FIELD_POSITION, position);

        if (description != null) {
            tFieldJSON.put(JSON_THRIFT_FIELD_DESCRIPTION, description);
        }

        if (sqlType != null || sqlLength != null) {
            JSONObject tFieldSQLJSON = new JSONObject();

            if (sqlType != null) {
                tFieldSQLJSON.put(JSON_THRIFT_FIELD_SQL_TYPE, sqlType);
            }

            if (sqlLength != null) {
                tFieldSQLJSON.put(JSON_THRIFT_FIELD_SQL_LENGTH, sqlLength);
            }

            tFieldJSON.put(JSON_THRIFT_FIELD_SQL_KEY, tFieldSQLJSON);
        }

        return tFieldJSON;
    }
}

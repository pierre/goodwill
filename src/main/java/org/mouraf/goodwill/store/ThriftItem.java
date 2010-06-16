package org.mouraf.goodwill.store;

import org.json.JSONException;
import org.json.JSONObject;

public class ThriftItem
{
    public static byte LONG_TYPE = 0;
    public static byte STRING_TYPE = 1;
    public static byte DOUBLE_TYPE = 2;
    public static byte BOOLEAN_TYPE = 3;
    public static byte INTEGER_TYPE = 4;
    public static byte SHORT_TYPE = 5;
    public static byte BYTE_TYPE = 6;

    private byte type;
    private int position;
    private String name;

    public ThriftItem(
        Integer position,
        String type,
        String name
    )
    {
        this.type = ThriftItem.typeFromString(type);
        this.position = position;
        this.name = name;
    }

    private static byte typeFromString(String type)
    {
        if (type.equals("string")) {
            return STRING_TYPE;
        }
        else if (type.equals("i64")) {
            return DOUBLE_TYPE;
        }
        else if (type.equals("i32")) {
            return INTEGER_TYPE;
        }
        else if (type.equals("i16")) {
            return SHORT_TYPE;
        }
        else if (type.equals("i8")) {
            return SHORT_TYPE;
        }
        else if (type.equals("bool")) {
            return BOOLEAN_TYPE;
        }
        else if (type.equals("date")) {
            return LONG_TYPE;
        }
        else {
            throw new IllegalArgumentException(String.format("%s not supported", type));
        }
    }

    public static String typeStringfromType(byte b)
    {
        if (b == LONG_TYPE) {
            return "date";
        }
        else if (b == STRING_TYPE) {
            return "string";
        }
        else if (b == DOUBLE_TYPE) {
            return "i64";
        }
        else if (b == BOOLEAN_TYPE) {
            return "bool";
        }
        else if (b == INTEGER_TYPE) {
            return "i32";
        }
        else if (b == SHORT_TYPE) {
            return "i8";
        }
        else {
            throw new IllegalArgumentException(String.format("%d not supported", b));
        }
    }

    public byte getType()
    {
        return type;
    }

    public int getPosition()
    {
        return position;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "ThriftItem{" +
            "name='" + name + '\'' +
            ", type=" + ThriftItem.typeStringfromType(type) +
            ", position=" + position +
            '}';
    }

    public JSONObject toJSON() throws JSONException
    {
        return new JSONObject()
            .put("name", name)
            .put("type", type)
            .put("position", position);
    }
}

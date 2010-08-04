package org.mouraf.goodwill.store;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ThriftFieldTest
{
    private static final String THRIFT_FIELD_NAME = "fileName";
    private static final String THRIFT_FIELD_TYPE = "string";
    private static final Integer THRIFT_FIELD_POSITION = 1;
    private static final String THRIFT_FIELD_DESCRIPTION = "Name of the file attached to a blob";
    private static final String THRIFT_FIELD_SQL_TYPE = "varchar";
    private static final Integer THRIFT_FIELD_SQL_LENGTH = 255;
    private static final Integer THRIFT_FIELD_SQL_SCALE = 12;
    private static final Integer THRIFT_FIELD_SQL_PRECISION = 12;
    
    private ThriftField thriftField;
    private ThriftField thriftFieldWithSQL;
    private ThriftField thriftFieldWithDescription;
    private ThriftField thriftFieldWithSQLAndDescription;

    @BeforeTest(alwaysRun = true)
    public void setUp()
    {
        thriftField = new ThriftField(THRIFT_FIELD_NAME, THRIFT_FIELD_TYPE, THRIFT_FIELD_POSITION);
        thriftFieldWithSQL = new ThriftField(THRIFT_FIELD_NAME, THRIFT_FIELD_TYPE, THRIFT_FIELD_POSITION,
            THRIFT_FIELD_SQL_TYPE, THRIFT_FIELD_SQL_LENGTH);
        thriftFieldWithDescription = new ThriftField(THRIFT_FIELD_NAME, THRIFT_FIELD_TYPE, THRIFT_FIELD_POSITION,
            THRIFT_FIELD_DESCRIPTION);
        thriftFieldWithSQLAndDescription = new ThriftField(THRIFT_FIELD_NAME, THRIFT_FIELD_TYPE, THRIFT_FIELD_POSITION,
            THRIFT_FIELD_DESCRIPTION, THRIFT_FIELD_SQL_TYPE, THRIFT_FIELD_SQL_LENGTH, THRIFT_FIELD_SQL_SCALE, THRIFT_FIELD_SQL_PRECISION);
    }

    @Test
    public void testJSONConstructor() throws Exception
    {
        thriftField = new ThriftField(thriftField.toJSON());
        thriftFieldWithSQL = new ThriftField(thriftFieldWithSQL.toJSON());
        thriftFieldWithDescription = new ThriftField(thriftFieldWithDescription.toJSON());
        thriftFieldWithSQLAndDescription = new ThriftField(thriftFieldWithSQLAndDescription.toJSON());

        runAllAsserts();
    }

    @Test
    public void testToJSON() throws Exception
    {
        runAllAsserts();
    }

    private void runAllAsserts() throws JSONException
    {
        JSONObject jsonThriftFieldSQL = null;

        JSONObject jsonThiftField = thriftField.toJSON();
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_NAME));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_NAME), THRIFT_FIELD_NAME);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_TYPE));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_TYPE), THRIFT_FIELD_TYPE);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_POSITION));
        Assert.assertEquals(jsonThiftField.getInt(ThriftField.JSON_THRIFT_FIELD_POSITION), (int) THRIFT_FIELD_POSITION);
        Assert.assertFalse(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_DESCRIPTION));
        Assert.assertFalse(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_SQL_KEY));

        jsonThiftField = thriftFieldWithSQL.toJSON();
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_NAME));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_NAME), THRIFT_FIELD_NAME);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_TYPE));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_TYPE), THRIFT_FIELD_TYPE);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_POSITION));
        Assert.assertEquals(jsonThiftField.getInt(ThriftField.JSON_THRIFT_FIELD_POSITION), (int) THRIFT_FIELD_POSITION);
        Assert.assertFalse(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_DESCRIPTION));
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_SQL_KEY));
        jsonThriftFieldSQL = jsonThiftField.getJSONObject(ThriftField.JSON_THRIFT_FIELD_SQL_KEY);
        Assert.assertTrue(jsonThriftFieldSQL.has(ThriftField.JSON_THRIFT_FIELD_SQL_TYPE));
        Assert.assertEquals(jsonThriftFieldSQL.getString(ThriftField.JSON_THRIFT_FIELD_SQL_TYPE), THRIFT_FIELD_SQL_TYPE);
        Assert.assertTrue(jsonThriftFieldSQL.has(ThriftField.JSON_THRIFT_FIELD_SQL_LENGTH));
        Assert.assertEquals(jsonThriftFieldSQL.getInt(ThriftField.JSON_THRIFT_FIELD_SQL_LENGTH), (int) THRIFT_FIELD_SQL_LENGTH);

        jsonThiftField = thriftFieldWithDescription.toJSON();
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_NAME));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_NAME), THRIFT_FIELD_NAME);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_TYPE));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_TYPE), THRIFT_FIELD_TYPE);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_POSITION));
        Assert.assertEquals(jsonThiftField.getInt(ThriftField.JSON_THRIFT_FIELD_POSITION), (int) THRIFT_FIELD_POSITION);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_DESCRIPTION));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_DESCRIPTION), THRIFT_FIELD_DESCRIPTION);
        Assert.assertFalse(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_SQL_KEY));

        jsonThiftField = thriftFieldWithSQLAndDescription.toJSON();
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_NAME));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_NAME), THRIFT_FIELD_NAME);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_TYPE));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_TYPE), THRIFT_FIELD_TYPE);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_POSITION));
        Assert.assertEquals(jsonThiftField.getInt(ThriftField.JSON_THRIFT_FIELD_POSITION), (int) THRIFT_FIELD_POSITION);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_DESCRIPTION));
        Assert.assertEquals(jsonThiftField.getString(ThriftField.JSON_THRIFT_FIELD_DESCRIPTION), THRIFT_FIELD_DESCRIPTION);
        Assert.assertTrue(jsonThiftField.has(ThriftField.JSON_THRIFT_FIELD_SQL_KEY));
        jsonThriftFieldSQL = jsonThiftField.getJSONObject(ThriftField.JSON_THRIFT_FIELD_SQL_KEY);
        Assert.assertTrue(jsonThriftFieldSQL.has(ThriftField.JSON_THRIFT_FIELD_SQL_TYPE));
        Assert.assertEquals(jsonThriftFieldSQL.getString(ThriftField.JSON_THRIFT_FIELD_SQL_TYPE), THRIFT_FIELD_SQL_TYPE);
        Assert.assertTrue(jsonThriftFieldSQL.has(ThriftField.JSON_THRIFT_FIELD_SQL_LENGTH));
        Assert.assertEquals(jsonThriftFieldSQL.getInt(ThriftField.JSON_THRIFT_FIELD_SQL_LENGTH), (int) THRIFT_FIELD_SQL_LENGTH);
    }
}

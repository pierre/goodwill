/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.metrics.goodwill.store;

import com.ning.metrics.goodwill.access.GoodwillSchema;
import com.ning.metrics.goodwill.access.GoodwillSchemaField;
import com.ning.metrics.goodwill.dao.DAOBoneCPAccess;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

//CREATE TABLE `thrift_types_tests` (
//   `event_type` varchar(255) DEFAULT NULL,
//   `field_id` int(11) DEFAULT NULL,
//   `field_type` varchar(255) DEFAULT NULL,
//   `field_name` varchar(255) DEFAULT NULL,
//   `sql_type` varchar(255) DEFAULT NULL,
//   `sql_length` int(11) DEFAULT NULL,
//   `sql_scale` int(11) DEFAULT NULL,
//   `sql_precision` int(11) DEFAULT NULL,
//   `description` varchar(255) DEFAULT NULL,
//   `id` int(11) NOT NULL AUTO_INCREMENT,
//   PRIMARY KEY (`id`),
//   UNIQUE KEY `unique_fields` (`event_type`,`field_id`)
//) ENGINE=InnoDB AUTO_INCREMENT=114 DEFAULT CHARSET=utf8

public class MySQLStoreTest
{
    private MySQLStore store;
    private DAOBoneCPAccess access;

    private static final String TYPE2_NAME = "Indiana Jones and the Last Crusade";
    private GoodwillSchema type1;
    private GoodwillSchema type2;
    private static final String TYPE1_NAME = "The Shawshank Redemption";

    @BeforeTest(alwaysRun = false, enabled = false)
    public void setUp() throws SQLException, IOException, ClassNotFoundException
    {
        access = new DAOBoneCPAccess("localhost", 3306, "goodwill", "root", "");
        store = new MySQLStore("thrift_types_tests", access);

        type1 = new GoodwillSchema(TYPE1_NAME, new ArrayList<GoodwillSchemaField>());
        type1.addThriftField(new GoodwillSchemaField("chair", "i32", (short) 0, null, null, null, null, null));
        type1.addThriftField(new GoodwillSchemaField("deal", "string", (short) 1, null, null, null, null, null));
        type1.addThriftField(new GoodwillSchemaField("continent", "string", (short) 2, null, null, null, null, null));
        type1.addThriftField(new GoodwillSchemaField("egg", "string", (short) 3, null, null, null, null, null));
        type1.addThriftField(new GoodwillSchemaField("car", "i32", (short) 4, null, null, null, null, null));
        type1.addThriftField(new GoodwillSchemaField("bear", "string", (short) 5, null, null, null, null, null));

        type2 = new GoodwillSchema(TYPE2_NAME, new ArrayList<GoodwillSchemaField>());
        type2.addThriftField(new GoodwillSchemaField("arm", "bool", (short) 0, null, null, null, null, null));
        type2.addThriftField(new GoodwillSchemaField("consonent", "i16", (short) 1, null, null, null, null, null));
        type2.addThriftField(new GoodwillSchemaField("bank", "bool", (short) 2, null, null, null, null, null));
        type2.addThriftField(new GoodwillSchemaField("cover", "string", (short) 3, null, null, null, null, null));
        type2.addThriftField(new GoodwillSchemaField("century", "string", (short) 4, null, null, null, null, null));
        type2.addThriftField(new GoodwillSchemaField("city", "string", (short) 5, null, null, null, null, null));
    }

    @AfterTest(alwaysRun = false, enabled = false)
    public void tearDown() throws SQLException
    {
    }

    @Test(enabled = false)
    public void testInvalidThriftField()
    {
        try {
            new GoodwillSchemaField("test", "string", (short) 1, "", null, 2, 23, 5);
            Assert.fail();
        }
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);

        }
    }


    @Test(enabled = false)
    public void testAddUpdateType() throws Exception
    {
        Collection<GoodwillSchema> types = store.getTypes();
        Assert.assertEquals(types.size(), 0, "You need to cleanup your test db");

        // Inserts

        addThriftType1();

        types = store.getTypes();
        Assert.assertEquals(types.size(), 1);

        addThriftType2();

        types = store.getTypes();
        Assert.assertEquals(types.size(), 2);

        Assert.assertNull(store.findByName("Frenchies won 1998 world cup"));

        runAssertsOnFields();

        // Updates

        type1.addThriftField(new GoodwillSchemaField("foo", "string", (short) 6, null, null, null, null, null));

        Assert.assertEquals(store.findByName(TYPE1_NAME).getSchema().size(), 6);
        Assert.assertTrue(store.updateType(type1));
        types = store.getTypes();
        Assert.assertEquals(store.findByName(TYPE1_NAME).getSchema().size(), 7);
        Assert.assertEquals(types.size(), 2);

        runAssertsOnFields();

        // TODO Test for dups
    }

    private void runAssertsOnFields()
    {
        GoodwillSchema shouldBeType1 = store.findByName(TYPE1_NAME);
        for (GoodwillSchemaField field : shouldBeType1.getSchema()) {
            Assert.assertEquals(field.getName(), type1.getFieldByPosition(field.getId()).getName());
            Assert.assertEquals(field.getType(), type1.getFieldByPosition(field.getId()).getType());
            Assert.assertEquals(field.getDescription(), type1.getFieldByPosition(field.getId()).getDescription());
            Assert.assertEquals(field.getSql().getType(), type1.getFieldByPosition(field.getId()).getSql().getType());
            Assert.assertEquals(field.getSql().getLength(), type1.getFieldByPosition(field.getId()).getSql().getLength());
            Assert.assertEquals(field.getId(), type1.getFieldByPosition(field.getId()).getId());
        }
    }

    private void addThriftType2()
    {
        store.addType(type2);
    }

    private void addThriftType1()
    {
        store.addType(type1);
    }
}

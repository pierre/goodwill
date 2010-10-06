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

import com.ning.metrics.goodwill.access.ThriftField;
import com.ning.metrics.goodwill.access.ThriftType;
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
    private static final String TYPE2_NAME = "Indiana Jones and the Last Crusade";
    private ThriftType type1;
    private ThriftType type2;
    private static final String TYPE1_NAME = "The Shawshank Redemption";

    @BeforeTest(alwaysRun = false, enabled = false)
    public void setUp() throws SQLException, IOException, ClassNotFoundException
    {
        store = new MySQLStore("localhost", 3306, "goodwill", "root", "", "thrift_types_tests");

        type1 = new ThriftType(TYPE1_NAME, new ArrayList<ThriftField>());
        type1.addThriftField(new ThriftField("chair", "i32", 0, null, null, null, null, null));
        type1.addThriftField(new ThriftField("deal", "string", 1, null, null, null, null, null));
        type1.addThriftField(new ThriftField("continent", "string", 2, null, null, null, null, null));
        type1.addThriftField(new ThriftField("egg", "string", 3, null, null, null, null, null));
        type1.addThriftField(new ThriftField("car", "i32", 4, null, null, null, null, null));
        type1.addThriftField(new ThriftField("bear", "string", 5, null, null, null, null, null));

        type2 = new ThriftType(TYPE2_NAME, new ArrayList<ThriftField>());
        type2.addThriftField(new ThriftField("arm", "bool", 0, null, null, null, null, null));
        type2.addThriftField(new ThriftField("consonent", "i16", 1, null, null, null, null, null));
        type2.addThriftField(new ThriftField("bank", "bool", 2, null, null, null, null, null));
        type2.addThriftField(new ThriftField("cover", "string", 3, null, null, null, null, null));
        type2.addThriftField(new ThriftField("century", "string", 4, null, null, null, null, null));
        type2.addThriftField(new ThriftField("city", "string", 5, null, null, null, null, null));
    }

    @AfterTest(alwaysRun = false, enabled = false)
    public void tearDown() throws SQLException
    {
        store.close();
    }

    @Test(enabled = false)
    public void testInvalidThriftField()
    {
        try {
            new ThriftField("test", "string", 1, "", null, 2, 23, 5);
            Assert.fail();
        }
        catch (IllegalArgumentException e) {
            Assert.assertTrue(true);

        }
    }


    @Test(enabled = false)
    public void testAddUpdateType() throws Exception
    {
        Collection<ThriftType> types = store.getTypes();
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

        type1.addThriftField(new ThriftField("foo", "string", 6, null, null, null, null, null));

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
        ThriftType shouldBeType1 = store.findByName(TYPE1_NAME);
        for (ThriftField field : shouldBeType1.getSchema()) {
            Assert.assertEquals(field.getName(), type1.getFieldByPosition(field.getPosition()).getName());
            Assert.assertEquals(field.getType(), type1.getFieldByPosition(field.getPosition()).getType());
            Assert.assertEquals(field.getDescription(), type1.getFieldByPosition(field.getPosition()).getDescription());
            Assert.assertEquals(field.getSql().getType(), type1.getFieldByPosition(field.getPosition()).getSql().getType());
            Assert.assertEquals(field.getSql().getLength(), type1.getFieldByPosition(field.getPosition()).getSql().getLength());
            Assert.assertEquals(field.getPosition(), type1.getFieldByPosition(field.getPosition()).getPosition());
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

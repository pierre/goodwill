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

package org.mouraf.goodwill.store;

import org.json.JSONException;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class ThriftTypeTest
{
    private static final String THRIFT_TYPE_NAME = "FrontDoorVisit";
    private static final String THRIFT_FIELD_NAME = "fileName";
    private static final String THRIFT_FIELD_TYPE = "string";
    private static final Integer THRIFT_FIELD_POSITION = 1;
    private static final String THRIFT_FIELD_DESCRIPTION = "Name of the file attached to a blob";
    private static final String THRIFT_FIELD_SQL_TYPE = "varchar";
    private static final Integer THRIFT_FIELD_SQL_LENGTH = 255;
    private static final Integer THRIFT_FIELD_SQL_SCALE = 12;
    private static final Integer THRIFT_FIELD_SQL_PRECISION = 4;

    private ThriftField thriftFieldWithSQLAndDescription;
    private ThriftType thriftType;

    @BeforeTest(alwaysRun = true)
    public void setUp()
    {
        thriftFieldWithSQLAndDescription = new ThriftField(THRIFT_FIELD_NAME, THRIFT_FIELD_TYPE, THRIFT_FIELD_POSITION,
            THRIFT_FIELD_DESCRIPTION, THRIFT_FIELD_SQL_TYPE, THRIFT_FIELD_SQL_LENGTH, THRIFT_FIELD_SQL_SCALE, THRIFT_FIELD_SQL_PRECISION);
        thriftType = new ThriftType(THRIFT_TYPE_NAME);
        thriftType.addThriftField(thriftFieldWithSQLAndDescription);
    }

    @Test
    public void testConstructor() throws Exception
    {
        runAllAssertions();
    }

    @Test
    public void testToJson() throws Exception
    {
        thriftType = new ThriftType(thriftType.toJSON());

        runAllAssertions();
    }

    private void runAllAssertions() throws JSONException
    {
        Assert.assertEquals(thriftType.getName(), THRIFT_TYPE_NAME);
        Assert.assertEquals(thriftType.getFieldByPosition(THRIFT_FIELD_POSITION).toString(), thriftFieldWithSQLAndDescription.toString());
    }


}

package org.mouraf.goodwill.store;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
  CREATE TABLE `thrift_types` (  `event_type` varchar(255) DEFAULT NULL,  `field_id` int(11) DEFAULT NULL,
  `field_type` varchar(255) DEFAULT NULL,
  `field_name` varchar(255) DEFAULT NULL,
  `sql_type` varchar(255) DEFAULT NULL,
  `sql_length` int(11) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  UNIQUE KEY `unique_fields` (`event_type`,`field_id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8
 */
public class MySQLStoreTest
{
    private MySQLStore store;

    @BeforeTest(alwaysRun = false)
    public void setUp() throws SQLException, IOException, ClassNotFoundException
    {
        store = new MySQLStore("localhost", "goodwill", "root", "thrift_types");
    }

    @AfterTest(alwaysRun = false)
    public void tearDown() throws SQLException
    {
        store.close();
    }

    @Test
    public void testGetTypes() throws Exception
    {
        List<ThriftType> types = store.getTypes();
        Assert.assertEquals(types.size(), 0);
    }

        @Test
    public void testAddType() throws Exception
    {
        List<ThriftType> types = store.getTypes();
    }
}

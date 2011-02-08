package com.ning.metrics.goodwill.dao;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;

import javax.sql.DataSource;

@Singleton
public class DAOBoneCPAccess implements DAOAccess
{
    private final DataSource dataSource;

    @Inject
    public DAOBoneCPAccess(
        GoodwillConfig config
    )
    {
        this(config.getStoreDBHost(), config.getStoreDBPort(), config.getStoreDBName(), config.getStoreDBUsername(), config.getStoreDBPassword());

    }

    public DAOBoneCPAccess(String DBHost,
                               int DBPort,
                               String DBName,
                               String DBUsername,
                               String DBPassword)
    {
        BoneCPConfig boneCPConfig = new BoneCPConfig();

        boneCPConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s", DBHost, DBPort, DBName));
        boneCPConfig.setUsername(DBUsername);
        boneCPConfig.setPassword(DBPassword);
        boneCPConfig.setMinConnectionsPerPartition(1);
        boneCPConfig.setMaxConnectionsPerPartition(10);
        boneCPConfig.setPartitionCount(1);

        dataSource = new BoneCPDataSource(boneCPConfig);
    }

    @Override
    public DataSource getDataSource()
    {
        return dataSource;
    }
}

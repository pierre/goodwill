package com.ning.metrics.goodwill.binder.modules;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import com.ning.metrics.goodwill.dao.DAOBoneCPAccess;
import com.ning.metrics.goodwill.store.CSVFileStore;
import com.ning.metrics.goodwill.store.GoodwillStore;
import com.ning.metrics.goodwill.store.MySQLStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GoodwillStoreProvider implements Provider<GoodwillStore>
{
    private static final Logger log = LoggerFactory.getLogger(GoodwillServicesModule.class);

    private final GoodwillConfig config;

    @Inject
    public GoodwillStoreProvider(final GoodwillConfig config)
    {
        this.config = config;
    }

    @Override
    public GoodwillStore get()
    {
        final String storeType = config.getStoreType();
        if (storeType.equals("mysql")) {
            log.info("Enabling MySQL store");
            try {
                return new MySQLStore(config, new DAOBoneCPAccess(config));
            }
            catch (IOException e) {
                log.error("Unable to connect to MySQL", e);
            }
        }
        else if (storeType.equals("csv")) {
            log.info("Enabling CSV store");
            try {
                return new CSVFileStore(config);
            }
            catch (IOException e) {
                log.error("Unable to create the CSV file store", e);
            }
        }
        else {
            throw new IllegalStateException("Unknown store type " + storeType);
        }

        return null;
    }
}

package com.ning.metrics.goodwill.binder.modules;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import com.ning.metrics.goodwill.sink.GoodwillSink;
import com.ning.metrics.goodwill.sink.NetezzaSink;
import org.apache.log4j.Logger;

public class GoodwillSinkProvider implements Provider<GoodwillSink>
{
    private static final Logger log = Logger.getLogger(GoodwillSinkProvider.class);

    private final GoodwillConfig config;

    @Inject
    public GoodwillSinkProvider(final GoodwillConfig config)
    {
        this.config = config;
    }

    @Override
    public GoodwillSink get()
    {
        final String sinkType = config.getSinkType();
        if (sinkType == null) {
            return null;
        }
        else if (sinkType.equals("netezza")) {
            log.info("Enabled Netezza sink");
            return new NetezzaSink(config);
        }
        else {
            throw new IllegalStateException("Unknown sink type " + sinkType);
        }
    }
}

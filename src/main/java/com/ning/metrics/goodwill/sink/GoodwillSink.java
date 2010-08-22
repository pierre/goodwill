package com.ning.metrics.goodwill.sink;

import com.ning.metrics.goodwill.store.ThriftType;

public interface GoodwillSink
{
    /**
     * Add a new type to the sink
     *
     * @param thriftType ThriftType to add
     */
    public boolean addType(ThriftType thriftType) throws Exception;

    /**
     * Update a type to the sink
     *
     * @param thriftType ThriftType to update
     * @return true is success, false otherwise
     */
    public boolean updateType(ThriftType thriftType);

    /**
     * Give human readable information on how to add a Type in the sink
     * This is used in the UI
     *
     * @param thriftType ThriftType to add
     * @return info how to create a Type in the sink
     */
    public String addTypeInfo(ThriftType thriftType);
}
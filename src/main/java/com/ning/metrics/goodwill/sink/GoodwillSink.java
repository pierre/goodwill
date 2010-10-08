package com.ning.metrics.goodwill.sink;

import com.ning.metrics.goodwill.access.GoodwillSchema;

public interface GoodwillSink
{
    /**
     * Add a new type to the sink
     *
     * @param schema GoodwillSchema to add
     * @return true on success, false otherwise
     * @throws Exception if an exception occurs talking to the sink
     */
    public boolean addType(GoodwillSchema schema) throws Exception;

    /**
     * Update a type to the sink
     *
     * @param schema GoodwillSchema to update
     * @return true is success, false otherwise
     */
    public boolean updateType(GoodwillSchema schema);

    /**
     * Give human readable information on how to add a Type in the sink
     * This is used in the UI
     *
     * @param schema GoodwillSchema to add
     * @return info how to create a Type in the sink
     */
    public String addTypeInfo(GoodwillSchema schema);
}
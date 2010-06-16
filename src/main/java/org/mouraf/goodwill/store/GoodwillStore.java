package org.mouraf.goodwill.store;

import java.io.IOException;
import java.util.List;

public interface GoodwillStore
{
    public List<ThriftType> getTypes() throws IOException;
}

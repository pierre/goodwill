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

import au.com.bytecode.opencsv.CSVReader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ning.metrics.goodwill.access.ThriftField;
import com.ning.metrics.goodwill.access.ThriftType;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Singleton
public class CSVFileStore extends GoodwillStore
{
    private final Logger log = Logger.getLogger(CSVFileStore.class);

    private List<ThriftType> thriftTypes;
    private String fileName;

    @Inject
    public CSVFileStore(
        GoodwillConfig config
    ) throws IOException
    {
        fileName = config.getCSVFilePath();
        parseFile();
    }

    public void parseFile() throws IOException
    {
        CSVReader reader = new CSVReader(new FileReader(fileName));
        log.info(String.format("Read CSV file: %s", fileName));
        List<String[]> entries = reader.readAll();

        ThriftType currentThriftType = null;
        String currentThriftTypeName = null;

        /**
         * CSV file format:
         *
         * "TermFrequency",1,"i64","app_id",1,2147483647
         * "TermFrequency",2,"string","subdomain",1,2147483647
         * "TermFrequency",3,"string","term_freq_json",1,2147483647
         * "SpamMarkEvent",19,"string","abuse_type",8,2147483647
         * ...
         *
         * TODO: extend file format with extra sql fields
         */
        List<ThriftType> thriftTypes = new ArrayList<ThriftType>();
        for (Object entry : entries) {
            short position;
            ThriftField thriftField;
            String[] line = (String[]) entry;

            try {
                position = Short.valueOf(line[1]);
            }
            catch (NumberFormatException e) {
                log.warn(String.format("Ignoring malformed line: %s", StringUtils.join(line, ",")));
                continue;
            }

            try {
                thriftField = new ThriftField(line[3], line[2], position, null, null, null, null, null);
            }
            catch (IllegalArgumentException e) {
                log.warn(String.format("Ignoring unsupported type <%s>: %s", line[2], StringUtils.join(line, ",")));
                continue;
            }

            if (currentThriftTypeName == null || !line[0].equals(currentThriftTypeName)) {
                currentThriftTypeName = line[0];
                currentThriftType = new ThriftType(currentThriftTypeName, new ArrayList<ThriftField>());
                thriftTypes.add(currentThriftType);
                log.debug(String.format("Found new ThriftType thriftField to: %s", currentThriftTypeName));
            }

            currentThriftType.addThriftField(thriftField);
            log.debug(String.format("Added ThriftField to %s: %s", currentThriftTypeName, thriftField.toString()));
        }

        this.thriftTypes = thriftTypes;
    }

    public Collection<ThriftType> getTypes()
    {
        return thriftTypes;
    }

    /**
     * Add a new type to the store
     *
     * @param thriftType ThriftType to add
     */
    @Override
    public void addType(ThriftType thriftType)
    {
        thriftTypes.add(thriftType);
    }

    /**
     * Update a type to the store
     *
     * @param thriftType ThriftType to update
     */
    @Override
    public boolean updateType(ThriftType thriftType)
    {
        // Seek etc. Painful here
        return false;
    }
}

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
import com.ning.metrics.goodwill.access.GoodwillSchema;
import com.ning.metrics.goodwill.access.GoodwillSchemaField;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Singleton
public class CSVFileStore extends GoodwillStore
{
    private final Logger log = Logger.getLogger(CSVFileStore.class);

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

        GoodwillSchema currentSchema = null;
        String currentSchemaName = null;

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
        HashMap<String, GoodwillSchema> schemata = new HashMap<String, GoodwillSchema>();
        for (Object entry : entries) {
            short position;
            GoodwillSchemaField thriftField;
            String[] line = (String[]) entry;

            try {
                position = Short.valueOf(line[1]);
            }
            catch (NumberFormatException e) {
                log.warn(String.format("Ignoring malformed line: %s", StringUtils.join(line, ",")));
                continue;
            }

            try {
                thriftField = new GoodwillSchemaField(line[3], line[2], position, null, null, null, null, null);
            }
            catch (IllegalArgumentException e) {
                log.warn(String.format("Ignoring unsupported type <%s>: %s", line[2], StringUtils.join(line, ",")));
                continue;
            }

            if (currentSchemaName == null || !line[0].equals(currentSchemaName)) {
                currentSchemaName = line[0];
                currentSchema = new GoodwillSchema(currentSchemaName, new ArrayList<GoodwillSchemaField>());
                schemata.put(currentSchemaName, currentSchema);
                log.debug(String.format("Found new ThriftType thriftField to: %s", currentSchemaName));
            }

            currentSchema.addThriftField(thriftField);
            log.debug(String.format("Added ThriftField to %s: %s", currentSchemaName, thriftField.toString()));
        }

        this.goodwillSchemata = schemata;
    }

    @Override
    public Collection<GoodwillSchema> getTypes() throws IOException
    {
        parseFile();

        final ArrayList<GoodwillSchema> thriftTypesList = new ArrayList(goodwillSchemata.values());
        Collections.sort(thriftTypesList, new Comparator<GoodwillSchema>()
        {
            @Override
            public int compare(GoodwillSchema o, GoodwillSchema o1)
            {
                return o.getName().compareTo(o1.getName());
            }
        });

        if (sink != null) {
            for (int i = 0; i < thriftTypesList.size(); i++) {
                GoodwillSchema schema = thriftTypesList.get(i);
                schema.setSinkAddInfo(sink.addTypeInfo(schema));
                thriftTypesList.set(i, schema);

            }
        }

        return thriftTypesList;
    }

    /**
     * Add a new type to the store
     *
     * @param schema GoodwillSchema to add
     */
    @Override
    public void addType(GoodwillSchema schema)
    {
        goodwillSchemata.put(schema.getName(), schema);
    }

    /**
     * Update a type to the store
     *
     * @param schema GoodwillSchema to update
     */
    @Override
    public boolean updateType(GoodwillSchema schema)
    {
        // Seek etc. Painful here
        return false;
    }
}

package org.mouraf.goodwill.store;

import au.com.bytecode.opencsv.CSVReader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.mouraf.goodwill.binder.config.GoodwillConfig;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class CSVFileStore implements GoodwillStore
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
         */
        List<ThriftType> thriftTypes = new ArrayList<ThriftType>();
        for (Object entry : entries) {
            Integer position = -1;
            ThriftField thriftField = null;
            String[] line = (String[]) entry;

            try {
                position = Integer.valueOf(line[1]);
            }
            catch (NumberFormatException e) {
                log.warn(String.format("Ignoring malformed line: %s", StringUtils.join(line, ",")));
                continue;
            }

            try {
                thriftField = new ThriftField(line[3], line[2], position);
            }
            catch (IllegalArgumentException e) {
                log.warn(String.format("Ignoring unsupported type <%s>: %s", line[2], StringUtils.join(line, ",")));
                continue;
            }

            if (currentThriftTypeName == null || !line[0].equals(currentThriftTypeName)) {
                currentThriftTypeName = line[0];
                currentThriftType = new ThriftType(currentThriftTypeName);
                thriftTypes.add(currentThriftType);
                log.debug(String.format("Found new ThriftType thriftField to: %s", currentThriftTypeName));
            }

            currentThriftType.addThriftField(thriftField);
            log.debug(String.format("Added ThriftField to %s: %s", currentThriftTypeName, thriftField.toString()));
        }

        this.thriftTypes = thriftTypes;
    }

    public List<ThriftType> getTypes()
    {
        return thriftTypes;
    }

    /**
     * Given a Thrift name, find it in the store
     *
     * @param typeName name of the Thrift to search
     * @return the ThriftType if found, null otherwise
     */
    @Override
    public ThriftType findByName(String typeName)
    {
        for (ThriftType thriftType : getTypes()) {
            if (thriftType.getName().equals(typeName)) {
                return thriftType;
            }
        }

        return null;
    }

    /**
     * Serialize all Thrifts in the store
     *
     * @return JSONArray representation
     */
    @Override
    public JSONArray toJSON() throws JSONException
    {
        JSONArray array = new JSONArray();
        for (ThriftType type : getTypes()) {
            array.put(type.toJSON());
        }

        return array;
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

}

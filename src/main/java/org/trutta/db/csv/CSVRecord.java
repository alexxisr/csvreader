package org.trutta.db.csv;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CSV file row.
 */
public final class CSVRecord {
    /**
     * Fields of record.
     */
    private List<String> fields;
    /**
     * Header to search fields by name.
     */
    private Map<String, Integer> header;

    /**
     * Empty record.
     */
    public CSVRecord() {
        fields = new ArrayList<>();
    }

    /**
     * Get field of record.
     * @param index index of field (starting from 0).
     * @return text of field.
     */
    public String get(final int index) {
        return this.fields.get(index);
    }

    /**
     * Get field by it`s name in header.
     * Header must be read at starting of parser.
     * @param fName name of field.
     * @return text of field or null if not found.
     */
    public String get(final String fName) {
        String result = null;
        if (header != null && header.get(fName) != null) {
            result = get(header.get(fName));
        }
        return result;
    }

    /**
     * Get number of fields in record.
     * @return number of fields in record.
     */
    public int size() {
        return fields == null ? 0 : fields.size();
    }

    /**
     * Setter for fields.
     * @param fields array of fields.
     */
    public void setFields(final List<String> fields) {
        this.fields = fields;
    }

    /**
     * Setter for header.
     * @param header header.
     */
    public void setHeader(final Map<String, Integer> header) {
        this.header = header;
    }
}

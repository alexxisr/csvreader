package org.trutta.db.csv;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Assertions;

import org.trutta.db.csv.CSVRecord;

public class CSVRecordTest {
    @Test
    void emptyRecord() {
        CSVRecord record = new CSVRecord();
        assertEquals(0, record.size());
    }

    @Test
    void getField() {
        CSVRecord record = new CSVRecord();
        record.setFields(List.of("string1", "string2"));
        assertEquals("string2", record.get(1));
    }

    @Test
    void getByFieldName() {
        CSVRecord record = new CSVRecord();
        record.setFields(List.of("string1", "string2"));
        record.setHeader(Map.ofEntries(
                    Map.entry("a", 0),
                    Map.entry("b", 1)));
        assertEquals("string1", record.get("a"));
    }

    @Test
    void getUnknownField() {
        CSVRecord record = new CSVRecord();
        record.setFields(List.of("string1", "string2"));
        record.setHeader(Map.ofEntries(
                    Map.entry("a", 0),
                    Map.entry("b", 1)));
        Assertions.assertNull(record.get("c"));
    }
}

package org.trutta.db.csv;

import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

public class CSVParserTest {
    @Test
    void emptyFile() {
        final String source = "";
        CSVParser parser = new CSVParser(new StringReader(source));
        try {
            Assertions.assertNull(parser.readRecord());
        } catch (Exception e) {
            Assertions.fail("unexpected exception " + e.getMessage());
        }
    }

    @Test
    void normalRecord() {
        final String source = "str1;str2";
        CSVParser parser = new CSVParser(new StringReader(source));
        try {
            CSVRecord record = parser.readRecord();
            Assertions.assertNotNull(record);
            Assertions.assertEquals("str2", record.get(1));
        } catch (Exception e) {
            Assertions.fail("unexpected exception " + e.getMessage());
        }
    }

    @Test
    void headerRecord() {
        final String source = "a;b\r\nstr1;str2";
        CSVParser parser = new CSVParser(new StringReader(source));
        try {
            parser.readHeader();
            CSVRecord record = parser.readRecord();
            Assertions.assertNotNull(record);
            Assertions.assertEquals("str2", record.get("b"));
        } catch (Exception e) {
            Assertions.fail("unexpected exception " + e.getMessage());
        }
    }
}

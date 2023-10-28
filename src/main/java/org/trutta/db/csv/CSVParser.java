package org.trutta.db.csv;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Parser of CSV files.
 */
public final class CSVParser implements Iterable<CSVRecord> {
    /**
     * Input we read from.
     */
    private final BufferedReader input;
    /**
     * Current row number.
     */
    private long currentLine;

    /**
     * Size of read buffer.
     */
    private static final int BUFFER_SIZE = 16;

    /**
     * BOM codepoint.
     */
    public static final int BOM = 0xFEFF;
    /**
     * Field separotor, semicolon.
     */
    public static final int FIELD_SEP = 0x003B;
    /**
     * Row separator, end of line.
     */
    public static final int RECORD_SEP = 0x000D;
    /**
     * End of file pseudo codepoint.
     */
    public static final int EOF = -1;
    /**
     * Quote mark codepoint.
     */
    public static final int QUOTE = 0x0022;
    /**
     * Second row separotor codepoint.
     */
    private static final int RECORD_SEP2 = 0x000A;

    /**
     * Header structure.
     */
    private Map<String, Integer> header;


    /**
     * Read any input stream.
     * @param stream the InputStream to read csv from.
     */
    public CSVParser(final InputStream stream) {
        this (new InputStreamReader(stream, StandardCharsets.UTF_8));
    }

    /**
     * Read from reader.
     * @param reader the InputStreamReader to read csv from.
     */
    public CSVParser(final InputStreamReader reader) {
        this (new BufferedReader(reader, BUFFER_SIZE));
    }

    /**
     * Read from buffered reader.
     * @param reader the BufferedReader to read csv from.
     */
    public CSVParser(final BufferedReader reader) {
        this.input = reader;
        this.currentLine = 0;
    }

    @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.CyclomaticComplexity"})
    private String readField() throws IOException {
        final StringBuilder result = new StringBuilder();
        ParserStates state = ParserStates.STATE_0;
        loop: for (;;) {
            input.mark(1);
            @SuppressWarnings("PMD.ShortVariable")
            final int c = input.read();

            if (c == EOF) {
                input.reset();
                break loop;
            }

            switch (state) {
                case STATE_0:
                    if (c == FIELD_SEP) {
                            break loop;
                    }
                    if (c == RECORD_SEP) {
                        input.reset();
                        break loop;
                    }
                    if (c == BOM) {
                        continue loop;
                    }
                    if (c == QUOTE) {
                        state = ParserStates.STATE_2;
                        continue loop;
                    }
                    state = ParserStates.STATE_1;
                    result.appendCodePoint(c);
                    break;
                case STATE_1:
                    if (c == FIELD_SEP) {
                        break loop;
                    }
                    if (c == RECORD_SEP) {
                        input.reset();
                        break loop;
                    }
                    if (c == BOM) {
                        continue loop;
                    }
                    result.appendCodePoint(c);
                    break;
                case STATE_2:
                    if (c == QUOTE) {
                        state = ParserStates.STATE_3;
                        continue loop;
                    }
                    result.appendCodePoint(c);
                    break;
                case STATE_3:
                    if (c == QUOTE) {
                        result.appendCodePoint(c);
                        state = ParserStates.STATE_2;
                        continue loop;
                    }
                    if (c == FIELD_SEP) {
                        break loop;
                    }
                    state = ParserStates.STATE_1;
                    input.reset();
                    break;
                default:
                    throw new IOException("Unknown character \"" + c + "\"");
            }
        }
        return result.toString();
    }

    /**
     * Read row from CSV file.
     * @return record of fields.
     */
    public CSVRecord readRecord() throws IOException {
        CSVRecord rec = null;
        final ArrayList<String> fields = new ArrayList<>();
        for (;;) {
            input.mark(1);
            @SuppressWarnings("PMD.ShortVariable")
            int c = input.read();
            input.reset();

            if (c == EOF) {
                break;
            }

            if (c == RECORD_SEP) {
                do {
                    input.mark(1);
                    c = input.read();
                } while (c == RECORD_SEP || c == RECORD_SEP2);
                input.reset();
                break;
            }

            final String fieldText = readField();
            fields.add(fieldText);
        }
        currentLine++;
        if (!fields.isEmpty()) {
            rec = new CSVRecord();
            rec.setHeader(header);
            rec.setFields(fields);
        }
        return rec;
    }

    /**
     * Read header row from CSV file.
     * Must precede any readRecord calls.
     */
    public void readHeader()
    throws IOException {
        final CSVRecord rec = readRecord();
        header = new HashMap<>();
        for (int i = 0; i < rec.size(); i++) {
            header.put(rec.get(i), Integer.valueOf(i));
        }
    }

    /**
     * Makes Iterator for rows.
     * @return iterator.
     */
    @Override
    public CSVIterator iterator() {
        return new CSVIterator(this);
    }

    /**
     * Returns current row number.
     * @return current row number.
     */
    public long getCurrentLine() {
        return currentLine;
    }

    /**
     * Skips some rows.
     * @param skipCount number of skipped rows.
     */
    public void skip(final long skipCount) throws IOException {
        long count = skipCount;
        while (count > 0) {
            readRecord();
            count--;
        }
    }

    /**
     * Iterator for rows.
     */
    public static final class CSVIterator implements Iterator<CSVRecord> {
        /**
         * Parser we use to get rows.
         */
        private final CSVParser csv;
        /**
         * Precached next row.
         */
        private CSVRecord nextRec;

        /**
         * Create csv row iterator from parser.
         * @param csv parser.
         */
        @SuppressWarnings("PMD.NullAssignment")
        public CSVIterator(final CSVParser csv) {
            this.csv = csv;
            try {
                nextRec = csv.readRecord();
            } catch (IOException e) {
                nextRec = null;
            }
        }

        /**
         * Checks if there is next row to get.
         * @return true if we can get next row.
         */
        @Override
        public boolean hasNext() {
            return nextRec != null;
        }

        /**
         * Read next row.
         * @return next row of CSV file,
         *  or null if there is no more rows.
         */
        @Override
        @SuppressWarnings("PMD.NullAssignment")
        public CSVRecord next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final CSVRecord res = nextRec;
            try {
                nextRec = csv.readRecord();
            } catch (IOException e) {
                nextRec = null;
            }
            return res;
        }
    }

    /**
     * State machine states.
     */
    private enum ParserStates {
        /**
         * State machine state 0.
         */
        STATE_0,
        /**
         * State machine state 1.
         */
        STATE_1,
        /**
         * State machine state 2.
         */
        STATE_2,
        /**
         * State machine state 3.
         */
        STATE_3
    }
}

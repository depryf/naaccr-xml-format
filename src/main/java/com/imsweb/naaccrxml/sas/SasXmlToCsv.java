/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.naaccrxml.sas;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class SasXmlToCsv {

    private File _xmlFile, _csvFile;

    private String _naaccrVersion, _recordType;

    public SasXmlToCsv(String xmlPath, String naaccrVersion, String recordType) {
        this(xmlPath, xmlPath.replace(".xml", ".csv"), naaccrVersion, recordType);
    }

    public SasXmlToCsv(String xmlPath, String csvPath, String naaccrVersion, String recordType) {
        _xmlFile = new File(xmlPath);
        if (!_xmlFile.exists())
            System.err.println("!!! Invalid XML file: " + xmlPath);
        System.out.println(" > input XML: " + _xmlFile.getAbsolutePath());

        if (csvPath.endsWith(".gz"))
            csvPath = csvPath.replace(".gz", "");
        _csvFile = new File(csvPath);
        System.out.println(" > temp CSV: " + _csvFile.getAbsolutePath());

        _naaccrVersion = naaccrVersion;
        _recordType = recordType;
    }

    public String getXmlPath() {
        return _xmlFile.getPath();
    }

    public String getCsvPath() {
        return _csvFile.getPath();
    }

    public String getNaaccrVersion() {
        return _naaccrVersion;
    }

    public String getRecordType() {
        return _recordType;
    }

    public void convert() throws IOException {
        convert(null);
    }

    public void convert(String fields) throws IOException {

        Set<String> requestedFields = null;
        if (fields != null) {
            requestedFields = new HashSet<>();
            for (String s : fields.split(","))
                requestedFields.add(s.trim());
        }

        SasXmlReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new SasXmlReader(_xmlFile.getPath(), _naaccrVersion, _recordType);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_csvFile), StandardCharsets.UTF_8));
            StringBuilder buf = new StringBuilder();
            for (String field : reader.getFields())
                if (requestedFields == null || requestedFields.contains(field))
                    buf.append(field).append(",");
            buf.setLength(buf.length() - 1);
            writer.write(buf.toString());
            writer.write("\n");
            buf.setLength(0);

            while (reader.nextRecord() > 0) {
                for (String field : reader.getFields()) {
                    if (requestedFields == null || requestedFields.contains(field)) {
                        String val = reader.getValue(field);
                        if (val != null && val.contains(","))
                            val = "\"" + val + "\"";
                        buf.append(val == null ? "" : val).append(",");
                    }
                }
                buf.setLength(buf.length() - 1);
                writer.write(buf.toString());
                writer.write("\n");
                buf.setLength(0);
            }
        }
        finally {
            if (writer != null)
                writer.close();
            if (reader != null)
                reader.close();
        }
    }

    public void cleanup() {
        if (!_csvFile.delete())
            System.err.println("!!! Unable to cleanup tmp CSV file.");
    }

    /**
     public static void main(String[] args) throws IOException {
     String xmlPath = "YOUR_PATH_HERE";

     long start = System.currentTimeMillis();
     SasXmlToCsv reader = new SasXmlToCsv(xmlPath, "180", "I");
     reader.convert();
     //reader.cleanup();
     System.out.println((System.currentTimeMillis() - start) + "ms");
     }
     */
}
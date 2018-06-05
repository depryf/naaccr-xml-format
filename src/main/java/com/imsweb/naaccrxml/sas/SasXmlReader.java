/*
 * Copyright (C) 2018 Information Management Services, Inc.
 */
package com.imsweb.naaccrxml.sas;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SasXmlReader {

    private File _xmlFile;

    private BufferedReader _reader;

    private List<String> _fields;

    private boolean _inPatient = false, _inTumor = false;

    private Map<String, String> _naaccrDataValues = new HashMap<>(), _patientValues = new HashMap<>(), _tumorValues = new HashMap<>();

    public SasXmlReader(String xmlPath, String version, String recordType) {
        _xmlFile = new File(xmlPath);
        if (!_xmlFile.exists())
            System.err.println("!!! Invalid XML file: " + xmlPath);

        System.out.println("Created NAACCR XML reader for following file: ");
        System.out.println(" > input XML: " + _xmlFile.getAbsolutePath());

        _fields = new ArrayList<>();
        for (Map.Entry<String, String> entry : SasUtils.getFields(version, recordType).entrySet())
            _fields.add(entry.getKey());
    }

    public List<String> getFields() {
        return _fields;
    }

    public int nextRecord() throws IOException {
        if (_reader == null)
            _reader = SasUtils.createReader(_xmlFile);

        _tumorValues.clear();

        String line = _reader.readLine();
        while (line != null) {
            int itemIdx = line.indexOf("<Item");
            if (itemIdx > -1) {
                int idIdx1 = line.indexOf('\"', itemIdx + 1);
                int idIdx2 = line.indexOf('\"', idIdx1 + 1);
                int valIdx1 = line.indexOf('>', idIdx2 + 1);
                int valIdx2 = line.indexOf('<', valIdx1 + 1);
                _naaccrDataValues.put(line.substring(idIdx1 + 1, idIdx2), line.substring(valIdx1 + 1, valIdx2));
            }
            else if (line.contains("<Patient>")) {
                _inPatient = true;
                _inTumor = false;
            }
            else if (line.contains("<Tumor>")) {
                _inPatient = false;
                _inTumor = true;
            }
            else {
                int endIdx = line.indexOf("</");
                if (endIdx > -1) {
                    if (line.indexOf("Patient>", endIdx) > -1) {
                        _inPatient = false;
                        _patientValues.clear();
                    }
                    else if (line.indexOf("Tumor>", endIdx) > -1) {
                        _tumorValues.putAll(_naaccrDataValues);
                        _tumorValues.putAll(_patientValues);
                        break;
                    }
                    else if (line.indexOf("NaaccrData>", endIdx) > -1)
                        return 0;
                }
            }

            line = _reader.readLine();
        }

        return 1;
    }

    public String getValue(String naaccrId) {
        return Objects.toString(_tumorValues.get(naaccrId), "");
    }

    public void close() {
        try {
            _reader.close();
        }
        catch (IOException e) {
            // ignored
        }
    }

    /**
     public static void main(String[] args) throws IOException {
     String xmlPath = "YOUR_PATH_HERE";

     long start = System.currentTimeMillis();
     SasXmlReader reader = new SasXmlReader(xmlPath, "180", "I");
     int count = 0;
     while (reader.nextRecord() == 1) {
     reader.getValue("primarySite");
     if (count < 5)
     System.out.println(reader.getValue("primarySite"));
     count++;
     }
     reader.close();
     System.out.println((System.currentTimeMillis() - start) + "ms (" + count + ")");
     }
     */
}
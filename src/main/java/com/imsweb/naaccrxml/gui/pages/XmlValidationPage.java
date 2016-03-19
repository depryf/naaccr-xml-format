/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.naaccrxml.gui.pages;

import java.io.File;

import com.imsweb.naaccrxml.NaaccrFormat;
import com.imsweb.naaccrxml.NaaccrIOException;
import com.imsweb.naaccrxml.NaaccrObserver;
import com.imsweb.naaccrxml.NaaccrOptions;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.NaaccrXmlUtils;
import com.imsweb.naaccrxml.PatientXmlReader;
import com.imsweb.naaccrxml.entity.NaaccrData;
import com.imsweb.naaccrxml.entity.Patient;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.gui.StandaloneOptions;

public class XmlValidationPage extends AbstractProcessingPage {

    @Override
    protected String getSourceLabelText() {
        return "Source XML File:";
    }

    @Override
    protected String getTargetLabelText() {
        return "N/A";
    }

    @Override
    protected boolean showTargetInput() {
        return false;
    }

    @Override
    protected StandaloneOptions createOptions() {
        return new StandaloneOptions(false, true, true, false);
    }

    @Override
    protected void runProcessing(File source, File target, NaaccrOptions options, NaaccrDictionary dictionary, NaaccrObserver observer) throws NaaccrIOException {
        try (PatientXmlReader reader = new PatientXmlReader(NaaccrXmlUtils.createReader(source), options, dictionary)) {
            Patient patient = reader.readPatient();
            while (patient != null && !Thread.currentThread().isInterrupted()) {
                // this is the call that will actually collect the errors and show them in the GUI...
                observer.patientRead(patient);
                patient = reader.readPatient();
            }
        }
    }

    @Override
    protected NaaccrFormat getFormatForInputFile(File file) {

        // make sure the file exists
        if (file == null || !file.exists()) {
            reportAnalysisError("unable to find selected file");
            return null;
        }

        try (PatientXmlReader reader = new PatientXmlReader(NaaccrXmlUtils.createReader(file), null, null)) {
            NaaccrData rootData = reader.getRootData(); // all the validation happens when creating the root object in the reader...
            return NaaccrFormat.getInstance(NaaccrXmlDictionaryUtils.extractVersionFromUri(rootData.getBaseDictionaryUri()), rootData.getRecordType());
        }
        catch (NaaccrIOException e) {
            String lineNum = e.getLineNumber() != null ? e.getLineNumber().toString() : "N/A";
            reportAnalysisError("line " + lineNum + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    protected String getProcessingResultText(String path, long analysisTime, long processingTime, String size) {
        return "Done validating source XML file.";
    }
}

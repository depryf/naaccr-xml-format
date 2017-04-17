/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.naaccrxml;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;

import com.imsweb.naaccrxml.entity.Item;
import com.imsweb.naaccrxml.entity.NaaccrData;
import com.imsweb.naaccrxml.entity.Patient;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.runtime.NaaccrStreamConfiguration;
import com.imsweb.naaccrxml.runtime.NaaccrStreamContext;
import com.imsweb.naaccrxml.runtime.RuntimeNaaccrDictionary;

/**
 * This class can be used to wrap a generic writer into a patient writer handling the NAACCR XML format.
 */
public class PatientXmlWriter implements AutoCloseable {

    // XStream object responsible for reading patient objects
    protected XStream _xstream;

    // the underlined writer
    protected HierarchicalStreamWriter _writer;

    // sometimes we want to finalize the writing operation without closing the writer itself...
    protected boolean _hasBeenFinalized = false;

    /**
     * Constructor.
     * @param writer required underlined writer
     * @param rootData required root data (corresponds to the content of NaaccrData)
     * @throws NaaccrIOException if anything goes wrong
     */
    public PatientXmlWriter(Writer writer, NaaccrData rootData) throws NaaccrIOException {
        this(writer, rootData, null, (NaaccrDictionary)null, null);
    }

    /**
     * Constructor.
     * @param writer required underlined writer
     * @param rootData required root data (corresponds to the content of NaaccrData)
     * @param options optional options
     * @throws NaaccrIOException if anything goes wrong
     */
    public PatientXmlWriter(Writer writer, NaaccrData rootData, NaaccrOptions options) throws NaaccrIOException {
        this(writer, rootData, options, (NaaccrDictionary)null, null);
    }

    /**
     * Constructor.
     * @param writer required underlined writer
     * @param rootData required root data (corresponds to the content of NaaccrData)
     * @param options optional options
     * @param userDictionary optional user-defined dictionary
     * @throws NaaccrIOException if anything goes wrong
     */
    public PatientXmlWriter(Writer writer, NaaccrData rootData, NaaccrOptions options, NaaccrDictionary userDictionary) throws NaaccrIOException {
        this(writer, rootData, options, Collections.singletonList(userDictionary), null);
    }

    /**
     * Constructor.
     * @param writer required underlined writer
     * @param rootData required root data (corresponds to the content of NaaccrData)
     * @param options optional options
     * @param userDictionaries optional user-defined dictionaries (can be null or empty)
     * @throws NaaccrIOException if anything goes wrong
     */
    public PatientXmlWriter(Writer writer, NaaccrData rootData, NaaccrOptions options, List<NaaccrDictionary> userDictionaries) throws NaaccrIOException {
        this(writer, rootData, options, userDictionaries, null);
    }

    /**
     * Constructor.
     * @param writer required underlined writer
     * @param rootData required root data (corresponds to the content of NaaccrData)
     * @param options optional options
     * @param userDictionary optional user-defined dictionary
     * @param configuration optional stream configuration
     * @throws NaaccrIOException if anything goes wrong
     */
    public PatientXmlWriter(Writer writer, NaaccrData rootData, NaaccrOptions options, NaaccrDictionary userDictionary, NaaccrStreamConfiguration configuration) throws NaaccrIOException {
        this(writer, rootData, options, Collections.singletonList(userDictionary), configuration);
    }

    /**
     * Constructor.
     * @param writer required underlined writer
     * @param rootData required root data (corresponds to the content of NaaccrData)
     * @param options optional options
     * @param userDictionaries optional user-defined dictionaries (can be null or empty)
     * @param configuration optional stream configuration
     * @throws NaaccrIOException if anything goes wrong
     */
    public PatientXmlWriter(Writer writer, NaaccrData rootData, NaaccrOptions options, List<NaaccrDictionary> userDictionaries, NaaccrStreamConfiguration configuration) throws NaaccrIOException {

        try {
            // we always need options
            if (options == null)
                options = new NaaccrOptions();

            // we always need a configuration
            if (configuration == null)
                configuration = new NaaccrStreamConfiguration();

            // clean-up the dictionaries
            Map<String, NaaccrDictionary> dictionaries = new HashMap<>();
            if (userDictionaries != null)
                for (NaaccrDictionary userDictionary : userDictionaries)
                    if (userDictionary != null)
                        dictionaries.put(userDictionary.getDictionaryUri(), userDictionary);

            // create the context
            NaaccrStreamContext context = new NaaccrStreamContext();
            context.setOptions(options);
            context.setConfiguration(configuration);

            NaaccrDictionary baseDictionary = NaaccrXmlDictionaryUtils.getBaseDictionaryByUri(rootData.getBaseDictionaryUri());

            // create the writer
            _writer = new PrettyPrintWriter(writer, new char[] {' ', ' ', ' ', ' '});

            // would be better to use a "header writer", I think XStream has one actually; that would be better...
            try {
                writer.write("<?xml version=\"1.0\"?>\n\n");
            }
            catch (IOException e) {
                throw new NaaccrIOException(e.getMessage());
            }

            // write standard attributes
            _writer.startNode(NaaccrXmlUtils.NAACCR_XML_TAG_ROOT);
            if (rootData.getBaseDictionaryUri() == null)
                throw new NaaccrIOException("base dictionary URI is required");
            _writer.addAttribute(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_BASE_DICT, rootData.getBaseDictionaryUri());
            if (!dictionaries.isEmpty()) {
                if (rootData.getUserDictionaryUri() != null && !rootData.getUserDictionaryUri().isEmpty() && !new HashSet<>(rootData.getUserDictionaryUri()).equals(dictionaries.keySet()))
                    throw new NaaccrIOException("Provided dictionaries are not the ones referenced in the rootData");
                _writer.addAttribute(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_USER_DICT, StringUtils.join(new TreeSet<>(dictionaries.keySet()), ' '));
            }
            if (rootData.getRecordType() == null)
                throw new NaaccrIOException("record type is required");
            _writer.addAttribute(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_REC_TYPE, rootData.getRecordType());
            if (rootData.getTimeGenerated() != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(rootData.getTimeGenerated());
                _writer.addAttribute(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_TIME_GENERATED, DatatypeConverter.printDateTime(cal));
            }
            else
                _writer.addAttribute(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_TIME_GENERATED, DatatypeConverter.printDateTime(Calendar.getInstance()));
            // always use the current specs; doesn't matter the value on the root object...
            _writer.addAttribute(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_SPEC_VERSION, NaaccrXmlUtils.CURRENT_SPECIFICATION_VERSION);

            // write non-standard attributes
            Set<String> standardAttributes = new HashSet<>();
            standardAttributes.add(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_BASE_DICT);
            standardAttributes.add(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_USER_DICT);
            standardAttributes.add(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_REC_TYPE);
            standardAttributes.add(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_TIME_GENERATED);
            standardAttributes.add(NaaccrXmlUtils.NAACCR_XML_ROOT_ATT_SPEC_VERSION);
            for (Entry<String, String> entry : rootData.getExtraRootParameters().entrySet())
                if (!standardAttributes.contains(entry.getKey()) && !entry.getKey().startsWith("xmlns"))
                    _writer.addAttribute(entry.getKey(), entry.getValue());

            // add the default namespace, always use the library value...
            _writer.addAttribute("xmlns", NaaccrXmlUtils.NAACCR_XML_NAMESPACE);

            // add any user-defined namespaces
            configuration.getRegisterNamespaces().forEach((key, value) -> _writer.addAttribute("xmlns:" + key, value));

            // now we are ready to create our reading context and make it available to the patient converter
            context.setDictionary(new RuntimeNaaccrDictionary(rootData.getRecordType(), baseDictionary, dictionaries.values()));
            configuration.getPatientConverter().setContext(context);

            // write the root items
            for (Item item : rootData.getItems())
                configuration.getPatientConverter().writeItem(item, _writer);

            // need to expose xstream so the other methods can use it...
            _xstream = configuration.getXstream();

            // handle extension
            if (!Boolean.TRUE.equals(options.getIgnoreExtensions()) && rootData.getExtension() != null)
                _xstream.marshal(rootData.getExtension(), _writer);
        }
        catch (ConversionException ex) {
            throw convertSyntaxException(ex);
        }
        catch (RuntimeException ex) {
            throw new NaaccrIOException("unable to write XML", ex);
        }
    }

    /**
     * Writes the given patient on this stream.
     * @throws NaaccrIOException if anything goes wrong
     */
    public void writePatient(Patient patient) throws NaaccrIOException {
        try {
            _xstream.marshal(patient, _writer);
        }
        catch (ConversionException ex) {
            throw convertSyntaxException(ex);
        }
        catch (RuntimeException ex) {
            throw new NaaccrIOException("unable to write XML", ex);
        }
    }

    /**
     * Write the final node of the document, without closing the stream.
     */
    public void closeAndKeepAlive() {
        if (!_hasBeenFinalized) {
            _writer.endNode();
            _hasBeenFinalized = true;
        }
    }

    @Override
    public void close() {
        closeAndKeepAlive();
        _writer.close();
    }

    /**
     * We don't want to expose the conversion exceptions, so let's translate them into our own exceptions...
     */
    protected NaaccrIOException convertSyntaxException(ConversionException ex) {
        String msg = ex.get("message");
        if (msg == null)
            msg = ex.getMessage();
        NaaccrIOException e = new NaaccrIOException(msg, ex);
        if (ex.get("line number") != null)
            e.setLineNumber(Integer.valueOf(ex.get("line number")));
        e.setPath(ex.get("path"));
        return e;
    }
}

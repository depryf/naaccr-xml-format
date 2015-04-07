/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package org.naaccr.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the options that a reader/writer can use to customize its operations.
 */
public class NaaccrOptions {

    /**
     * The different ways of handling an unknown item.
     */
    public static final String ITEM_HANDLING_ERROR = "error";
    public static final String ITEM_HANDLING_IGNORE = "ignore";
    public static final String ITEM_HANDLING_PROCESS = "process";

    /**
     * If set to false, no validation of the values will take place. Defaults to true.
     */
    private Boolean _validateValues; // TODO FPD

    /**
     * How to handle unknown items (items with an ID that is not defined in the dictionary). See the handling constants.
     */
    private String _unknownItemHandling; // TODO FPD

    /**
     * When reading/writing a file, the item IDs to ignore. Defaults to not ignoring any items.
     */
    private List<String> _itemsToExclude;

    /**
     * When reading from flat file, which item IDs to use to group the tumors into patient. If empty, not grouping takes place. Defaults to using the Patient ID Number.
     */
    private List<String> _tumorGroupingItems;

    /**
     * When reading from flat file, whether or not errors need to be reported for patient-level mismatch. Default to false.
     */
    private Boolean _reportLevelMismatch;

    /**
     * If set to true, both the NAACCR ID and NAACCR Number will be written to the created XML files. Defaults to false.
     */
    private Boolean _writeItemNumber;

    /**
     * Default constructor.
     */
    public NaaccrOptions() {
        _validateValues = true;
        _unknownItemHandling = ITEM_HANDLING_ERROR;
        _itemsToExclude = new ArrayList<>();
        _tumorGroupingItems = new ArrayList<>();
        _tumorGroupingItems.add(NaaccrXmlUtils.DEFAULT_TUMOR_GROUPING_ITEM);
        _reportLevelMismatch = false;
        _writeItemNumber = false;
    }

    public String getUnknownItemHandling() {
        return _unknownItemHandling;
    }

    public void setUnknownItemHandling(String unknownItemHandling) {
        _unknownItemHandling = unknownItemHandling;
    }

    public Boolean getValidateValues() {
        return _validateValues;
    }

    public void setValidateValues(Boolean validateValues) {
        _validateValues = validateValues;
    }

    public List<String> getItemsToExclude() {
        return _itemsToExclude;
    }

    public void setItemsToExclude(List<String> itemsToExclude) {
        _itemsToExclude = itemsToExclude;
    }

    public List<String> getTumorGroupingItems() {
        return _tumorGroupingItems;
    }

    public void setTumorGroupingItems(List<String> tumorGroupingItems) {
        _tumorGroupingItems = tumorGroupingItems;
    }

    public Boolean getReportLevelMismatch() {
        return _reportLevelMismatch;
    }

    public void setReportLevelMismatch(Boolean reportLevelMismatch) {
        _reportLevelMismatch = reportLevelMismatch;
    }

    public Boolean getWriteItemNumber() {
        return _writeItemNumber;
    }

    public void setWriteItemNumber(Boolean writeItemNumber) {
        _writeItemNumber = writeItemNumber;
    }
}

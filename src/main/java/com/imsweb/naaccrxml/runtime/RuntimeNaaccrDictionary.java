/*
 * Copyright (C) 2015 Information Management Services, Inc.
 */
package com.imsweb.naaccrxml.runtime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.imsweb.naaccrxml.NaaccrFormat;
import com.imsweb.naaccrxml.NaaccrXmlDictionaryUtils;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionary;
import com.imsweb.naaccrxml.entity.dictionary.NaaccrDictionaryItem;

public class RuntimeNaaccrDictionary {

    private String _baseDictionaryUri;

    private String _userDictionaryUri;

    private NaaccrFormat _format;

    private List<RuntimeNaaccrDictionaryItem> _items;

    // caches used to improve lookup performances
    private Map<String, RuntimeNaaccrDictionaryItem> _cachedById;
    private Map<Integer, RuntimeNaaccrDictionaryItem> _cachedByNumber;

    public RuntimeNaaccrDictionary(String recordType, NaaccrDictionary baseDictionary, NaaccrDictionary userDictionary) {

        // use the default user dictionary if one is not provided...
        if (userDictionary == null)
            userDictionary = NaaccrXmlDictionaryUtils.getDefaultUserDictionaryByVersion(baseDictionary.getNaaccrVersion());

        _baseDictionaryUri = baseDictionary.getDictionaryUri();
        _userDictionaryUri = userDictionary.getDictionaryUri();
        _format = NaaccrFormat.getInstance(baseDictionary.getNaaccrVersion(), recordType);
        _items = new ArrayList<>();
        for (NaaccrDictionaryItem item : baseDictionary.getItems())
            if (item.getRecordTypes() == null || Arrays.asList(item.getRecordTypes().split(",")).contains(recordType))
                _items.add(new RuntimeNaaccrDictionaryItem(item));
        for (NaaccrDictionaryItem item : userDictionary.getItems())
            if (item.getRecordTypes() == null || Arrays.asList(item.getRecordTypes().split(",")).contains(recordType))
                _items.add(new RuntimeNaaccrDictionaryItem(item));

        // sort the fields by starting columns
        Collections.sort(_items, new Comparator<RuntimeNaaccrDictionaryItem>() {
            @Override
            public int compare(RuntimeNaaccrDictionaryItem o1, RuntimeNaaccrDictionaryItem o2) {
                return o1.getStartColumn().compareTo(o2.getStartColumn());
            }
        });
    }

    public String getBaseDictionaryUri() {
        return _baseDictionaryUri;
    }

    public String getUserDictionaryUri() {
        return _userDictionaryUri;
    }

    public String getNaaccrVersion() {
        return _format.getNaaccrVersion();
    }

    public String getRecordType() {
        return _format.getRecordType();
    }

    public Integer getLineLength() {
        return _format.getLineLength();
    }

    public List<RuntimeNaaccrDictionaryItem> getItems() {
        if (_items == null)
            _items = new ArrayList<>();
        return _items;
    }

    public RuntimeNaaccrDictionaryItem getItemByNaaccrId(String id) {
        if (_cachedById == null) {
            Map<String, RuntimeNaaccrDictionaryItem> cache = new HashMap<>();
            for (RuntimeNaaccrDictionaryItem item : _items)
                if (item.getNaaccrId() != null)
                    cache.put(item.getNaaccrId(), item);
            _cachedById = cache;
        }
        return _cachedById.get(id);
    }

    public RuntimeNaaccrDictionaryItem getItemByNaaccrNum(Integer number) {
        if (_cachedByNumber == null) {
            Map<Integer, RuntimeNaaccrDictionaryItem> cache = new HashMap<>();
            for (RuntimeNaaccrDictionaryItem item : _items)
                if (item.getNaaccrNum() != null)
                    cache.put(item.getNaaccrNum(), item);
            _cachedByNumber = cache;
        }
        return _cachedByNumber.get(number);
    }
}
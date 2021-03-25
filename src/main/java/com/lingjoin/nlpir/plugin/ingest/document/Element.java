package com.lingjoin.nlpir.plugin.ingest.document;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Element{

    protected void parseList(Map<String, Object> map, String fieldName, List<? extends Element> listField) {
        if (listField == null) return;
        map.put(fieldName, listField.stream().map(Element::toMap).collect(Collectors.toList()));
    }

    public void parseObject(Map<String, Object> map, String fieldName, Element field) {
        map.put(fieldName, field.toMap());
    }
    public abstract Map<String, Object> toMap();
}

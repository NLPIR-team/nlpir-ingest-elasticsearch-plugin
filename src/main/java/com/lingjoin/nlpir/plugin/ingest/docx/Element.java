package com.lingjoin.nlpir.plugin.ingest.docx;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class Element implements IElement {

    protected void parseList(Map<String, Object> map, String fieldName, List<? extends IElement> listField) {
        if (listField == null) return;
        map.put(fieldName, listField.stream().map(IElement::toMap).collect(Collectors.toList()));
    }

    public void parseObject(Map<String, Object> map, String fieldName, IElement field) {
        map.put(fieldName, field.toMap());
    }
}

package com.lingjoin.nlpir.plugin.ingest.document.pdf;

import com.lingjoin.nlpir.plugin.ingest.document.Element;

import java.util.HashMap;
import java.util.Map;

public class Page extends Element {
    private final String text;
    private final Integer pageNo;
    public Page(String text, Integer pageNo){
        this.pageNo = pageNo;
        this.text = text;
    }
    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("text", this.text);
        map.put("pageNo", this.pageNo);
        return map;

    }
}

package com.lingjoin.nlpir.plugin.ingest.document;

import java.util.Map;

interface IElement {
    Map<String, Object> toMap();
}
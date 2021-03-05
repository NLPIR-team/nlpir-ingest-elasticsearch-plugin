package com.lingjoin.nlpir.plugin.ingest;

import org.elasticsearch.ingest.Processor;
import org.elasticsearch.plugins.Plugin;

import java.util.Collections;
import java.util.Map;

public class NLPIRIngestPlugin extends Plugin implements org.elasticsearch.plugins.IngestPlugin {

    @Override
    public Map<String, Processor.Factory> getProcessors(Processor.Parameters parameters) {
        return Collections.singletonMap(DocExtractorProcessor.TYPE, new DocExtractorProcessor.Factory());
    }
}

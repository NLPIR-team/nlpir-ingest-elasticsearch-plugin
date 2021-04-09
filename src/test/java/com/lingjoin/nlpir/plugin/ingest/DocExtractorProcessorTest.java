package com.lingjoin.nlpir.plugin.ingest;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;
import org.elasticsearch.ingest.RandomDocumentPicks;
import org.elasticsearch.test.ESTestCase;

import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocExtractorProcessorTest extends ESTestCase {
    private Map<String, Object> parseDocument(String file, Processor processor, Map<String, Object> hookInfo)
            throws Exception {
        Map<String, Object> document = new HashMap<>();
        document.put("source_field", getAsBinaryOrBase64(file));


        IngestDocument ingestDocument = RandomDocumentPicks.randomIngestDocument(random(), document);
        if (hookInfo != null) {
            ingestDocument.setFieldValue("hookInfo", hookInfo);
        }
        processor.execute(ingestDocument);


        @SuppressWarnings("unchecked")
        Map<String, Object> attachmentData = (Map<String, Object>) ingestDocument.getSourceAndMetadata().get("doc_extract");
        System.out.println(attachmentData);
        return attachmentData;
    }

    private Map<String, Object> parseDocument(String file, Processor processor) throws Exception {
        return parseDocument(file, processor, null);
    }

    private Object getAsBinaryOrBase64(String filename) throws Exception {
        String path = "/com/lingjoin/nlpir/plugin/ingest/" + filename;
        try (InputStream is = DocExtractorProcessorTest.class.getResourceAsStream(path)) {
            byte[] bytes = IOUtils.toByteArray(is);
            // behave like CBOR from time to time
            if (rarely()) {
                return bytes;
            } else {
                return Base64.getEncoder().encodeToString(bytes);
            }
        }
    }

    public void testExtractPdf() throws Exception {
        Processor processor = new DocExtractorProcessor(randomAlphaOfLength(10), null, "source_field", DocExtractorProcessor.FieldType.PDF, "doc_extract");
        Map<String, Object> document;
        Gson gson = new Gson();
        document = this.parseDocument("test.pdf", processor);
        System.out.println(gson.toJson(new HashMap<>(document)));
        document = this.parseDocument("test2.pdf", processor);
        System.out.println(gson.toJson(document));
    }

    public void testExtractDoc() throws Exception {
        Processor processor = new DocExtractorProcessor(randomAlphaOfLength(10), null, "source_field", DocExtractorProcessor.FieldType.DOCX, "doc_extract");
        Map<String, Object> document;
        document = this.parseDocument("test.docx", processor);
        System.out.println(document);
        document = this.parseDocument("test2.docx", processor);
        System.out.println(document);
        document = this.parseDocument("test3.docx", processor);
        System.out.println(document);
    }


    public void testHook() throws Exception {
        Map<String, Object> hookInfo = Map.of(
                "scheme", "http",
                "host", "127.0.0.1",
                "port", 8000,
                "path", "",
                "headers", List.of(Map.of("name", "test_name", "value", "test_value")),
                "body", Map.of(),
                "retry", 5
        );
        Processor processor = new DocExtractorProcessor(
                randomAlphaOfLength(10),
                null,
                "source_field",
                DocExtractorProcessor.FieldType.DOCX,
                "doc_extract",
                true
        );
        Map<String, Object> document;
        document = this.parseDocument("test3.docx", processor, hookInfo);
        System.out.println(document);
    }
}
package com.lingjoin.nlpir.plugin.ingest;


import com.lingjoin.nlpir.plugin.ingest.docx.Document;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;


public final class DocExtractorProcessor extends AbstractProcessor {

    public static final String TYPE = "NLPIR_DocExtract";
    private final String field;
    private final FieldType fieldType;
    private final String targetField;

    DocExtractorProcessor(String tag, String description, String field, FieldType fieldType, String targetField) {
        super(tag, description);
        this.field = field;
        this.fieldType = fieldType;
        this.targetField = targetField;
    }
    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws IOException, InvalidFormatException{
        ZipSecureFile.setMinInflateRatio(-1.0d);
        Map<String, Object> fieldMap ;
        byte[] input = ingestDocument.getFieldValueAsBytes(this.field);
        InputStream is = new ByteArrayInputStream(input);
        switch (fieldType){
            case PDF:
                fieldMap = new com.lingjoin.nlpir.plugin.ingest.pdf.Document(is).toMap();
                break;
            case DOCX:
                fieldMap = new Document(is).toMap();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + fieldType);
        }
        ingestDocument.setFieldValue(targetField, fieldMap);
        return ingestDocument;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    String getField() {
        return field;
    }

    String getTargetField() {
        return targetField;
    }

    public static final class Factory implements Processor.Factory {

        @Override
        public DocExtractorProcessor create(Map<String, Processor.Factory> registry, String processorTag,
                                            String description, Map<String, Object> config) {
            String field = readStringProperty(TYPE, processorTag, config, "field");
            FieldType fieldType = FieldType.valueOf(readStringProperty(TYPE, processorTag, config, "fieldType"));
            String targetField = readStringProperty(TYPE, processorTag, config, "target_field", "doc_extract");

            return new DocExtractorProcessor(processorTag, description, field, fieldType, targetField);
        }
    }
    public enum FieldType{
        DOCX, PDF;
    }

}

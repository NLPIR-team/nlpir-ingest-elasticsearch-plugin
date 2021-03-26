package com.lingjoin.nlpir.plugin.ingest;

import com.lingjoin.nlpir.plugin.ingest.document.Element;
import com.lingjoin.nlpir.plugin.ingest.document.docx.Document;
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

    /*public static final Set<Class<?>> endClass = Set.of(
            int.class,
            long.class,
            float.class,
            double.class,
            boolean.class,
            char.class,
            String.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Boolean.class,
            Character.class,
            Number.class,
            Enum.class,
            CharSequence.class
    );*/
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
    public IngestDocument execute(IngestDocument ingestDocument) throws IOException, InvalidFormatException {
        ZipSecureFile.setMinInflateRatio(-1.0d);
        byte[] input = ingestDocument.getFieldValueAsBytes(this.field);
        InputStream is = new ByteArrayInputStream(input);
        Element fieldObj;
        switch (fieldType) {
            case PDF:
                fieldObj = new com.lingjoin.nlpir.plugin.ingest.document.pdf.Document(is);
                break;
            case DOCX:
                fieldObj = new Document(is);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + fieldType);
        }
        Map<String, Object> fieldMap = fieldObj.toMap();
        if (targetField.equals("root")) {
            for (Map.Entry<String, Object> entry : fieldMap.entrySet()) {
                ingestDocument.setFieldValue(entry.getKey(), entry.getValue());
            }
        } else {
            ingestDocument.setFieldValue(targetField, fieldMap);
        }
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

    /*public Map<String, Object> object2Map(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return null;
        }
        Map<String, Object> objMap = new HashMap<>();

//        System.out.println("--------------------");
//        System.out.println(obj);
//        System.out.println(obj.toString());

        for (Field field : obj.getClass().getDeclaredFields()) {
//            System.out.println("=========================");
//            System.out.println(field.getType());
//            System.out.println(field.toString());
//            System.out.println(field.getName());
            field.setAccessible(true);
            if (endClass.contains(field.getType())) {

                objMap.put(field.getName(), field.get(obj));
            } else {
                Object fieldObj = field.get(obj);
                if (fieldObj instanceof List) {
                    List<?> fieldObjListList = (List<?>) fieldObj;
                    List<Map<String, Object>> listMap = new ArrayList<>();
                    for (Object itemObject : fieldObjListList) {
                        listMap.add(this.object2Map(itemObject));
                    }
                    objMap.put(field.getName(), listMap);
                } else {
                    objMap.put(field.getName(), this.object2Map(field.get(obj)));
                }
            }
        }
        return objMap;
    }*/

    public static final class Factory implements Processor.Factory {

        @Override
        public DocExtractorProcessor create(Map<String, Processor.Factory> registry, String processorTag,
                                            String description, Map<String, Object> config) {
            String field = readStringProperty(TYPE, processorTag, config, "field");
            FieldType fieldType = FieldType.valueOf(readStringProperty(TYPE, processorTag, config, "fieldType"));
            String targetField = readStringProperty(TYPE, processorTag, config, "targetField", "doc_extract");

            return new DocExtractorProcessor(processorTag, description, field, fieldType, targetField);
        }
    }

    public enum FieldType {
        DOCX, PDF
    }

}

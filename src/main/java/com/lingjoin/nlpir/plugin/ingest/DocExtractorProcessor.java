package com.lingjoin.nlpir.plugin.ingest;

import com.google.gson.Gson;
import com.lingjoin.nlpir.plugin.ingest.document.Element;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.HttpContext;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.ingest.AbstractProcessor;
import org.elasticsearch.ingest.IngestDocument;
import org.elasticsearch.ingest.Processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import static org.elasticsearch.ingest.ConfigurationUtils.readBooleanProperty;
import static org.elasticsearch.ingest.ConfigurationUtils.readStringProperty;


public final class DocExtractorProcessor extends AbstractProcessor {

    public static final String TYPE = "NLPIR_DocExtract";
    private final String field;
    private final FieldType fieldType;
    private final String targetField;
    private final boolean useHook;

    DocExtractorProcessor(
            String tag,
            String description,
            String field,
            FieldType fieldType,
            String targetField,
            boolean useHook
    ) {
        super(tag, description);
        this.field = field;
        this.fieldType = fieldType;
        this.targetField = targetField;
        this.useHook = useHook;
    }

    DocExtractorProcessor(
            String tag,
            String description,
            String field,
            FieldType fieldType,
            String targetField
    ) {
        this(tag, description, field, fieldType, targetField, false);
    }

    @Override
    public IngestDocument execute(IngestDocument ingestDocument) throws IOException, InvalidFormatException, URISyntaxException {
        ZipSecureFile.setMinInflateRatio(-1.0d);
        byte[] input = ingestDocument.getFieldValueAsBytes(this.field);
        @SuppressWarnings({"unchecked"})
        Map<String, Object> hookInfo = ingestDocument.getFieldValue("hookInfo", Map.class, true);
        InputStream is = new ByteArrayInputStream(input);
        Element fieldObj;
        DocExtractStatus status;

        switch (fieldType) {
            case PDF:
                fieldObj = new com.lingjoin.nlpir.plugin.ingest.document.pdf.Document(is);
                status = new DocExtractStatus(
                        DocExtractStatus.ConvertFileFormat.PDF,
                        (String) ingestDocument.getMetadata().get(IngestDocument.Metadata.INDEX),
                        true,
                        System.currentTimeMillis() << 10
                );
                break;
            case DOCX:
                fieldObj = new com.lingjoin.nlpir.plugin.ingest.document.docx.Document(is);
                status = new DocExtractStatus(
                        DocExtractStatus.ConvertFileFormat.DOCX,
                        (String) ingestDocument.getMetadata().get(IngestDocument.Metadata.ID),
                        true,
                        System.currentTimeMillis() << 10
                );
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
        if (this.useHook && hookInfo != null) {
            this.callHook(ingestDocument, HookInfo.fromMap(hookInfo), status);
        }

        return ingestDocument;
    }

    private void callHook(IngestDocument ingestDocument, HookInfo hookInfo, DocExtractStatus status) throws IOException, URISyntaxException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
        String jsonBody = AccessController.doPrivileged((PrivilegedAction<String>) () -> {
                    Gson gson = new Gson();
                    return gson.toJson(Map.of(
                            "uuid", ingestDocument.getMetadata().get(IngestDocument.Metadata.ID),
                            "hook_type", "post_transform_task",
                            "hook_info", status
                    ));
                }
        );

        HttpClientBuilder clientBuilder = HttpClients.custom();
        if (hookInfo.retry > 2) {
            StandardHttpRequestRetryHandler retryHandler = new StandardHttpRequestRetryHandler(
                    hookInfo.retry, true);
            ServiceUnavailableRetryStrategy retryStrategy = new ServiceUnavailableRetryStrategy() {
                private final int maxRetries;
                private final long retryInterval;

                {
                    this.maxRetries = hookInfo.getRetry();
                    this.retryInterval = 500;
                }

                public boolean retryRequest(HttpResponse response, int executionCount, HttpContext context) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    return executionCount <= maxRetries &&
                            (response.getStatusLine().getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE || statusCode != 201);
                }

                @Override
                public long getRetryInterval() {
                    return this.retryInterval;
                }
            };
            clientBuilder.setRetryHandler(retryHandler).setServiceUnavailableRetryStrategy(retryStrategy);

        }
        CloseableHttpClient client = clientBuilder
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
        HttpPost httpPost = new HttpPost(new URIBuilder()
                .setScheme(hookInfo.getScheme())
                .setHost(hookInfo.getHost())
                .setPort(hookInfo.getPort())
                .setPath(hookInfo.getPath())
                .build());
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setEntity(new StringEntity(jsonBody));
        for (HookInfo.Header header : hookInfo.headers) {
            httpPost.setHeader(header.name, header.value);
        }
        SpecialPermission.check();
        CloseableHttpResponse response = AccessController.doPrivileged((PrivilegedAction<CloseableHttpResponse>) () -> {
            try {
                return client.execute(httpPost);
            } catch (IOException e) {
                e.printStackTrace();
                ingestDocument.setFieldValue("on_failure_message", e.getMessage());
            }
            return null;
        });
        if (response.getStatusLine().getStatusCode() != 201) {
            ingestDocument.setFieldValue("on_failure_message", new String(response.getEntity().getContent().readAllBytes()));
        }
        response.close();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public static final class Factory implements Processor.Factory {

        @Override
        public DocExtractorProcessor create(Map<String, Processor.Factory> registry, String processorTag,
                                            String description, Map<String, Object> config) {
            String field = readStringProperty(TYPE, processorTag, config, "field");
            FieldType fieldType = FieldType.valueOf(readStringProperty(TYPE, processorTag, config, "fieldType"));
            String targetField = readStringProperty(TYPE, processorTag, config, "targetField", "doc_extract");
            boolean useHook = readBooleanProperty(TYPE, processorTag, config, "useHook", false);
            return new DocExtractorProcessor(processorTag, description, field, fieldType, targetField, useHook);
        }
    }

    public enum FieldType {
        DOCX, PDF
    }

}

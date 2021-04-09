package com.lingjoin.nlpir.plugin.ingest;

public class DocExtractStatus {
    public enum ConvertFileFormat {
        PDF, DOCX
    }

    private String format;
    private String index;
    private Boolean success;
    private long timestamp;

    public DocExtractStatus(ConvertFileFormat format, String index, Boolean success, long timestamp) {
        this.format = format.name().toLowerCase();
        this.index = index;
        this.success = success;
        this.timestamp = timestamp;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}

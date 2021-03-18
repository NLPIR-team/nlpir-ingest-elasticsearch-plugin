package com.lingjoin.nlpir.plugin.ingest.pdf;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.elasticsearch.SpecialPermission;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Document {
    private final List<Page> pages;

    public Document(InputStream is) throws IOException {
        pages = new ArrayList<>();
        PDFTextStripper reader = new PDFTextStripper();
        PDDocument pdd = PDDocument.load(is);
        int pageCount = pdd.getNumberOfPages();
        for (int pageNo = 0; pageNo < pageCount; pageNo++) {
            reader.setStartPage(pageNo);
            reader.setEndPage(pageNo);
            String pageText = reader.getText(pdd);
            pages.add(new Page(pageText, pageNo));
        }
        pdd.close();
    }

    public List<Page> getPages() {
        return pages;
    }

    public Map<String, Object> toMap() {


        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
        return AccessController.doPrivileged(
                (PrivilegedAction<Map<String, Object>>) () -> {
                    Gson gson = new Gson();
                    String json = gson.toJson(this);
                    Type type = new TypeToken<Map<String, Object>>() {
                    }.getType();
                    return gson.fromJson(json, type);
                }
        );

    }
}

package com.lingjoin.nlpir.plugin.ingest.document.pdf;

import com.lingjoin.nlpir.plugin.ingest.document.Element;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Document extends Element {
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

    public Map<String, Object> toMap() {

        Map<String, Object> map = new HashMap<>();
        this.parseList(map, "pages", pages);
        return map;
    }
}

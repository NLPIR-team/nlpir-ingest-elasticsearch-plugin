package com.lingjoin.nlpir.plugin.ingest.document.docx;

import com.lingjoin.nlpir.plugin.ingest.document.Element;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.xwpf.usermodel.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Document extends Element {

    protected final String author;
    protected final String title;
    protected final String created;
    protected final String subject;
    protected final String keywords;
    protected final String description;
    protected final String category;
    protected int charCount;

    protected List<Footer> footers;
    protected List<Header> headers;

    protected List<Element> elements;

    public Document(InputStream is) throws InvalidFormatException, IOException {
        XWPFDocument doc = new XWPFDocument(is);
        //TODO implement it
        this.charCount = 0;

        PackageProperties properties = doc.getPackage().getPackageProperties();
        this.author = properties.getCreatorProperty().orElse(null);
        this.title = properties.getTitleProperty().orElse(null);
        this.created = properties.getCreatedProperty().map(Date::toString).orElse(null);
        this.subject = properties.getSubjectProperty().orElse(null);
        this.keywords = properties.getKeywordsProperty().orElse(null);
        this.description = properties.getDescriptionProperty().orElse(null);
        this.category = properties.getCategoryProperty().orElse(null);

        this.elements = new ArrayList<>();
        for (IBodyElement element : doc.getBodyElements()) {
            switch (element.getElementType()) {
                case TABLE:
                    this.elements.add(new Table((XWPFTable) element));
                    break;
                case PARAGRAPH:
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    this.elements.add(new Paragraph(paragraph));
                    this.elements.addAll(Picture.parsePictures(paragraph));
                    break;
                case CONTENTCONTROL:
                    XWPFSDT xwpfsdt = (XWPFSDT) element;
                    this.elements.add(new StructuredDocumentTag(xwpfsdt));
                    break;
                default:
                    break;
            }
        }
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            this.elements.add(new Paragraph(paragraph));
        }
        doc.close();

    }
    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("author", this.author);
        map.put("title", this.title);
        map.put("created", created);
        map.put("subject",subject);
        map.put("keywords", keywords);
        map.put("description", description);
        map.put("category", category);
        map.put("charCount", charCount);
        this.parseList(map, "footers", footers);
        this.parseList(map, "headers", headers);
        this.parseList(map, "elements", elements);
        return map;
    }

}

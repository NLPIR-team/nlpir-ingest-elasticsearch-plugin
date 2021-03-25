package com.lingjoin.nlpir.plugin.ingest.document.docx;

import com.lingjoin.nlpir.plugin.ingest.document.Element;
import org.apache.poi.xwpf.usermodel.*;
import org.elasticsearch.SpecialPermission;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructuredDocumentTag extends Element {
    private final List<Element> content;
    private final String elementType;

    public StructuredDocumentTag(XWPFSDT xwpfsdt) {
        elementType = "StructuredDocumentTag";
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SpecialPermission());
        }
        @SuppressWarnings("unchecked")
        List<IBodyElement> bodyElements = AccessController.doPrivileged(
                (PrivilegedAction<List<IBodyElement>>) () -> {
                    try {
                        Field bodyElementsField = XWPFSDTContent.class.getDeclaredField("bodyElements");
                        bodyElementsField.setAccessible(true);
                        return (List<IBodyElement>) bodyElementsField.get(xwpfsdt.getContent());
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
        );

        this.content = new ArrayList<>();
        for (IBodyElement element : bodyElements) {
            switch (element.getElementType()) {
                case TABLE:
                    this.content.add(new Table((XWPFTable) element));
                    break;
                case PARAGRAPH:
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    this.content.add(new Paragraph(paragraph));
                    this.content.addAll(Picture.parsePictures(paragraph));
                    break;
                case CONTENTCONTROL:
                default:
                    break;
            }
        }
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        this.parseList(map, "content", content);
        map.put("elementType", elementType);
        return map;
    }
}

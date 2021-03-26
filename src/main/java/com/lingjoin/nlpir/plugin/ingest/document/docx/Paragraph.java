package com.lingjoin.nlpir.plugin.ingest.document.docx;

import com.lingjoin.nlpir.plugin.ingest.document.Element;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHpsMeasure;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTParaRPr;

import java.math.BigInteger;
import java.util.*;

public class Paragraph extends Element {
    private final List<Run> runs;
    private final Double fontSize;
    private final String elementType;
    private final String text;

    public Paragraph(XWPFParagraph paragraph) {
        Double size = null;
        text = paragraph.getText();
        List<CTHpsMeasure> ctHpsMeasureList = Optional.of(paragraph)
                .map(XWPFParagraph::getCTP)
                .map(CTP::getPPr)
                .map(CTPPr::getRPr)
                .map(CTParaRPr::getSzList)
                .orElse(List.of());
        for (CTHpsMeasure ignored : ctHpsMeasureList) {
            size = ((BigInteger) ignored.getVal()).doubleValue() / 2.0;
            break;
        }
        this.fontSize = size;
        this.elementType = "Paragraph";
        this.runs = new ArrayList<>();
        for (XWPFRun run : paragraph.getRuns()) {
            // 字体字号可能为空,一般空时按照上一个Run的字体字号计算
            if (this.runs.isEmpty()) this.runs.add(new Run(run));
            else this.runs.add(new Run(run, this.runs.get(this.runs.size() - 1)));
        }
        Run.cleanRuns(this.runs);
    }

    public static void cleanParagraphs(List<Paragraph> paragraphs) {
        List<Paragraph> removedObject = new ArrayList<>();
        for (Paragraph paragraph : paragraphs) {
            if (paragraph.runs.size() == 0) removedObject.add(paragraph);
        }
        paragraphs.removeAll(removedObject);
    }


    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        this.parseList(map, "runs", runs);
        map.put("fontSize", fontSize);
        map.put("elementType", elementType);
        map.put("text", text);
        return map;
    }
}
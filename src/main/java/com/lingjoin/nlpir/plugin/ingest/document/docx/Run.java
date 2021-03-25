package com.lingjoin.nlpir.plugin.ingest.document.docx;

import com.lingjoin.nlpir.plugin.ingest.document.Element;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.*;

public class Run extends Element {
    private final Double fontSize;
    private final String font;
    // int lineSpace;
    private final String text;

    Run(XWPFRun run) {
        this(run, null);
    }


    Run(XWPFRun run, Run lastRun) {
        this.fontSize = Optional.ofNullable(lastRun)
                .map(Run::getFontSize)
                .orElse(run.getFontSizeAsDouble());
        this.font = Optional.ofNullable(lastRun)
                .map(Run::getFont)
                .orElse(run.getFontName());
        this.text = run.text();
    }

    public static void cleanRuns(List<Run> runs) {
        List<Run> removedObject = new ArrayList<>();
        for (Run run : runs) {
            if (run.text.length() == 0) removedObject.add(run);
        }
        runs.removeAll(removedObject);
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("fontSize", fontSize);
        map.put("font", font);
        map.put("text", text);
        map.put("elementType", "run");
        return map;
    }

    public Double getFontSize() {
        return fontSize;
    }

    public String getFont() {
        return font;
    }
}

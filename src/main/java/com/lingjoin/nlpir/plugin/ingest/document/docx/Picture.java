package com.lingjoin.nlpir.plugin.ingest.document.docx;

import com.lingjoin.nlpir.plugin.ingest.document.Element;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Picture extends Element {
    private final String elementType;
    private final String description;
    private final String name;

    private Picture(String name, String description){
        this.name = name;
        this.description = description;
        this.elementType = "Picture";
    }

    public static List<Picture> parsePictures(XWPFParagraph paragraph){
        List<Picture> pictures = new ArrayList<>();
        for (XWPFRun run : paragraph.getRuns()){
            for (XWPFPicture picture: run.getEmbeddedPictures()){
                String description = picture.getDescription();
                String name = picture.getCTPicture().getNvPicPr().getCNvPr().getName();
                pictures.add(new Picture(name, description));
            }
        }
        return pictures;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("description", description);
        map.put("name", name);
        map.put("elementType" ,elementType);
        return map;
    }
}

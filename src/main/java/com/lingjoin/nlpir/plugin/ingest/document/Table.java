package com.lingjoin.nlpir.plugin.ingest.document;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTcPrBase;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTVMerge;

import java.math.BigInteger;
import java.util.*;

public class Table extends Element {

    private final String elementType;
    private final int rowCount;
    private final int cellCount;
    private final List<Row> tableRows;


    protected class Row extends Element {
        private final List<Cell> tableCells;

        protected Row(XWPFTableRow row) {
            this.tableCells = new ArrayList<>();
            for (XWPFTableCell cell : row.getTableCells()) {
                this.tableCells.add(new Cell(cell));
            }
        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            this.parseList(map, "tableCells", tableCells);
            return map;
        }
    }

    protected enum STMerge {
        /* http://officeopenxml.com/WPtableCellProperties.php
        This element specifies that the cell is part of a vertically merged set of cells.
        defines the number of logical columns across which the cell spans. It has a single
        attribute w:val which specifies how the cell is part of a vertically merged region.
        The cell can be part of an existing group of merged cells or it can start a new
        group of merged cells. Possible values are:
            continue -- the current cell continues a previously existing merge group
            restart -- the current cell starts a new merge group

        If omitted, the value is assumed to be continue.
        See the discussion of <w:tblGrid> at Table Grid/Column Definition.
         */
        CONTINUE, RESTART;
    }

    protected class Cell extends Element {
        /* http://officeopenxml.com/WPtableCellProperties.php
        This element specifies that the cell is part of a vertically merged set of cells.
        defines the number of logical columns across which the cell spans. It has a single
        attribute w:val which specifies how the cell is part of a vertically merged region.
        The cell can be part of an existing group of merged cells or it can start a new
        group of merged cells. Possible values are:
            continue -- the current cell continues a previously existing merge group
            restart -- the current cell starts a new merge group

        If omitted, the value is assumed to be continue.
        See the discussion of <w:tblGrid> at Table Grid/Column Definition.
         */
        private final STMerge vMerge;
        /* http://officeopenxml.com/WPtableGrid.php
        The <w:gridSpan> indicates a cell horizontally spanning multiple logical cells
        (as defined by the <w:tblGrid>, much like the HTML colspan attribute placed on a
        cell. Cells can also span vertically using the <w:vMerge> element/property in a
        <w:tcPr> element.
        Note that the <w:gridSpan> value is 2, meaning the cell spans two logical columns.
        */
        private final Integer gridSpan;
        private final List<Paragraph> paragraphs;

        protected Cell(XWPFTableCell cell) {
            paragraphs = new ArrayList<>();
            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                paragraphs.add(new Paragraph(paragraph));
            }
            Paragraph.cleanParagraphs(this.paragraphs);
            this.gridSpan = Optional.of(cell)
                    .map(XWPFTableCell::getCTTc)
                    .map(CTTc::getTcPr)
                    .map(CTTcPrBase::getGridSpan)
                    .map(CTDecimalNumber::getVal)
                    .map(BigInteger::intValue)
                    .orElse(null);
            String merge = Optional.of(cell)
                    .map(XWPFTableCell::getCTTc)
                    .map(CTTc::getTcPr)
                    .map(CTTcPrBase::getVMerge)
                    .map(CTVMerge::xgetVal)
                    .map(XmlAnySimpleType::getStringValue)
                    .map(String::toUpperCase)
                    .orElse(null);
            if (merge == null) this.vMerge = null;
            else this.vMerge = STMerge.valueOf(merge);

        }

        @Override
        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            this.parseList(map, "paragraphs", paragraphs);
            map.put("vMerge", Optional.ofNullable(vMerge).map(Enum::name).orElse(null));
            map.put("gridSpan", gridSpan);
            return map;
        }
    }

    public Table(XWPFTable table) {
        this.elementType = "TABLE";
        this.rowCount = table.getNumberOfRows();
        this.tableRows = new ArrayList<>();
        int cellCount = 0;
        for (XWPFTableRow xwpfRow : table.getRows()) {
            Row row = new Row(xwpfRow);
            if (row.tableCells.size() > cellCount) cellCount = row.tableCells.size();
            this.tableRows.add(row);
        }
        this.cellCount = cellCount;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        this.parseList(map, "tableRows", tableRows);
        map.put("cellCount", cellCount);
        map.put("rowCount", rowCount);
        map.put("elementType", elementType);
        return map;
    }
}

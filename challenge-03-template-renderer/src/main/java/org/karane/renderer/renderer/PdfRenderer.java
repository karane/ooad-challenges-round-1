package org.karane.renderer.renderer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.karane.renderer.domain.Row;
import org.karane.renderer.domain.Template;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class PdfRenderer implements TemplateRenderer {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float ROW_HEIGHT = 20;
    private static final float TITLE_FONT_SIZE = 16;
    private static final float BODY_FONT_SIZE = 11;

    private final PDType1Font titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private final PDType1Font bodyFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private final PDType1Font headerFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    @Override
    public String render(Template template) {
        throw new UnsupportedOperationException("PdfRenderer produces binary output — use renderToFile(Template, Path) instead.");
    }

    public void renderToFile(Template template, Path outputPath) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = PAGE_HEIGHT - MARGIN;

                y = drawTitle(cs, template.title(), y);
                y -= 10;
                y = drawTableHeader(cs, template.headers(), y);
                y = drawSeparator(cs, y);
                for (Row row : template.rows()) {
                    y = drawRow(cs, row.cells(), bodyFont, y);
                }
                drawSeparator(cs, y);
            }

            doc.save(outputPath.toFile());
        }
    }

    private float drawTitle(PDPageContentStream cs, String title, float y) throws IOException {
        cs.beginText();
        cs.setFont(titleFont, TITLE_FONT_SIZE);
        cs.newLineAtOffset(MARGIN, y);
        cs.showText(title);
        cs.endText();
        return y - TITLE_FONT_SIZE - 4;
    }

    private float drawTableHeader(PDPageContentStream cs, List<String> headers, float y) throws IOException {
        return drawRow(cs, headers, headerFont, y);
    }

    private float drawRow(PDPageContentStream cs, List<String> cells, PDType1Font font, float y) throws IOException {
        float colWidth = (PAGE_WIDTH - MARGIN * 2) / cells.size();
        cs.setFont(font, BODY_FONT_SIZE);
        for (int i = 0; i < cells.size(); i++) {
            float x = MARGIN + colWidth * i;
            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(cells.get(i));
            cs.endText();
        }
        return y - ROW_HEIGHT;
    }

    private float drawSeparator(PDPageContentStream cs, float y) throws IOException {
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y + ROW_HEIGHT - 4);
        cs.lineTo(PAGE_WIDTH - MARGIN, y + ROW_HEIGHT - 4);
        cs.stroke();
        return y;
    }
}

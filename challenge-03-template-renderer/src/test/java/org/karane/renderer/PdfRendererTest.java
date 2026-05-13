package org.karane.renderer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.karane.renderer.builder.TemplateBuilder;
import org.karane.renderer.domain.Template;
import org.karane.renderer.renderer.PdfRenderer;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PdfRendererTest {

    private final PdfRenderer renderer = new PdfRenderer();

    private Template sampleTemplate() {
        return new TemplateBuilder()
                .title("Report")
                .headers("Col1", "Col2")
                .row("A", "B")
                .row("C", "D")
                .build();
    }

    @Test
    void renderProducesValidPdfFile(@TempDir Path dir) throws Exception {
        Path output = dir.resolve("report.pdf");
        renderer.renderToFile(sampleTemplate(), output);
        assertTrue(Files.exists(output));
        byte[] bytes = Files.readAllBytes(output);
        assertTrue(bytes.length > 0);
        // All PDF files start with %PDF
        assertEquals("%PDF", new String(bytes, 0, 4));
    }

    @Test
    void renderThrowsForStringRender() {
        assertThrows(UnsupportedOperationException.class, () -> renderer.render(sampleTemplate()));
    }
}

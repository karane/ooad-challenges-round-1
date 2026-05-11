package org.karane.renderer;

import org.junit.jupiter.api.Test;
import org.karane.renderer.builder.TemplateBuilder;
import org.karane.renderer.domain.Template;
import org.karane.renderer.renderer.CsvRenderer;

import static org.junit.jupiter.api.Assertions.*;

class CsvRendererTest {

    private final CsvRenderer renderer = new CsvRenderer();

    private Template sampleTemplate() {
        return new TemplateBuilder()
                .title("Inventory")
                .headers("Item", "Qty", "Price")
                .row("Apple", "10", "1.00")
                .row("Banana", "5", "0.50")
                .build();
    }

    @Test
    void headerRowIsCommaSeparated() {
        String output = renderer.render(sampleTemplate());
        assertTrue(output.contains("Item,Qty,Price"));
    }

    @Test
    void dataRowsAreCommaSeparated() {
        String output = renderer.render(sampleTemplate());
        assertTrue(output.contains("Apple,10,1.00"));
        assertTrue(output.contains("Banana,5,0.50"));
    }

    @Test
    void titleAppearsAsComment() {
        String output = renderer.render(sampleTemplate());
        assertTrue(output.contains("# Inventory"));
    }
}

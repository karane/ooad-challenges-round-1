package org.karane.renderer;

import org.junit.jupiter.api.Test;
import org.karane.renderer.builder.TemplateBuilder;
import org.karane.renderer.domain.Template;
import org.karane.renderer.renderer.HtmlRenderer;

import static org.junit.jupiter.api.Assertions.*;

class HtmlRendererTest {

    private final HtmlRenderer renderer = new HtmlRenderer();

    private Template sampleTemplate() {
        return new TemplateBuilder()
                .title("Sales")
                .headers("Product", "Price")
                .row("Widget", "$10")
                .build();
    }

    @Test
    void containsH1Title() {
        String output = renderer.render(sampleTemplate());
        assertTrue(output.contains("<h1>Sales</h1>"));
    }

    @Test
    void containsTableTag() {
        String output = renderer.render(sampleTemplate());
        assertTrue(output.contains("<table>"));
        assertTrue(output.contains("</table>"));
    }

    @Test
    void containsThForHeaders() {
        String output = renderer.render(sampleTemplate());
        assertTrue(output.contains("<th>Product</th>"));
        assertTrue(output.contains("<th>Price</th>"));
    }

    @Test
    void containsTdForDataCells() {
        String output = renderer.render(sampleTemplate());
        assertTrue(output.contains("<td>Widget</td>"));
        assertTrue(output.contains("<td>$10</td>"));
    }
}

package org.karane.renderer;

import org.junit.jupiter.api.Test;
import org.karane.renderer.builder.TemplateBuilder;
import org.karane.renderer.domain.Template;
import org.karane.renderer.renderer.CsvRenderer;
import org.karane.renderer.renderer.HtmlRenderer;
import org.karane.renderer.service.RenderService;

import static org.junit.jupiter.api.Assertions.*;

class RenderServiceTest {

    private Template sampleTemplate() {
        return new TemplateBuilder()
                .title("T")
                .headers("H1")
                .row("V1")
                .build();
    }

    @Test
    void serviceDelegatesToRenderer() {
        RenderService service = new RenderService(new HtmlRenderer());
        String output = service.render(sampleTemplate());
        assertTrue(output.contains("<table>"));
    }

    @Test
    void serviceCanSwitchRenderer() {
        RenderService csv = new RenderService(new CsvRenderer());
        String output = csv.render(sampleTemplate());
        assertTrue(output.contains("H1"));
        assertFalse(output.contains("<table>"));
    }
}

package org.karane.renderer;

import org.junit.jupiter.api.Test;
import org.karane.renderer.builder.TemplateBuilder;
import org.karane.renderer.domain.Template;

import static org.junit.jupiter.api.Assertions.*;

class TemplateBuilderTest {

    @Test
    void buildsTemplateWithTitleHeadersAndRows() {
        Template t = new TemplateBuilder()
                .title("Report")
                .headers("Name", "Age")
                .row("Alice", "30")
                .row("Bob", "25")
                .build();

        assertEquals("Report", t.title());
        assertEquals(2, t.headers().size());
        assertEquals(2, t.rows().size());
        assertEquals("Alice", t.rows().get(0).cells().get(0));
    }

    @Test
    void rowsAreImmutable() {
        Template t = new TemplateBuilder().title("T").headers("A").row("1").build();
        assertThrows(UnsupportedOperationException.class, () -> t.rows().add(null));
    }
}

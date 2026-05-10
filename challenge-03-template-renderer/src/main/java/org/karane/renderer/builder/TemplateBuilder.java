package org.karane.renderer.builder;

import org.karane.renderer.domain.Row;
import org.karane.renderer.domain.Template;

import java.util.ArrayList;
import java.util.List;

public class TemplateBuilder {
    private String title = "";
    private final List<String> headers = new ArrayList<>();
    private final List<Row> rows = new ArrayList<>();

    public TemplateBuilder title(String title) {
        this.title = title;
        return this;
    }

    public TemplateBuilder headers(String... headers) {
        this.headers.addAll(List.of(headers));
        return this;
    }

    public TemplateBuilder row(String... cells) {
        this.rows.add(new Row(List.of(cells)));
        return this;
    }

    public Template build() {
        return new Template(title, headers, rows);
    }
}

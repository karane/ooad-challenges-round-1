package org.karane.renderer.domain;

import java.util.List;

public record Template(String title, List<String> headers, List<Row> rows) {
    public Template(String title, List<String> headers, List<Row> rows) {
        this.title = title;
        this.headers = List.copyOf(headers);
        this.rows = List.copyOf(rows);
    }
}

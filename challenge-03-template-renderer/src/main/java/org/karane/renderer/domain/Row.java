package org.karane.renderer.domain;

import java.util.List;

public record Row(List<String> cells) {
    public Row(List<String> cells) {
        this.cells = List.copyOf(cells);
    }
}

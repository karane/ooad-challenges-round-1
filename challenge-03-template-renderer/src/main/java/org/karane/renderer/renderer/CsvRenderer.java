package org.karane.renderer.renderer;

import java.util.List;

public class CsvRenderer extends BaseRenderer {

    @Override
    protected String renderTitle(String title) {
        return "# " + title + "\n";
    }

    @Override
    protected String renderHeaders(List<String> headers) {
        return String.join(",", headers) + "\n";
    }

    @Override
    protected String renderRow(List<String> cells) {
        return String.join(",", cells) + "\n";
    }
}

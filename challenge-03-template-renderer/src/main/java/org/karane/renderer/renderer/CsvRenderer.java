package org.karane.renderer.renderer;

import java.util.List;

public class CsvRenderer extends BaseRenderer {

    @Override
    protected void renderTitle(StringBuilder sb, String title) {
        sb.append("# ").append(title).append("\n");
    }

    @Override
    protected void renderHeaders(StringBuilder sb, List<String> headers) {
        sb.append(String.join(",", headers)).append("\n");
    }

    @Override
    protected void renderRow(StringBuilder sb, List<String> cells) {
        sb.append(String.join(",", cells)).append("\n");
    }
}

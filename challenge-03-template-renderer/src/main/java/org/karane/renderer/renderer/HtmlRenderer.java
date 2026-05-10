package org.karane.renderer.renderer;

import java.util.List;

public class HtmlRenderer extends BaseRenderer {

    @Override
    protected void renderTitle(StringBuilder sb, String title) {
        sb.append("<h1>").append(title).append("</h1>\n");
        sb.append("<table>\n");
    }

    @Override
    protected void renderHeaders(StringBuilder sb, List<String> headers) {
        sb.append("  <tr>");
        for (String h : headers) {
            sb.append("<th>").append(h).append("</th>");
        }
        sb.append("</tr>\n");
    }

    @Override
    protected void renderRow(StringBuilder sb, List<String> cells) {
        sb.append("  <tr>");
        for (String cell : cells) {
            sb.append("<td>").append(cell).append("</td>");
        }
        sb.append("</tr>\n");
    }

    @Override
    protected void renderFooter(StringBuilder sb) {
        sb.append("</table>\n");
    }
}

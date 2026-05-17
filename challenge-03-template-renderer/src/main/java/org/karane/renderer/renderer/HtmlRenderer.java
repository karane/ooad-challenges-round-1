package org.karane.renderer.renderer;

import java.util.List;

public class HtmlRenderer extends BaseRenderer {

    @Override
    protected String renderTitle(String title) {
        return "<h1>" + title + "</h1>\n<table>\n";
    }

    @Override
    protected String renderHeaders(List<String> headers) {
        StringBuilder sb = new StringBuilder("  <tr>");
        for (String h : headers) {
            sb.append("<th>").append(h).append("</th>");
        }
        sb.append("</tr>\n");
        return sb.toString();
    }

    @Override
    protected String renderRow(List<String> cells) {
        StringBuilder sb = new StringBuilder("  <tr>");
        for (String cell : cells) {
            sb.append("<td>").append(cell).append("</td>");
        }
        sb.append("</tr>\n");
        return sb.toString();
    }

    @Override
    protected String renderFooter() {
        return "</table>\n";
    }
}

package org.karane.renderer.renderer;

import org.karane.renderer.domain.Row;
import org.karane.renderer.domain.Template;

import java.util.List;

public abstract class BaseRenderer implements TemplateRenderer {

    @Override
    public final String render(Template template) {
        StringBuilder sb = new StringBuilder();
        renderTitle(sb, template.title());
        renderHeaders(sb, template.headers());
        for (Row row : template.rows()) {
            renderRow(sb, row.cells());
        }
        renderFooter(sb);
        return sb.toString();
    }

    protected abstract void renderTitle(StringBuilder sb, String title);
    protected abstract void renderHeaders(StringBuilder sb, List<String> headers);
    protected abstract void renderRow(StringBuilder sb, List<String> cells);
    protected void renderFooter(StringBuilder sb) {}
}

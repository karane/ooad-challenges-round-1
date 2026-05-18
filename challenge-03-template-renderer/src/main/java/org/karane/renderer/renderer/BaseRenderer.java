package org.karane.renderer.renderer;

import org.karane.renderer.domain.Row;
import org.karane.renderer.domain.Template;

import java.util.List;

public abstract class BaseRenderer implements TemplateRenderer {

    @Override
    public final String render(Template template) {
        StringBuilder sb = new StringBuilder();
        sb.append(renderTitle(template.title()));
        sb.append(renderHeaders(template.headers()));
        for (Row row : template.rows()) {
            sb.append(renderRow(row.cells()));
        }
        sb.append(renderFooter());
        return sb.toString();
    }

    protected abstract String renderTitle(String title);
    protected abstract String renderHeaders(List<String> headers);
    protected abstract String renderRow(List<String> cells);
    protected String renderFooter() { return ""; }
}

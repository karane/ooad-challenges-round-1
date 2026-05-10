package org.karane.renderer.service;

import org.karane.renderer.domain.Template;
import org.karane.renderer.renderer.TemplateRenderer;

public class RenderService {
    private final TemplateRenderer renderer;

    public RenderService(TemplateRenderer renderer) {
        this.renderer = renderer;
    }

    public String render(Template template) {
        return renderer.render(template);
    }
}

package org.karane.renderer;

import java.nio.file.Path;

import org.karane.renderer.builder.TemplateBuilder;
import org.karane.renderer.domain.Template;
import org.karane.renderer.renderer.CsvRenderer;
import org.karane.renderer.renderer.HtmlRenderer;
import org.karane.renderer.renderer.PdfRenderer;
import org.karane.renderer.service.RenderService;

public class Main {
    public static void main(String[] args) {
        Template template = new TemplateBuilder()
                .title("Sales Report Q1 2026")
                .headers("Product", "Units", "Revenue")
                .row("Widget A", "120", "$2,400")
                .row("Widget B", "85", "$1,700")
                .row("Widget C", "200", "$6,000")
                .build();

        System.out.println("=== HTML ===");
        System.out.println(new RenderService(new HtmlRenderer()).render(template));

        System.out.println("=== CSV ===");
        System.out.println(new RenderService(new CsvRenderer()).render(template));

        
        System.out.println("=== PDF ===");

        Path pdfPath = Path.of("report.pdf");

        try {

            new PdfRenderer().renderToFile(template, pdfPath);
            System.out.println("PDF written to " + pdfPath.toAbsolutePath());

        } catch (Exception e) {

            System.err.println("PDF rendering failed: " + e.getMessage());

        }

}
}

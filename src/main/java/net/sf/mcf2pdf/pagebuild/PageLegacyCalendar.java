package net.sf.mcf2pdf.pagebuild;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;

import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.w3c.dom.svg.SVGDocument;

import net.sf.mcf2pdf.calendar.CalendarDateModel.CalendarMonth;
import net.sf.mcf2pdf.calendar.LegacyCalendarSvg;
import net.sf.mcf2pdf.mcfelements.impl.McfSvgCalendarImpl;
import net.sf.mcf2pdf.mcfelements.util.ImageUtil;

/** Renders the project's SVG fragments in their original area and z order. */
public final class PageLegacyCalendar implements PageDrawable {
    private final McfSvgCalendarImpl svg;
    private final LocalDate start;
    private final CalendarMonth month;
    private final String language;

    public PageLegacyCalendar(McfSvgCalendarImpl svg, LocalDate start, CalendarMonth month, String language) {
        this.svg = svg;
        this.start = start;
        this.month = month;
        this.language = language;
    }

    @Override public float getLeftMM() { return svg.getArea().getLeft() / 10f; }
    @Override public float getTopMM() { return svg.getArea().getTop() / 10f; }
    @Override public int getZPosition() { return svg.getArea().getZPosition(); }
    @Override public boolean isVectorGraphic() { return false; }
    @Override public void renderAsSvgElement(Writer writer, PageRenderContext context) { throw new UnsupportedOperationException(); }

    @Override public BufferedImage renderAsBitmap(PageRenderContext context, Point offset, int width, int height)
            throws IOException {
        String source = svg.getSvgDocument();
        if (source == null || source.trim().isEmpty()) source = readRequired(context, svg.getFileName());
        String css = readRequired(context, svg.getCssSheet());
        // StandardSheet names a non-existent aggregate CSS in CEWE 5.x. The actual
        // layout stylesheet is beside it and has the selected colour's basename.
        String standard = svg.getStandardSheet();
        if (standard == null && svg.getFileName() != null) standard = svg.getFileName();
        if (standard != null) {
            String color = svg.getCssSheet().replace('\\', '/');
            String local = standard.substring(0, standard.replace('\\', '/').lastIndexOf('/') + 1)
                    + color.substring(color.lastIndexOf('/') + 1);
            if (!local.equals(color)) css += "\n" + readRequired(context, local);
        }
        if (svg.getCustomCssSheet() != null && !svg.getCustomCssSheet().trim().isEmpty()) {
            css += "\n" + readRequired(context, svg.getCustomCssSheet());
        }
        SVGDocument document = LegacyCalendarSvg.prepare(source, css, start, month, language);
        Raster transcoder = new Raster();
        transcoder.addTranscodingHint(ImageTranscoder.KEY_WIDTH, (float) context.toPixel(svg.getArea().getWidth() / 10f));
        transcoder.addTranscodingHint(ImageTranscoder.KEY_HEIGHT, (float) context.toPixel(svg.getArea().getHeight() / 10f));
        try {
            transcoder.transcode(new TranscoderInput(document), null);
        } catch (TranscoderException | RuntimeException e) {
            throw new IOException("Cannot render legacy calendar SVG: " + svg.getFileName(), e);
        }
        BufferedImage image = transcoder.image;
        if (svg.getArea().getRotation() != 0) {
            image = ImageUtil.rotateImage(image, (float) Math.toRadians(svg.getArea().getRotation()), offset);
        }
        return image;
    }

    private static String readRequired(PageRenderContext context, String path) throws IOException {
        File file = context.getCalendarResource(path);
        if (file == null) throw new IOException("Required legacy calendar resource is missing: " + path
                + "; add the old CEWE Resources/photofun directory with -e");
        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    private static final class Raster extends ImageTranscoder {
        private BufferedImage image;
        @Override public BufferedImage createImage(int w, int h) { return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB); }
        @Override public void writeImage(BufferedImage value, TranscoderOutput output) { image = value; }
    }
}

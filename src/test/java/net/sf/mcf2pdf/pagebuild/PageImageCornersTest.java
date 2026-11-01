package net.sf.mcf2pdf.pagebuild;

import static org.junit.Assert.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.StringReader;
import javax.imageio.ImageIO;
import org.apache.commons.digester3.Digester;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import net.sf.mcf2pdf.mcfelements.*;
import net.sf.mcf2pdf.mcfelements.impl.DigesterConfiguratorImpl;

public class PageImageCornersTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();
    private Point offset = new Point();

    private String corners(String enabled, int length, String... positions) {
        String xml = "<corners enabled=\"" + enabled + "\">";
        for (String position : positions)
            xml += "<corner length=\"" + length + "\" shape=\"convex\" where=\"" + position + "\"/>";
        return xml + "</corners>";
    }

    private String allCorners(String enabled, int length) {
        return corners(enabled, length, "top-left", "top-right", "bottom-left", "bottom-right");
    }

    private BufferedImage render(String attributes, String decoration, int width, int height, int dpi) throws Exception {
        File dir = tmp.newFolder();
        BufferedImage source = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = source.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, 200, 200);
        g.dispose();
        ImageIO.write(source, "png", new File(dir, "source.png"));
        String xml = "<fotobook imagedir=\".\"><page pagenr=\"68\"><area " + attributes + ">"
                + "<position width=\"" + width + "\" height=\"" + height + "\"/>"
                + "<decoration>" + decoration + "</decoration>"
                + "<image filename=\"source.png\"><cutout scale=\"1\" left=\"0\" top=\"0\"/></image>"
                + "</area></page></fotobook>";
        Digester d = new Digester();
        new DigesterConfiguratorImpl().configureDigester(d, null);
        McfFotobook book = d.parse(new StringReader(xml));
        ((net.sf.mcf2pdf.mcfelements.impl.McfFotobookImpl) book).setFile(new File(dir, "book.mcf"));
        McfImage image = (McfImage) book.getPages().get(0).getAreas().get(0).getContent();
        return new PageImage(image).renderAsBitmap(new PageRenderContext(dpi, 1, null, null), offset, 0, 0);
    }

    private int alpha(BufferedImage image, int x, int y) { return image.getRGB(x, y) >>> 24; }

    @Test public void legacyRectangularImageAndBorderStayUnchanged() throws Exception {
        BufferedImage image = render("borderenabled=\"true\" sizeborder=\"2\" colorborder=\"#0000ff\"", "", 100, 100, 254);
        assertEquals(104, image.getWidth());
        assertEquals(104, image.getHeight());
        assertEquals(new Point(-2, -2), offset);
        assertEquals(Color.BLUE.getRGB(), image.getRGB(0, 0));
        assertEquals(Color.RED.getRGB(), image.getRGB(2, 2));
    }

    @Test public void modernRectangularBorderStaysUnchanged() throws Exception {
        BufferedImage image = render("", "<border enabled=\"1\" width=\"2\" color=\"#ff0000ff\"/>", 100, 100, 254);
        assertEquals(Color.BLUE.getRGB(), image.getRGB(0, 0));
        assertEquals(Color.RED.getRGB(), image.getRGB(2, 2));
    }

    @Test public void page68RoundsPhotoAndBorderTogether() throws Exception {
        BufferedImage image = render("", "<border enabled=\"1\" width=\"2\" color=\"#ff0000ff\"/>" + allCorners("yes", 5000), 100, 100, 254);
        assertEquals(0, alpha(image, 0, 0));
        assertEquals(0, alpha(image, 4, 4));
        assertEquals(Color.BLUE.getRGB(), image.getRGB(52, 1));
        assertEquals(Color.RED.getRGB(), image.getRGB(52, 5));
        assertEquals(new Point(-2, -2), offset);
    }

    @Test public void roundingIsRelativeToImageSizeAndIndependentOfDpi() throws Exception {
        for (int dpi : new int[] {127, 254, 508}) {
            BufferedImage image = render("", allCorners("yes", 5000), 100, 100, dpi);
            int n = image.getWidth();
            assertEquals(0, alpha(image, n / 10, n / 10));
            assertEquals(Color.RED.getRGB(), image.getRGB(n / 2, n / 2));
            assertEquals(0, alpha(image, n - 1, n - 1));
            boolean antialiased = false;
            for (int y = 0; y < n; y++) for (int x = 0; x < n; x++)
                if (alpha(image, x, y) > 0 && alpha(image, x, y) < 255) antialiased = true;
            assertTrue("Curves should have smooth transparent edges", antialiased);
        }
    }

    @Test public void rectangularPhotoUsesHalfShorterSideForMaximumRounding() throws Exception {
        BufferedImage image = render("", allCorners("yes", 5000), 100, 160, 254);
        assertEquals(0, alpha(image, 0, 0));
        assertEquals(Color.RED.getRGB(), image.getRGB(1, 60));
        assertEquals(Color.RED.getRGB(), image.getRGB(50, 1));
    }

    @Test public void onlyRequestedCornerIsRounded() throws Exception {
        BufferedImage image = render("", corners("yes", 2500, "top-left"), 100, 100, 254);
        assertEquals(0, alpha(image, 0, 0));
        assertEquals(Color.RED.getRGB(), image.getRGB(30, 1));
        assertEquals(Color.RED.getRGB(), image.getRGB(99, 0));
        assertEquals(Color.RED.getRGB(), image.getRGB(0, 99));
        assertEquals(Color.RED.getRGB(), image.getRGB(99, 99));
    }

    @Test public void eachCornerCanBeRoundedIndependently() throws Exception {
        String[] names = {"top-left", "top-right", "bottom-right", "bottom-left"};
        int[][] points = {{0, 0}, {99, 0}, {99, 99}, {0, 99}};
        for (int selected = 0; selected < names.length; selected++) {
            BufferedImage image = render("", corners("yes", 2500, names[selected]), 100, 100, 254);
            for (int corner = 0; corner < points.length; corner++)
                assertEquals(names[selected] + " / " + names[corner], selected == corner ? 0 : 255,
                        alpha(image, points[corner][0], points[corner][1]));
        }
    }

    @Test public void shadowFollowsRoundedOutline() throws Exception {
        String shadow = "shadowEnabled=\"true\" shadowDistance=\"10\" shadowAngle=\"135\" shadowIntensity=\"128\"";
        BufferedImage rounded = render(shadow, allCorners("yes", 5000), 100, 100, 254);
        assertEquals(107, rounded.getWidth());
        assertEquals(0, alpha(rounded, 106, 106));
        assertEquals(128, alpha(rounded, 104, 57));
        BufferedImage rectangle = render(shadow, "", 100, 100, 254);
        assertEquals(128, alpha(rectangle, 106, 106));
    }

    @Test public void numericAndBooleanEnabledValuesRoundPhotos() throws Exception {
        for (String enabled : new String[] {"1", "true"}) {
            BufferedImage image = render("", allCorners(enabled, 5000), 100, 100, 254);
            assertEquals(0, alpha(image, 0, 0));
        }
    }

    @Test public void outOfRangeLengthsAreClamped() throws Exception {
        BufferedImage negative = render("", allCorners("yes", -1), 100, 100, 254);
        assertEquals(Color.RED.getRGB(), negative.getRGB(0, 0));
        BufferedImage large = render("", allCorners("yes", 10000), 100, 100, 254);
        assertEquals(0, alpha(large, 0, 0));
        assertEquals(Color.RED.getRGB(), large.getRGB(50, 50));
    }

    @Test public void disabledAndZeroLengthCornersLeaveRectangle() throws Exception {
        for (String enabled : new String[] {"no", "false", "0"}) {
            BufferedImage image = render("", allCorners(enabled, 5000), 100, 100, 254);
            assertEquals(Color.RED.getRGB(), image.getRGB(0, 0));
        }
        BufferedImage image = render("", allCorners("yes", 0), 100, 100, 254);
        assertEquals(Color.RED.getRGB(), image.getRGB(0, 0));
    }
}

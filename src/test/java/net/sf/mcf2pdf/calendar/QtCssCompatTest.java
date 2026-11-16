package net.sf.mcf2pdf.calendar;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.StringReader;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.XMLResourceDescriptor;
import org.junit.Test;
import org.w3c.dom.svg.SVGDocument;

public class QtCssCompatTest {

    private static final String M = "[" + QtCssCompat.PREVIOUS_SIBLING_ATTRIBUTE + "]";

    @Test public void adjacentSiblingAfterAnAncestorDropsTheSiblingButKeepsSpecificity() {
        assertEquals(".cm_cal_sunday0 text" + M, QtCssCompat.rewrite(".cm_cal_sunday0 .internal + text"));
        assertEquals(".a > text" + M + M, QtCssCompat.rewrite(".a > g.b.c+text"));
        assertEquals("rect.internal + text", QtCssCompat.rewrite("rect.internal + text"));
        assertEquals(".a .b text", QtCssCompat.rewrite(".a .b text"));
        assertEquals("text.holiday" + " {fill:#000}\n" + ".x text" + M + ", .y {fill:#fff}\n",
                QtCssCompat.emulateAdjacentSiblingQuirk("/* c */text.holiday {fill:#000}\n.x .i + text, .y {fill:#fff}"));
    }

    @Test public void reproducesCewe518HolidayLabelColours() throws Exception {
        String colour = "text { fill:#ff0000; }\n"
                + ".cm_cal_sunday0 text { fill:#ffffff; }\n"
                + "text.holiday { fill:#ffffff; }\n"
                + "rect.internal + text { fill:#000000; }\n"
                + ".cm_cal_sunday0 .internal + text { fill:#ffffff; }\n";
        String layout = "text.holiday, .holiday text.holiday { fill:#000000; }\n";
        String cell = "<rect width='10' height='10' fill='#808080'/>"
                + "<g class='weekdays'><text x='0' y='9' font-size='12'>&#9608;</text></g>"
                + "<text class='holiday' x='10' y='9' font-size='12'>&#9608;</text>";
        String svg = "<svg xmlns='http://www.w3.org/2000/svg' width='60' height='30'>"
                + "<g class='cm_cal_weekday0'>" + cell + "</g>"
                + "<g class='cm_cal_sunday0' transform='translate(30,0)'>" + cell + "</g></svg>";
        SVGDocument document = new SAXSVGDocumentFactory(XMLResourceDescriptor.getXMLParserClassName())
                .createSVGDocument(null, new StringReader(svg));
        QtCssCompat.markPreviousSiblings(document.getDocumentElement());
        org.w3c.dom.Element style = document.createElementNS("http://www.w3.org/2000/svg", "style");
        style.setTextContent(QtCssCompat.emulateAdjacentSiblingQuirk(colour + layout));
        document.getDocumentElement().insertBefore(style, document.getDocumentElement().getFirstChild());

        BufferedImage[] image = new BufferedImage[1];
        ImageTranscoder transcoder = new ImageTranscoder() {
            @Override public BufferedImage createImage(int w, int h) {
                return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            }
            @Override public void writeImage(BufferedImage value, TranscoderOutput output) { image[0] = value; }
        };
        transcoder.transcode(new TranscoderInput(document), null);

        assertEquals("weekday date", 0xff0000, image[0].getRGB(4, 5) & 0xffffff);
        assertEquals("weekday label", 0x000000, image[0].getRGB(14, 5) & 0xffffff);
        assertEquals("Sunday date", 0xffffff, image[0].getRGB(34, 5) & 0xffffff);
        assertEquals("Sunday label", 0xffffff, image[0].getRGB(44, 5) & 0xffffff);
    }
}

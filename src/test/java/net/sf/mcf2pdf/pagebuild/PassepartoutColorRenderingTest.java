package net.sf.mcf2pdf.pagebuild;

import static net.sf.mcf2pdf.testutil.McfTestSupport.parse;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.mcf2pdf.mcfconfig.Clipart;
import net.sf.mcf2pdf.mcfconfig.Fading;
import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.McfImage;
import net.sf.mcf2pdf.mcfelements.impl.McfFotobookImpl;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

public class PassepartoutColorRenderingTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void recolorsFrameWithoutRecoloringPhotograph() throws Exception {
		File dir = tmp.newFolder();
		BufferedImage source = new BufferedImage(80, 80, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = source.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, 80, 80);
		g.dispose();
		ImageIO.write(source, "png", new File(dir, "photo.png"));
		File frame = new File(dir, "frame.svg");
		Files.writeString(frame.toPath(),
				"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\"><rect width=\"5\" height=\"20\" fill=\"black\"/></svg>");
		McfResourceScanner resources = mock(McfResourceScanner.class);
		Fading config = new Fading();
		Clipart clip = new Clipart();
		clip.setFile("frame.svg");
		config.setClipart(clip);
		config.setFile("mask.svg");
		when(resources.getDecoration("7")).thenReturn(config);
		when(resources.getClip("frame")).thenReturn(frame);
		for (String target : new String[] { "#d26838", "#ffffff" }) {
			McfFotobook book = parse("<fotobook imagedir=\".\"><page><area><position width=\"20\" height=\"20\"/>"
					+ "<image filename=\"photo.png\" scale=\"1\" passepartoutDesignElementId=\"7\"><ClipartConfiguration><colors>"
					+ "<color source=\"#000000\" target=\"" + target
					+ "\"/></colors></ClipartConfiguration></image></area></page></fotobook>");
			((McfFotobookImpl) book).setFile(new File(dir, "book.mcf"));
			McfImage image = (McfImage) book.getPages().get(0).getAreas().get(0).getContent();
			BufferedImage result = new PageImage(image).renderAsBitmap(new PageRenderContext(254, 1, resources, null),
					new Point(), 0, 0);
			assertEquals(Color.decode(target).getRGB(), result.getRGB(2, 10));
			assertEquals(Color.BLACK.getRGB(), result.getRGB(10, 10));
		}
	}

}

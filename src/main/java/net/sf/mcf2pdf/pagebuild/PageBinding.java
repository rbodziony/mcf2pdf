/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import net.sf.mcf2pdf.mcfelements.util.ImageUtil;

public class PageBinding implements PageDrawable {

	private BufferedImage bindingImg;

	public PageBinding(File fBindingImg) throws IOException {
		bindingImg = ImageUtil.readImage(fBindingImg);
	}

	@Override
	public boolean isVectorGraphic() {
		return false;
	}

	@Override
	public void renderAsSvgElement(Writer writer, PageRenderContext context) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedImage renderAsBitmap(PageRenderContext context,
			Point drawOffsetPixels, int widthPX, int heightPX) throws IOException {
		context.getLog().debug("Rendering page binding");
		
		int width = Math.round(widthPX / 16.0f);

		BufferedImage img = new BufferedImage(width, heightPX, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

		g2d.drawImage(bindingImg, 0, 0, width, heightPX, 0, 0, bindingImg.getWidth(), bindingImg.getHeight(), null);
		g2d.dispose();

		drawOffsetPixels.x = Math.round((widthPX - width) / 2.0f);
		drawOffsetPixels.y = 0;

		return img;
	}

	@Override
	public int getZPosition() {
		// always on top!
		return 10000;
	}

	@Override
	public float getLeftMM() {
		return 0;
	}

	@Override
	public float getTopMM() {
		return 0;
	}

}

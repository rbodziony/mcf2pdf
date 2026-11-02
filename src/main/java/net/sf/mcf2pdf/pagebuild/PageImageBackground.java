/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;

import net.sf.mcf2pdf.mcfelements.McfImageBackground;
import net.sf.mcf2pdf.mcfglobals.McfAlbumType;


/**
 * TODO comment
 */
public class PageImageBackground extends PageImage {
	
	private McfImageBackground image;

	public PageImageBackground(McfImageBackground image) {
		super(image);
		this.image = image;
	}
	
	@Override
	protected double sourceExtent(double millimeters, double pixelsPerMM, double scale) {
		// A nested cutout stores tenths of a millimeter per source pixel, independent of DPI.
		if (image.getCutout() != null && image.getScale() > 0)
			return millimeters * 10 / image.getScale();
		return super.sourceExtent(millimeters, pixelsPerMM, scale);
	}

	@Override
	public BufferedImage renderAsBitmap(PageRenderContext context,
			Point drawOffsetPixels, int widthPX, int heightPX) throws IOException {
		BufferedImage img = super.renderAsBitmap(context, drawOffsetPixels, widthPX, heightPX);
		if (McfImageBackground.RIGHT_OR_BOTTOM.equals(image.getBackgroundPosition())) {
			McfAlbumType albumType = context.getAlbumType();
			drawOffsetPixels.x += context.toPixel(albumType.getUsableWidth() / 10.0f + 
					albumType.getBleedMargin() / 10.0f);
		}
		return img;
	}

}

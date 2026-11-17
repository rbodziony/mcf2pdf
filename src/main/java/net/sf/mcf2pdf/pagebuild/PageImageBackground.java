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

	private boolean vertical;

	public PageImageBackground(McfImageBackground image) {
		this(image, false);
	}

	public PageImageBackground(McfImageBackground image, boolean vertical) {
		super(image);
		this.image = image;
		this.vertical = vertical;
	}
	
	@Override
	public BufferedImage renderAsBitmap(PageRenderContext context,
			Point drawOffsetPixels, int widthPX, int heightPX) throws IOException {
		BufferedImage img = super.renderAsBitmap(context, drawOffsetPixels, widthPX, heightPX);
		if (McfImageBackground.RIGHT_OR_BOTTOM.equals(image.getBackgroundPosition()) && vertical) {
			drawOffsetPixels.y += context.toPixel((image.getArea().getHeight() + 2 * image.getArea().getTop()) / 10.0f);
		}
		else if (McfImageBackground.RIGHT_OR_BOTTOM.equals(image.getBackgroundPosition())) {
			McfAlbumType albumType = context.getAlbumType();
			drawOffsetPixels.x += context.toPixel(albumType.getUsableWidth() / 10.0f + 
					albumType.getBleedMargin() / 10.0f);
		}
		return img;
	}

}

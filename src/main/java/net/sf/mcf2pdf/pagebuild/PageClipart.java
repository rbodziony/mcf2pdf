/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import net.sf.mcf2pdf.mcfelements.McfClipart;
import net.sf.mcf2pdf.mcfelements.util.ImageUtil;


/**
 * TODO comment
 */
public class PageClipart implements PageDrawable {
	
	private McfClipart clipart;
	
	public PageClipart(McfClipart clipart) {
		this.clipart = clipart;
	}
	
	@Override
	public float getLeftMM() {
		return clipart.getArea().getLeft() / 10.0f;
	}
	
	@Override
	public float getTopMM() {
		return clipart.getArea().getTop() / 10.0f;
	}

	@Override
	public boolean isVectorGraphic() {
		return true;
	}

	@Override
	public void renderAsSvgElement(Writer writer, PageRenderContext context) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BufferedImage renderAsBitmap(PageRenderContext context,
			Point drawOffsetPixels, int widthPX, int heightPX) throws IOException {
		File f = null;
		if(clipart.getDesignElementId() == null) {
			 f = context.getClipart(clipart.getUniqueName());
		} else {
			 f = context.getClipartViaDesignElementId(clipart.getDesignElementId());
		}
		if (f == null) {
			if(clipart.getDesignElementId() == null)
				context.getLog().warn("Clipart not found: " + clipart.getUniqueName());
			else
				context.getLog().warn("Clipart designElementId not found: " + clipart.getDesignElementId());
			return null;
		}
		context.getLog().debug("Rendering clipart " + f);
		
		int widthPixel = context.toPixel(clipart.getArea().getWidth() / 10.0f);
		int heightPixel = context.toPixel(clipart.getArea().getHeight() / 10.0f);

		drawOffsetPixels.x = drawOffsetPixels.y = 0;
		BufferedImage loadedClip = ImageUtil.loadClpFile(f, widthPixel, heightPixel);
		ImageUtil.replaceColors(loadedClip, clipart.getColorMappings());
		// apply rotation
		if (clipart.getArea().getRotation() != 0) {
			loadedClip = ImageUtil.rotateImage(loadedClip, (float)Math.toRadians(clipart.getArea().getRotation()), drawOffsetPixels);
		}
		return loadedClip;
	}

	@Override
	public int getZPosition() {
		return clipart.getArea().getZPosition();
	}

}

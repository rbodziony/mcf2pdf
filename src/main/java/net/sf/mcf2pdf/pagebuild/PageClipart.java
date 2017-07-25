/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import net.sf.mcf2pdf.mcfelements.McfBorder;
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
			Point drawOffsetPixels) throws IOException {
		File f = context.getClipart(clipart.getUniqueName());
		if (f == null) {
			context.getLog().warn("Clipart not found: " + clipart.getUniqueName());
			return null;
		}
		context.getLog().debug("Rendering clipart " + f);
		
		int widthPixel = context.toPixel(clipart.getArea().getWidth() / 10.0f);
		int heightPixel = context.toPixel(clipart.getArea().getHeight() / 10.0f);
//		context.getLog().debug("widthPixel " + widthPixel);
//		context.getLog().debug("heightPixel " + heightPixel);
		//int scale = 
		drawOffsetPixels.x = drawOffsetPixels.y = 0;
		//return ImageUtil.loadClpFile(f, widthPixel, heightPixel);
		BufferedImage baseImg = ImageUtil.loadClpFile(f, widthPixel, heightPixel);
		// apply rotation
		int borderWidth = ( !clipart.getArea().isBorderEnabled()) ? 0
				: context.toPixel(clipart.getArea().getBorderSize() / 10.0f);
		Color borderColor = clipart.getArea().getBorderColor();
		// check for Format 6 - border may be child element
		if (clipart.getArea().getBorder() != null) {
			McfBorder border = clipart.getArea().getBorder();
			borderWidth = border.isEnabled() ? context.toPixel(border.getWidth() / 10.0f) : 0;
//			context.getLog().debug("borderWidth " + borderWidth);
			borderColor = border.getColor();
		}
		int shadowDistance = (!clipart.getArea().isShadowEnabled()) ? 0 : context.toPixel(clipart.getArea().getShadowDistance() / 10.0f);
		int xAddShadow = (int)Math.round(shadowDistance * Math.sin(Math.toRadians(clipart.getArea().getShadowAngle())));
		int yAddShadow = (int)Math.round(shadowDistance * -Math.cos(Math.toRadians(clipart.getArea().getShadowAngle())));
		int xAdd = Math.max(Math.abs(xAddShadow), borderWidth * 2);
		int yAdd = Math.max(Math.abs(yAddShadow), borderWidth * 2);
//		context.getLog().debug("shadowDistance " + shadowDistance);
//		context.getLog().debug("xAddShadow " + xAddShadow);
//		context.getLog().debug("yAddShadow " + yAddShadow);
//		context.getLog().debug("xAdd " + xAdd);
//		context.getLog().debug("yAdd " + yAdd);
		// create image without rotation
				BufferedImage img = new BufferedImage(widthPixel + xAdd, heightPixel + yAdd,
						BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2d = img.createGraphics();

				int imgLeft = 0, imgTop = 0;

				// draw shadow
		if (shadowDistance > 0) {
					int sleft = xAddShadow < 0 ? 0 : xAddShadow;
					int stop = yAddShadow < 0 ? 0 : yAddShadow;
					imgLeft = sleft == 0 ? -xAddShadow : 0;
					imgTop = stop == 0 ? -yAddShadow : 0;
//					context.getLog().debug("sleft " + sleft);
//					context.getLog().debug("stop " + stop);
//					context.getLog().debug("imgLeft " + imgLeft);
//					context.getLog().debug("imgTop " + imgTop);
					g2d.setColor(new Color(0, 0, 0, clipart.getArea().getShadowIntensity()));
					g2d.fillRect(sleft, stop, widthPixel, heightPixel);
		}

		// draw border
		if (borderWidth != 0) {
					int bleft = xAddShadow < 0 ? Math.max(0, -xAddShadow - borderWidth) : 0;
					int btop = yAddShadow < 0 ? Math.max(0, -yAddShadow - borderWidth) : 0;
					imgLeft = bleft + borderWidth;
					imgTop = btop + borderWidth;
					g2d.setColor(borderColor);
					g2d.fillRect(bleft, btop, widthPixel + 2 * borderWidth, heightPixel + 2 * borderWidth);
		}

		// hide shadow when completely occupied by border
		if (Math.abs(xAddShadow) <= borderWidth && Math.abs(yAddShadow) <= borderWidth)
			shadowDistance = 0;
		int leftOffset = -(int)Math.round(clipart.getArea().getLeft()/ 10.0f);
		int topOffset = -(int)Math.round(clipart.getArea().getTop()/ 10.0f);
		
		drawOffsetPixels.x = -imgLeft;
		drawOffsetPixels.y = -imgTop;
		
	
		int effImgWidth = widthPixel;
		int effImgHeight = heightPixel;
//		context.getLog().debug("leftOffset " + leftOffset);
//		context.getLog().debug("topOffset " + topOffset);

		// draw main image
		g2d.drawImage(baseImg, 
						imgLeft, imgTop, imgLeft + effImgWidth, imgTop + effImgHeight,
						leftOffset, topOffset, leftOffset + 0, topOffset + 0,
						null);
		
		BufferedImage imgClipart = ImageUtil.loadClpFile(f, widthPixel, heightPixel);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		int x = 0;
		int y = 0;
		g2d.drawImage(imgClipart, x, y, null);		
		if (clipart.getArea().getRotation() != 0) {
			context.getLog().warn("Clipart rotating : " + clipart.getArea().getRotation());
			baseImg = ImageUtil.rotateImage(baseImg, (float)Math.toRadians(clipart.getArea().getRotation()), drawOffsetPixels);
		}
		return baseImg;
	}

	@Override
	public int getZPosition() {
		return clipart.getArea().getZPosition();
	}

}

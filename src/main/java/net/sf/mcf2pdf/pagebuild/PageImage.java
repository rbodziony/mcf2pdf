/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Writer;

import net.sf.mcf2pdf.mcfelements.McfBorder;
import net.sf.mcf2pdf.mcfelements.McfImage;
import net.sf.mcf2pdf.mcfelements.McfCorner;
import net.sf.mcf2pdf.mcfelements.McfCorners;
import net.sf.mcf2pdf.mcfelements.util.FadingComposite;
import net.sf.mcf2pdf.mcfelements.util.ImageUtil;
import net.sf.mcf2pdf.mcfelements.util.McfFileUtil;
import net.sf.mcf2pdf.mcfglobals.McfFotoFrame;
import net.sf.mcf2pdf.mcfconfig.Fading;
import net.sf.mcf2pdf.mcfconfig.Fotoarea;


/**
 * TODO comment
 */
public class PageImage implements PageDrawable {

	private McfImage image;

	public PageImage(McfImage image) {
		this.image = image;
	}

	@Override
	public float getLeftMM() {
		return image.getArea().getLeft() / 10.0f;
	}

	@Override
	public float getTopMM() {
		return image.getArea().getTop() / 10.0f;
	}

	@Override
	public int getZPosition() {
		return image.getArea().getZPosition();
	}

	@Override
	public boolean isVectorGraphic() {
		return false;
	}

	@Override
	public void renderAsSvgElement(Writer writer,	PageRenderContext context) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public BufferedImage renderAsBitmap(PageRenderContext context, Point drawOffsetPixels, int widthPX, int heightPX) throws IOException {
		int widthPixel = context.toPixel(image.getArea().getWidth() / 10.0f);
		int heightPixel = context.toPixel(image.getArea().getHeight() / 10.0f);
		if (image.getFileName() == null || "".equals(image.getFileName())) {
			// return empty image
			return new BufferedImage(widthPixel, heightPixel, BufferedImage.TYPE_INT_ARGB);
		}

		File baseImage = McfFileUtil.getImageFile(image.getFileName(),
				image.getArea().getPage().getFotobook());

		context.getLog().debug("Rendering image " + baseImage);

		// check for "fading" file
		File maskFile = null;
		File clipartFile = null;
		Fotoarea fotoArea = null;
		if (image.getFadingFile() != null) {
			// TODO change if passpoart get all config
			McfFotoFrame frame=null;
			// old version
			if(image.getPassepartoutDesignElementId() == null)
				frame = context.getFotoFrame(image.getFadingFile());
			else
				frame = context.getFotoFrameViaDesignElementID(image.getPassepartoutDesignElementId());
			if (frame != null && frame.getFading() != null) {
				maskFile = frame.getFading();
			} else {
				maskFile = context.getFading(image.getFadingFile());
			}
			if (frame != null && frame.getClipart() != null) {
				clipartFile = frame.getClipart();
			}
			if (frame != null) {
				Fading config = frame.getConfig();
				if (config != null) {
					fotoArea = config.getFotoarea();
				}
			}
			if (maskFile == null)
				context.getLog().warn("Could not find fading file: " + image.getFadingFile());
		}

		// get image resolution; load base image into memory
		float[] res = ImageUtil.getImageResolution(baseImage);
		BufferedImage baseImg = ImageUtil.readImage(baseImage);

		double tmmX = image.getArea().getWidth() /10.0f;
		double tmmY = image.getArea().getHeight() / 10.0f;

		double resX = res[0] / ImageUtil.MM_PER_INCH;
		double resY = res[1] / ImageUtil.MM_PER_INCH;

		double scale = image.getScale() / ImageUtil.SQRT_2;

		double sw = (tmmX * resX) / scale;
		double sh = (tmmY * resY) / scale;

		// draw border and / or shadow?
		int borderWidth = (maskFile != null || !image.getArea().isBorderEnabled()) ? 0
				: context.toPixel(image.getArea().getBorderSize() / 10.0f);
		Color borderColor = image.getArea().getBorderColor();
		// check for Format 6 - border may be child element
		if (image.getArea().getBorder() != null) {
			McfBorder border = image.getArea().getBorder();
			borderWidth = border.isEnabled() ? context.toPixel(border.getWidth() / 10.0f) : 0;
			borderColor = border.getColor();
		}

		int shadowDistance = (maskFile != null | !image.getArea().isShadowEnabled()) ? 0 : context.toPixel(image.getArea().getShadowDistance() / 10.0f);
		int xAddShadow = (int)Math.round(shadowDistance * Math.sin(Math.toRadians(image.getArea().getShadowAngle())));
		int yAddShadow = (int)Math.round(shadowDistance * -Math.cos(Math.toRadians(image.getArea().getShadowAngle())));
		int xAdd = Math.max(Math.abs(xAddShadow), borderWidth * 2);
		int yAdd = Math.max(Math.abs(yAddShadow), borderWidth * 2);

		// hide shadow when completely occupied by border
		if (Math.abs(xAddShadow) <= borderWidth && Math.abs(yAddShadow) <= borderWidth)
			shadowDistance = 0;

		// create image without rotation
		BufferedImage img = new BufferedImage(widthPixel + xAdd, heightPixel + yAdd,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();

		McfCorners corners = image.getArea().getCorners();
		boolean rounded = hasRoundedCorners(corners);
		if (rounded)
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int imgLeft = 0, imgTop = 0;

		// draw shadow
		if (shadowDistance > 0) {
			int sleft = xAddShadow < 0 ? 0 : xAddShadow;
			int stop = yAddShadow < 0 ? 0 : yAddShadow;
			imgLeft = sleft == 0 ? -xAddShadow : 0;
			imgTop = stop == 0 ? -yAddShadow : 0;

			g2d.setColor(new Color(0, 0, 0, image.getArea().getShadowIntensity()));
			g2d.fill(imageShape(corners, sleft, stop, widthPixel, heightPixel));
		}

		// draw border
		if (borderWidth != 0) {
			int bleft = xAddShadow < 0 ? Math.max(0, -xAddShadow - borderWidth) : 0;
			int btop = yAddShadow < 0 ? Math.max(0, -yAddShadow - borderWidth) : 0;
			imgLeft = bleft + borderWidth;
			imgTop = btop + borderWidth;
			g2d.setColor(borderColor);
			if (rounded) {
				Shape outline = imageShape(corners, imgLeft, imgTop, widthPixel, heightPixel);
				g2d.fill(outline);
				g2d.setStroke(new BasicStroke(2 * borderWidth));
				g2d.draw(outline);
			} else {
				g2d.fillRect(bleft, btop, widthPixel + 2 * borderWidth, heightPixel + 2 * borderWidth);
			}
		}
		int leftOffset = -(int)(image.getLeft());
		int topOffset = -(int)(image.getTop());
		
		drawOffsetPixels.x = -imgLeft;
		drawOffsetPixels.y = -imgTop;
		
		int effImgWidth = widthPixel;
		int effImgHeight = heightPixel;
		if (fotoArea != null) {
			imgLeft += (int)Math.round(fotoArea.getX() * widthPixel);
			imgTop += (int)Math.round(fotoArea.getY() * heightPixel);
			effImgWidth = (int)Math.round(widthPixel * fotoArea.getWidth());
			effImgHeight = (int)Math.round(heightPixel * fotoArea.getHeight());
			sw = sw * fotoArea.getWidth();
			sh = sh * fotoArea.getHeight();
		}
		// hack for calculating this 4 s'values for SVG
		if(baseImage.getName().toUpperCase().contains(".SVG")) {
			leftOffset = (int)Math.round(leftOffset * 1.333);
			topOffset = (int)Math.round(topOffset *1.3333);
			sw = sw *1.3333;
			sh = sh *1.3333;
		}// end hack

		// Draw rounded photos through an antialiased alpha mask. A Graphics2D clip alone
		// produces jagged edges and clearing corners afterwards erases the border too.
		BufferedImage photo = rounded ? new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB) : null;
		Graphics2D photoGraphics = rounded ? photo.createGraphics() : g2d;
		if (rounded) {
			photoGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			photoGraphics.setColor(Color.WHITE);
			photoGraphics.fill(imageShape(corners, imgLeft, imgTop, effImgWidth, effImgHeight));
			photoGraphics.setComposite(AlphaComposite.SrcIn);
		}
		photoGraphics.drawImage(baseImg,
				imgLeft, imgTop, imgLeft + effImgWidth, imgTop + effImgHeight,
				leftOffset, topOffset, leftOffset + (int)Math.round(sw), topOffset + (int)Math.round(sh), null);
		if (rounded) {
			photoGraphics.dispose();
			g2d.drawImage(photo, 0, 0, null);
		}

		// mask image
		if (maskFile != null) {
			int x = 0;
			int y = 0;
			int effWidth = widthPixel;
			int effHeight = heightPixel;
			if (fotoArea != null) {
				x = (int)(fotoArea.getX() * widthPixel);
				y = (int)(fotoArea.getY() * heightPixel);
				effWidth = (int)(widthPixel * fotoArea.getWidth());
				effHeight = (int)(heightPixel * fotoArea.getHeight());
			}
			
			context.getLog().debug("Applying fading file " + maskFile);
			BufferedImage imgMask = ImageUtil.loadClpFile(maskFile, effWidth, effHeight);

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g2d.setComposite(FadingComposite.INSTANCE);

			g2d.drawImage(imgMask, x, y, null);
			g2d.setPaintMode();
		}
		
		// clipart image
		if (clipartFile != null) {
			int x = 0;
			int y = 0;
			context.getLog().debug("Applying clipart file " + maskFile);
			BufferedImage imgClipart = ImageUtil.loadClpFile(clipartFile, widthPixel, heightPixel);
			
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.drawImage(imgClipart, x, y, null);
		}

		g2d.dispose();

		// apply rotation
		if (image.getArea().getRotation() != 0) {
			img = ImageUtil.rotateImage(img, (float)Math.toRadians(image.getArea().getRotation()), drawOffsetPixels);
		}

		return img;
	}

	private static boolean hasRoundedCorners(McfCorners corners) {
		if (corners == null || !corners.isEnabled()) return false;
		for (McfCorner corner : corners.getCorners())
			if (corner.getLength() > 0 && "convex".equals(corner.getShape()) && cornerIndex(corner.getWhere()) >= 0)
				return true;
		return false;
	}

	private static int cornerIndex(String where) {
		if ("top-left".equals(where)) return 0;
		if ("top-right".equals(where)) return 1;
		if ("bottom-right".equals(where)) return 2;
		if ("bottom-left".equals(where)) return 3;
		return -1;
	}

	private static Shape imageShape(McfCorners corners, double x, double y, double width, double height) {
		if (!hasRoundedCorners(corners)) return new Rectangle2D.Double(x, y, width, height);
		double[] r = new double[4];
		for (McfCorner corner : corners.getCorners()) {
			int i = cornerIndex(corner.getWhere());
			if (i >= 0 && "convex".equals(corner.getShape())) {
				// CEWE stores a fraction of the shorter side in ten-thousandths,
				// not a physical length: 5000 gives a circle or a capsule.
				r[i] = Math.min(width, height) * Math.max(0, Math.min(5000, corner.getLength())) / 10000.0;
			}
		}
		double right = x + width, bottom = y + height;
		double k = 0.5522847498307936; // cubic approximation of a quarter circle
		Path2D.Double path = new Path2D.Double();
		path.moveTo(x + r[0], y);
		path.lineTo(right - r[1], y);
		path.curveTo(right - r[1] + k*r[1], y, right, y + r[1] - k*r[1], right, y + r[1]);
		path.lineTo(right, bottom - r[2]);
		path.curveTo(right, bottom - r[2] + k*r[2], right - r[2] + k*r[2], bottom, right - r[2], bottom);
		path.lineTo(x + r[3], bottom);
		path.curveTo(x + r[3] - k*r[3], bottom, x, bottom - r[3] + k*r[3], x, bottom - r[3]);
		path.lineTo(x, y + r[0]);
		path.curveTo(x, y + r[0] - k*r[0], x + r[0] - k*r[0], y, x + r[0], y);
		path.closePath();
		return path;
	}

}

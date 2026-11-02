/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import net.sf.mcf2pdf.mcfelements.McfBackground;
import net.sf.mcf2pdf.mcfelements.McfPage;
import net.sf.mcf2pdf.mcfelements.util.ImageUtil;


public class PageBackground implements PageDrawable {

	private static final String FLYLEAF_DESIGN_ELEMENT_ID = "99";

	private List<? extends McfBackground> leftBg;

	private List<? extends McfBackground> rightBg;

	public PageBackground(List<? extends McfBackground> leftBg,
			List<? extends McfBackground> rightBg) {
		this.leftBg = leftBg;
		this.rightBg = rightBg;
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
		Resolved left = resolveBackground(leftBg, context);
		Resolved right = resolveBackground(rightBg, context);

		BufferedImage img = new BufferedImage(widthPX, heightPX, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

		boolean legacyWhole = left != null && right != null && left.file != null && left.file.equals(right.file)
				&& left.bg.getAlignment() == 0 && right.bg.getAlignment() == 0;
		if (legacyWhole) {
			// no alignment information: draw the background image on whole page
			drawBackground(left, g2d, 0, 0, img.getWidth(), img.getHeight(), false, false);
		}
		else {
			if (left != null) {
				drawBackground(left, g2d, 0, 0, img.getWidth() / 2, img.getHeight(), true, false);
			}
			if (right != null) {
				drawBackground(right, g2d, img.getWidth() / 2, 0, img.getWidth() / 2, img.getHeight(), true, true);
			}
		}

		g2d.dispose();
		return img;
	}

	private void drawBackground(Resolved r, Graphics2D g2d, int x, int y, int width, int height,
			boolean useAlignment, boolean rightPage) throws IOException {
		if (r.file == null) {
			g2d.setColor(r.bg.getPage().getFotobook().getFlyleafColor());
			g2d.fillRect(x, y, width, height);
			return;
		}
		BufferedImage img = ImageUtil.readImage(r.file);
		if (img == null) {
			throw new IOException("Could not read image file: " + r.file.getAbsolutePath());
		}
		img = rotateQuarterTurns(img, r.bg.getRotation());
		if (isMulticolor(r.bg.getHue(), r.bg.getFading())) {
			img = recolor(img, r.bg.getHue(), r.bg.getFading());
		}

		int[] src = useAlignment
				? computeSourceRect(img.getWidth(), img.getHeight(), width, height, r.bg.getAlignment(), rightPage)
				: computeCenterCover(img.getWidth(), img.getHeight(), width, height);

		g2d.drawImage(img, x, y, x + width, y + height, src[0], src[1], src[0] + src[2], src[1] + src[3], null);
	}

	/** Centre cover-crop of the image to the target aspect ratio: {sx, sy, sw, sh}. */
	static int[] computeCenterCover(int imgW, int imgH, int tgtW, int tgtH) {
		float tgtRatio = tgtW / (float)tgtH;
		float imgRatio = imgW / (float)imgH;
		float scale;
		boolean xVar;
		if (imgRatio > tgtRatio) {
			scale = tgtH / (float)imgH;
			xVar = true;
		}
		else {
			scale = tgtW / (float)imgW;
			xVar = false;
		}
		int sx = (int)(xVar ? ((imgW - (tgtW / scale)) / 2) : 0);
		int sy = (int)(xVar ? 0 : ((imgH - (tgtH / scale)) / 2));
		return new int[] { sx, sy, (int)(tgtW / scale), (int)(tgtH / scale) };
	}

	/**
	 * CEWE crops the image to the aspect ratio of the whole spread and shows the left, centre or right half of
	 * that crop on a page. Alignment 4 (automatic) is left for the left page and right for the right page, 0 or
	 * unknown values are treated as centre.
	 *
	 * @return {sx, sy, sw, sh} in image pixels
	 */
	static int[] computeSourceRect(int imgW, int imgH, int pageW, int pageH, int alignment, boolean rightPage) {
		int[] spread = computeCenterCover(imgW, imgH, pageW * 2, pageH);
		int effective = alignment == 4 ? (rightPage ? 3 : 1) : alignment;
		int halfW = spread[2] / 2;
		int sx;
		switch (effective) {
		case 1:
			sx = spread[0];
			break;
		case 3:
			sx = spread[0] + spread[2] - halfW;
			break;
		default:
			sx = spread[0] + (spread[2] - halfW) / 2;
			break;
		}
		return new int[] { sx, spread[1], halfW, spread[3] };
	}

	/** hue and fading exist as a pair only on multicolor backgrounds; -1 means not multicolor. */
	static boolean isMulticolor(int hue, int fading) {
		return hue >= 0 && fading >= 0 && !(hue == 0 && fading == 0);
	}

	/** Rotates clockwise by multiples of 90 degrees; any other value is ignored like CEWE does. */
	static BufferedImage rotateQuarterTurns(BufferedImage image, int rotation) {
		int r = ((rotation % 360) + 360) % 360;
		if (r % 90 != 0 || r == 0) {
			return image;
		}
		int w = image.getWidth();
		int h = image.getHeight();
		boolean swap = r != 180;
		BufferedImage res = new BufferedImage(swap ? h : w, swap ? w : h, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int rgb = image.getRGB(x, y);
				switch (r) {
				case 90:
					res.setRGB(h - 1 - y, x, rgb);
					break;
				case 180:
					res.setRGB(w - 1 - x, h - 1 - y, rgb);
					break;
				default:
					res.setRGB(y, w - 1 - x, rgb);
					break;
				}
			}
		}
		return res;
	}

	/**
	 * CEWE multicolor recolouring: the hue of every pixel is replaced, saturation and value are kept. Fading 0-2
	 * shifts the saturation right by the fading value, fading 3-5 scales it so that the most saturated pixel ends
	 * at 180, 90 or 45 (of 255).
	 */
	static BufferedImage recolor(BufferedImage image, int hueDegrees, int fading) {
		int width = image.getWidth();
		int height = image.getHeight();
		float hue = (hueDegrees % 360) / 360.0f;
		int[] sat = new int[width * height];
		int[] val = new int[width * height];
		int[] alpha = new int[width * height];
		float[] hsb = new float[3];
		int maxS = 0;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int argb = image.getRGB(x, y);
				Color.RGBtoHSB((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, hsb);
				int i = y * width + x;
				sat[i] = Math.round(hsb[1] * 255);
				val[i] = Math.round(hsb[2] * 255);
				alpha[i] = argb >>> 24;
				maxS = Math.max(maxS, sat[i]);
			}
		}

		BufferedImage res = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = y * width + x;
				int s = fadeSaturation(sat[i], maxS, fading);
				int rgb = Color.HSBtoRGB(hue, s / 255.0f, val[i] / 255.0f) & 0x00ffffff;
				res.setRGB(x, y, (alpha[i] << 24) | rgb);
			}
		}
		return res;
	}

	static int fadeSaturation(int s, int maxS, int fading) {
		if (fading <= 2) {
			return s >> fading;
		}
		if (maxS <= 0) {
			return 0;
		}
		double factor = Math.pow(2, 3 - fading);
		return Math.min(255, (int)Math.floor(s * factor * 180 / maxS));
	}

	private static class Resolved {
		final File file;
		final McfBackground bg;

		Resolved(File file, McfBackground bg) {
			this.file = file;
			this.bg = bg;
		}
	}

	/** Backgrounds of layer type 1 are preferred, as CEWE only loads those; others are a fallback for old files. */
	private Resolved resolveBackground(List<? extends McfBackground> bgs, PageRenderContext context) {
		Resolved r = resolveBackground(bgs, context, true);
		return r != null ? r : resolveBackground(bgs, context, false);
	}

	private Resolved resolveBackground(List<? extends McfBackground> bgs, PageRenderContext context,
			boolean typeOneOnly) {
		for (McfBackground bg : bgs) {
			// layout="1" backgrounds are page layout schemas (e.g. fotobook_full_landscape_47), not pictures
			if (bg.getLayout() != 0) {
				continue;
			}
			if ((bg.getType() == 1) != typeOneOnly) {
				continue;
			}
			if (FLYLEAF_DESIGN_ELEMENT_ID.equals(bg.getDesignElementId())
					&& bg.getPage().getType() != null
					&& McfPage.EMPTY.matcher(bg.getPage().getType()).matches())
				return new Resolved(null, bg);
			String tn = bg.getTemplateName();
			String designElementId = bg.getDesignElementId();
			if (designElementId != null) {
				tn = designElementId;
			} else {
				if (tn == null || !tn.matches("[a-zA-Z0-9_]+,normal(,.*)?"))
					continue;
				tn = tn.substring(0, tn.indexOf(","));
			}
			File f = context.getBackgroundImage(tn);
			if (f == null) {
				f = context.getBackgroundColor(tn);
			}
			if (f == null)
				context.getLog().warn("Background not found for page " + bg.getPage().getPageNr() + ": " + tn);
			else
				return new Resolved(f, bg);
		}
		return null;
	}

	@Override
	public int getZPosition() {
		return 0;
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

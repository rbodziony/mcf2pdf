/*******************************************************************************
 * ${licenseText}
 * All rights reserved. This file is made available under the terms of the
 * Common Development and Distribution License (CDDL) v1.0 which accompanies
 * this distribution, and is available at
 * http://www.opensource.org/licenses/cddl1.txt
 *******************************************************************************/
package net.sf.mcf2pdf.pagebuild;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import net.sf.mcf2pdf.mcfelements.util.XslFoDocumentBuilder;

import org.jdom.Namespace;



public class BitmapPageBuilder extends AbstractPageBuilder {

	private File tempImageDir;

	private PageRenderContext context;

	private int widthPX;

	private int heightPX;

	/** Target output page size; -1 means "same as the rendered spread". */
	private int targetWidthPX = -1;

	private int targetHeightPX = -1;

	private boolean split;

	private boolean emitLeft = true;

	private boolean emitRight = true;

	private static final Comparator<PageDrawable> zComp = new Comparator<PageDrawable>() {
		@Override
		public int compare(PageDrawable p1, PageDrawable p2) {
			return p1.getZPosition() - p2.getZPosition();
		}
	};

	public BitmapPageBuilder(int widthPX, int heightPX,
			PageRenderContext context, File tempImageDir) throws IOException {
		this.widthPX = widthPX;
		this.heightPX = heightPX;
		this.context = context;
		this.tempImageDir = tempImageDir;
	}

	/**
	 * Fits the rendered spread onto an output page of the given size (keeping
	 * the aspect ratio, centered on a white page) instead of emitting it 1:1.
	 *
	 * @param targetWidthPX Output page width, in pixels.
	 * @param targetHeightPX Output page height, in pixels.
	 * @param split If <code>true</code>, the spread is cut in the middle and
	 * each half is placed on its own output page.
	 * @param emitLeft Whether to emit the left half (only used when splitting).
	 * @param emitRight Whether to emit the right half (only used when splitting).
	 */
	public void setOutputPage(int targetWidthPX, int targetHeightPX, boolean split,
			boolean emitLeft, boolean emitRight) {
		this.targetWidthPX = targetWidthPX;
		this.targetHeightPX = targetHeightPX;
		this.split = split;
		this.emitLeft = emitLeft;
		this.emitRight = emitRight;
	}

	@Override
	public void addToDocumentBuilder(XslFoDocumentBuilder docBuilder)
			throws IOException {
		// render drawables onto image, regardless of type
		List<PageDrawable> pageContents = new Vector<PageDrawable>(getDrawables());
		Collections.sort(pageContents, zComp);

		context.getLog().debug("Creating full page image from page elements");

		BufferedImage img = new BufferedImage(widthPX, heightPX, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, img.getWidth(), img.getHeight());

		for (PageDrawable pd : pageContents) {
			int left = context.toPixel(pd.getLeftMM());
			int top = context.toPixel(pd.getTopMM());

			Point offset = new Point();
			try {
				BufferedImage pdImg = pd.renderAsBitmap(context, offset, widthPX, heightPX);
				if (pdImg != null)
					g2d.drawImage(pdImg, left + offset.x, top + offset.y, null);
			}
			catch (FileNotFoundException e) {
				// the page is still rendered, but without this element
				context.getLog().warn("Skipping page element, " + e.getMessage());
			}
		}

		g2d.dispose();

		if (targetWidthPX <= 0 || targetHeightPX <= 0) {
			docBuilder.addPageElement(createImgFoElement(img, docBuilder.getNamespace()));
		}
		else if (!split) {
			docBuilder.addPageElement(createImgFoElement(fitOnPage(img), docBuilder.getNamespace()));
		}
		else {
			int half = img.getWidth() / 2;
			if (emitLeft) {
				docBuilder.addPageElement(createImgFoElement(
						fitOnPage(img.getSubimage(0, 0, half, img.getHeight())), docBuilder.getNamespace()));
			}
			if (emitRight) {
				docBuilder.addPageElement(createImgFoElement(
						fitOnPage(img.getSubimage(half, 0, img.getWidth() - half, img.getHeight())),
						docBuilder.getNamespace()));
			}
		}
	}

	/**
	 * Scales the given image to fit into the target page (keeping aspect ratio)
	 * and centers it on a white page of the target size.
	 */
	BufferedImage fitOnPage(BufferedImage src) {
		if (src.getWidth() == targetWidthPX && src.getHeight() == targetHeightPX) {
			return src;
		}
		double scale = Math.min(targetWidthPX / (double) src.getWidth(),
				targetHeightPX / (double) src.getHeight());
		int w = Math.max(1, (int) Math.round(src.getWidth() * scale));
		int h = Math.max(1, (int) Math.round(src.getHeight() * scale));

		BufferedImage page = new BufferedImage(targetWidthPX, targetHeightPX, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = page.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, targetWidthPX, targetHeightPX);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.drawImage(src, (targetWidthPX - w) / 2, (targetHeightPX - h) / 2, w, h, null);
		g.dispose();
		return page;
	}

	private String createImgFoElement(BufferedImage img, Namespace xslFoNs) throws IOException {
		// save bitmap to file
		File f;
		int i = 1;
		do {
			f = new File(tempImageDir, (i++) + ".jpg");
		}
		while (f.isFile());

		BufferedImage imgPlain = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = imgPlain.createGraphics();
		g2d.drawImage(img, 0, 0, null);
		g2d.dispose();

		ImageIO.write(imgPlain, "jpeg", f);
		String src = f.getAbsolutePath();
		f.deleteOnExit();

		return src;
	}


}

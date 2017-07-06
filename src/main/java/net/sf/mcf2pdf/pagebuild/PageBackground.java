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
import java.util.List;

import javax.imageio.ImageIO;

import net.sf.mcf2pdf.mcfelements.McfBackground;
import net.sf.mcf2pdf.mcfglobals.McfAlbumType;


public class PageBackground implements PageDrawable {

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
			Point drawOffsetPixels) throws IOException {
		File fLeft = extractBackground(leftBg, context);
		File fRight = extractBackground(rightBg, context);
		if(fLeft != null) {
			context.getLog().debug("leftBg="+leftBg.get(0).getTemplateName());
			context.getLog().debug("file for fLeft bg = "+fLeft.getAbsolutePath());
		}
		if(fRight != null) {
			context.getLog().debug("rightBg="+rightBg.get(0).getTemplateName());
			context.getLog().debug("file for fRight bg = "+fRight.getAbsolutePath());
		}
		McfAlbumType albumType = context.getAlbumType();

		float widthMM = (albumType.getUsableWidth() + albumType.getBleedMargin()) / 10.0f * 2;
		float heightMM = (albumType.getUsableHeight() + albumType.getBleedMargin() * 2) / 10.0f;

		BufferedImage img = new BufferedImage(context.toPixel(widthMM),
				context.toPixel(heightMM), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		int hue=1000;
		if (fLeft != null && fLeft.equals(fRight)) {
			//1st we checking if hue is 
			String templateName = leftBg.get(0).getTemplateName();
			if(templateName.contains(",hue")) {
				String hueinString = templateName.substring(templateName.indexOf("hue"),templateName.indexOf("fading")-1);
				hue = Integer.parseInt(hueinString.substring(hueinString.indexOf("=")+1,hueinString.length())); 
				context.getLog().debug("hueinString="+hueinString+" hue = " + hue);
				
			}
			// draw the background image on whole page
			drawBackground(fLeft, g2d, 0, 0, img.getWidth(), img.getHeight(),hue);
		}
		else {
			// process background parts separate
			if (fLeft != null)
				drawBackground(fLeft, g2d, 0, 0, img.getWidth() / 2, img.getHeight(),hue);
			if (fRight != null)
				drawBackground(fRight, g2d, img.getWidth() / 2, 0, img.getWidth() / 2, img.getHeight(),hue);
		}

		g2d.dispose();
		return img;
	}

	private void drawBackground(File f, Graphics2D g2d, int x, int y, int width, int height,int hue) throws IOException {
		
		BufferedImage img = null;
		if(hue==180 || hue==1000)
		 img = ImageIO.read(f);
		else
		{
			BufferedImage raw;
			 raw = ImageIO.read(f);
			 int WIDTH = raw.getWidth();
			 int HEIGHT = raw.getHeight();
			 img = new BufferedImage(WIDTH,HEIGHT,raw.getType());
			 float huef = hue/360.0f;
			 img = new BufferedImage(WIDTH,HEIGHT,raw.getType());

				 for(int Y=0; Y<HEIGHT;Y++)
			 {
			  for(int X=0;X<WIDTH;X++)
			  {
			   int RGB = raw.getRGB(X,Y);
			   int R = (RGB >> 16) & 0xff;
			   int G = (RGB >> 8) & 0xff;
			   int B = (RGB) & 0xff;
			   float HSV[]=new float[3];
			   Color.RGBtoHSB(R,G,B,HSV);
			   img.setRGB(X,Y,Color.getHSBColor(hue,HSV[1],HSV[2]).getRGB());
			   
			  }
			 }
		}
		float tgtRatio = width / (float)height;

		float imgRatio = img.getWidth() / (float)img.getHeight();
		float scale;
		boolean xVar;

		if (imgRatio > tgtRatio) {
			// scale image Y to target Y
			scale = height / (float)img.getHeight();
			xVar = true;
		}
		else {
			// scale image X to target X
			scale = width / (float)img.getWidth();
			xVar = false;
		}

		int sx = (int)(xVar ? ((img.getWidth() - (width / scale)) / 2) : 0);
		int sy = (int)(xVar ? 0 : ((img.getHeight() - (height / scale)) / 2));

		int sw = (int)(width / scale);
		int sh = (int)(height / scale);

		g2d.drawImage(img, x, y, x + width, y + height, sx, sy, sx + sw, sy + sh, null);
	}

	private File extractBackground(List<? extends McfBackground> bgs,
			PageRenderContext context) throws IOException {
		for (McfBackground bg : bgs) {
			String tn = bg.getTemplateName();
			for(McfBackground a :bg.getPage().getBackgrounds())
			 context.getLog().debug("bg="+a.getLayout()+" layout "+a.getTemplateName());
			if (tn == null )//|| !tn.matches("[a-zA-Z0-9_]+,normal(,.*)?"))
				continue;

			tn = tn.substring(0, tn.indexOf(","));
			 context.getLog().debug("tring to load background="+ tn);
			File f = context.getBackgroundImage(tn);
			if (f == null) {
				f = context.getBackgroundColor(tn);
			}
			if (f == null)
				context.getLog().warn("Background not found for page " + bg.getPage().getPageNr() + ": " + tn);
			else
				return f;
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

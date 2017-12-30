package net.sf.mcf2pdf.pagebuild;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.McfPage;
import net.sf.mcf2pdf.mcfelements.McfPageNum;



public class PageNum implements PageDrawable {
	
	private McfPageNum pageNum;
	private McfPage page;
	private String side;
	
	private int leftPX = -1;
	private int topPX = -1;
	private int pageWidth;
	private int pageHeight;
	
	public PageNum(McfFotobook book, McfPage page, String side, int pageWidth, int pageHeight) {
		this.pageNum = book.getPageNum();
		this.page = page;
		this.side = side;
		this.pageWidth = pageWidth;
		this.pageHeight = pageHeight;
		
		setPosition();
		
		FormattedTextParagraph para = new FormattedTextParagraph();
		para.addText(createFormattedText());
	}

	@Override
	public int getZPosition() {
		return 100000;
	}

	@Override
	public float getLeftMM() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public float getTopMM() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	private void setPosition() {
		int offsetXCenter = (side.equals("right")) ? Math.round(pageWidth / 2.0f) : 0;
		int offsetXOutside = (side.equals("right")) ? (pageWidth - 2 * pageNum.getHorizontalMargin()) : 0;
		switch (pageNum.getPosition()) {
			case 2: // top center
				topPX = pageNum.getVerticalMargin();
				leftPX = Math.round(pageWidth / 4.0f) + offsetXCenter;
				break;
			case 1: // top outside
				topPX = pageNum.getVerticalMargin();
				leftPX = pageNum.getHorizontalMargin() + offsetXOutside;
				break;
			case 5: // bottom center
				topPX = pageHeight - pageNum.getVerticalMargin();
				leftPX = Math.round(pageWidth / 4.0f) + offsetXCenter;
				break;
			case 4: // bottom outside
				topPX = pageHeight - pageNum.getVerticalMargin();
				leftPX = pageNum.getHorizontalMargin() + offsetXOutside;
				break;
		}
	}
	
	private FormattedText createFormattedText() {
		String text = pageNum.getTextString().replaceAll("%", Integer.toString(page.getPageNr()));
		Boolean bold = pageNum.getFBold();
		Boolean italics = pageNum.getFItalics();
		Color textColor = pageNum.getTextColor();
		String fontFamily = pageNum.getFontFamily();
		Float fontSize = pageNum.getFontSize();
		return new FormattedText(text, bold, italics, false, textColor, fontFamily, fontSize);
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
	public BufferedImage renderAsBitmap(PageRenderContext context, Point drawOffsetPixels, int widthPX, int heightPX)
			throws IOException {
		context.getLog().debug("Rendering pageNumber");
		
		
		BufferedImage tempImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D tempGraphics = tempImg.createGraphics();
		
		FontMetrics fm = tempGraphics.getFontMetrics();
		
		return null;
	}
}

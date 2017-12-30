package net.sf.mcf2pdf.pagebuild;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;

import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.McfPage;
import net.sf.mcf2pdf.mcfelements.McfPageNum;



public class PageNum implements PageDrawable {
	
	private McfPageNum pageNum;
	private McfPage leftPage;
	private McfPage rightPage;
	
	public PageNum(McfFotobook book, McfPage leftPage, McfPage rightPage) {
		this.pageNum = book.getPageNum();
		this.leftPage = leftPage;
		this.rightPage = rightPage;
	}

	@Override
	public int getZPosition() {
		// TODO Auto-generated method stub
		return 0;
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
	
	private String getTextString(McfPage page) {
		return pageNum.getTextString().replace("%", page.getPageNr() + "");
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
		// TODO Auto-generated method stub
		return null;
	}
}

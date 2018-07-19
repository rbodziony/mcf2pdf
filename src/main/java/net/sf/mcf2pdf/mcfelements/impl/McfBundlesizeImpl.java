package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfBundlesize;
import net.sf.mcf2pdf.mcfelements.McfPage;

public class McfBundlesizeImpl implements McfBundlesize {

	private McfPage page;
	private int height;
	private int width;
	
	@Override
	public McfPage getPage() {
		return page;
	}
	
	public void setPage(McfPage page) {
		this.page = page;
	}

	@Override
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}

}

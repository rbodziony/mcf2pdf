package net.sf.mcf2pdf.mcfelements.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.mcf2pdf.mcfelements.McfCorner;

public class McfCornerImpl implements McfCorner {

	private final static Log log = LogFactory.getLog(McfCornerImpl.class);
	int length = 0;
	private String shape = null;
	private String  where = null;

	@Override
	public int getLength() {
		return this.length;
	}
	
	public void setLength( int l ) {
		log.debug("setLength");
		this.length = l;
	}

	@Override
	public String getShape() {
		return this.shape;
	}

	public void setShape(String text) {
		log.debug("setShape");
	}

	@Override
	public String getWhere() {
		return this.where;
	}

	public void setWhere(String text) {
		log.debug("setWhere");
		this.where = text;
/*
 		if ("bottom-left".equals(text)) {

			position = where.bottom_left;
		} else if ("bottom-right".equals(text)) {
			position = where.bottom_right;
		} else if ("top-left".equals(text)) {
			position = where.top_left;
		} else if ("top-right".equals(text)) {
			position = where.top_right;
		} else {
			System.out.println("unknown where: " + text);
		}
		*/
	}

}

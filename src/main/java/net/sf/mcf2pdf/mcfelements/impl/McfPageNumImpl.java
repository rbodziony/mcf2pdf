package net.sf.mcf2pdf.mcfelements.impl;

import java.awt.Color;

import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.McfPageNum;

public class McfPageNumImpl implements McfPageNum {
	
	private McfFotobook fotobook;
	private String textString;
	private int position;
	private int verticalMargin;
	private int horizontalMargin;
	private Color textColor;
	private Color bgColor;
	private float fontSize;
	private int fontBold;
	private int fontItalics;
	private String fontFamily;
	private int format;
	
	public void setFotobook(McfFotobook fotobook) {
		this.fotobook = fotobook;
	}
	
	@Override
	public McfFotobook getFotobook() {
		return fotobook;
	}

	public void setTextString(String text) {
		this.textString = text;
	}
	
	@Override
	public String getTextString() {
		return textString;
	}
	
	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public int getPosition() {
		return position;
	}
	
	public void setVerticalMargin(int verticalMargin) {
		this.verticalMargin = verticalMargin;
	}
	
	@Override
	public int getVerticalMargin() {
		return verticalMargin;
	}
	
	public void setHorizontalMargin(int horizontalMargin) {
		this.horizontalMargin = horizontalMargin;
	}

	@Override
	public int getHorizontalMargin() {
		return horizontalMargin;
	}
	
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	@Override
	public Color getTextColor() {
		return textColor;
	}
	
	public void setBgColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	@Override
	public Color getBgColor() {
		return bgColor;
	}
	
	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	@Override
	public float getFontSize() {
		return fontSize;
	}
	
	public void setFontBold(int fontBold) {
		this.fontBold = fontBold;
	}

	@Override
	public int getFontBold() {
		return fontBold;
	}
	
	@Override
	public boolean getFBold() {
		return (fontBold == 1) ? true : false;
	}
	
	public void setFontItalics(int fontItalics) {
		this.fontItalics = fontItalics;
	}

	@Override
	public int getFontItalics() {
		return fontItalics;
	}
	
	@Override
	public boolean getFItalics() {
		return (fontItalics == 1) ? true : false;
	}
	
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	@Override
	public String getFontFamily() {
		return fontFamily;
	}
	
	public void setFormat(int format) {
		this.format = format;
	}

	@Override
	public int getFormat() {
		return format;
	}

}

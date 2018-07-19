package net.sf.mcf2pdf.mcfelements;

import java.awt.Color;

public interface McfPageNum {
	
	public McfFotobook getFotobook();
	
	public String getTextString();

	public int getPosition();
	
	public int getVerticalMargin();
	
	public int getHorizontalMargin();

	public Color getTextColor();
	
	public Color getBgColor();
	
	public float getFontSize();

	public int getFontBold();
	
	public boolean getFBold();
	
	public int getFontItalics();
	
	public boolean getFItalics();
	
	public String getFontFamily();
	
	public int getFormat();
	
}

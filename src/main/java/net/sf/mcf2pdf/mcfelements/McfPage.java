/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements;

import java.util.List;
import java.util.regex.Pattern;

/**
 * TODO comment
 */
public interface McfPage {

	public final static Pattern FULLCOVER = Pattern.compile("FULLCOVER|fullcover");

	public final static Pattern CONTENT = Pattern.compile("CONTENT|normalpage");

	/** Wall calendar covers: <code>calendercover*</code> (CEWE 6.x) and <code>calendarcover*</code> (7.x and newer). */
	public final static Pattern CALENDAR_COVER = Pattern.compile("calend[ae]rcover(back|front)");

	public final static Pattern EMPTY = Pattern.compile("EMPTY|emptypage");

	public final static Pattern SPINE = Pattern.compile("SPINE|spine");

	public McfFotobook getFotobook();
	
	public McfBundlesize getBundlesize();

	public List<? extends McfArea> getAreas();

	public List<? extends McfBackground> getBackgrounds();

	public int getPageNr();

	public String getType();

}
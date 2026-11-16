package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * How a CEWE program generation renders the old SVG month blocks. The same project looks different in
 * CEWE 5.x and 6.x, so the style follows the installation used for rendering.
 */
public enum LegacyCalendarStyle {

	/**
	 * CEWE 5.x: the colour sheet is followed by the template's StandardSheet, cells of neighbouring months
	 * stay visible without a date, and only Sundays get the Sunday style.
	 */
	CEWE5,

	/**
	 * CEWE 6.x and later: the colour sheet is followed by the colour stylesheet beside the template, days
	 * outside the month are hidden, and visible holidays get the Sunday style.
	 */
	CEWE6;

	private static final Pattern VERSION = Pattern.compile("<version\\s+number\\s*=\\s*\"(\\d+)\\.");

	/**
	 * Reads <code>Resources/version.xml</code> of a CEWE installation. CEWE 5.x and 6.x ship this file;
	 * newer versions do not, and are treated as {@link #CEWE6}.
	 */
	public static LegacyCalendarStyle fromResources(File resourcesDirectory) throws IOException {
		File file = new File(resourcesDirectory, "version.xml");
		if (!file.isFile()) {
			return CEWE6;
		}
		Matcher matcher = VERSION.matcher(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
		return matcher.find() && Integer.parseInt(matcher.group(1)) < 6 ? CEWE5 : CEWE6;
	}
}

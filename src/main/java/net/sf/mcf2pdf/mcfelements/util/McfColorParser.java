package net.sf.mcf2pdf.mcfelements.util;

import java.awt.Color;

/**
 * Parses the colour notations found in MCF files: <code>#RRGGBB</code>, <code>#AARRGGBB</code>,
 * <code>#RRGGBB,AA</code> (alpha as an extra hex suffix), plain decimal ARGB integers and empty values
 * such as <code>#</code>.
 */
public final class McfColorParser {

	private McfColorParser() {
	}

	/**
	 * @return the colour, or <code>null</code> if the value is empty (e.g. blank or just <code>#</code>)
	 *
	 * @throws NumberFormatException if the value is not empty and not a valid colour
	 */
	public static Color parse(String value) {
		if (value == null) {
			return null;
		}
		String v = value.trim();
		if (v.isEmpty() || v.equals("#")) {
			return null;
		}

		if (!v.startsWith("#")) {
			return fromArgb(Long.parseLong(v));
		}

		int alphaOverride = -1;
		int comma = v.indexOf(',');
		if (comma >= 0) {
			String alpha = v.substring(comma + 1).trim();
			if (!alpha.isEmpty()) {
				alphaOverride = Integer.parseInt(alpha, 16);
			}
			v = v.substring(0, comma);
		}

		String hex = v.substring(1);
		if (hex.isEmpty()) {
			return null;
		}

		long argb = Long.parseLong(hex, 16);
		if (hex.length() > 6) {
			return fromArgb(argb);
		}

		int rgb = (int)(argb & 0xFFFFFF);
		if (alphaOverride >= 0) {
			return new Color((alphaOverride << 24) | rgb, true);
		}
		return new Color(rgb);
	}

	private static Color fromArgb(long colorValue) {
		return new Color((int)(colorValue & 0xFFFFFFFFL), true);
	}
}

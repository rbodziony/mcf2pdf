package net.sf.mcf2pdf.pagebuild;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfCalendarArea;

/**
 * Draws the title parts of a wall calendar: the year (layout schemas starting with
 * <code>Year</code>) and the month name (<code>Month</code>, <code>Month_center</code>).
 * Day tables are not supported yet. The text size is derived from the height of the area,
 * which approximates the sizes shown by CEWE.
 */
public class PageCalendar implements PageDrawable {

	private static final DateTimeFormatter START_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private final McfArea area;

	private final McfCalendarArea calendar;

	private final String text;

	private final boolean centered;

	private final float heightFactor;

	private PageCalendar(McfArea area, McfCalendarArea calendar, String text, boolean centered, float heightFactor) {
		this.area = area;
		this.calendar = calendar;
		this.text = text;
		this.centered = centered;
		this.heightFactor = heightFactor;
	}

	/**
	 * Creates the drawable for a calendar area.
	 *
	 * @param startDate Value of <code>startdatecalendarium</code> (dd.MM.yyyy).
	 * @param language Language of the project, e.g. <code>pl_PL</code>; may be <code>null</code>.
	 * @param month First day of the month shown on the current sheet, <code>null</code> on the cover.
	 * @return The drawable, or <code>null</code> if the layout schema is not supported (yet).
	 */
	public static PageCalendar create(String startDate, String language, McfArea area, McfCalendarArea calendar,
			LocalDate month) {
		String schema = calendar.getLayoutSchema();
		if (schema == null) {
			return null;
		}
		if (schema.startsWith("Year")) {
			LocalDate start = parse(startDate);
			return start == null ? null
					: new PageCalendar(area, calendar, String.valueOf(start.getYear()), true, 0.42f);
		}
		if (("Month".equals(schema) || "Month_center".equals(schema)) && month != null) {
			Locale locale = language == null || language.isEmpty() ? Locale.getDefault()
					: Locale.forLanguageTag(language.replace('_', '-'));
			String name = month.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, locale);
			name = name.substring(0, 1).toUpperCase(locale) + name.substring(1);
			return new PageCalendar(area, calendar, name, "Month_center".equals(schema), 0.58f);
		}
		return null;
	}

	/** Parses <code>startdatecalendarium</code>; <code>null</code> if missing or malformed. */
	public static LocalDate parse(String startDate) {
		try {
			return startDate == null ? null : LocalDate.parse(startDate, START_DATE);
		}
		catch (java.time.format.DateTimeParseException e) {
			return null;
		}
	}

	String getText() {
		return text;
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
	public BufferedImage renderAsBitmap(PageRenderContext context, Point drawOffsetPixels, int widthPX, int heightPX)
			throws IOException {
		int w = Math.max(1, context.toPixel(area.getWidth() / 10.0f));
		int h = Math.max(1, context.toPixel(area.getHeight() / 10.0f));
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		Font base = calendar.getFont() == null ? null : context.getFont(calendar.getFont());
		// the scanner keeps one font per family and may hand out an italic face; titles are upright
		if (base == null || base.getFontName().toLowerCase(Locale.ROOT).contains("italic")) {
			base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
		}
		Font font = base.deriveFont(Font.BOLD, h * heightFactor);
		g.setFont(font);
		g.setColor(Color.BLACK);

		FontRenderContext frc = g.getFontRenderContext();
		LineMetrics lm = font.getLineMetrics(text, frc);
		float textWidth = (float) font.getStringBounds(text, frc).getWidth();
		float x = centered ? (w - textWidth) / 2 : 0;
		float y = (h - lm.getHeight()) / 2 + lm.getAscent();
		g.drawString(text, x, y);
		g.dispose();
		return img;
	}

	@Override
	public int getZPosition() {
		return area.getZPosition();
	}

	@Override
	public float getLeftMM() {
		return area.getLeft() / 10.0f;
	}

	@Override
	public float getTopMM() {
		return area.getTop() / 10.0f;
	}

}

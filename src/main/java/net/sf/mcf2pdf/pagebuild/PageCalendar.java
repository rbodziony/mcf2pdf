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
import java.util.Map;
import java.time.LocalDate;
import java.util.Locale;

import net.sf.mcf2pdf.calendar.CalendarColorCatalogue;
import net.sf.mcf2pdf.calendar.CalendarDateModel;
import net.sf.mcf2pdf.calendar.CalendarLayoutCatalogue;
import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfCalendarColorSchema;
import net.sf.mcf2pdf.mcfelements.McfCalendarArea;
import net.sf.mcf2pdf.mcfelements.util.McfColorParser;

/**
 * Draws the title parts of a wall calendar: the year (layout schemas starting with
 * <code>Year</code>) and the month name (<code>Month</code>, <code>Month_center</code>).
 * Day tables are not supported yet. The text size is derived from the height of the area,
 * which approximates the sizes shown by CEWE.
 */
public class PageCalendar implements PageDrawable {

	private final McfArea area;

	private final McfCalendarArea calendar;

	private final String text;

	private final String textAlignment;

	private final float heightFactor;

	private final String fontFamily;

	private final boolean bold;

	private final float fontSizePoints;

	private final String textColor;

	private final CalendarLayoutCatalogue.Cell layoutCell;

	private PageCalendar(McfArea area, McfCalendarArea calendar, String text, String textAlignment, float heightFactor,
			String fontFamily, boolean bold, float fontSizePoints, String textColor,
			CalendarLayoutCatalogue.Cell layoutCell) {
		this.area = area;
		this.calendar = calendar;
		this.text = text;
		this.textAlignment = textAlignment;
		this.heightFactor = heightFactor;
		this.fontFamily = fontFamily;
		this.bold = bold;
		this.fontSizePoints = fontSizePoints;
		this.textColor = textColor;
		this.layoutCell = layoutCell;
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
		return create(startDate, language, area, calendar, month, null, null);
	}

	public static PageCalendar create(String startDate, String language, McfArea area, McfCalendarArea calendar,
			LocalDate month, CalendarLayoutCatalogue layouts, CalendarColorCatalogue colors) {
		return create(startDate, language, area, calendar, month, layouts, colors,
				java.util.Collections.<McfCalendarColorSchema>emptyList());
	}

	public static PageCalendar create(String startDate, String language, McfArea area, McfCalendarArea calendar,
			LocalDate month, CalendarLayoutCatalogue layouts, CalendarColorCatalogue colors,
			java.util.Collection<? extends McfCalendarColorSchema> userColorSchemas) {
		String schema = calendar.getLayoutSchema();
		if (schema == null) {
			return null;
		}
		if (schema.startsWith("Year")) {
			LocalDate start = parse(startDate);
			return start == null ? null
					: createTitle(area, calendar, String.valueOf(start.getYear()), "CALENDAR_CELL_TYPE_YEAR",
							"AlignVCenter,AlignHCenter", 0.42f, layouts, colors, userColorSchemas);
		}
		if (("Month".equals(schema) || "Month_center".equals(schema)) && month != null) {
			String name = CalendarDateModel.monthName(month.getMonth(), language);
			String defaultAlignment = "Month_center".equals(schema) ? "AlignTop,AlignHCenter" : "AlignTop,AlignLeft";
			return createTitle(area, calendar, name, "CALENDAR_CELL_TYPE_MONTH", defaultAlignment, 0.58f,
					layouts, colors, userColorSchemas);
		}
		return null;
	}

	private static PageCalendar createTitle(McfArea area, McfCalendarArea calendar, String text, String cellType,
			String defaultAlignment, float heightFactor, CalendarLayoutCatalogue layouts,
			CalendarColorCatalogue colors, java.util.Collection<? extends McfCalendarColorSchema> userColorSchemas) {
		CalendarLayoutCatalogue.Layout layout = layouts == null ? null : layouts.getLayout(calendar.getLayoutSchema());
		CalendarLayoutCatalogue.Cell cell = layout == null ? null : layout.getCell(cellType);
		Map<String, String> cellAttributes = cell == null ? null : cell.getAttributes();
		Map<String, String> fontAttributes = cell == null ? null : cell.getFontAttributes();
		String alignment = cellAttributes == null ? defaultAlignment
				: cellAttributes.getOrDefault("textalign", defaultAlignment);
		String fontFamily = fontAttributes == null ? calendar.getFont() : fontAttributes.get("face");
		if (fontFamily == null) {
			fontFamily = calendar.getFont();
		}
		boolean bold = fontAttributes == null || Boolean.parseBoolean(fontAttributes.get("bold"));
		float fontSize = parseFloat(fontAttributes == null ? null : fontAttributes.get("size"));
		if (!Float.isNaN(fontSize)) {
			fontSize *= (100 + calendar.getFontSizePercentage()) / 100.0f;
		}

		String textColor = null;
		if (colors != null && calendar.getColorSchema() != null) {
			CalendarColorCatalogue.ColorSchema colorSchema = colors.getSchema(calendar.getColorSchema(),
					userColorSchemas);
			Map<String, String> colorAttributes = colorSchema == null ? null : colorSchema.getCell(cellType);
			if (colorAttributes != null) {
				textColor = colorAttributes.get("textcolor");
			}
		}
		return new PageCalendar(area, calendar, text, alignment, heightFactor, fontFamily, bold, fontSize, textColor,
				cell);
	}

	private static float parseFloat(String value) {
		if (value == null || value.isEmpty()) {
			return Float.NaN;
		}
		try {
			return Float.parseFloat(value);
		}
		catch (NumberFormatException e) {
			return Float.NaN;
		}
	}

	/** Parses <code>startdatecalendarium</code>; <code>null</code> if missing or malformed. */
	public static LocalDate parse(String startDate) {
		return CalendarDateModel.parseStartDate(startDate);
	}

	String getText() {
		return text;
	}

	String getTextAlignment() {
		return textAlignment;
	}

	String getFontFamily() {
		return fontFamily;
	}

	boolean isBold() {
		return bold;
	}

	float getFontSizePoints() {
		return fontSizePoints;
	}

	String getTextColor() {
		return textColor;
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

		Font base = fontFamily == null ? null : context.getFont(fontFamily, bold);
		if (base == null || base.getFontName().toLowerCase(Locale.ROOT).contains("italic")) {
			base = new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, 12);
		}
		float fontSize = Float.isNaN(fontSizePoints)
				? h * heightFactor
				: context.toPixel(fontSizePoints * 25.4f / 72.0f);
		Font font = base.deriveFont(Math.max(1f, fontSize));
		g.setFont(font);
		g.setColor(resolveTextColor(context));

		FontRenderContext frc = g.getFontRenderContext();
		LineMetrics lm = font.getLineMetrics(text, frc);
		float textWidth = (float) font.getStringBounds(text, frc).getWidth();
		float left = paddingPixels(context, "paddingleft");
		float right = paddingPixels(context, "paddingright");
		float top = paddingPixels(context, "paddingtop");
		float bottom = paddingPixels(context, "paddingbottom");
		float x = textAlignment.contains("AlignHCenter") ? left + (w - left - right - textWidth) / 2
				: textAlignment.contains("AlignRight") ? w - right - textWidth : left;
		float y = textAlignment.contains("AlignTop") ? top + lm.getAscent()
				: textAlignment.contains("AlignBottom") ? h - bottom - lm.getDescent()
				: top + (h - top - bottom - lm.getHeight()) / 2 + lm.getAscent();
		g.drawString(text, x, y);
		g.dispose();
		return img;
	}

	private Color resolveTextColor(PageRenderContext context) {
		if (textColor == null || textColor.isEmpty()) {
			return Color.BLACK;
		}
		if ("transparent".equalsIgnoreCase(textColor)) {
			return new Color(0, 0, 0, 0);
		}
		try {
			Color parsed = McfColorParser.parse(textColor);
			return parsed == null ? Color.BLACK : parsed;
		}
		catch (NumberFormatException e) {
			context.getLog().warn("Invalid calendar text color '" + textColor + "'", e);
			return Color.BLACK;
		}
	}

	private float paddingPixels(PageRenderContext context, String attribute) {
		if (layoutCell == null) {
			return 0;
		}
		float padding = parseFloat(layoutCell.getAttributes().get(attribute));
		return Float.isNaN(padding) ? 0 : context.toPixel(padding / 10.0f);
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

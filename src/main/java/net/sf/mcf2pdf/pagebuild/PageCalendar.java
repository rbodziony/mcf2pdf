package net.sf.mcf2pdf.pagebuild;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	private final CalendarDateModel.CalendarMonth dateMonth;

	private final String language;

	private final CalendarLayoutCatalogue.Layout dayTableLayout;

	private final CalendarColorCatalogue.ColorSchema dayTableColors;

	private PageCalendar(McfArea area, McfCalendarArea calendar, String text, String textAlignment, float heightFactor,
			String fontFamily, boolean bold, float fontSizePoints, String textColor,
			CalendarLayoutCatalogue.Cell layoutCell, CalendarDateModel.CalendarMonth dateMonth, String language,
			CalendarLayoutCatalogue.Layout dayTableLayout, CalendarColorCatalogue.ColorSchema dayTableColors) {
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
		this.dateMonth = dateMonth;
		this.language = language;
		this.dayTableLayout = dayTableLayout;
		this.dayTableColors = dayTableColors;
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

	public static PageCalendar createFromDateModel(String startDate, String language, McfArea area,
			McfCalendarArea calendar, CalendarDateModel.CalendarMonth month, CalendarLayoutCatalogue layouts,
			CalendarColorCatalogue colors, java.util.Collection<? extends McfCalendarColorSchema> userColorSchemas) {
		if (calendar == null) {
			return null;
		}
		if (month != null && "OneDayPerRow (1)".equals(calendar.getLayoutSchema())) {
			CalendarLayoutCatalogue.Layout layout = layouts == null ? null : layouts.getLayout(calendar.getLayoutSchema());
			if (layout == null) {
				return null;
			}
			java.util.Collection<? extends McfCalendarColorSchema> userSchemas = userColorSchemas == null
					? java.util.Collections.<McfCalendarColorSchema>emptyList() : userColorSchemas;
			CalendarColorCatalogue.ColorSchema colorSchema = colors == null ? null
					: colors.getSchema(calendar.getColorSchema(), userSchemas);
			return new PageCalendar(area, calendar, null, "", 0, calendar.getFont(), false, Float.NaN, null, null,
					month, language, layout, colorSchema);
		}
		return create(startDate, language, area, calendar, month == null ? null : month.getFirstDay(), layouts, colors,
				userColorSchemas);
	}

	public static boolean isLayoutSupported(String schema) {
		return schema != null && (schema.startsWith("Year") || "Month".equals(schema)
				|| "Month_center".equals(schema) || "OneDayPerRow (1)".equals(schema));
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
				cell, null, null, null, null);
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
		if (dateMonth != null) {
			renderDayTable(g, context, w, h);
			g.dispose();
			return img;
		}

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

	private void renderDayTable(Graphics2D g, PageRenderContext context, int width, int height) {
		List<CalendarDateModel.CalendarDay> days = dateMonth.getDays();
		if (days.isEmpty()) {
			return;
		}

		int weekWidth = Math.round(width * 0.11f);
		int dateWidth = Math.round(width * 0.12f);
		int weekdayWidth = Math.round(width * 0.15f);
		float rowHeight = (float) height / days.size();
		renderWeekNumbers(g, context, days, weekWidth, rowHeight);

		for (int i = 0; i < days.size(); i++) {
			CalendarDateModel.CalendarDay day = days.get(i);
			int y = Math.round(i * rowHeight);
			int nextY = Math.round((i + 1) * rowHeight);
			int cellHeight = Math.max(1, nextY - y);
			String dayType = dayType(day);
			String headType = headType(day);
			String dateType = i == 0 ? "CALENDAR_CELL_TYPE_TOPLINE" : dayType;
			Map<String, String> dateColors = colorAttributes(dateType);
			Map<String, String> headColors = colorAttributes(headType);
			Map<String, String> commentColors = colorAttributes("CALENDAR_CELL_TYPE_COMMENT");
			if (!commentColors.containsKey("textcolor")) {
				commentColors = headColors;
			}

			fillCell(g, context, dateColors.get("bgcolor"), weekWidth, y, width - weekWidth, cellHeight);
			fillCell(g, context, headColors.get("bgcolor"), weekWidth + dateWidth, y, weekdayWidth, cellHeight);

			boolean bold = day.isSunday() || day.isHoliday();
			drawCellText(g, context, Integer.toString(day.getDate().getDayOfMonth()), dateType, dateColors,
					weekWidth, y, dateWidth, cellHeight, 0, bold, 13f, true);
			drawCellText(g, context, day.getWeekdayAbbreviation(language), headType, headColors,
					weekWidth + dateWidth, y, weekdayWidth, cellHeight, 0, bold, 13f, false);

			String description = dayDescription(day);
			if (!description.isEmpty()) {
				drawCellText(g, context, description, "CALENDAR_CELL_TYPE_COMMENT", commentColors,
						weekWidth + dateWidth + weekdayWidth, y,
						width - weekWidth - dateWidth - weekdayWidth, cellHeight,
						calendar.getFontSizePercentageHolidays(), false, 13f, false);
			}

			drawBottomBorder(g, context, dateType, dateColors, weekWidth, y + cellHeight - 1, width - weekWidth);
		}
	}

	private void renderWeekNumbers(Graphics2D g, PageRenderContext context,
			List<CalendarDateModel.CalendarDay> days, int width, float rowHeight) {
		Map<String, String> colors = colorAttributes("CALENDAR_CELL_TYPE_WEEK_OF_YEAR");
		CalendarLayoutCatalogue.Cell cell = dayTableLayout.getCell("CALENDAR_CELL_TYPE_WEEK_OF_YEAR");
		for (int first = 0; first < days.size();) {
			CalendarDateModel.CalendarDay firstDay = days.get(first);
			int end = first + 1;
			while (end < days.size() && days.get(end).getIsoWeekNumber() == firstDay.getIsoWeekNumber()
					&& days.get(end).getIsoWeekBasedYear() == firstDay.getIsoWeekBasedYear()) {
				end++;
			}
			float top = first * rowHeight;
			float bottom = end * rowHeight;
			int y = Math.round(top);
			int h = Math.max(1, Math.round(bottom) - y);
			fillCell(g, context, colors.get("bgcolor"), 0, y, width, h);

			String prefix = "pl".equals(parseLanguage(language).getLanguage()) ? "Tyd " : "Wk ";
			String label = prefix + firstDay.getIsoWeekNumber();
			boolean bold = cell != null && Boolean.parseBoolean(cell.getFontAttributes().get("bold"));
			Font font = tableFont(context, cell, 0, bold, 6f);
			float maxTextWidth = Math.max(1, h - context.toPixel(1f));
			float textWidth = (float) font.getStringBounds(label, g.getFontRenderContext()).getWidth();
			if (textWidth > maxTextWidth) {
				font = font.deriveFont(Math.max(1f, font.getSize2D() * maxTextWidth / textWidth));
			}
			g.setFont(font);
			g.setColor(resolveColor(context, colors.get("textcolor"), Color.BLACK));
			float centerX = width / 2f;
			float centerY = y + h / 2f;
			LineMetrics metrics = font.getLineMetrics(label, g.getFontRenderContext());
			textWidth = (float) font.getStringBounds(label, g.getFontRenderContext()).getWidth();
			AffineTransform original = g.getTransform();
			g.rotate(-Math.PI / 2, centerX, centerY);
			g.drawString(label, centerX - textWidth / 2,
					centerY + (metrics.getAscent() - metrics.getDescent()) / 2);
			g.setTransform(original);
			first = end;
		}
	}

	private void drawCellText(Graphics2D g, PageRenderContext context, String value, String type,
			Map<String, String> colors, int x, int y, int width, int height, int extraFontPercentage,
			boolean bold, float defaultSize, boolean alignRight) {
		CalendarLayoutCatalogue.Cell cell = dayTableLayout.getCell(type);
		Font font = tableFont(context, cell, extraFontPercentage, bold, defaultSize);
		g.setFont(font);
		g.setColor(resolveColor(context, colors.get("textcolor"), Color.BLACK));

		float left = paddingPixels(context, cell, "paddingleft");
		float right = paddingPixels(context, cell, "paddingright");
		float textWidth = (float) font.getStringBounds(value, g.getFontRenderContext()).getWidth();
		String alignment = cell == null ? "" : cell.getAttributes().getOrDefault("textalign", "");
		float textX = alignment.contains("AlignHCenter") ? x + left + (width - left - right - textWidth) / 2
				: alignRight || alignment.contains("AlignRight") ? x + width - right - textWidth : x + left;
		LineMetrics metrics = font.getLineMetrics(value, g.getFontRenderContext());
		float textY = y + (height - metrics.getHeight()) / 2f + metrics.getAscent();
		g.drawString(value, textX, textY);
	}

	private Font tableFont(PageRenderContext context, CalendarLayoutCatalogue.Cell cell, int extraPercentage,
			boolean forceBold, float defaultSize) {
		Map<String, String> attributes = cell == null ? java.util.Collections.<String, String>emptyMap()
				: cell.getFontAttributes();
		String family = attributes.get("face");
		if (family == null || family.isEmpty()) {
			family = calendar.getFont();
		}
		boolean bold = forceBold || Boolean.parseBoolean(attributes.get("bold"));
		Font base = family == null ? null : context.getFont(family, bold);
		if (base == null || base.getFontName().toLowerCase(Locale.ROOT).contains("italic")) {
			base = new Font(Font.SANS_SERIF, bold ? Font.BOLD : Font.PLAIN, 12);
		}
		float size = parseFloat(attributes.get("size"));
		if (Float.isNaN(size)) {
			size = defaultSize;
		}
		size *= (100 + calendar.getFontSizePercentage()) / 100f;
		size *= (100 + extraPercentage) / 100f;
		float pixels = context.toPixel(size * 25.4f / 72.0f);
		return base.deriveFont(Math.max(1f, pixels));
	}

	private void drawBottomBorder(Graphics2D g, PageRenderContext context, String type,
			Map<String, String> colors, int x, int y, int width) {
		CalendarLayoutCatalogue.Cell cell = dayTableLayout.getCell(type);
		String borderType = cell == null ? null : cell.getAttributes().get("bordertype");
		if (borderType == null || !borderType.contains("BOTTOM")) {
			return;
		}
		String borderColor = colors.get("bordercolor");
		if (borderColor == null || borderColor.isEmpty() || "transparent".equalsIgnoreCase(borderColor)) {
			borderColor = dayTableColors == null ? null : dayTableColors.getGridColor();
		}
		Color resolved = resolveColor(context, borderColor, null);
		if (resolved != null && resolved.getAlpha() > 0) {
			g.setColor(resolved);
			g.drawLine(x, y, x + width - 1, y);
		}
	}

	private void fillCell(Graphics2D g, PageRenderContext context, String colorValue,
			int x, int y, int width, int height) {
		Color color = resolveColor(context, colorValue, null);
		if (color != null && color.getAlpha() > 0 && width > 0 && height > 0) {
			g.setColor(color);
			g.fillRect(x, y, width, height);
		}
	}

	private Map<String, String> colorAttributes(String type) {
		return dayTableColors == null ? java.util.Collections.<String, String>emptyMap()
				: dayTableColors.getCell(type);
	}

	private String dayDescription(CalendarDateModel.CalendarDay day) {
		Set<String> descriptions = new LinkedHashSet<String>();
		String showHoliday = calendar.getShowHoliday();
		boolean includeHolidays = showHoliday == null
				|| (!"0".equals(showHoliday.trim()) && !"false".equalsIgnoreCase(showHoliday.trim()));
		if (includeHolidays) {
			for (CalendarDateModel.HolidayOccurrence holiday : day.getHolidays()) {
				addDescription(descriptions, holiday.isVisible() ? holiday.getName() : null);
			}
		}
		for (net.sf.mcf2pdf.mcfelements.McfCalendarEvent event : day.getEvents()) {
			addDescription(descriptions, event.getName());
		}
		return String.join(", ", new ArrayList<String>(descriptions));
	}

	private static void addDescription(Set<String> descriptions, String value) {
		if (value != null && !value.trim().isEmpty()) {
			descriptions.add(value.trim());
		}
	}

	private static String dayType(CalendarDateModel.CalendarDay day) {
		if (day.isSunday()) {
			return "CALENDAR_CELL_TYPE_SUNDAY";
		}
		if (day.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
			return "CALENDAR_CELL_TYPE_SATURDAY";
		}
		return "CALENDAR_CELL_TYPE_WEEKDAY";
	}

	private static String headType(CalendarDateModel.CalendarDay day) {
		if (day.isSunday()) {
			return "CALENDAR_CELL_TYPE_HEAD_SUNDAY";
		}
		if (day.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
			return "CALENDAR_CELL_TYPE_HEAD_SATURDAY";
		}
		return "CALENDAR_CELL_TYPE_HEAD_WEEKDAY";
	}

	private static Locale parseLanguage(String language) {
		return language == null || language.trim().isEmpty() ? Locale.getDefault()
				: Locale.forLanguageTag(language.replace('_', '-'));
	}

	private float paddingPixels(PageRenderContext context, CalendarLayoutCatalogue.Cell cell, String attribute) {
		if (cell == null) {
			return 0;
		}
		float padding = parseFloat(cell.getAttributes().get(attribute));
		return Float.isNaN(padding) ? 0 : context.toPixel(padding / 10.0f);
	}

	private Color resolveColor(PageRenderContext context, String value, Color fallback) {
		if (value == null || value.isEmpty()) {
			return fallback;
		}
		if ("transparent".equalsIgnoreCase(value)) {
			return new Color(0, 0, 0, 0);
		}
		try {
			Color parsed = McfColorParser.parse(value);
			return parsed == null ? fallback : parsed;
		}
		catch (NumberFormatException e) {
			context.getLog().warn("Invalid calendar color '" + value + "'", e);
			return fallback;
		}
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

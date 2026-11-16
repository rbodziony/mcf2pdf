package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum LegacyCalendarStyle {

	CEWE5,

	CEWE6;

	private static final Pattern VERSION = Pattern.compile("<version\\s+number\\s*=\\s*\"(\\d+)\\.");

	public static LegacyCalendarStyle fromResources(File resourcesDirectory) throws IOException {
		File file = new File(resourcesDirectory, "version.xml");
		if (!file.isFile()) {
			return CEWE6;
		}
		Matcher matcher = VERSION.matcher(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
		return matcher.find() && Integer.parseInt(matcher.group(1)) < 6 ? CEWE5 : CEWE6;
	}
}

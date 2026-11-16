package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;

public enum LegacyCalendarStyle {

	CEWE5,

	CEWE6;

	public static LegacyCalendarStyle fromResources(File resourcesDirectory) throws IOException {
		int[] version = CeweInstallation.version(resourcesDirectory);
		return version != null && version[0] < 6 ? CEWE5 : CEWE6;
	}
}

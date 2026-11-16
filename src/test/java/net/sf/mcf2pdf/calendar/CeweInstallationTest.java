package net.sf.mcf2pdf.calendar;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CeweInstallationTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	@Test
	public void readsVersionAndActiveKeyAccountAndLocatesDownloadedResources() throws Exception {
		File resources = tmp.newFolder("Resources");
		File config = new File(resources, "config");
		assertTrue(config.mkdir());
		write(config, "version.xml", "<config> <version number=\"6.4.3\"/> </config>");
		write(config, "keyaccount.xml", "<config><!--\n<keyAccount>1111</keyAccount>\n-->"
				+ "<keyAccount>29241</keyAccount></config>");
		File hps = tmp.newFolder("hps");

		assertArrayEquals(new int[] { 6, 4, 3 }, CeweInstallation.version(resources));
		assertEquals("29241", CeweInstallation.keyAccount(resources));
		assertNull(CeweInstallation.writableResources(resources, hps));
		File downloaded = new File(hps, "29241/06.04.03/resources");
		assertTrue(downloaded.mkdirs());
		assertEquals(downloaded, CeweInstallation.writableResources(resources, hps));
		assertNull(CeweInstallation.writableResources(resources, null));
		assertEquals(LegacyCalendarStyle.CEWE6, LegacyCalendarStyle.fromResources(resources));
	}

	@Test
	public void downloadedCatalogueWinsUnlessTheInstalledOneIsNewer() throws Exception {
		File resources = calendarResources("Resources");
		File writable = tmp.newFolder("writable");
		File installedEntries = new File(resources, "calendarentries");
		File writableEntries = new File(writable, "calendarentries");
		assertTrue(installedEntries.mkdir() && writableEntries.mkdir());
		File installed = write(installedEntries, "pl_PL.xml", catalogue("1. Boże Narodzenie"));
		File downloaded = write(writableEntries, "pl_PL.xml", catalogue("Boże Narodzenie"));
		assertTrue(installed.setLastModified(1556092067000L));
		assertTrue(downloaded.setLastModified(1794653509000L));

		CalendarResources byLanguage = CalendarResources.load(resources, writable, "pl_PL", null, null);
		assertEquals(downloaded, byLanguage.getHolidayFile());
		assertEquals("Boże Narodzenie", byLanguage.getHolidays().getHolidays().get(0).getAttribute("name"));
		assertEquals(downloaded, CalendarResources.load(resources, writable, null, "Polska", null).getHolidayFile());

		assertTrue(installed.setLastModified(1794653510000L));
		assertEquals(installed, CalendarResources.load(resources, writable, "pl_PL", null, null).getHolidayFile());
		assertEquals(installed, CalendarResources.load(resources, null, "pl_PL", null, null).getHolidayFile());
	}

	private File calendarResources(String name) throws IOException {
		File resources = tmp.newFolder(name);
		write(resources, "calendar_layout.xml", "<calendar_layout/>");
		write(resources, "calendar_schema.xml", "<calendar_schema/>");
		write(resources, "calendar_synonym.xml", "<synonyms/>");
		return resources;
	}

	private static String catalogue(String christmas) {
		return "<holidays><country iso=\"Polska\" locale=\"pl_PL\"><nationwide>"
				+ "<day id=\"16\" honor=\"free\" name=\"" + christmas + "\"/>"
				+ "</nationwide></country></holidays>";
	}

	private static File write(File directory, String name, String contents) throws IOException {
		File file = new File(directory, name);
		Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
		return file;
	}
}

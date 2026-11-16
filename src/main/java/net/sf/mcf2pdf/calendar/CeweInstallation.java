package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CeweInstallation {

	private static final Pattern VERSION = Pattern.compile("<version\\s+number\\s*=\\s*\"(\\d+)\\.(\\d+)\\.(\\d+)");
	private static final Pattern KEY_ACCOUNT = Pattern.compile("<keyAccount>\\s*(\\d+)\\s*</keyAccount>");

	private CeweInstallation() {
	}

	public static int[] version(File resourcesDirectory) throws IOException {
		for (File file : new File[] { new File(resourcesDirectory, "version.xml"),
				new File(new File(resourcesDirectory, "config"), "version.xml") }) {
			if (file.isFile()) {
				Matcher matcher = VERSION.matcher(read(file));
				if (matcher.find()) {
					return new int[] { Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)),
							Integer.parseInt(matcher.group(3)) };
				}
			}
		}
		return null;
	}

	public static String keyAccount(File resourcesDirectory) throws IOException {
		File file = new File(new File(resourcesDirectory, "config"), "keyaccount.xml");
		if (!file.isFile()) {
			return null;
		}
		Matcher matcher = KEY_ACCOUNT.matcher(read(file).replaceAll("(?s)<!--.*?-->", ""));
		return matcher.find() ? matcher.group(1) : null;
	}

	public static File writableResources(File resourcesDirectory, File hpsDirectory) throws IOException {
		if (hpsDirectory == null) {
			return null;
		}
		int[] version = version(resourcesDirectory);
		String keyAccount = keyAccount(resourcesDirectory);
		if (version == null || keyAccount == null) {
			return null;
		}
		File directory = new File(new File(new File(hpsDirectory, keyAccount),
				String.format("%02d.%02d.%02d", version[0], version[1], version[2])), "resources");
		return directory.isDirectory() ? directory : null;
	}

	public static boolean usesLocalAppDataHps(int[] version) {
		return version != null && (version[0] > 7 || version[0] == 7 && version[1] >= 2);
	}

	public static File mostCurrent(File installed, File writable) {
		if (writable != null && writable.isFile()
				&& !(installed.isFile() && installed.lastModified() > writable.lastModified())) {
			return writable;
		}
		return installed;
	}

	private static String read(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}
}

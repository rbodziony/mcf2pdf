package net.sf.mcf2pdf.pagebuild;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * Maps the colour names used by old MCF files (e.g. <code>Weiss</code>) to the design element IDs of the
 * corresponding single-colour backgrounds.
 */
public final class LegacyBackgroundNames {

	private static final Properties NAMES = load();

	private LegacyBackgroundNames() {
	}

	private static Properties load() {
		Properties props = new Properties();
		try (InputStream in = LegacyBackgroundNames.class.getResourceAsStream("/legacy-background-names.properties")) {
			if (in != null) {
				try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
					props.load(reader);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read legacy-background-names.properties", e);
		}
		return props;
	}

	/**
	 * @return the design element ID of the named colour background, or <code>null</code> if the name is unknown
	 */
	public static String idFor(String name) {
		return name == null ? null : NAMES.getProperty(name);
	}
}

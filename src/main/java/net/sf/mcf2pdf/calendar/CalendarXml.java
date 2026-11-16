package net.sf.mcf2pdf.calendar;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

final class CalendarXml {

	private static final Log log = LogFactory.getLog(CalendarXml.class);

	private static final Pattern START_TAG = Pattern.compile(
			"<([A-Za-z_][\\w:.-]*)((?:\\s+[A-Za-z_][\\w:.-]*\\s*=\\s*(?:\"[^\"]*\"|'[^']*'))*)(\\s*/?>)");

	private static final Pattern ATTRIBUTE = Pattern.compile(
			"\\s+([A-Za-z_][\\w:.-]*)\\s*=\\s*(\"[^\"]*\"|'[^']*')");

	private CalendarXml() {
	}

	static Document parse(File file) throws IOException {
		DocumentBuilder builder = newBuilder(file);
		try {
			return builder.parse(file);
		}
		catch (SAXException e) {
			// Some CEWE 5.x resources repeat an attribute with the same value, which CEWE tolerates
			// but which is not well-formed XML. Retry once without such exact duplicates.
			byte[] original = Files.readAllBytes(file.toPath());
			byte[] repaired = removeIdenticalDuplicateAttributes(original);
			if (repaired == null) {
				throw new IOException("Cannot parse calendar resource '" + file.getAbsolutePath() + "'", e);
			}
			try {
				InputSource source = new InputSource(new ByteArrayInputStream(repaired));
				source.setSystemId(file.toURI().toString());
				Document document = newBuilder(file).parse(source);
				log.warn("Ignored repeated identical XML attributes in calendar resource '"
						+ file.getAbsolutePath() + "'");
				return document;
			}
			catch (SAXException retry) {
				IOException failure = new IOException(
						"Cannot parse calendar resource '" + file.getAbsolutePath() + "'", e);
				failure.addSuppressed(retry);
				throw failure;
			}
		}
	}

	/**
	 * Removes repeated attributes whose value equals the first occurrence. The bytes are handled as
	 * ISO-8859-1 so that every byte round-trips unchanged whatever the file's declared encoding is;
	 * XML markup is ASCII, so tag and attribute boundaries are still recognised in UTF-8 files.
	 *
	 * @return the repaired bytes, or <code>null</code> if nothing could be repaired.
	 */
	static byte[] removeIdenticalDuplicateAttributes(byte[] xml) {
		String text = new String(xml, StandardCharsets.ISO_8859_1);
		Matcher tag = START_TAG.matcher(text);
		StringBuilder result = new StringBuilder(text.length());
		boolean changed = false;
		while (tag.find()) {
			Map<String, String> seen = new LinkedHashMap<String, String>();
			StringBuilder attributes = new StringBuilder();
			boolean tagChanged = false;
			Matcher attribute = ATTRIBUTE.matcher(tag.group(2));
			while (attribute.find()) {
				String name = attribute.group(1);
				String value = attribute.group(2);
				String previous = seen.get(name);
				if (previous == null) {
					seen.put(name, value);
					attributes.append(attribute.group());
				}
				else if (unquote(previous).equals(unquote(value))) {
					tagChanged = true;
				}
				else {
					// Conflicting values: there is no safe choice, so leave the file broken.
					return null;
				}
			}
			if (tagChanged) {
				changed = true;
				tag.appendReplacement(result, Matcher.quoteReplacement(
						"<" + tag.group(1) + attributes + tag.group(3)));
			}
		}
		if (!changed) {
			return null;
		}
		tag.appendTail(result);
		return result.toString().getBytes(StandardCharsets.ISO_8859_1);
	}

	private static String unquote(String quoted) {
		return quoted.substring(1, quoted.length() - 1);
	}

	private static DocumentBuilder newBuilder(File file) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setExpandEntityReferences(false);
			factory.setXIncludeAware(false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			// Keep the default error handler from printing "[Fatal Error]" before the retry.
			builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
				@Override
				public void warning(org.xml.sax.SAXParseException exception) {
				}

				@Override
				public void error(org.xml.sax.SAXParseException exception) {
				}

				@Override
				public void fatalError(org.xml.sax.SAXParseException exception) throws SAXException {
					throw exception;
				}
			});
			return builder;
		}
		catch (ParserConfigurationException e) {
			throw new IOException("Cannot parse calendar resource '" + file.getAbsolutePath() + "'", e);
		}
	}
}

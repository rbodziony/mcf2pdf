package net.sf.mcf2pdf.calendar;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

final class CalendarXml {

	private CalendarXml() {
	}

	static Document parse(File file) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setExpandEntityReferences(false);
			factory.setXIncludeAware(false);
			return factory.newDocumentBuilder().parse(file);
		}
		catch (ParserConfigurationException | SAXException e) {
			throw new IOException("Cannot parse calendar resource '" + file.getAbsolutePath() + "'", e);
		}
	}
}

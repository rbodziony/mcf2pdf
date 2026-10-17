/*******************************************************************************
 * ${licenseText}
 * All rights reserved. This file is made available under the terms of the
 * Common Development and Distribution License (CDDL) v1.0 which accompanies
 * this distribution, and is available at
 * http://www.opensource.org/licenses/cddl1.txt
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.util;

import java.io.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for working with PDFs.
 */
public class PdfUtil {

	private final static Log log = LogFactory.getLog(PdfUtil.class);

	/**
	 * Converts an FO file to a PDF file using Apache FOP.
	 *
	 * @param fo the FO file
	 * @param pdf the target PDF file
	 * @param dpi the DPI resolution to use for bitmaps in the PDF
	 * @throws IOException In case of an I/O problem
	 * @throws FOPException In case of a FOP problem
	 * @throws TransformerException In case of XML processing problem
	 */
	@SuppressWarnings("rawtypes")
	public static void convertFO2PDF(InputStream fo, OutputStream pdf, int dpi) throws IOException,
			FOPException, TransformerException {

		//FopFactory fopFactory = FopFactory.newInstance();
		FopFactory		fopFactory = FopFactory.newInstance(new File(".").toURI());

		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		// configure foUserAgent as desired
		foUserAgent.setTargetResolution(dpi);


		// Setup output stream. Note: Using BufferedOutputStream
		// for performance reasons (helpful with FileOutputStreams).
		OutputStream out = new BufferedOutputStream(pdf);

		// Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

		try {
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			parserFactory.setNamespaceAware(true);
			parserFactory.newSAXParser().parse(new InputSource(fo), fop.getDefaultHandler());
		} catch (ParserConfigurationException | SAXException e) {
			throw new TransformerException("Failed to parse XSL-FO input", e);
		}

		// Result processing
		FormattingResults foResults = fop.getResults();
		java.util.List pageSequences = foResults.getPageSequences();
		for (java.util.Iterator it = pageSequences.iterator(); it.hasNext();) {
			PageSequenceResults pageSequenceResults = (PageSequenceResults)it
					.next();
			log.debug("PageSequence "
							+ (String.valueOf(pageSequenceResults.getID()).length() > 0 ? pageSequenceResults
									.getID() : "<no id>") + " generated "
							+ pageSequenceResults.getPageCount() + " pages.");
		}
		log.info("Generated " + foResults.getPageCount() + " PDF pages in total.");
		out.flush();
	}

}

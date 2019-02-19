/*******************************************************************************
 * ${licenseText}
 * All rights reserved. This file is made available under the terms of the
 * Common Development and Distribution License (CDDL) v1.0 which accompanies
 * this distribution, and is available at
 * http://www.opensource.org/licenses/cddl1.txt
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopConfParser;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.apps.PageSequenceResults;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;
import org.xml.sax.SAXException;

/**
 * Utility class for working with PDFs.
 */
public class PdfUtil {

	private final static Log log = LogFactory.getLog(PdfUtil.class);

	/**
	 * Converts an FO file to a PDF file using Apache FOP.
	 *
	 * @param fo
	 *            the FO file
	 * @param pdf
	 *            the target PDF file
	 * @param dpi
	 *            the DPI resolution to use for bitmaps in the PDF
	 * @throws IOException
	 *             In case of an I/O problem
	 * @throws TransformerException
	 *             In case of XML transformer problem
	 * @throws SAXException
	 * @throws URISyntaxException
	 * @throws ConfigurationException
	 */
	@SuppressWarnings("rawtypes")
	public static void convertFO2PDF(InputStream fo, OutputStream pdf, int dpi)
			throws IOException, TransformerException, SAXException, URISyntaxException, ConfigurationException {

		// FopFactory fopFactory = FopFactory.newInstance();
		FopFactory fopFactory = FopFactory.newInstance(new File(".").toURI());

		FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
		// configure foUserAgent as desired
		foUserAgent.setTargetResolution(dpi);

		// Setup output stream. Note: Using BufferedOutputStream
		// for performance reasons (helpful with FileOutputStreams).
		OutputStream out = new BufferedOutputStream(pdf);

		// Construct fop with desired output format
		Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, out);

		// Setup JAXP using identity transformer
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer(); // identity
															// transformer

		// Setup input stream
		Source src = new StreamSource(fo);

		// Resulting SAX events (the generated FO) must be piped through to FOP
		Result res = new SAXResult(fop.getDefaultHandler());

		// Start XSLT transformation and FOP processing
		transformer.transform(src, res);

		// Result processing
		FormattingResults foResults = fop.getResults();
		java.util.List pageSequences = foResults.getPageSequences();
		for (java.util.Iterator it = pageSequences.iterator(); it.hasNext();) {
			PageSequenceResults pageSequenceResults = (PageSequenceResults) it.next();
			log.debug("PageSequence "
					+ (String.valueOf(pageSequenceResults.getID()).length() > 0 ? pageSequenceResults.getID()
							: "<no id>")
					+ " generated " + pageSequenceResults.getPageCount() + " pages.");
		}
		log.info("Generated " + foResults.getPageCount() + " PDF pages in total.");
		out.flush();
	}

}

/*******************************************************************************
 * ${licenseText}
 * All rights reserved. This file is made available under the terms of the 
 * Common Development and Distribution License (CDDL) v1.0 which accompanies 
 * this distribution, and is available at 
 * http://www.opensource.org/licenses/cddl1.txt     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * A builder for XSL-FO documents. This builder provides a simple way
 * to build a flow of pages, and internally maintains a DOM with the according
 * XSL-FO elements. When finished with adding content, call <code>createDocument()</code>
 * to receive the DOM object.
 */
public class XslFoDocumentBuilder {
	
	private Document document;
	
	private Element flow;
	
	private static final Namespace ns = Namespace.getNamespace("fo", "http://www.w3.org/1999/XSL/Format");

	protected static final String EMPTY_DTD = "<!ELEMENT svg ANY>";
	
	public XslFoDocumentBuilder() {
		document = new Document();
		
		// create root element
		Element e = new Element("root", ns);
		
		document.addContent(e);
	}
	
	public void addPageMaster(String name, int widthPX, int heightPX) {
		Element m = new Element("simple-page-master", ns);
		m.setAttribute("master-name", name);
		m.setAttribute("page-width", widthPX + "px");
		m.setAttribute("page-height", heightPX + "px");
		
		Element body = new Element("region-body", ns);
		m.addContent(body);
		
		// insert into master set, if present
		Element ms = document.getRootElement().getChild("layout-master-set", ns);
		if (ms == null) {
			ms = new Element("layout-master-set", ns);
			document.getRootElement().addContent(ms);
		}
		
		ms.addContent(m);
	}
	
	public void startFlow(String masterName) {
		if (flow != null)
			throw new IllegalStateException("Please call endFlow() first before starting a new flow!");
		
		Element ps = new Element("page-sequence", ns);
		ps.setAttribute("master-reference", masterName);
		Element f = new Element("flow", ns);
		f.setAttribute("flow-name", "xsl-region-body");
		ps.addContent(f);
		document.getRootElement().addContent(ps);
		flow = f;
	}
	
	public void endFlow() {
		flow = null;
	}
	
	public void newPage() {
		if (flow == null)
			throw new IllegalStateException("Please call startFlow() first");
		
		Element e = new Element("block", ns);
		e.setAttribute("break-after", "page");
		e.setAttribute("padding", "0px");
		e.setAttribute("margin", "0px");
		e.setAttribute("border-width", "0px");
		flow.addContent(e);
	}
	
	public Document createDocument() {
		return (Document)document.clone();
	}

	public Namespace getNamespace() {
		return ns;
	}

	public void addPageElement(String imgSource) {
		if (flow == null)
			throw new IllegalStateException("Please call startFlow() first");
		
		// create block container
		Element bc = new Element("block-container",ns);
		bc.setAttribute("background-image", imgSource);
		bc.setAttribute("width", "100%");
		bc.setAttribute("height", "100%");
		bc.addContent(new Element("block", ns));
		
		flow.addContent(bc);
	}

}

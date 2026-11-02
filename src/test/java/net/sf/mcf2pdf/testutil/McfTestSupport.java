package net.sf.mcf2pdf.testutil;

import java.io.StringReader;

import org.apache.commons.digester3.Digester;

import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.impl.DigesterConfiguratorImpl;

/** Shared XML parser for rendering tests. */
public final class McfTestSupport {
	private McfTestSupport() {
	}

	public static McfFotobook parse(String xml) throws Exception {
		Digester digester = new Digester();
		new DigesterConfiguratorImpl().configureDigester(digester, null);
		return digester.parse(new StringReader(xml));
	}
}

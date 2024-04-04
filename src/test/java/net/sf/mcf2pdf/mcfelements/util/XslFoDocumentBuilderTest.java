package net.sf.mcf2pdf.mcfelements.util;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import net.sf.mcf2pdf.mcfelements.util.XslFoDocumentBuilder;

public class XslFoDocumentBuilderTest {

    private XslFoDocumentBuilder builder;

    @Before
    public void setUp() {
        builder = new XslFoDocumentBuilder();
    }

    @Test
    public void testAddPageMaster() {
        builder.addPageMaster("master1", 800, 600);
        Document doc = builder.createDocument();
        Element ms = doc.getRootElement().getChild("layout-master-set", builder.getNamespace());
        assertNotNull(ms);
        Element m = ms.getChild("simple-page-master", builder.getNamespace());
        assertNotNull(m);
        assertEquals("master1", m.getAttributeValue("master-name"));
        assertEquals("800px", m.getAttributeValue("page-width"));
        assertEquals("600px", m.getAttributeValue("page-height"));
    }

    @Test
    public void testStartAndEndFlow() {
        builder.startFlow("master1");
        Document doc = builder.createDocument();
        Element ps = doc.getRootElement().getChild("page-sequence", builder.getNamespace());
        assertNotNull(ps);
        assertEquals("master1", ps.getAttributeValue("master-reference"));
        builder.endFlow();
    }

    @Test(expected = IllegalStateException.class)
    public void testStartFlowWithoutEnding() {
        builder.startFlow("master1");
        builder.startFlow("master2");
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void testEndFlowWithoutStarting() {
        builder.endFlow();
    }

    @Test
    public void testNewPage() {
        builder.startFlow("master1");
        builder.newPage();
        Document doc = builder.createDocument();
        Element ps = doc.getRootElement().getChild("page-sequence", builder.getNamespace());
        Element f = ps.getChild("flow", builder.getNamespace());
        Element e = f.getChild("block", builder.getNamespace());
        assertNotNull(e);
        assertEquals("page", e.getAttributeValue("break-after"));
        builder.endFlow();
    }

    @Test(expected = IllegalStateException.class)
    public void testNewPageWithoutStartingFlow() {
        builder.newPage();
    }

    @Test
    public void testAddPageElement() {
        builder.startFlow("master1");
        builder.addPageElement("image1.png");
        Document doc = builder.createDocument();
        Element ps = doc.getRootElement().getChild("page-sequence", builder.getNamespace());
        Element f = ps.getChild("flow", builder.getNamespace());
        Element bc = f.getChild("block-container", builder.getNamespace());
        assertNotNull(bc);
        assertEquals("file:///image1.png", bc.getAttributeValue("background-image"));
        builder.endFlow();
    }

    @Test(expected = IllegalStateException.class)
    public void testAddPageElementWithoutStartingFlow() {
        builder.addPageElement("image1.png");
    }
}
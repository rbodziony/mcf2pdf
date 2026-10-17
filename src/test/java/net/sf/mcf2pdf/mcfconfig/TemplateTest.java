package net.sf.mcf2pdf.mcfconfig;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TemplateTest {

    private Template template;

    @Before
    public void setUp() {
        template = new Template();
    }

    @Test
    public void testSetAndGetName() {
        String name = "testName";
        template.setName(name);
        assertEquals(name, template.getName());
    }

    @Test
    public void testSetAndGetFilename() {
        String filename = "testFilename";
        template.setFilename(filename);
        assertEquals(filename, template.getFilename());
    }
}
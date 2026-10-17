package net.sf.mcf2pdf.mcfelements.impl;
import net.sf.mcf2pdf.mcfelements.McfAreaContent;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import net.sf.mcf2pdf.mcfelements.impl.McfImageImpl;
import net.sf.mcf2pdf.mcfelements.impl.McfCutoutImpl;


public class McfImageImplTest {

    private McfImageImpl image;
    private McfCutoutImpl cutout;

    @Before
    public void setUp() {
        image = new McfImageImpl();
        cutout = new McfCutoutImpl();
    }

    @Test
    public void testContentType() {
        assertEquals(McfAreaContent.ContentType.IMAGE, image.getContentType());
    }

    @Test
    public void testParentChildRelationshipNatureMethods() {
        image.setParentChildRelationshipNature("relationshipNature");
        assertEquals("relationshipNature", image.getParentChildRelationshipNature());
    }

    @Test
    public void testScaleMethods() {
        image.setScale(1.5f);
        assertEquals(1.5f, image.getScale(), 0.001);
    }

    @Test
    public void testUseABKMethods() {
        image.setUseABK(1);
        assertEquals(1, image.getUseABK());
    }

    @Test
    public void testLeftMethods() {
        image.setLeft(10.0f);
        assertEquals(10.0f, image.getLeft(), 0.001);
    }

    @Test
    public void testTopMethods() {
        image.setTop(20.0f);
        assertEquals(20.0f, image.getTop(), 0.001);
    }

    @Test
    public void testFileNameMasterMethods() {
        image.setFileNameMaster("fileNameMaster");
        assertEquals("fileNameMaster", image.getFileNameMaster());
    }

    @Test
    public void testSafeContainerLocationMethods() {
        image.setSafeContainerLocation("safeContainerLocation");
        assertEquals("safeContainerLocation", image.getSafeContainerLocation());
    }

    @Test
    public void testFileNameMethods() {
        image.setFileName("fileName");
        assertEquals("fileName", image.getFileName());
    }

    @Test
    public void testFadingFileMethods() {
        image.setFadingFile("fadingFile");
        assertEquals("fadingFile", image.getFadingFile());
    }

    @Test
    public void testPassepartoutDesignElementIdMethods() {
        image.setPassepartoutDesignElementId("passepartoutDesignElementId");
        assertEquals("passepartoutDesignElementId", image.getPassepartoutDesignElementId());
    }

    @Test
    public void testCutoutMethods() {
        image.setCutout(cutout);
        assertEquals(cutout, image.getCutout());
    }
}
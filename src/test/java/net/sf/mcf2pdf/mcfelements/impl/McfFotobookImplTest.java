package net.sf.mcf2pdf.mcfelements.impl;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.util.List;
import net.sf.mcf2pdf.mcfelements.McfPage;
import net.sf.mcf2pdf.mcfelements.McfPageNum;
import net.sf.mcf2pdf.mcfelements.impl.McfFotobookImpl;

public class McfFotobookImplTest {

    private McfFotobookImpl fotobook;
    private McfPageImpl page;
    private McfPageNumImpl pageNum;

    @Before
    public void setUp() {
        fotobook = new McfFotobookImpl();
        page = new McfPageImpl(); // Assuming McfPage has a no-arg constructor
        pageNum = new McfPageNumImpl(); // Assuming McfPageNum has a no-arg constructor
    }

    @Test
    public void testAddAndGetPage() {
        fotobook.addPage(page);
        List<? extends McfPage> pages = fotobook.getPages();
        assertTrue(pages.contains(page));
    }

    @Test
    public void testAddAndGetPageNum() {
        fotobook.addPageNum(pageNum);
        assertEquals(pageNum, fotobook.getPageNum());
    }

    @Test
    public void testProductTypeMethods() {
        fotobook.setProductType(1);
        assertEquals(1, fotobook.getProductType());
    }

    @Test
    public void testProductNameMethods() {
        fotobook.setProductName("productName");
        assertEquals("productName", fotobook.getProductName());
    }

    @Test
    public void testVersionMethods() {
        fotobook.setVersion("version");
        assertEquals("version", fotobook.getVersion());
    }
    @Test
    public void testCreatedWithHPSVersionMethods() {
        fotobook.setCreatedWithHPSVersion("HPSVersion");
        assertEquals("HPSVersion", fotobook.getCreatedWithHPSVersion());
    }
    @Test
    public void testProgramVersionMethods() {
        fotobook.setProgramVersion("ProgramVersion");
        assertEquals("ProgramVersion", fotobook.getProgramVersion());
    }

    @Test
    public void testImageDirMethods() {
        fotobook.setImageDir("ImageDir");
        assertEquals("ImageDir", fotobook.getImageDir());
    }

    @Test
    public void testNormalPagesMethods() {
        fotobook.setNormalPages(5);
        assertEquals(5, fotobook.getNormalPages());
    }

    @Test
    public void testTotalPagesMethods() {
        fotobook.setTotalPages(10);
        assertEquals(10, fotobook.getTotalPages());
    }

    @Test
    public void testFileMethods() {
        File file = new File("path/to/file");
        fotobook.setFile(file);
        assertEquals(file, fotobook.getFile());
    }
}
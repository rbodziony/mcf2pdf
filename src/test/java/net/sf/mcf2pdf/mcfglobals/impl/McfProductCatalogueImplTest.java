package net.sf.mcf2pdf.mcfglobals.impl;

import net.sf.mcf2pdf.mcfglobals.McfAlbumType;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class McfProductCatalogueImplTest {

    private McfProductCatalogueImpl catalogue;
    private McfAlbumTypeImpl albumType;

    @Before
    public void setUp() {
        catalogue = new McfProductCatalogueImpl();
        albumType = new McfAlbumTypeImpl();
        albumType.setName("Test Album");
    }

    @Test
    public void testAddAndGetAlbumType() {
        catalogue.addAlbumType(albumType);
        assertEquals(albumType, catalogue.getAlbumType("Test Album"));
        assertNull(catalogue.getAlbumType("Nonexistent Album"));
    }

    @Test
    public void testIsEmpty() {
        assertTrue(catalogue.isEmpty());
        catalogue.addAlbumType(albumType);
        assertFalse(catalogue.isEmpty());
    }
}
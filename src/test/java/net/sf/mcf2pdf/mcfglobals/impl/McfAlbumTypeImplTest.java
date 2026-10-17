package net.sf.mcf2pdf.mcfglobals.impl;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class McfAlbumTypeImplTest {

    private McfAlbumTypeImpl albumType;

    @Before
    public void setUp() {
        albumType = new McfAlbumTypeImpl();
    }

    @Test
    public void testGettersAndSetters() {
        albumType.setName("Test Album");
        assertEquals("Test Album", albumType.getName());

        albumType.setSafetyMargin(10);
        assertEquals(10, albumType.getSafetyMargin());

        albumType.setBleedMargin(20);
        assertEquals(20, albumType.getBleedMargin());

        albumType.setNormalPageHorizontalClamp(30);
        assertEquals(30, albumType.getNormalPageHorizontalClamp());

        albumType.setUsableWidth(40);
        assertEquals(40, albumType.getUsableWidth());

        albumType.setUsableHeight(50);
        assertEquals(50, albumType.getUsableHeight());

        albumType.setBleedMarginCover(60);
        assertEquals(60, albumType.getBleedMarginCover());

        albumType.setCoverExtraVertical(70);
        assertEquals(70, albumType.getCoverExtraVertical());

        albumType.setCoverExtraHorizontal(80);
        assertEquals(80, albumType.getCoverExtraHorizontal());
    }

    @Test
    public void testSpineWidth() {
        assertEquals(0, albumType.getSpineWidth(10));

        albumType.addSpine(10, 100);
        assertEquals(100, albumType.getSpineWidth(10));
    }
}
package net.sf.mcf2pdf.mcfelements.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.Color;
import net.sf.mcf2pdf.mcfelements.McfAreaContent;
import net.sf.mcf2pdf.mcfelements.McfPosition;
import org.mockito.Mockito;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class McfAreaImplTest {

    private McfAreaImpl area;
    private McfPosition position;

    private McfBorderImpl border;
    private McfCornersImpl corner;
    private McfPageImpl page;

    @Before
    public void setUp() {
        area = new McfAreaImpl();
        position = mock(McfPosition.class);
        border = mock(McfBorderImpl.class);
        corner = mock(McfCornersImpl.class);
        page = mock(McfPageImpl.class);
    }

    @Test
    public void testFields() {
        area.setTop(10.0f);
        area.setWidth(20.0f);
        area.setHeight(30.0f);
        area.setRotation(40.0f);
        area.setzPosition(50);
        area.setAreaType("testType");
        area.setBorderEnabled(true);
        area.setBorderSize(60.0f);
        area.setBorderColor(Color.RED);
        area.setShadowEnabled(true);
        area.setShadowAngle(70);
        area.setShadowIntensity(80);
        area.setShadowDistance(90.0f);
        area.setBackgroundColor(Color.BLUE);
        area.setContent(new McfClipartImpl());

        assertEquals(10.0f, area.getTop(), 0.001);
        assertEquals(20.0f, area.getWidth(), 0.001);
        assertEquals(30.0f, area.getHeight(), 0.001);
        assertEquals(40.0f, area.getRotation(), 0.001);
        assertEquals(50, area.getZPosition());
        assertEquals("testType", area.getAreaType());
        assertTrue(area.isBorderEnabled());
        assertEquals(60.0f, area.getBorderSize(), 0.001);
        assertEquals(Color.RED, area.getBorderColor());
        assertTrue(area.isShadowEnabled());
        assertEquals(70, area.getShadowAngle());
        assertEquals(80, area.getShadowIntensity());
        assertEquals(90.0f, area.getShadowDistance(), 0.001);
        assertEquals(Color.BLUE, area.getBackgroundColor());
        assertNotNull(area.getContent());
    }

    @Test
    public void testLeft() {
        // Test setLeft and getLeft when position is null
        area.setLeft(10.0f);
        assertEquals(10.0f, area.getLeft(), 0.001);

        // Test getLeft when position is not null
        when(position.getLeft()).thenReturn(20.0f);
        area.setPosition(position);
        assertEquals(20.0f, area.getLeft(), 0.001);
    }

    @Test
    public void testHeightMethods() {
        // Test setHeight and getHeight when position is null
        area.setHeight(10.0f);
        assertEquals(10.0f, area.getHeight(), 0.001);

        // Test getHeight when position is not null
        when(position.getHeight()).thenReturn(20.0f);
        area.setPosition(position);
        assertEquals(20.0f, area.getHeight(), 0.001);
    }
    @Test
    public void testGetSetTop() {
        // Test setTop and getTop when position is null
        area.setTop(10.0f);
        assertEquals(10.0f, area.getTop(), 0.001);

        // Test getTop when position is not null
        when(position.getTop()).thenReturn(20.0f);
        area.setPosition(position);
        assertEquals(20.0f, area.getTop(), 0.001);
    }

    @Test
    public void testGetSetWidth() {
        // Test setWidth and getWidth when position is null
        area.setWidth(30.0f);
        assertEquals(30.0f, area.getWidth(), 0.001);

        // Test getWidth when position is not null
        when(position.getWidth()).thenReturn(40.0f);
        area.setPosition(position);
        assertEquals(40.0f, area.getWidth(), 0.001);
    }

    @Test
    public void testRotationMethods() {
        // Test setRotation and getRotation when position is null
        area.setRotation(30.0f);
        assertEquals(30.0f, area.getRotation(), 0.001);

        // Test getRotation when position is not null
        when(position.getRotation()).thenReturn(40.0f);
        area.setPosition(position);
        assertEquals(40.0f, area.getRotation(), 0.001);
    }

    @Test
    public void testZPositionMethods() {
        // Test setZPosition and getZPosition when position is null
        area.setzPosition(50);
        assertEquals(50, area.getZPosition());

        // Test getZPosition when position is not null
        when(position.getZPosition()).thenReturn(60);
        area.setPosition(position);
        assertEquals(60, area.getZPosition());
    }

    @Test
    public void testSetBorderSize() {
        McfAreaImpl area = new McfAreaImpl();
        float borderSize = 2.5f;
        area.setBorderSize(borderSize);
        Assert.assertEquals(borderSize, area.getBorderSize(), 0.001);
    }
@Test
public void testGetBorderSize() {
    McfAreaImpl area = new McfAreaImpl();
    float borderSize = 2.5f;
    area.setBorderSize(borderSize);
    Assert.assertEquals(borderSize, area.getBorderSize(), 0.001);
}

@Test
public void testSetBorderColor() {
    McfAreaImpl area = new McfAreaImpl();
    Color borderColor = Color.RED;
    area.setBorderColor(borderColor);
    Assert.assertEquals(borderColor, area.getBorderColor());
}

@Test
public void testIsShadowEnabled() {
    McfAreaImpl area = new McfAreaImpl();
    boolean shadowEnabled = true;
    area.setShadowEnabled(shadowEnabled);
    Assert.assertEquals(shadowEnabled, area.isShadowEnabled());
}

@Test
public void testGetShadowAngle() {
    McfAreaImpl area = new McfAreaImpl();
    int shadowAngle = 45;
    area.setShadowAngle(shadowAngle);
    Assert.assertEquals(shadowAngle, area.getShadowAngle());
}

@Test
public void testGetShadowIntensity() {
    McfAreaImpl area = new McfAreaImpl();
    int shadowIntensity = 50;
    area.setShadowIntensity(shadowIntensity);
    Assert.assertEquals(shadowIntensity, area.getShadowIntensity());
}

@Test
public void testGetShadowDistance() {
    McfAreaImpl area = new McfAreaImpl();
    float shadowDistance = 3.5f;
    area.setShadowDistance(shadowDistance);
    Assert.assertEquals(shadowDistance, area.getShadowDistance(), 0.001);
}

@Test
public void testGetBackgroundColor() {
    McfAreaImpl area = new McfAreaImpl();
    Color backgroundColor = Color.WHITE;
    area.setBackgroundColor(backgroundColor);
    Assert.assertEquals(backgroundColor, area.getBackgroundColor());
}

// ...

@Test
public void testGetContent() {
    McfAreaImpl area = new McfAreaImpl();
    McfAreaContent content = Mockito.mock(McfAreaContent.class);
    area.setContent(content);
    Assert.assertEquals(content, area.getContent());
}

// ...

// ...

@Test
public void testGetPosition() {
    McfAreaImpl area = new McfAreaImpl();
    McfPosition position = Mockito.mock(McfPosition.class);
    area.setPosition(position);
    Assert.assertEquals(position, area.getPosition());
}

    @Test
    public void testGetBorder() {
        area.setBorder(border);
        assertEquals(border, area.getBorder());
    }

    @Test
    public void testGetCorner() {
        area.setCorner(corner);
        assertEquals(corner, area.getCorners());
    }

    @Test
    public void testGetPage() {
        area.setPage(page);
        assertEquals(page, area.getPage());
    }
}
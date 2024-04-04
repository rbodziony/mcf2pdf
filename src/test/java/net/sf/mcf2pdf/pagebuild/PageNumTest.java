package net.sf.mcf2pdf.pagebuild;

import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.McfPage;
import net.sf.mcf2pdf.mcfelements.McfPageNum;
import net.sf.mcf2pdf.pagebuild.PageNum;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class PageNumTest {

    private McfFotobook book;
    private McfPage page;
    private McfPageNum mcfPageNum;

    private PageNum pageNum;

    @Before
    public void setUp() {
        book = mock(McfFotobook.class);
      //  book - new Book()
        page = mock(McfPage.class);
        mcfPageNum = mock(McfPageNum.class);
        when(book.getPageNum()).thenReturn(mcfPageNum);
        when(page.getPageNr()).thenReturn(1);
        when(mcfPageNum.getPosition()).thenReturn(2);
        when(mcfPageNum.getHorizontalMargin()).thenReturn(20);
        when(mcfPageNum.getVerticalMargin()).thenReturn(10);
        when(mcfPageNum.getTextString()).thenReturn("textString");
        when(mcfPageNum.getTextColor()).thenReturn(new Color(255, 255, 255));
        pageNum = new PageNum(book, page, "right", 100, 200);
    }

    //@Test
    public void testSetPosition() {
        // Mock the methods used in setPosition
        //when(pageNum.getVerticalMargin()).thenReturn(10);
       // when(pageNum.getHorizontalMargin()).thenReturn(20);
       // when(pageNum.getPosition()).thenReturn(2);

        //pageNum.setPosition();

        // Verify the values of topPX and leftPX after calling setPosition
        //assertEquals(10, pageNum.getTopPX());
       // assertEquals(50, pageNum.getLeftPX());
    }


    @Test
    public void testGetZPosition() {

        assertEquals(100000, pageNum.getZPosition());
    }

    @Test
    public void testGetLeftMM() {
        // TODO: Replace with actual expected value
        assertEquals(0, pageNum.getLeftMM(), 0.001);
    }

    @Test
    public void testGetTopMM() {
        // TODO: Replace with actual expected value
        assertEquals(0, pageNum.getTopMM(), 0.001);
    }

    @Test
    public void testIsVectorGraphic() {
        assertTrue(pageNum.isVectorGraphic());
    }

  /*  @Test
    public void testSetPositionTopCenter() {
        double expected = 0.0; // replace with the expected value
        double actual = pageNum.getTopMM();
        assertEquals(expected, actual, 0.01);
        actual =pageNum.getLeftMM();
        assertEquals(expected, actual, 0.01);
    }*/

    @Test
    public void testSetPositionTopCenter() {
        when(mcfPageNum.getPosition()).thenReturn(2);
        assertEquals(10, pageNum.getTopPX());
        assertEquals(75, pageNum.getLeftPX());
    }

    @Test
    public void testSetPositionTopOutsideRight() {
        when(mcfPageNum.getPosition()).thenReturn(1);
        pageNum = new PageNum(book, page, "right", 100, 200);
        assertEquals(10, pageNum.getTopPX());
        assertEquals(80, pageNum.getLeftPX());
    }

    @Test
    public void testSetPositionTopOutsideLeft() {
        when(mcfPageNum.getPosition()).thenReturn(1);
        pageNum = new PageNum(book, page, "left", 100, 200);
         assertEquals(10, pageNum.getTopPX());
        assertEquals(20, pageNum.getLeftPX());
    }

    @Test
    public void testSetPositionBottomCenter() {
        when(mcfPageNum.getPosition()).thenReturn(5);
        pageNum = new PageNum(book, page, "left", 100, 200);
        assertEquals(190, pageNum.getTopPX());
        assertEquals(25, pageNum.getLeftPX());
    }

    @Test
    public void testSetPositionBottomOutsideRight() {
        when(mcfPageNum.getPosition()).thenReturn(4);
        pageNum = new PageNum(book, page, "right", 100, 200);
        assertEquals(190, pageNum.getTopPX());
        assertEquals(80, pageNum.getLeftPX());
    }

    @Test
    public void testSetPositionBottomOutsideLeft() {
        when(mcfPageNum.getPosition()).thenReturn(4);
        pageNum = new PageNum(book, page, "left", 100, 200);
        assertEquals(190, pageNum.getTopPX());
        assertEquals(20, pageNum.getLeftPX());
    }

}
package net.sf.mcf2pdf.pagebuild;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import net.sf.mcf2pdf.mcfelements.impl.McfTextImpl;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PageTextTest {

	// @Test
	@Test
	public void test_001NoSpanOnlnyParagraph() throws Exception {
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent(
				"<html><head></head><body><p align=\"justify\" style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">OnlyParagraph</p></body></html>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() > 0);
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("OnlyParagraph"));
	}

	@Test
	public void test_002MultiSpanOnewithOneParagraph() throws Exception {
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent(
				"<html><head></head><body><span style=\" font-size:8.25pt;\">FirstSpan</span><p align=\"justify\" style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">SecondParagraph</p></body></html>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() == 2);
		Assert.assertTrue("First span should be First",test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("FirstSpan"));
		Assert.assertTrue("Last span should be LAst",test01.getParas().get(1).getTexts().get(0).getText().equalsIgnoreCase("SecondParagraph"));
	}

	@Test
	public void test_003ParagrahOverSpan() throws Exception {
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent(
				"<html><head></head><body><p align=\"justify\" style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">test1<span style=\" font-size:8.25pt;\">P_And_Span</span></p></body></html>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() > 0);
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("P_And_Span"));
	}

	@Test
	public void test_004SpanNoParagraph() throws Exception {
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent(
				"<html><head></head><body><span style=\" font-size:8.25pt;\">OnlySpan</span></body></html>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() == 1);
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("OnlySpan"));
	}
	//@Test
	// need change it later to support br in span only
	public void test_004SpanNoParagraphwithBR() throws Exception {
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent(
				"<html><head></head><body><span style=\" font-size:8.25pt;\">OnlySpan<br /></span></body></html>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() == 1);
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("OnlySpan"));
	}
	
    @Test
    public void test_005LongSpanandParafraps() throws Exception {
    	McfTextImpl text = new McfTextImpl();
    	String html = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("LongSpanAndParagraph.html"),
                "UTF-8");
    	text.setHtmlContent(
    			html
    			);
    	
    	PageText test01 = new PageText(text);
		Assert.assertTrue("First span should be First",test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("FirstSpan"));
		Assert.assertTrue("Last span should be LAst",test01.getParas().get(23).getTexts().get(0).getText().equalsIgnoreCase("lastSpan"));
    }
    @Test
    public void test_006CheckingBodyFontFamilyNormal() throws Exception{
    	McfTextImpl text = new McfTextImpl();
		text.setHtmlContent(
				"<html><head></head><body style=\" font-family:'MyTestFont'; font-size:12pt; font-weight:400; font-style:normal;\"><p align=\"justify\" style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">OnlyParagraph</p></body></html>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() > 0);
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("OnlyParagraph"));
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getFontFamily().equalsIgnoreCase("MyTestFont"));
    }
    @Test
    public void test_006CheckingBodyFontFamilyWrong() throws Exception{
    	McfTextImpl text = new McfTextImpl();
		text.setHtmlContent(
				"<html><head></head><body style=\" font-family:'MS Shell Dlg 2'; font-size:12pt; font-weight:400; font-style:normal;\"><p align=\"justify\" style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">OnlyParagraph</p></body></html>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() > 0);
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("OnlyParagraph"));
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getFontFamily().equalsIgnoreCase("Calibri"));
    }
}

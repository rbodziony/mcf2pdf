package net.sf.mcf2pdf.pagebuild;

import net.sf.mcf2pdf.mcfelements.impl.McfTextImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(1).getText().equalsIgnoreCase("P_And_Span"));
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
	@Test
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
		Assert.assertTrue(test01.getParas().size() == 1);
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("OnlyParagraph"));
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getFontFamily().equalsIgnoreCase("MyTestFont"));
    }
    @Test
    public void test_006CheckingBodyFontFamilyWrong() throws Exception{
    	McfTextImpl text = new McfTextImpl();
		text.setHtmlContent(
				"<html><head></head><body style=\" font-family:'MS Shell Dlg 2'; font-size:12pt; font-weight:400; font-style:normal;\"><p align=\"justify\" style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">OnlyParagraph</p></body></html>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() == 1);
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("OnlyParagraph"));
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getFontFamily().equalsIgnoreCase("Calibri"));
    }

	@Test
	public void test_007TableNormal() throws Exception{
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent("<body style=\" font-family:'Calibri'; font-size:12pt; font-weight:400; font-style:normal;\">\n" +
				" <table style=\"-qt-table-type: root; margin-top:29px; margin-bottom:0px; margin-left:0px; margin-right:0px;\">\n" +
				"  <tbody>\n" +
				"   <tr>\n" +
				"    <td style=\"border: none;\"><p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" color:#000000;\">aaaaaa -aaaa,bbbbbb</span></p></td>\n" +
				"   </tr>\n" +
				"  </tbody>\n" +
				" </table>\n" +
				"</body>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() ==1);
	}
	@Test
	public void test_008Issue10WithFonts() throws Exception {
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent("html<!DOCTYPE HTML PUBLIC \"\n" +
				"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\"><html><he\n" +
				"ad><meta name=\"qrichtext\" content=\"1\" /><style type=\"text/css\">p, li { white-spa\n" +
				"ce: pre-wrap; }</style></head><body style=\" font-family:'Arial'; font-size:21pt;\n" +
				" font-weight:600; font-style:normal;\"><table style=\"-qt-table-type: root; margin\n" +
				"-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px;\"><tr><td style=\"\n" +
				"border: none;\"><p align=\"center\" style=\" margin-top:0px; margin-bottom:0px; marg\n" +
				"in-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style\n" +
				"=\" font-size:36pt; color:#ffffff;\">TEST</span></p></td></tr></table></body></htm\n" +
				"l>");
		PageText test08 = new PageText(text);
		Assert.assertTrue(test08.getParas().size()==1);
		Assert.assertTrue(test08.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("TEST"));
	}

	@Test
	public void test_009ParwithSpanAndText() throws Exception{
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent("<body style=\" font-family:'Calibri'; font-size:12pt; font-weight:400; font-style:normal;\">\n" +
				"<table style=\"-qt-table-type: root; margin-top:29px; margin-bottom:0px; margin-left:0px; margin-right:0px;\">\n" +
				"<tbody>\n" +
				"<tr>\n" +
				"<td style=\"border: none;\">" +
				"<p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\">" +
				"<span style=\" color:#000000;\">test01</span>test02</p></td>\n" +
				"</tr>\n" +
				"</tbody>\n" +
				"</table>\n" +
				"</body>");
		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size() ==1);
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("test01"));
		Assert.assertTrue(test01.getParas().get(0).getTexts().get(1).getText().equalsIgnoreCase("test02"));
	}

	@Test
	public void test_010Test01FileSpanLinesWithoutSPan() throws Exception {
		McfTextImpl text = new McfTextImpl();
		String html = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("test01.html"),
				"UTF-8");
		text.setHtmlContent(
				html
		);

		PageText test01 = new PageText(text);
		Assert.assertTrue(test01.getParas().size()==1);
		Assert.assertTrue("First span should be First",test01.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("Line01"));
		Assert.assertTrue("Last span should be LAst",test01.getParas().get(0).getTexts().get(1).getText().equalsIgnoreCase("Line02Line03Line04"));
	}
	@Test
	public void test_011Test02Span() throws Exception {
		McfTextImpl text = new McfTextImpl();
		String html = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("test02.html"),
				"UTF-8");
		text.setHtmlContent(
				html
		);

		PageText test02 = new PageText(text);
		Assert.assertTrue(test02.getParas().size()==24);
		Assert.assertTrue("First span should be First",test02.getParas().get(0).getTexts().get(1).getText().equalsIgnoreCase("01 SPAN"));
		Assert.assertTrue("Last span should be LAst",test02.getParas().get(22).getTexts().get(1).getText().equalsIgnoreCase("15 SPAN"));
	}


}

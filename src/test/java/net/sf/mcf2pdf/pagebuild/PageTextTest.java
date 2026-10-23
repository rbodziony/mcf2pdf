package net.sf.mcf2pdf.pagebuild;

import net.sf.mcf2pdf.mcfelements.impl.McfAreaImpl;
import net.sf.mcf2pdf.mcfelements.impl.McfTextImpl;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
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
		Assert.assertTrue(test01.getParas().size() == 1);
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
	@Ignore
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
	public void test_009Issue10WithPara01() throws Exception {
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\"><html><head><meta name=\"qrichtext\" content=\"1\"/><style type=\"text/css\">p, li { white-space: pre-wrap; }</style></head><body style=\" font-family:'MS Shell Dlg 2'; font-size:9pt; font-weight:400; font-style:normal;\"><table style=\"-qt-table-type: root; margin-top:5px; margin-bottom:5px; margin-left:5px; margin-right:5px;\"><tr><td style=\"border: none;\"><p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:'Calibri';\">•</span><span style=\" font-family:'Calibri'; font-weight:600;\">TEST11</span></p><p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:'Calibri';\">AAA BBB CCC </span></p><p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-family:'Calibri';\">XXX DDD WWW. </span></p></td></tr></table></body></html>");
		PageText test09 = new PageText(text);
		Assert.assertTrue(test09.getParas().size()==3);
	//	Assert.assertTrue(test009.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase("TEST"));
	}

	@Test
	public void test_0010Issue10WithPara02() throws Exception {
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\"><html><head><meta name=\"qrichtext\" content=\"1\"/><style type=\"text/css\">p, li { white-space: pre-wrap; }</style></head><body style=\" font-family:'MS Shell Dlg 2'; font-size:8pt; font-weight:400; font-style:italic;\"><table style=\"-qt-table-type: root; margin-top:5px; margin-bottom:5px; margin-left:5px; margin-right:5px;\"><tr><td style=\"border: none;\"><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:18pt; font-style:normal;\"> </span><span style=\" font-size:18pt; font-style:normal; color:#1b7dd4;\">TEST12</span><span style=\" font-size:8.25pt; font-style:normal; color:#1b7dd4;\"> </span></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal; color:#1b7dd4;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">CCCC</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">DDDD</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">EEEEE</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">FFFF</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">GGGG</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">HHHH</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">IIII</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">JJJJJ</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\"> </span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">KKKK</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">LLLL </span></p><p style=\"-qt-paragraph-type:empty; margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; font-size:8.25pt; font-style:normal;\"><br/></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">MMMMM</span></p><p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%;\"><span style=\" font-family:'Calibri'; font-size:11pt; font-style:normal;\"> NNNNN</span></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\"-qt-paragraph-type:empty; margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%; font-family:'Calibri'; font-size:11pt; font-style:normal;\"><br/></p><p style=\" margin-top:0px; margin-bottom:0px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px; line-height:107%;\"><span style=\" font-family:'Times New Roman'; font-size:18pt; font-style:normal; color:#1b7dd4;\">  </span><span style=\" font-size:18pt; font-style:normal; color:#1b7dd4;\">OOOO</span><span style=\" font-size:8.25pt; font-style:normal;\"> PPP</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">RRRR</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">SSSS</span></p><p style=\" margin-top:12px; margin-bottom:12px; margin-left:0px; margin-right:0px; -qt-block-indent:0; text-indent:0px;\"><span style=\" font-size:8.25pt; font-style:normal;\">TTTT</span></p></td></tr></table></body></html>");
		PageText test10 = new PageText(text);
		//Assert.assertTrue(test10.getParas().size()==3);
		Assert.assertTrue(test10.getParas().get(0).getTexts().get(0).getText().equalsIgnoreCase(" "));
		Assert.assertTrue(test10.getParas().get(0).getTexts().get(1).getText().equalsIgnoreCase("TEST12"));
	}

  // old testcases 
  @Ignore
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
   @Ignore
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
  @Ignore
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

			@Test
			public void test_012VerticalCenterIgnoresTableMarginTop() throws Exception {
		BufferedImage centered = renderText("ALIGNVCENTER,ALIGNHCENTER");
		int[] rows = textRows(centered);
		int gapTop = rows[0];
		int gapBottom = centered.getHeight() - 1 - rows[1];

		Assert.assertTrue("text must be drawn", rows[1] > rows[0]);
		// glyph bounds differ slightly from the layout box (descent/leading), so allow some slack
		Assert.assertTrue("gaps top=" + gapTop + " bottom=" + gapBottom, Math.abs(gapTop - gapBottom) <= 10);
		Assert.assertTrue("must not use table margin-top 165, top gap=" + gapTop, gapTop < 100);
	}

	@Test
	public void test_013WithoutVerticalCenterUsesTableMarginTop() throws Exception {
		BufferedImage top = renderText("ALIGNHCENTER");
		int[] rows = textRows(top);

		Assert.assertTrue("text must be drawn", rows[1] > rows[0]);
		Assert.assertTrue("text starts at the table margin, first row=" + rows[0], rows[0] >= 160);
	}

	private static BufferedImage renderText(String alignment) throws Exception {
		McfTextImpl text = new McfTextImpl();
		text.setHtmlContent("<html><body style=\" font-family:'Serif'; font-size:16pt;\">"
				+ "<table style=\"-qt-table-type: root; margin-top:165px; margin-bottom:5px; margin-left:5px; margin-right:5px;\">"
				+ "<tr><td><p align=\"center\" style=\" margin-top:0px; margin-bottom:0px;\"><span style=\" font-size:16pt; color:#000000;\">3 listopada 2003</span></p>"
				+ "<p align=\"center\" style=\" margin-top:0px; margin-bottom:0px;\"><span style=\" font-size:16pt; color:#000000;\">Krysia i jej prababcia</span></p>"
				+ "</td></tr></table></body></html>");
		text.setAlignment(alignment);
		text.setIndentMargin(5);
		McfAreaImpl area = new McfAreaImpl();
		area.setLeft(0);
		area.setTop(0);
		area.setWidth(953);
		area.setHeight(472);
		text.setArea(area);

		PageRenderContext context = new PageRenderContext(100, 1.0, new McfResourceScanner(Collections.emptyList()), null);
		return new PageText(text).renderAsBitmap(context, new Point(0, 0), 0, 0);
	}

	/** Returns the first and last pixel row containing any non-transparent pixel. */
	private static int[] textRows(BufferedImage img) {
		int first = -1;
		int last = -1;
		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				if ((img.getRGB(x, y) >>> 24) != 0) {
					if (first < 0) {
						first = y;
					}
					last = y;
					break;
				}
			}
		}
		return new int[] { first, last };
	}

}

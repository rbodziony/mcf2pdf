package net.sf.mcf2pdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import net.sf.mcf2pdf.mcfelements.util.PdfUtil;

public class DecorationsTests {
	public static Mcf2FoConverter test01converter = null;
	public static int dpi = 150;
	public int maxPageNo = -1;
	
	@BeforeClass
	public static void settingInitialResources() throws IOException, SAXException{
	  

		File mcfInstallDir= new File("./src/test/resources/test01");
		File mcfTempDir=  new File("./src/test/resources/test01");
		File tempImageDir =  new File(new File(System.getProperty("user.home")), ".mcf2pdf");
		test01converter = new Mcf2FoConverter(mcfInstallDir, mcfTempDir, tempImageDir,false);
}
	
	@Test
	public void testRender_fading_DECO_6202_OK() throws Exception {
		
		File mcfFile = new File("./src/test/resources/test01.mcf");
		OutputStream finalOut = new FileOutputStream("test_6202_OK.pdf");
		OutputStream xslFoOut = new ByteArrayOutputStream();
		test01converter.convert(
				mcfFile, xslFoOut, dpi, maxPageNo);
		xslFoOut.flush();
		byte[] data = ((ByteArrayOutputStream)xslFoOut).toByteArray();
		PdfUtil.convertFO2PDF(new ByteArrayInputStream(data), finalOut, dpi);
		finalOut.flush();
		
	}
	@Test
	public void testRender_fading_DECO_6213_NOK() throws Exception {
		
		File mcfFile = new File("./src/test/resources/test02.mcf");
		OutputStream finalOut = new FileOutputStream("test_6213_NOK.pdf");
		OutputStream xslFoOut = new ByteArrayOutputStream();
		test01converter.convert(
				mcfFile, xslFoOut, dpi, maxPageNo);
		xslFoOut.flush();
		byte[] data = ((ByteArrayOutputStream)xslFoOut).toByteArray();
		PdfUtil.convertFO2PDF(new ByteArrayInputStream(data), finalOut, dpi);
		finalOut.flush();
	}
	
}

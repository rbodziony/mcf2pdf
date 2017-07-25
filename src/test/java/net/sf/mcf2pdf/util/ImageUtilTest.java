package net.sf.mcf2pdf.util;

import java.io.File;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.gvt.GraphicsNode;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.svg.SVGDocument;

import net.sf.mcf2pdf.mcfelements.util.ImageUtil;

public class ImageUtilTest {
	
	  File clpWithViewbox= new File("./src/test/resources/test01/Resources/photofun/decorations/6202-DECO-CC-clip.clp");
//    .\\src\\test\\resources\\test01\\Resources\\photofun\\decorations
      File clpWithoutViewBox =  new File("./src/test/resources/test01/Resources/photofun/decorations/6213-DECO-CC-clip.clp");
      UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
      BridgeContext bridgeContext = new BridgeContext(userAgentAdapter);

	@Test
	@Ignore("Currently disabled as no longer used by CEWE?!")
	public void testPngWithRes() throws Exception {
		float[] res = ImageUtil.getImageResolution(new File("./src/test/resources/Bertrand1-figure-withres.png"));
		Assert.assertEquals(72.009f, res[0], 0.01f);
		Assert.assertEquals(72.009f, res[1], 0.01f);
	}

	@Test
	public void testPngWithoutRes() throws Exception {
		float[] res = ImageUtil.getImageResolution(new File("./src/test/resources/Bertrand1-figure.png"));
		Assert.assertEquals(180.0f, res[0], 0.01f);
		Assert.assertEquals(180.0f, res[1], 0.01f);
	}
	
	  @Test
      public void testClpWithViewBox() throws Exception {
            UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
            BridgeContext bridgeContext = new BridgeContext(userAgentAdapter);
            SVGDocument svgDocument = ImageUtil.getSVGDocument(clpWithViewbox);
            GraphicsNode rootSvgNode = ImageUtil.getRootNode(svgDocument, bridgeContext);
            float[] vb = ImageUtil.getViewBox(bridgeContext, svgDocument);
            Assert.assertNotNull(vb);
      }
      @Test
      public void testClpWithoutViewBox() throws Exception {
            UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
            BridgeContext bridgeContext = new BridgeContext(userAgentAdapter);
            SVGDocument svgDocument = ImageUtil.getSVGDocument(clpWithoutViewBox);
            GraphicsNode rootSvgNode = ImageUtil.getRootNode(svgDocument, bridgeContext);
            float[] vb = ImageUtil.getViewBox(bridgeContext, svgDocument);
            Assert.assertNotNull(vb);
      }
	

}

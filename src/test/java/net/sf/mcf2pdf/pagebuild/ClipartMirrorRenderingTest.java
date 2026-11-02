package net.sf.mcf2pdf.pagebuild;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import javax.imageio.ImageIO;
import org.apache.commons.digester3.Digester;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import net.sf.mcf2pdf.mcfelements.*;
import net.sf.mcf2pdf.mcfelements.impl.*;
import net.sf.mcf2pdf.mcfglobals.*;
public class ClipartMirrorRenderingTest {
 @Rule public TemporaryFolder tmp = new TemporaryFolder();
 private McfFotobook parse(String xml) throws Exception {
  Digester d = new Digester();
  new DigesterConfiguratorImpl().configureDigester(d, null);
  return d.parse(new StringReader(xml));
 }

 @Test public void mirrorsParsedClipartBeforeRotation() throws Exception {
  File svg=tmp.newFile("arrow.svg");
  Files.writeString(svg.toPath(),"<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"20\" height=\"20\"><rect width=\"10\" height=\"10\" fill=\"red\"/></svg>");
  McfResourceScanner resources=mock(McfResourceScanner.class);
  net.sf.mcf2pdf.mcfconfig.Fading config=new net.sf.mcf2pdf.mcfconfig.Fading();
  net.sf.mcf2pdf.mcfconfig.Clipart clip=new net.sf.mcf2pdf.mcfconfig.Clipart();
  clip.setFile("frame.svg");config.setClipart(clip);config.setFile("mask.svg");
  when(resources.getDecoration("7")).thenReturn(config);
  when(resources.getClip("frame")).thenReturn(svg);
  PageRenderContext ctx=new PageRenderContext(254,1,resources,null);
  for(String mirror:new String[]{"none","x","y","both"}) {
   McfFotobook book=parse("<fotobook><page><area><position width=\"20\" height=\"20\"/>"
    +"<clipart designElementId=\"7\"><ClipartConfiguration mirror=\""+mirror+"\"/></clipart></area></page></fotobook>");
   McfClipart c=(McfClipart)book.getPages().get(0).getAreas().get(0).getContent();
   assertEquals(mirror,c.getMirror());
   Point offset=new Point(); BufferedImage result=new PageClipart(c).renderAsBitmap(ctx,offset,0,0);
   boolean x=mirror.equals("x")||mirror.equals("both"), y=mirror.equals("y")||mirror.equals("both");
   assertEquals(Color.RED.getRGB(), result.getRGB(x?15:5,y?15:5));
   assertEquals(0,result.getRGB(x?5:15,y?15:5)>>>24);
   assertEquals(new Point(),offset);
  }
 }

}

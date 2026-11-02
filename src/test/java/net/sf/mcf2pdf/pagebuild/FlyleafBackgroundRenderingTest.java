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
public class FlyleafBackgroundRenderingTest {
 @Rule public TemporaryFolder tmp = new TemporaryFolder();
 private McfFotobook parse(String xml) throws Exception {
  Digester d = new Digester();
  new DigesterConfiguratorImpl().configureDigester(d, null);
  return d.parse(new StringReader(xml));
 }

 @Test public void emptyPageUsesFlyleafColorWithoutResourceLookup() throws Exception {
  for(String color:new String[]{"white","black","#d26838",""}) {
   String addon=color.isEmpty()?"":"<addOns><addOn variant=\"flyleafcolor:"+color+"\"/></addOns>";
   McfFotobook book=parse("<fotobook>"+addon+"<page type=\"emptypage\"><background designElementId=\"99\" type=\"1\"/></page></fotobook>");
   McfResourceScanner resources=mock(McfResourceScanner.class);
   BufferedImage result=new PageBackground(book.getPages().get(0).getBackgrounds(),java.util.Collections.emptyList())
    .renderAsBitmap(new PageRenderContext(254,1,resources,null),new Point(),40,20);
   int rgb=color.equals("black")?Color.BLACK.getRGB():color.startsWith("#")?Color.decode(color).getRGB():Color.WHITE.getRGB();
   assertEquals(rgb,result.getRGB(5,5));
   assertEquals(0,result.getRGB(35,5)>>>24);
   verifyNoInteractions(resources);
  }
 }
 @Test public void normalPage99StillUsesResourceLookup() throws Exception {
  McfFotobook book=parse("<fotobook><page type=\"normalpage\"><background designElementId=\"99\" type=\"1\"/></page></fotobook>");
  McfResourceScanner resources=mock(McfResourceScanner.class);
  new PageBackground(book.getPages().get(0).getBackgrounds(),java.util.Collections.emptyList())
   .renderAsBitmap(new PageRenderContext(254,1,resources,null),new Point(),40,20);
  verify(resources,atLeastOnce()).getImage("99");
 }

}

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
public class ModernImageBackgroundTest {
 @Rule public TemporaryFolder tmp = new TemporaryFolder();
 private McfFotobook parse(String xml) throws Exception {
  Digester d = new Digester();
  new DigesterConfiguratorImpl().configureDigester(d, null);
  return d.parse(new StringReader(xml));
 }

 @Test public void nestedCutoutRendersAllPositionsAtDifferentDpi() throws Exception {
  BufferedImage source = new BufferedImage(80, 40, BufferedImage.TYPE_INT_RGB);
  Graphics2D g = source.createGraphics(); g.setColor(Color.RED); g.fillRect(0,0,40,40);
  g.setColor(Color.BLUE); g.fillRect(40,0,40,40); g.dispose();
  File dir = tmp.newFolder(); ImageIO.write(source,"png",new File(dir,"photo.png"));
  for (String position : new String[]{"LEFT_OR_TOP","RIGHT_OR_BOTTOM","BUNDLE"}) {
   McfFotobook book = parse("<fotobook version=\"4.0\" imagedir=\".\"><page><area><position width=\"80\" height=\"80\"/>"
    +"<imagebackground filename=\"photo.png\" backgroundPosition=\""+position+"\"><cutout scale=\"2\" left=\"-80\" top=\"0\"/>"
    +"</imagebackground></area></page></fotobook>");
   ((McfFotobookImpl)book).setFile(new File(dir,"book.mcf"));
   McfImageBackground model=(McfImageBackground)book.getPages().get(0).getAreas().get(0).getContent();
   assertEquals(2,model.getScale(),0.001);
   McfAlbumType album=mock(McfAlbumType.class); when(album.getUsableWidth()).thenReturn(100);
   when(album.getBleedMargin()).thenReturn(10);
   for(int dpi:new int[]{127,254}) {
    PageRenderContext ctx=new PageRenderContext(dpi,1,null,album); Point offset=new Point();
    BufferedImage result=new PageImageBackground(model).renderAsBitmap(ctx,offset,0,0);
    assertEquals(Color.BLUE.getRGB(),result.getRGB(0,0));
    assertEquals(Color.BLUE.getRGB(),result.getRGB(result.getWidth()-1,result.getHeight()-1));
    assertEquals("RIGHT_OR_BOTTOM".equals(position)?ctx.toPixel(11):0,offset.x);
   }
  }
 }
 @Test public void legacyScaleStillUsesImageResolution() {
  McfImageBackgroundImpl model=new McfImageBackgroundImpl(); model.setScale(2);
  assertEquals(30,new PageImageBackground(model).sourceExtent(10,6,2),0.001);
 }

}

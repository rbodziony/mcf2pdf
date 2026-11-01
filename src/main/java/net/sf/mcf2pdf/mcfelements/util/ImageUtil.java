/*******************************************************************************
 * ${licenseText}
 * All rights reserved. This file is made available under the terms of the
 * Common Development and Distribution License (CDDL) v1.0 which accompanies
 * this distribution, and is available at
 * http://www.opensource.org/licenses/cddl1.txt
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;
//import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGPreserveAspectRatio;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.xmp.XmpDirectory;

import net.sf.mcf2pdf.mcfelements.util.webp.Webp;
import net.sf.mcf2pdf.mcfelements.util.webp.WebpLib;

/**
 * Utility class for working with images in the context of the mcf2pdf project.
 */
public final class ImageUtil {

	private final static Log log = LogFactory.getLog(ImageUtil.class);
	
	/*
	 * FIXME this uses CEWE Fotobook for MY personal pictures when they do not
	 * have resolution information.
	 * Don't know if this is default, or just taken from other photos in that MCF file?
	 */
	private static final float DEFAULT_RESOLUTION = 180.0f;

	public static final float MM_PER_INCH = 25.4f;

	public static final double SQRT_2 = Math.sqrt(2);

	private static WebpLib qt5Library;

	private ImageUtil() {
	}

	/**
	 * Retrieves resolution information from the given image file. As CEWE algorithm seems to have changed, always returns default
	 * resolution for JPEG files.
	 *
	 * @return An array containing the x- and the y-resolution, in dots per inch, of the given file.
	 *
	 * @throws IOException If any I/O related problem occurs reading the file.
	 */
	public static float[] getImageResolution(File imageFile) throws IOException {

		return new float[] { DEFAULT_RESOLUTION, DEFAULT_RESOLUTION };
	}

	public static BufferedImage readImage(File imageFile) throws IOException {
		int orientation = getImageOrientation(imageFile);
		BufferedImage img = internalRead(imageFile);

		if (orientation < 2 || orientation > 8) {
			return img;
		}

		int w = img.getWidth();
		int h = img.getHeight();
		boolean swapXY = orientation >= 5;

		// AffineTransform(m00, m10, m01, m11, m02, m12): x' = m00*x + m01*y + m02, y' = m10*x + m11*y + m12
		AffineTransform tx;
		switch (orientation) {
		case 2:
			tx = new AffineTransform(-1, 0, 0, 1, w, 0);
			break;
		case 3:
			tx = new AffineTransform(-1, 0, 0, -1, w, h);
			break;
		case 4:
			tx = new AffineTransform(1, 0, 0, -1, 0, h);
			break;
		case 5:
			tx = new AffineTransform(0, 1, 1, 0, 0, 0);
			break;
		case 6:
			tx = new AffineTransform(0, 1, -1, 0, h, 0);
			break;
		case 7:
			tx = new AffineTransform(0, -1, -1, 0, h, w);
			break;
		default:
			tx = new AffineTransform(0, -1, 1, 0, 0, w);
			break;
		}

		BufferedImage rotated = new BufferedImage(swapXY ? h : w, swapXY ? w : h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = rotated.createGraphics();
		g2d.drawImage(img, tx, null);
		g2d.dispose();

		return rotated;
	}

	private static int getImageOrientation(File imageFile) throws IOException {
		try {
			if(imageFile.getName().endsWith("svg")) return 0;
			Metadata md = ImageMetadataReader.readMetadata(imageFile);

			ExifDirectoryBase ed = md.getFirstDirectoryOfType(ExifDirectoryBase.class);

			int o = 0;
			if (ed != null && ed.containsTag(ExifDirectoryBase.TAG_ORIENTATION)) {
				o = ed.getInt(ExifDirectoryBase.TAG_ORIENTATION);
			}
			if (o == 0) {
				o = getXmpOrientation(md);
			}
			if (o == 0 && imageFile.getName().toLowerCase().endsWith(".png")) {
				o = getPngRawProfileOrientation(imageFile);
			}
			return o;
		} catch (ImageProcessingException e) {
			throw new IOException(e);
		} catch (MetadataException e) {
			return 0;
		}

	}

	private static int getXmpOrientation(Metadata md) {
		for (XmpDirectory xmp : md.getDirectoriesOfType(XmpDirectory.class)) {
			try {
				if (xmp.getXMPMeta() == null) {
					continue;
				}
				Integer o = xmp.getXMPMeta().getPropertyInteger("http://ns.adobe.com/tiff/1.0/", "Orientation");
				if (o != null) {
					return o;
				}
			} catch (Exception e) {
				// ignore unreadable XMP
			}
		}
		return 0;
	}

	/**
	 * Reads the orientation from a "Raw profile type exif" text chunk, as written by GIMP and ImageMagick, which
	 * metadata-extractor does not interpret.
	 */
	private static int getPngRawProfileOrientation(File png) {
		try (java.io.DataInputStream in = new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(png)))) {
			in.skipBytes(8);
			while (true) {
				int len = in.readInt();
				byte[] type = new byte[4];
				in.readFully(type);
				String t = new String(type, java.nio.charset.StandardCharsets.US_ASCII);
				if (t.equals("IDAT") || t.equals("IEND") || len < 0) {
					return 0;
				}
				byte[] data = new byte[len];
				in.readFully(data);
				in.skipBytes(4);
				if (t.equals("tEXt") || t.equals("zTXt")) {
					int nul = 0;
					while (nul < data.length && data[nul] != 0) {
						nul++;
					}
					if (nul >= data.length || !new String(data, 0, nul, java.nio.charset.StandardCharsets.ISO_8859_1)
							.equals("Raw profile type exif")) {
						continue;
					}
					byte[] text;
					if (t.equals("zTXt")) {
						java.util.zip.Inflater inf = new java.util.zip.Inflater();
						inf.setInput(data, nul + 2, data.length - nul - 2);
						java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
						byte[] buf = new byte[4096];
						while (!inf.finished()) {
							int n = inf.inflate(buf);
							if (n == 0 && (inf.needsInput() || inf.needsDictionary())) {
								break;
							}
							bos.write(buf, 0, n);
						}
						inf.end();
						text = bos.toByteArray();
					} else {
						text = java.util.Arrays.copyOfRange(data, nul + 1, data.length);
					}
					return parseRawProfileOrientation(new String(text, java.nio.charset.StandardCharsets.ISO_8859_1));
				}
			}
		} catch (Exception e) {
			return 0;
		}
	}

	private static int parseRawProfileOrientation(String text) {
		String[] lines = text.trim().split("\n", 3);
		if (lines.length < 3) {
			return 0;
		}
		String hex = lines[2].replaceAll("\\s", "");
		byte[] b = new byte[hex.length() / 2];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		int base = (b.length > 6 && b[0] == 'E' && b[1] == 'x') ? 6 : 0;
		if (b.length < base + 8) {
			return 0;
		}
		boolean le = b[base] == 'I';
		java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(b).order(le ? java.nio.ByteOrder.LITTLE_ENDIAN : java.nio.ByteOrder.BIG_ENDIAN);
		int ifd = base + bb.getInt(base + 4);
		int count = bb.getShort(ifd) & 0xffff;
		for (int i = 0; i < count; i++) {
			int e = ifd + 2 + i * 12;
			if (e + 12 > b.length) {
				break;
			}
			if ((bb.getShort(e) & 0xffff) == 0x0112) {
				return bb.getShort(e + 8) & 0xffff;
			}
		}
		return 0;
	}

	/**
	 * Loads the given CLP or SVG file and creates a BufferedImage with the given dimensions. As CLP files contain Vector images,
	 * they can be scaled to every size needed. The contents are scaled to the given width and height, <b>not</b> preserving any
	 * ratio of the image.
	 *
	 * @param clpFile CLP or SVG file.
	 * @param widthPixel The width, in pixels, the resulting image shall have.
	 * @param heightPixel The height, in pixels, the resulting image shall have.
	 *
	 * @return An image displaying the contents of the loaded CLP file.
	 *
	 * @throws IOException If any I/O related problem occurs reading the file.
	 */
	public static BufferedImage loadClpFile(File clpFile, int widthPixel, int heightPixel) throws IOException {
		
		UserAgentAdapter userAgentAdapter = new UserAgentAdapter();
        BridgeContext bridgeContext = new BridgeContext(userAgentAdapter);

        SVGDocument svgDocument;
        GraphicsNode rootSvgNode;
        svgDocument = getSVGDocument(clpFile);
        rootSvgNode = getRootNode(svgDocument, bridgeContext);
		float viewportWidth = svgDocument.getRootElement().getWidth().getBaseVal().getValue();
		float viewportHeight = svgDocument.getRootElement().getHeight().getBaseVal().getValue();
		if (viewportWidth <= 0 || viewportHeight <= 0) {
			throw new IOException("Could not determine SVG viewport dimensions: " + clpFile.getAbsolutePath());
		}
		AffineTransform usr2dev = AffineTransform.getScaleInstance(
				widthPixel / viewportWidth, heightPixel / viewportHeight);

		BufferedImage img = new BufferedImage(widthPixel, heightPixel, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();

		g2d.setColor(new Color(0.0f, 0.0f, 0.0f, 0.0f));
		g2d.fillRect(0, 0, widthPixel, heightPixel);
		g2d.transform(usr2dev);

		// fixes "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" - part 1
		final Object oldBufferedImage = g2d
				.getRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE);
		g2d.setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE,
				new WeakReference<BufferedImage>(img));
		rootSvgNode.paint(g2d);
		// fixes "Graphics2D from BufferedImage lacks BUFFERED_IMAGE hint" - part 2
		if (oldBufferedImage != null)
			g2d.setRenderingHint(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE,
					oldBufferedImage);
		else
			g2d.getRenderingHints().remove(RenderingHintsKeyExt.KEY_BUFFERED_IMAGE);

		g2d.dispose();
		return img;
	}

	public static void replaceColors(BufferedImage image, Map<Color, Color> colorMappings) {
		if (colorMappings.isEmpty())
			return;

		Map<Integer, Integer> rgbMappings = new HashMap<Integer, Integer>();
		for (Map.Entry<Color, Color> mapping : colorMappings.entrySet()) {
			rgbMappings.put(mapping.getKey().getRGB() & 0x00ffffff,
					mapping.getValue().getRGB() & 0x00ffffff);
		}

		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				Integer replacementRgb = rgbMappings.get(argb & 0x00ffffff);
				if (replacementRgb != null)
					image.setRGB(x, y, (argb & 0xff000000) | replacementRgb);
			}
		}
	}

	 /**
     * Returns SVG document from File
      * @param  clpFile to file clp/svg.
     *
      *
     * @return SVG Document
      * @throws IOException If any I/O related problem occurs reading the file.
     */
     public static SVGDocument getSVGDocument(File clpFile)
                  throws IOException, UnsupportedEncodingException {
           FileInputStream fis = new FileInputStream(clpFile);
           ClpInputStream cis = null;
           InputStream in = clpFile.getName().toLowerCase().endsWith(".clp") ? (cis = new ClpInputStream(fis)) : fis;
           SVGDocument svgDocument;
           try {
         String parser = XMLResourceDescriptor.getXMLParserClassName();
         SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
                  svgDocument = (SVGDocument)factory.createDocument(clpFile.toURI().toString(), new InputStreamReader(in, "ISO-8859-1"));
           }
           finally {
                  IOUtils.closeQuietly(cis);
                  IOUtils.closeQuietly(fis);
           }
           return svgDocument;
     }
	
	/**
     * Create default array view box for scaling svg or clp files based on width/height of image attributes
      * @param svgDocument SVGDocument.
     *
     * @return float array with default viewbox for clp/or/svg file based on width/height of SVGDocument
     *
     */
     private static float[] createDefaultViewBox(SVGDocument svgDocument) {

           Element svgRoot = svgDocument.getDocumentElement();
           Attr width =null;
           Attr height = null;
           width = svgRoot.getAttributeNodeNS(null, "width");   
           if(width!=null) {
                  log.debug("width in file="+width.getValue());
           }
           height = svgRoot.getAttributeNodeNS(null, "height");
           if(height!=null) {
                  log.debug("height in file="+height.getValue());
           }
           log.debug("Creating default viewbox with 0f,0.0f,"+width.getValue()+","+height.getValue());
           return new float[] {0.0f,0.0f,Float.parseFloat(width.getValue()),Float.parseFloat(height.getValue())};
     }

     /**
     * Returns viewbox array from document or create default
      * @param svgDocument SVGDocument.
     * @param bridgeContext BridgeContext
     *
     * @return float array with default viewbox for clp/or/svg file based on width/height of SVGDocument
     *
     */
     public static float[] getViewBox(BridgeContext bridgeContext,
                  SVGDocument svgDocument) {
           float[] vb = ViewBox.parseViewBoxAttribute(svgDocument.getRootElement(),
                         svgDocument.getRootElement().getAttribute("viewBox"), bridgeContext);
           if( vb == null )
           {
                  log.debug("viewbox empty");
                  vb = createDefaultViewBox(svgDocument);
           }
           return vb;
     }
	
	private static GraphicsNode getRootNode(SVGDocument document, BridgeContext bridgeContext) {
		// Build the tree and get the document dimensions
		GVTBuilder builder = new GVTBuilder();
		return builder.build(bridgeContext, document);
	}

	/**
	 * Rotates the given buffered image by the given angle, and returns a newly
	 * created image, containing the rotated image.
	 *
	 * @param img Image to rotate.
	 * @param angle Angle, in radians, by which to rotate the image.
	 * @param drawOffset Receives the offset which is required to draw the image,
	 * relative to the original (0,0) corner, so that the center of the image is
	 * still on the same position.
	 *
	 * @return A newly created image containing the rotated image.
	 */
	public static BufferedImage rotateImage(BufferedImage img, float angle, Point drawOffset) {
		int w = img.getWidth();
		int h = img.getHeight();

		AffineTransform tf = AffineTransform.getRotateInstance(angle,
				w / 2.0,  h / 2.0);

		// get coordinates for all corners to determine real image size
		Point2D[] ptSrc = new Point2D[4];
		ptSrc[0] = new Point(0, 0);
		ptSrc[1] = new Point(w, 0);
		ptSrc[2] = new Point(w, h);
		ptSrc[3] = new Point(0, h);

		Point2D[] ptTgt = new Point2D[4];
		tf.transform(ptSrc, 0, ptTgt, 0, ptSrc.length);

		Rectangle rc = new Rectangle(0, 0, w, h);

		for (Point2D p : ptTgt) {
			if (p.getX() < rc.x) {
				rc.width += rc.x - p.getX();
				rc.x = (int) p.getX();
			}
			if (p.getY() < rc.y) {
				rc.height += rc.y - p.getY();
				rc.y = (int) p.getY();
			}
			if (p.getX() > rc.x + rc.width)
				rc.width = (int) (p.getX() - rc.x);
			if (p.getY() > rc.y + rc.height)
				rc.height = (int) (p.getY() - rc.y);
		}

		BufferedImage imgTgt = new BufferedImage(rc.width, rc.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = imgTgt.createGraphics();

		// create a NEW rotation transformation around new center
		tf = AffineTransform.getRotateInstance(angle, rc.getWidth() / 2, rc.getHeight() / 2);
		g2d.setTransform(tf);
		g2d.drawImage(img, -rc.x, -rc.y, null);
		g2d.dispose();

		drawOffset.x += rc.x;
		drawOffset.y += rc.y;

		return imgTgt;
	}

	private static BufferedImage internalRead(File f) throws IOException {
		// special treatment for webp files
		if (f.getName().toLowerCase(Locale.US).endsWith(".webp")) {
			if (qt5Library == null) {
				qt5Library = Webp.loadLibrary();
			}

			return Webp.loadWebPImage(f, qt5Library);
		}

		BufferedImage ret = null;
		try {
			ret = ImageIO.read(f);
		} catch (IOException o){
			if( o.getMessage().contains("Unsupported JPEG process: SOF type")) {
				log.error("Cant read file "+f.toString()+ " convert file to png/tiff/gif");
				throw o;
			}
		}
		return ret;
	}

}

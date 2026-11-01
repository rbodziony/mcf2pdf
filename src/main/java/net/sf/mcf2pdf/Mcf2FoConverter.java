/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfBackground;
import net.sf.mcf2pdf.mcfelements.McfClipart;
import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.McfImage;
import net.sf.mcf2pdf.mcfelements.McfImageBackground;
import net.sf.mcf2pdf.mcfelements.McfPage;
import net.sf.mcf2pdf.mcfelements.McfText;
import net.sf.mcf2pdf.mcfelements.McfxProject;
import net.sf.mcf2pdf.mcfelements.FotobookBuilder;
import net.sf.mcf2pdf.mcfelements.util.XslFoDocumentBuilder;
import net.sf.mcf2pdf.mcfglobals.McfAlbumType;
import net.sf.mcf2pdf.mcfglobals.McfProductCatalogue;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;
import net.sf.mcf2pdf.pagebuild.BitmapPageBuilder;
import net.sf.mcf2pdf.pagebuild.PageBackground;
import net.sf.mcf2pdf.pagebuild.PageBinding;
import net.sf.mcf2pdf.pagebuild.PageBuilder;
import net.sf.mcf2pdf.pagebuild.PageClipart;
import net.sf.mcf2pdf.pagebuild.PageImage;
import net.sf.mcf2pdf.pagebuild.PageImageBackground;
import net.sf.mcf2pdf.pagebuild.PageNum;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;
import net.sf.mcf2pdf.pagebuild.PageText;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.output.XMLOutputter;
import org.xml.sax.SAXException;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.ShlObj;
import com.sun.jna.platform.win32.WinDef;

/**
 * The core class to convert MCF files to XSL-FO data (which then can be used
 * to render page based documents, e.g. PDF files).
 */
public class Mcf2FoConverter {

	private final static Log log = LogFactory.getLog(Mcf2FoConverter.class);

	private Set<McfProductCatalogue> productCatalogues = new HashSet<McfProductCatalogue>();

	private McfResourceScanner resources;

	private McfAlbumType albumType;

	private PageBuilder currentPage;

	private PageRenderContext context;

	private File tempImageDir;

	/** A4 portrait width, in mm. */
	public static final float A4_WIDTH_MM = 210f;

	/** A4 portrait height, in mm. */
	public static final float A4_HEIGHT_MM = 297f;

	private boolean splitToA4;

	private boolean splitPages;

	private boolean splitCover;

	/**
	 * Creates a new converter object with given settings.
	 *
	 * @param mcfInstallDir The installation directory of the MCF Software.
	 * @param mcfTempDir The temporary directory of the MCF Software to use. This
	 * is normally <code>&lt;USER_HOME>/.mcf</code>.
	 * @param tempImageDir The temporary directory to use for storing image files
	 * (created by this converter object). This directory is created if it does not exist.
	 *
	 * @throws IOException If any I/O problem occurs when reading files in the
	 * installation directory, or the temporary directory could not be created.
	 *
	 * @throws SAXException If any of the XML files in the installation directory
	 * which are parsed have a format error.
	 */
	public Mcf2FoConverter(File mcfInstallDir, File mcfTempDir, File tempImageDir,File hpsDirPath) throws IOException, SAXException {
		this.tempImageDir = tempImageDir;

		// get all products - scan ALL XML files in Resources for fotobookdefinitions
		File resourcesDir = new File(mcfInstallDir, "Resources");
		if (!resourcesDir.isDirectory()) {
			throw new IOException("MCF Resources Directory " + resourcesDir.getAbsolutePath() + " does not exist");
		}

		scanForProducts(resourcesDir, McfProductCatalogue.CatalogueVersion.PRE_V6);
		File productsDir = new File(resourcesDir, "products");
		if (productsDir.isDirectory()) {
			scanForProducts(productsDir, McfProductCatalogue.CatalogueVersion.V6);
		}

		// search all resources
		// on Windows, also scan "<Common Application Data>/hps", if existing
		File hpsDir = null;
		if (hpsDirPath == null) {
			hpsDir = null;
			try {
				char[] pszPath = new char[WinDef.MAX_PATH];
				if (Shell32.INSTANCE.SHGetFolderPath(null, ShlObj.CSIDL_COMMON_APPDATA, null, ShlObj.SHGFP_TYPE_CURRENT, pszPath).intValue() == 0) {
					File f = new File(new File(Native.toString(pszPath)), "hps");
					if (f.isDirectory()) {
						hpsDir = f;
					}
				}
			} catch (Throwable t) {
				// OK, no Windows, obviously
				// try home directory
				File homeHpsDir = new File(new File(System.getProperty("user.home")), ".mcf/hps");
				if (homeHpsDir.isDirectory()) {
					hpsDir = homeHpsDir;
				}
			}
		} else {
			hpsDir = hpsDirPath;
		}
		List<File> scanDirs = new ArrayList<File>();
		scanDirs.add(new File(mcfInstallDir, "Resources"));
		scanDirs.add(mcfTempDir);
		if (hpsDir != null) {
			scanDirs.add(hpsDir);
		}
		log.debug("Searching for MCF resources in " + scanDirs);
		resources = new McfResourceScanner(scanDirs);
		resources.scan();
	}

	/**
	 * Enables A4 output mode. Instead of emitting each double page 1:1 with the
	 * size defined in the MCF file, every double page is split into its left and
	 * right page, and each one is scaled to fit an A4 page. The cover (back,
	 * spine and front) is scaled to fit one A4 page. The A4 orientation follows
	 * the shape of the album page (landscape album pages give landscape A4).
	 *
	 * @param splitToA4 <code>true</code> to enable A4 output.
	 */
	public void setSplitToA4(boolean splitToA4) {
		this.splitToA4 = splitToA4;
	}

	/**
	 * Enables single page output. Every double page is split into its left and
	 * right page, and each one becomes a PDF page in the original album page
	 * size. The cover (back, spine and front) stays one page in its original
	 * size. Ignored if A4 output is enabled, which splits pages as well.
	 *
	 * @param splitPages <code>true</code> to enable single page output.
	 */
	public void setSplitPages(boolean splitPages) {
		this.splitPages = splitPages;
	}

	/**
	 * Enables cover splitting. The cover is cut into back cover, spine and
	 * front cover. The front cover becomes the first page of the document, the
	 * back cover the last one, and the spine is skipped. The cover parts keep
	 * their original size, or are fitted onto A4 if A4 output is enabled.
	 *
	 * @param splitCover <code>true</code> to enable cover splitting.
	 */
	public void setSplitCover(boolean splitCover) {
		this.splitCover = splitCover;
	}

	/**
	 * Performs conversion of the given MCF input file to XSL-FO data.
	 *
	 * @param mcfFile MCF input file.
	 * @param xslFoOut Stream to write the XSL-FO data to.
	 * @param dpi Resolution, in dots per inch, to use for image rendering. The
	 * higher the value, the better the quality, but the more RAM is consumed.
	 * @param maxPageNo Renders only up to the given page number (e.g. for testing settings).
	 * A value of -1 indicates to render all pages.
	 *
	 * @throws IOException If any I/O related problem occurs, e.g. reading the
	 * input file or any referenced image files.
	 *
	 * @throws SAXException If any XML related problem occurs, e.g. the input file
	 * has an invalid format.
	 */
	public void convert(File mcfFile, OutputStream xslFoOut, int dpi, boolean binding, boolean pageNum, int maxPageNo, double sx) throws IOException, SAXException {
		convert(mcfFile, xslFoOut, dpi, binding, pageNum, maxPageNo, sx, null);
	}

	/**
	 * Performs conversion of the given MCF input file to XSL-FO data.
	 * Supports both .mcf files and native CEWE .mcfx databases.
	 *
	 * @param mcfFile MCF input file (.mcf) or database file (.mcfx).
	 * @param xslFoOut Stream to write the XSL-FO data to.
	 * @param dpi Resolution, in dots per inch, to use for image rendering. The
	 * higher the value, the better the quality, but the more RAM is consumed.
	 * @param maxPageNo Renders only up to the given page number (e.g. for testing settings).
	 * A value of -1 indicates to render all pages.
	 * @param dbFilename Optional MCF entry selection when a database contains
	 * more than one .mcf file.
	 *
	 * @throws IOException If any I/O related problem occurs, e.g. reading the
	 * input file or any referenced image files.
	 *
	 * @throws SAXException If any XML related problem occurs, e.g. the input file
	 * has an invalid format.
	 */
	public void convert(File mcfFile, OutputStream xslFoOut, int dpi, boolean binding, boolean pageNum, int maxPageNo, double sx, String dbFilename) throws IOException, SAXException {
		if (mcfFile.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".mcfx")) {
			try (McfxProject project = McfxProject.extract(mcfFile, dbFilename)) {
				convertMcf(project.getMcfFile(), xslFoOut, dpi, binding, pageNum, maxPageNo, sx);
			}
		} else {
			convertMcf(mcfFile, xslFoOut, dpi, binding, pageNum, maxPageNo, sx);
		}
	}

	private void convertMcf(File mcfFile, OutputStream xslFoOut, int dpi, boolean binding, boolean pageNum,
			int maxPageNo, double sx) throws IOException, SAXException {
		// build MCF DOM
		log.debug("Reading MCF file");
		McfFotobook book = new FotobookBuilder().readFotobook(mcfFile);

		// retrieve global information
		File baseDir = mcfFile.getParentFile().getAbsoluteFile();
		File imageDir = new File(baseDir, book.getImageDir());
		if (!imageDir.isDirectory()) {
			log.warn("Image directory '" + imageDir.getAbsolutePath() + "' (imagedir=\"" + book.getImageDir()
					+ "\" in the project) does not exist; images will only be found next to the project file");
		}

		albumType = getAlbumType(book.getProductName());
		// fallback workaround
		if (albumType == null && book.getProductName().contains("_")) {
			String fixedType = book.getProductName().substring(0, book.getProductName().indexOf("_"));
			albumType = getAlbumType(fixedType);
			if (albumType != null)
				log.warn("Album Type " + book.getProductName() + " not found, falling back to " + fixedType);
		}

		if (albumType == null)
			throw new IOException("No album type definition found for used print product '" + book.getProductName() + "'");

		// prepare page render context
		context = new PageRenderContext(dpi, sx, resources, albumType);

		// create XSL-FO document
		XslFoDocumentBuilder docBuilder = new XslFoDocumentBuilder();

		// setting page widths for master pages
		int coverPageWidthPX = -1;
		int coverPageHeightPX = -1;
		int pageWidthPX = -1;
		int pageHeightPX = -1;
		
		for (McfPage p : book.getPages()) {
			if (McfPage.FULLCOVER.matcher(p.getType()).matches() && coverPageWidthPX == -1 && coverPageHeightPX == -1) {
				coverPageWidthPX = context.toPixel(p.getBundlesize().getWidth() / 10.0f);
				coverPageHeightPX = context.toPixel(p.getBundlesize().getHeight() / 10.0f);
			} else if (McfPage.CONTENT.matcher(p.getType()).matches()&& pageWidthPX == -1 && pageHeightPX == -1 ) {
				pageWidthPX = context.toPixel(p.getBundlesize().getWidth() / 10.0f);
				pageHeightPX = context.toPixel(p.getBundlesize().getHeight() / 10.0f);
			}
			if (coverPageWidthPX != -1 && coverPageHeightPX != -1 && pageWidthPX != -1 && pageHeightPX != -1) {
				break;
			}
		}
		
		// setting master pages
		int a4ShortPX = context.toPixel(A4_WIDTH_MM);
		int a4LongPX = context.toPixel(A4_HEIGHT_MM);
		// in A4 mode, orientation follows the shape of a single album page / the cover
		boolean pageLandscape = pageWidthPX / 2 > pageHeightPX;
		boolean coverLandscape = coverPageWidthPX > coverPageHeightPX;
		int a4PageW = pageLandscape ? a4LongPX : a4ShortPX;
		int a4PageH = pageLandscape ? a4ShortPX : a4LongPX;
		int a4CoverW = coverLandscape ? a4LongPX : a4ShortPX;
		int a4CoverH = coverLandscape ? a4ShortPX : a4LongPX;
		boolean split = splitToA4 || splitPages;
		int singlePageW = pageWidthPX / 2;

		// cover split: back cover | spine | front cover
		int spinePX = 0;
		int coverPartW = coverPageWidthPX;
		if (splitCover && coverPageWidthPX > 0) {
			spinePX = computeSpineWidthPX(book, coverPageWidthPX, singlePageW);
			coverPartW = (coverPageWidthPX - spinePX) / 2;
			boolean partLandscape = coverPartW > coverPageHeightPX;
			a4CoverW = partLandscape ? a4LongPX : a4ShortPX;
			a4CoverH = partLandscape ? a4ShortPX : a4LongPX;
		}
		int coverOutW = splitCover ? coverPartW : coverPageWidthPX;

		if (splitToA4) {
			docBuilder.addPageMaster("cover", a4CoverW, a4CoverH);
			docBuilder.addPageMaster("default", a4PageW, a4PageH);
		} else if (splitPages) {
			docBuilder.addPageMaster("cover", coverOutW, coverPageHeightPX);
			docBuilder.addPageMaster("default", singlePageW, pageHeightPX);
		} else {
			docBuilder.addPageMaster("cover", coverOutW, coverPageHeightPX);
			docBuilder.addPageMaster("default", pageWidthPX, pageHeightPX);
		}

		// prepare temporary folder
		log.debug("Preparing temporary working directory");
		tempImageDir.mkdirs();

		// look for cover
		McfPage leftCover = null;
		McfPage rightCover = null;
		for (McfPage p : book.getPages()) {
			if (McfPage.FULLCOVER.matcher(p.getType()).matches()) {
				if (leftCover == null)
					leftCover = p;
				else
					rightCover = p;
			}
		}

		BitmapPageBuilder coverBuilder = null;
		BufferedImage backCover = null;
		if (leftCover != null) {
			log.info("Rendering cover...");
			coverBuilder = new BitmapPageBuilder(coverPageWidthPX, coverPageHeightPX, context, tempImageDir);
			if (splitToA4) {
				coverBuilder.setOutputPage(a4CoverW, a4CoverH, false, true, true);
			}
			currentPage = coverBuilder;
			processDoublePage(leftCover, rightCover, imageDir, false, false,
					book, coverPageWidthPX, coverPageHeightPX);
			docBuilder.startFlow("cover");
			if (splitCover) {
				// front cover first, back cover is added after the last page
				BufferedImage cover = coverBuilder.renderImage();
				backCover = cover.getSubimage(0, 0, coverPartW, cover.getHeight());
				coverBuilder.addImage(docBuilder,
						cover.getSubimage(cover.getWidth() - coverPartW, 0, coverPartW, cover.getHeight()));
			} else {
				coverBuilder.addToDocumentBuilder(docBuilder);
			}
			docBuilder.endFlow();
		}

		// collect pages
		List<McfPage> normalPages = new Vector<McfPage>();

		for (McfPage p : book.getPages()) {
			if (McfPage.CONTENT.matcher(p.getType()).matches() || McfPage.EMPTY.matcher(p.getType()).matches()) {
				if (maxPageNo < 0 || p.getPageNr() <= maxPageNo)
					normalPages.add(p);
				else
					break;
			}
		}
		if (normalPages.isEmpty()) {
			throw new IOException("No standard pages found for rendering. Perhaps unknown MCF format version?");
		}

		// now, process pages as a pair
		docBuilder.startFlow("default");
		log.debug("Starting rendering of " + normalPages.size() + " pages");
		for (int i = 0; i < normalPages.size(); i += 2) {
			log.info("Rendering pages " + i + "+" + (i+1) + "...");
			McfPage left = normalPages.get(i);
			McfPage right = i + 1 < normalPages.size() ? normalPages.get(i + 1) : null;
			BitmapPageBuilder spread = new BitmapPageBuilder(pageWidthPX, pageHeightPX, context, tempImageDir);
			// binding shadow makes no sense on separate sheets
			if (splitToA4) {
				spread.setOutputPage(a4PageW, a4PageH, true, isPrintable(left), isPrintable(right));
			} else if (splitPages) {
				spread.setOutputPage(singlePageW, pageHeightPX, true, isPrintable(left), isPrintable(right));
			}
			currentPage = spread;
			processDoublePage(left, right, imageDir, binding && !split, pageNum, book, pageWidthPX, pageHeightPX);
			currentPage.addToDocumentBuilder(docBuilder);
		}

		docBuilder.endFlow();

		if (backCover != null) {
			docBuilder.startFlow("cover");
			coverBuilder.addImage(docBuilder, backCover);
			docBuilder.endFlow();
		}

		// create and write document
		log.debug("Creating XSL-FO data");
		new XMLOutputter().output(docBuilder.createDocument(), xslFoOut);
	}

	private void processDoublePage(McfPage leftPage, McfPage rightPage, File imageDir,
			boolean addBinding, boolean addPageNum, McfFotobook book,
			int pageWidth, int pageHeight) throws IOException {
		// handle backgrounds
		PageBackground bg = new PageBackground(
				leftPage == null ? Collections.<McfBackground>emptyList() : leftPage.getBackgrounds(),
				rightPage == null ? Collections.<McfBackground>emptyList() : rightPage.getBackgrounds());
		currentPage.addDrawable(bg);

		// handle "area" elements
		List<McfArea> areas = new Vector<McfArea>();
		if (leftPage != null)
			areas.addAll(leftPage.getAreas());
		if (rightPage != null)
			areas.addAll(rightPage.getAreas());

		for (McfArea a : areas) {
			if (McfArea.IMAGEBACKGROUNDAREA.matcher(a.getAreaType()).matches()) {
				currentPage.addDrawable(new PageImageBackground((McfImageBackground)a.getContent()));
			}
			else if (McfArea.IMAGEAREA.matcher(a.getAreaType()).matches()) {
				currentPage.addDrawable(new PageImage((McfImage)a.getContent()));
			}
			else if (McfArea.CLIPARTAREA.matcher(a.getAreaType()).matches()) {
				currentPage.addDrawable(new PageClipart((McfClipart)a.getContent()));
			}
			else if (McfArea.TEXTAREA.matcher(a.getAreaType()).matches()) {
				currentPage.addDrawable(new PageText((McfText)a.getContent()));
			}
			else if (McfArea.SPINETEXTAREA.matcher(a.getAreaType()).matches()) {
				currentPage.addDrawable(new PageText((McfText)a.getContent()));
			}

		}
		
		if (addPageNum) {
			currentPage.addDrawable(new PageNum(book, leftPage, "left", pageWidth, pageHeight));
			currentPage.addDrawable(new PageNum(book, rightPage, "right", pageWidth, pageHeight));
		}

		if (addBinding) {
			currentPage.addDrawable(new PageBinding(resources.getBinding()));
		}
	}

	/**
	 * Computes the spine width of the cover. The cover consists of back cover,
	 * spine and front cover. Sources, in this order:
	 * <ol>
	 * <li>the cover wrap (<code>coverextrahorizontal</code>) of the album type:
	 * back and front cover are one album page plus the wrap each;</li>
	 * <li>the thickness of a rotated spine text or logo area in the centre of
	 * the cover, which CEWE sizes to the spine;</li>
	 * <li>otherwise everything between two album pages is treated as spine.</li>
	 * </ol>
	 */
	private int computeSpineWidthPX(McfFotobook book, int coverWidthPX, int singlePageWidthPX) {
		int extraPX = context.toPixel(albumType.getCoverExtraHorizontal() / 10.0f);
		int spinePX = coverWidthPX - 2 * (singlePageWidthPX + extraPX);
		if (extraPX > 0 && spinePX >= 0 && spinePX <= coverWidthPX / 3) {
			log.debug("Spine width from album type: " + spinePX + " px");
			return spinePX;
		}

		for (McfPage p : book.getPages()) {
			if (!McfPage.FULLCOVER.matcher(p.getType()).matches()) {
				continue;
			}
			for (McfArea a : p.getAreas()) {
				int rotation = Math.round(a.getRotation()) % 180;
				if (a.getAreaType() == null || !a.getAreaType().toLowerCase(Locale.ROOT).startsWith("spine")
						|| rotation != 90 && rotation != -90) {
					continue;
				}
				// rotated by 90 degrees around its centre: its height is the horizontal extent
				int centerPX = context.toPixel((a.getLeft() + a.getWidth() / 2) / 10.0f);
				int thicknessPX = context.toPixel(a.getHeight() / 10.0f);
				if (Math.abs(centerPX - coverWidthPX / 2) <= coverWidthPX / 100
						&& thicknessPX > 0 && thicknessPX <= coverWidthPX / 3) {
					log.debug("Spine width from " + a.getAreaType() + ": " + thicknessPX + " px");
					return thicknessPX;
				}
			}
		}

		spinePX = coverWidthPX - 2 * singlePageWidthPX;
		if (spinePX < 0 || spinePX > coverWidthPX / 3) {
			spinePX = 0;
		}
		log.warn("Spine width of the cover is unknown; assuming " + spinePX
				+ " px, cover parts may contain a part of the spine");
		return spinePX;
	}

	private static boolean isPrintable(McfPage page) {
		return page != null && !McfPage.EMPTY.matcher(page.getType()).matches();
	}

	private McfAlbumType getAlbumType(String albumTypeName) throws IOException {
		for (McfProductCatalogue cat : productCatalogues) {
			McfAlbumType atype = cat.getAlbumType(albumTypeName);
			if (atype != null)
				return atype;
		}

		return null;
	}

	private void scanForProducts(File productsDir, McfProductCatalogue.CatalogueVersion version) {
		for (File f : productsDir.listFiles((FilenameFilter)FileFilterUtils.suffixFileFilter(".xml"))) {
			// try to read, fail is ok!
			FileInputStream fis = null;
			try {
				log.debug("Checking XML file for product catalogue: " + f);
				fis = new FileInputStream(f);
				McfProductCatalogue cat = McfProductCatalogue.read(fis, version);
				if (cat != null && !cat.isEmpty()) {
					log.debug("Adding product catalogue found in " + f);
					productCatalogues.add(cat);
				}
			}
			catch (Exception e) {
			}
			finally {
				IOUtils.closeQuietly(fis);
			}
		}
	}

}

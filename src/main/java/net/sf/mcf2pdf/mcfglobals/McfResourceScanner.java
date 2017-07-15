/*******************************************************************************
 * ${licenseText}
 *******************************************************************************/
package net.sf.mcf2pdf.mcfglobals;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester3.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.mcf2pdf.mcfconfig.Template;
import net.sf.mcf2pdf.mcfelements.impl.DigesterConfiguratorImpl;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;

import org.apache.commons.digester3.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.mcf2pdf.mcfconfig.Decoration;
import net.sf.mcf2pdf.mcfconfig.Fading;
import net.sf.mcf2pdf.mcfelements.impl.DigesterConfiguratorImpl;

/**
 * "Dirty little helper" which scans installation directory and temporary
 * directory of fotobook software for background images, cliparts, fonts,
 * and masks (fadings). As there is no (known) usable TOC for these, we just
 * take what we find.
 */
public class McfResourceScanner {
	
	private final static Log log = LogFactory.getLog(McfResourceScanner.class);

	private List<File> scanDirs = new ArrayList<File>();

	private Map<String, File> foundImages = new HashMap<String, File>();

	private Map<String, File> foundClips = new HashMap<String, File>();

	private Map<String, Font> foundFonts = new HashMap<String, Font>();
	
	private Map<String, File> foundColors = new HashMap<String, File>();
	
	private Map<String, Fading> foundDecorations = new HashMap<String, Fading>();

	private File foundBinding;

	public McfResourceScanner(List<File> scanDirs) {
		this.scanDirs.addAll(scanDirs);
	}

	public void scan() throws IOException {
		for (File f : scanDirs) {
			scanDirectory(f);
		}
	}

	private void scanDirectory(File dir) throws IOException {
		if (!dir.isDirectory())
			return;

		for (File f : dir.listFiles()) {
			if (f.isDirectory())
				scanDirectory(f);
			else {
				String nm = f.getName().toLowerCase();
				String path = f.getAbsolutePath();
				//log.debug("nm="+nm+" path="+path);
				if (nm.matches("[0-9]+\\.jp(e?)g")) {
					String id = nm.substring(0, nm.indexOf("."));
					foundImages.put(id, f);
				}
				else if (nm.matches(".+\\.(clp|svg)")) {
					String id = f.getName().substring(0, nm.lastIndexOf("."));
					foundClips.put(id, f);
				}
				else if (nm.equals("1_color_backgrounds.xml")) {
					log.debug("Processing 1-color backgrounds " + f.getAbsolutePath());
					List<Template> colors = loadColorsMapping(f);
					if (colors == null) {
						log.warn("Cannot load colors from file " + f.getAbsolutePath());
						continue;
					}
					for (Template color : colors) {
						File colorFile = new File(f.getParent() + '/' + color.getFilename());
						foundColors.put(color.getName(), colorFile);
					}
				}
				else if (nm.equals("backgrounds_default.xml")) {
					log.debug("Processing backgrounds_default backgrounds " + f.getAbsolutePath());
					List<Template> colors = loadColorsMapping(f);
					//log.debug("colors size = "+colors.size());
					if (colors == null) {
						log.warn("Cannot load colors from file " + f.getAbsolutePath());
						continue;
					}
					for (Template color : colors) {
						File colorFile = new File(f.getParent() + '/' + color.getFilename());
						log.debug("adding color="+color.getName()+" at file"+color.getFilename());
						foundColors.put(color.getName(), colorFile);
					}
				}
				else if (nm.equals("multicolor-2012.xml")) {
					log.debug("Processing multicolor-2012 backgrounds " + f.getAbsolutePath());
					List<Template> colors = loadColorsMapping(f);
					//log.debug("colors size = "+colors.size());
					if (colors == null) {
						log.warn("Cannot load colors from file " + f.getAbsolutePath());
						continue;
					}
					for (Template color : colors) {
						File colorFile = new File(f.getParent() + '/' + color.getFilename());
						log.debug("adding color="+color.getName()+" at file"+color.getFilename());
						foundColors.put(color.getName(), colorFile);
					}
				}
				else if (nm.equals("multicolor-bg.xml")) {
					log.debug("Processing multicolor-bg.xml backgrounds " + f.getAbsolutePath());
					List<Template> colors = loadColorsMapping(f);
					//log.debug("colors size = "+colors.size());
					if (colors == null) {
						log.warn("Cannot load colors from file " + f.getAbsolutePath());
						continue;
					}
					for (Template color : colors) {
						File colorFile = new File(f.getParent() + '/' + color.getFilename());
						log.debug("adding color="+color.getName()+" at file"+color.getFilename());
						foundColors.put(color.getName(), colorFile);
					}
				}
				else if(nm.matches(".+\\.ttf")) {
					Font font = loadFont(f);
					foundFonts.put(font.getFamily(), font);
				}
				else if(nm.matches("normalbinding.*\\.png")) {
					foundBinding = f;
				}
				else if (nm.matches(".+\\.xml") && path.contains("decorations")) {
					String id = f.getName().substring(0, nm.lastIndexOf("."));
					log.debug("checking decorations id ="+id+" file name="+f.getAbsolutePath());
					List<Decoration> spec = loadDecoration(f);
					if (spec.size() == 1) {
						foundDecorations.put(id, spec.get(0).getFading());
						log.debug("adding decorations id ="+id+" fading="+spec.get(0).getFading());
					} else {
						
						for(Decoration d:spec){
							Fading fad = d.getFading();
							//oundDecorations.put(id, spec.get(0).getFading());
							//log.warn("Failed to load decorations from: " path );
						}
						log.warn("Failed to load decorations from: " + path);
					}
				}
			}
		}
	}

	private static Font loadFont(File f) throws IOException {
		FileInputStream is = new FileInputStream(f);
		try {
			return Font.createFont(Font.TRUETYPE_FONT, is);
		} catch (FontFormatException e) {
			throw new IOException(e);
		}
		finally {
			try { is.close(); } catch (Exception e) { }
		}
	}
	
	private static LinkedList<Template> loadColorsMapping(File f) {
		Digester digester = new Digester();
		DigesterConfiguratorImpl configurator = new DigesterConfiguratorImpl();
		try {
			configurator.configureDigester(digester, f);
			return digester.parse(f);
		} catch (Exception e) {
			log.warn("Cannot parse 1-color file", e);
		}
		return null;
	}
	
	private static LinkedList<Decoration> loadDecoration(File f) {
		Digester digester = new Digester();
		DigesterConfiguratorImpl configurator = new DigesterConfiguratorImpl();
		try {
			configurator.configureDigester(digester, f);
			return digester.parse(f);
		} catch (Exception e) {
			log.warn("Failed to load decorations", e);
		}
		return null;
	}


	public File getImage(String id) {
		return foundImages.get(id);
	}
	
	public File getColorImage(String name) {
		return foundColors.get(name);
	}

	public File getClip(String id) {
		return foundClips.get(id);
	}

	public File getBinding() {
		return foundBinding;
	}

	public Font getFont(String id) {
		return foundFonts.get(id);
	}
	
	public Fading getDecoration(String id) {
		return foundDecorations.get(id);
	}
}

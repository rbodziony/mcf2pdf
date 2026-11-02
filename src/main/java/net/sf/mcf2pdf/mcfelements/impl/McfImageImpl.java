/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.mcf2pdf.mcfelements.McfCutout;
import net.sf.mcf2pdf.mcfelements.McfImage;
import net.sf.mcf2pdf.mcfelements.util.McfColorParser;

public class McfImageImpl extends AbstractMcfAreaContentImpl implements McfImage {
	
	private String parentChildRelationshipNature;
	
	private float scale;
	
	private int useABK;
	
	private float left;
	
	private float top;
	
	private String fileNameMaster;
	
	private String safeContainerLocation;
	
	private String fileName;
	
	private String fadingFile;

	private String passepartoutDesignElementId;

	private McfCutout cutout;
	private final Map<Color, Color> colorMappings = new LinkedHashMap<Color, Color>();

	public void addColorMapping(String source, String target) {
		Color from = McfColorParser.parse(source);
		Color to = McfColorParser.parse(target);
		if (from != null && to != null) {
			colorMappings.put(from, to);
		}
	}

	@Override
	public Map<Color, Color> getColorMappings() {
		return Collections.unmodifiableMap(colorMappings);
	}

	@Override
	public ContentType getContentType() {
		return ContentType.IMAGE;
	}

	public String getParentChildRelationshipNature() {
		return parentChildRelationshipNature;
	}

	public void setParentChildRelationshipNature(
			String parentChildRelationshipNature) {
		this.parentChildRelationshipNature = parentChildRelationshipNature;
	}

	public float getScale() {
		// Version 4.x
		if (this.cutout != null) {
			return this.cutout.getScale();
		}
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public int getUseABK() {
		return useABK;
	}

	public void setUseABK(int useABK) {
		this.useABK = useABK;
	}

	public float getLeft() {
		// Version 4.x
		if (this.cutout != null) {
			return this.cutout.getLeft()/this.cutout.getScale();
		}
		return left;
	}

	public void setLeft(float left) {
		this.left = left;
	}

	public float getTop() {
		// Version 4.x
		if (this.cutout != null) {
		//	log.debug("top from cutout: " + this.cutout.getTop());
			return this.cutout.getTop()/this.cutout.getScale();
		}
		return top;
	}

	public void setTop(float top) {
		this.top = top;
	}

	public String getFileNameMaster() {
		return fileNameMaster;
	}

	public void setFileNameMaster(String fileNameMaster) {
		this.fileNameMaster = fileNameMaster;
	}

	public String getSafeContainerLocation() {
		return safeContainerLocation;
	}

	public void setSafeContainerLocation(String safeContainerLocation) {
		this.safeContainerLocation = safeContainerLocation;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFadingFile() {
		// Version 6/7 ...
		if (this.cutout != null) {
			return passepartoutDesignElementId;
		}
		if(this.passepartoutDesignElementId != null) {
			return passepartoutDesignElementId;
		}
		return fadingFile;
	}

	@Override
	public String getPassepartoutDesignElementId() {
		return passepartoutDesignElementId;
	}

	public void setPassepartoutDesignElementId(String passepartoutDesignElementId) {
		this.passepartoutDesignElementId = passepartoutDesignElementId;
	}

	public void setFadingFile(String fadingFile) {
		this.fadingFile = fadingFile;
	}

	public McfCutout getCutout() {
		return cutout;
	}

	public void setCutout(McfCutout cutout) {
//		log.debug("cutout.top: " + cutout.getTop());
		this.cutout = cutout;
	}

}

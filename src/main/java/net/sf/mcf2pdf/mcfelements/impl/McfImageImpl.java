/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.mcf2pdf.mcfelements.McfCutout;
import net.sf.mcf2pdf.mcfelements.McfImage;
import net.sf.mcf2pdf.pagebuild.PageRenderContext;

public class McfImageImpl extends AbstractMcfAreaContentImpl implements McfImage {

	private final static Log log = LogFactory.getLog(McfImageImpl.class);

	private String parentChildRelationshipNature;

	private float scale;

	private int useABK;

	private int left;

	private int top;

	private String fileNameMaster;

	private String safeContainerLocation;

	private String fileName;

	private String fadingFile;

	private McfCutout cutout;

	@Override
	public ContentType getContentType() {
		return ContentType.IMAGE;
	}

	public String getParentChildRelationshipNature() {
		return parentChildRelationshipNature;
	}

	public void setParentChildRelationshipNature(String parentChildRelationshipNature) {
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
			return this.cutout.getLeft();
		}
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public float getTop() {
		// Version 4.x
		if (this.cutout != null) {
			log.debug("top from cutout: " + this.cutout.getTop());
			return this.cutout.getTop();
		}
		return top;
	}

	public void setTop(int top) {
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
		log.debug("Image filename: " + fileName);
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFadingFile() {
		return fadingFile;
	}

	public void setFadingFile(String fadingFile) {
		this.fadingFile = fadingFile;
	}

	public McfCutout getCutout() {
		return cutout;
	}

	public void setCutout(McfCutout cutout) {
		log.debug("cutout.top: " + cutout.getTop());
		this.cutout = cutout;
	}

}

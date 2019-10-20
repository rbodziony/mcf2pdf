/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import java.awt.Color;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfAreaContent;
import net.sf.mcf2pdf.mcfelements.McfBorder;
import net.sf.mcf2pdf.mcfelements.McfCorners;
import net.sf.mcf2pdf.mcfelements.McfPage;
import net.sf.mcf2pdf.mcfelements.McfPosition;

public class McfAreaImpl implements McfArea {

	private final static Log log = LogFactory.getLog(McfAreaImpl.class);

	private McfPage page;

	private McfPosition position;

	private float left;

	private float top;

	private float width;

	private float height;

	private float rotation;

	private int zPosition;

	private String areaType;

	private boolean borderEnabled;

	private float borderSize;

	private Color borderColor;

	private boolean shadowEnabled;

	private int shadowAngle;

	private int shadowIntensity;

	private float shadowDistance;

	private Color backgroundColor;

	private McfAreaContent content;

	private McfBorder border;

	private McfCorners corners;

	@Override
	public McfPage getPage() {
		return page;
	}

	public void setPage(McfPage page) {
		this.page = page;
	}

	public void setPosition(McfPosition position) {
		log.debug("position.left: " + position.getLeft());
		this.position = position;
	}

	@Override
	public float getLeft() {
		// Version 4.x
		if (this.position != null) {
			return this.position.getLeft();
		}
		return left;
	}

	public void setLeft(float left) {
		this.left = left;
	}

	@Override
	public float getTop() {
		// Version 4.x
		if (this.position != null) {
			return this.position.getTop();
		}
		return top;
	}

	public void setTop(float top) {
		this.top = top;
	}

	@Override
	public float getWidth() {
		// Version 4.x
		if (this.position != null) {
			return this.position.getWidth();
		}
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	@Override
	public float getHeight() {
		// Version 4.x
		if (this.position != null) {
			return this.position.getHeight();
		}
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	@Override
	public float getRotation() {
		// Version 4.x
		if (this.position != null) {
			return this.position.getRotation();
		}
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	@Override
	public int getZPosition() {
		// Version 4.x
		if (this.position != null) {
			return this.position.getZPosition();
		}
		return zPosition;
	}

	public void setZPosition(int zPosition) {
		this.zPosition = zPosition;
	}

	@Override
	public String getAreaType() {
		return areaType;
	}

	public void setAreaType(String areaType) {
		this.areaType = areaType;
	}

	@Override
	public boolean isBorderEnabled() {
		return borderEnabled;
	}

	public void setBorderEnabled(boolean borderEnabled) {
		this.borderEnabled = borderEnabled;
	}

	@Override
	public float getBorderSize() {
		return borderSize;
	}

	public void setBorderSize(float borderSize) {
		this.borderSize = borderSize;
	}

	@Override
	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	@Override
	public boolean isShadowEnabled() {
		return shadowEnabled;
	}

	public void setShadowEnabled(boolean shadowEnabled) {
		this.shadowEnabled = shadowEnabled;
	}

	@Override
	public int getShadowAngle() {
		return shadowAngle;
	}

	public void setShadowAngle(int shadowAngle) {
		this.shadowAngle = shadowAngle;
	}

	@Override
	public int getShadowIntensity() {
		return shadowIntensity;
	}

	public void setShadowIntensity(int shadowIntensity) {
		this.shadowIntensity = shadowIntensity;
	}

	@Override
	public float getShadowDistance() {
		return shadowDistance;
	}

	public void setShadowDistance(float shadowDistance) {
		this.shadowDistance = shadowDistance;
	}

	@Override
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	@Override
	public McfAreaContent getContent() {
		return content;
	}

	public void setContent(McfAreaContent content) {
		this.content = content;
	}

	public McfBorder getBorder() {
		return border;
	}

	public void setBorder(McfBorder border) {
		this.border = border;
	}

	public McfCorners getCorners() {
		return corners;
	}

	public void setCorners(McfCorners corners) {
		this.corners = corners;
	}
}

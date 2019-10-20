/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfArea;
import net.sf.mcf2pdf.mcfelements.McfPosition;

public class McfPositionImpl implements McfPosition {

	private McfArea area;

	private float left;

	private float top;

	private float width;

	private float height;

	private float rotation;

	private int zPosition;

	private String areaType;


	@Override
	public McfArea getArea() {
		return area;
	}

	public void setArea(McfArea area) {
		this.area = area;
	}

	@Override
	public float getLeft() {
		return left;
	}

	public void setLeft(float left) {
		this.left = left;
	}

	@Override
	public float getTop() {
		return top;
	}

	public void setTop(float top) {
		this.top = top;
	}

	@Override
	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	@Override
	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	@Override
	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	@Override
	public int getZPosition() {
		return zPosition;
	}

	public void setZPosition(int zPosition) {
		this.zPosition = zPosition;
	}
}

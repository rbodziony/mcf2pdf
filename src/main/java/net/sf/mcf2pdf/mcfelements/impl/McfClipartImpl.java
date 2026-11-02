/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.mcf2pdf.mcfelements.McfClipart;
import net.sf.mcf2pdf.mcfelements.util.McfColorParser;

public class McfClipartImpl extends AbstractMcfAreaContentImpl implements McfClipart {
	
	private String mirror;
	public String getMirror() { return mirror; }
	public void setMirror(String mirror) { this.mirror = mirror; }

	private String uniqueName;
	private String designElementId;
	private final Map<Color, Color> colorMappings = new LinkedHashMap<Color, Color>();

	@Override
	public ContentType getContentType() {
		return ContentType.CLIPART;
	}

	public String getUniqueName() {
		return uniqueName;
	}

	@Override
	public String getDesignElementId() {
		return designElementId;
	}

	public void setDesignElementId(String designElementId) {
		this.designElementId = designElementId;
	}

	public void setUniqueName(String uniqueName) {
		this.uniqueName = uniqueName;
	}

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

}

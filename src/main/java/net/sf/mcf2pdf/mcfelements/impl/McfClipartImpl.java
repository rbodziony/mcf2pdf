/*******************************************************************************
 * ${licenseText}     
 *******************************************************************************/
package net.sf.mcf2pdf.mcfelements.impl;

import java.awt.Color;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.mcf2pdf.mcfelements.McfClipart;

public class McfClipartImpl extends AbstractMcfAreaContentImpl implements McfClipart {
	
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
		colorMappings.put(Color.decode(source), Color.decode(target));
	}

	@Override
	public Map<Color, Color> getColorMappings() {
		return Collections.unmodifiableMap(colorMappings);
	}

}

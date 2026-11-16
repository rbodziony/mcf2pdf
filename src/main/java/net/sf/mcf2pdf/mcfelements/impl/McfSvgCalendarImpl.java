package net.sf.mcf2pdf.mcfelements.impl;

/** CEWE 5.x stores either an escaped SVG document or a reference to a template. */
public class McfSvgCalendarImpl extends AbstractMcfAreaContentImpl {
    private String cssSheet;
    private String customCssSheet;
    private String standardSheet;
    private String fileName;
    private String svgDocument;

    @Override public ContentType getContentType() { return ContentType.CALENDAR; }
    public String getCssSheet() { return cssSheet; }
    public void setCssSheet(String value) { cssSheet = value; }
    public String getCustomCssSheet() { return customCssSheet; }
    public void setCustomCssSheet(String value) { customCssSheet = value; }
    public String getStandardSheet() { return standardSheet; }
    public void setStandardSheet(String value) { standardSheet = value; }
    public String getFileName() { return fileName; }
    public void setFileName(String value) { fileName = value; }
    public String getSvgDocument() { return svgDocument; }
    public void setSvgDocument(String value) { svgDocument = value; }
}

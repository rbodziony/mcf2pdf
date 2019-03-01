package net.sf.mcf2pdf.mcfconfig;

public class Decoration {
	private Fading fading;

	private Clipart clipart;
	public Fading getFading() {
		return fading;
	}
	public Clipart getClipart() {return clipart;}
	public void setFading(Fading fading) {
		this.fading = fading;
	}
	public void setClipart(Clipart clipart) {this.clipart = clipart;}
}
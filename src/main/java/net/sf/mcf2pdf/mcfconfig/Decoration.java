package net.sf.mcf2pdf.mcfconfig;

import java.util.List;

public class Decoration {
	List<Category> categories;
	Fading fading;
	
	public List<Category> getCategories() {
		return categories;
	}
	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	public Fading getFading() {
		return fading;
	}
	public void setFading(Fading fading) {
		this.fading = fading;
	}
}
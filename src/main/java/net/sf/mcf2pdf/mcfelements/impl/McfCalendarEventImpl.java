package net.sf.mcf2pdf.mcfelements.impl;

import net.sf.mcf2pdf.mcfelements.McfCalendarEvent;

public class McfCalendarEventImpl implements McfCalendarEvent {

	private String name;
	private String dateTime;
	private String recurrent;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getDateTime() {
		return dateTime;
	}

	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}

	@Override
	public String getRecurrent() {
		return recurrent;
	}

	public void setRecurrent(String recurrent) {
		this.recurrent = recurrent;
	}
}

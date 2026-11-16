package net.sf.mcf2pdf.mcfelements;

/** A user-defined calendar event stored in an MCF project. */
public interface McfCalendarEvent {

	String getName();

	String getDateTime();

	String getRecurrent();
}

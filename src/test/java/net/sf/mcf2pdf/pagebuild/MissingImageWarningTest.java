package net.sf.mcf2pdf.pagebuild;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import net.sf.mcf2pdf.mcfelements.McfFotobook;
import net.sf.mcf2pdf.mcfelements.util.McfFileUtil;
import net.sf.mcf2pdf.mcfelements.util.XslFoDocumentBuilder;
import net.sf.mcf2pdf.mcfglobals.McfResourceScanner;

/**
 * Images that cannot be found (e.g. the "..._mcf-Dateien" folder next to an old MCF file is missing) must be
 * reported in the log instead of silently leaving blank spaces in the PDF.
 */
public class MissingImageWarningTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final List<LoggingEvent> events = new ArrayList<LoggingEvent>();

	private AppenderSkeleton appender;

	private Logger contextLogger;

	@Before
	public void captureLog() {
		contextLogger = Logger.getLogger(PageRenderContext.class);
		appender = new AppenderSkeleton() {
			@Override
			protected void append(LoggingEvent event) {
				events.add(event);
			}

			@Override
			public void close() {
			}

			@Override
			public boolean requiresLayout() {
				return false;
			}
		};
		contextLogger.addAppender(appender);
	}

	@After
	public void releaseLog() {
		contextLogger.removeAppender(appender);
	}

	private McfFotobook bookInFolder(File dir, String imageDir) {
		McfFotobook book = mock(McfFotobook.class);
		when(book.getFile()).thenReturn(new File(dir, "book.mcf"));
		when(book.getImageDir()).thenReturn(imageDir);
		return book;
	}

	@Test
	public void testExceptionNamesFileAndSearchedLocationsAndMissingDirectory() throws IOException {
		McfFotobook book = bookInFolder(tmp.getRoot(), "book_mcf-Dateien");

		try {
			McfFileUtil.getImageFile("IMG_6386.jpeg", book);
			fail("expected FileNotFoundException");
		}
		catch (FileNotFoundException e) {
			assertTrue(e.getMessage(), e.getMessage().contains("IMG_6386.jpeg"));
			assertTrue(e.getMessage(), e.getMessage().contains("book_mcf-Dateien"));
			assertTrue(e.getMessage(), e.getMessage().contains("directory does not exist"));
		}
	}

	@Test
	public void testImageIsStillFoundInExistingImageDirectory() throws IOException {
		File images = tmp.newFolder("book_mcf-Dateien");
		File image = new File(images, "IMG_1.jpeg");
		assertTrue(image.createNewFile());

		assertEquals(image, McfFileUtil.getImageFile("IMG_1.jpeg", bookInFolder(tmp.getRoot(), "book_mcf-Dateien")));
	}

	@Test
	public void testMissingImageIsLoggedAsWarningAndOtherElementsAreStillRendered() throws IOException {
		McfResourceScanner resources = mock(McfResourceScanner.class);
		PageRenderContext context = new PageRenderContext(100, 1.0, resources, null);
		BitmapPageBuilder builder = new BitmapPageBuilder(200, 100, context, tmp.getRoot());
		PageDrawable missing = mock(PageDrawable.class);
		when(missing.renderAsBitmap(any(), any(), anyInt(), anyInt()))
				.thenThrow(new FileNotFoundException("Image file 'IMG_6386.jpeg' not found"));
		builder.addDrawable(missing);
		XslFoDocumentBuilder docBuilder = mock(XslFoDocumentBuilder.class);

		builder.addToDocumentBuilder(docBuilder);

		List<LoggingEvent> warnings = new ArrayList<LoggingEvent>();
		for (LoggingEvent event : events) {
			if (event.getLevel().isGreaterOrEqual(Level.WARN)) {
				warnings.add(event);
			}
		}
		assertEquals(1, warnings.size());
		assertTrue(String.valueOf(warnings.get(0).getMessage()), String.valueOf(warnings.get(0).getMessage()).contains("IMG_6386.jpeg"));
		assertEquals(Level.WARN, warnings.get(0).getLevel());
	}
}

package net.sf.mcf2pdf.mcfglobals;

import net.sf.mcf2pdf.mcfconfig.Fading;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import static org.junit.Assert.*;

public class McfFotoFrameTest {

    private McfFotoFrame mcfFotoFrame;
    private File clipart;
    private File fading;
    private Fading config;

    @Before
    public void setUp() {
        clipart = new File("path/to/clipart");
        fading = new File("path/to/fading");
        config = new Fading();
        mcfFotoFrame = new McfFotoFrame(clipart, fading, config);
    }

    @Test
    public void testGetClipart() {
        assertEquals(clipart, mcfFotoFrame.getClipart());
    }

    @Test
    public void testGetFading() {
        assertEquals(fading, mcfFotoFrame.getFading());
    }

    @Test
    public void testGetConfig() {
        assertEquals(config, mcfFotoFrame.getConfig());
    }
}

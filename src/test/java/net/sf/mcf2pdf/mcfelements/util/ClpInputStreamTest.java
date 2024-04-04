package net.sf.mcf2pdf.mcfelements.util;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.io.InputStream;
import net.sf.mcf2pdf.mcfelements.util.ClpInputStream;

public class ClpInputStreamTest {

    private ClpInputStream clpInputStream;
    private InputStream inputStream;

    @Before
    public void setUp() {
        inputStream = mock(InputStream.class);
        clpInputStream = new ClpInputStream(inputStream);
    }

    @Test
    public void testReadFirstByte() throws IOException {
        when(inputStream.read()).thenReturn(0x61, 0x31, 0x32); // 0x61 is 'a', 0x31 is '1', 0x32 is '2'
        assertEquals(0x12, clpInputStream.read());
    }

    @Test(expected = IOException.class)
    public void testReadFirstByteInvalid() throws IOException {
        when(inputStream.read()).thenReturn(0x62); // 0x62 is 'b'
        clpInputStream.read();
    }

    @Test
    public void testReadBytes() throws IOException {
        byte[] b = new byte[2];
        when(inputStream.read()).thenReturn(0x61, 0x31, 0x32, 0x33, 0x34); // 0x61 is 'a', 0x31 is '1', 0x32 is '2', 0x33 is '3', 0x34 is '4'
        assertEquals(2, clpInputStream.read(b));
        assertArrayEquals(new byte[] {0x12, 0x34}, b);
    }

    @Test
    public void testMarkSupported() {
        assertFalse(clpInputStream.markSupported());
    }

    @Test(expected = IOException.class)
    public void testReadValidCharacterInvalid() throws IOException {
        when(inputStream.read()).thenReturn(0x67); // 0x67 is 'g'
        clpInputStream.read();
    }
}

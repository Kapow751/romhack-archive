package com.github.videogamearchive.rompatcher;

import com.github.videogamearchive.util.PathUtil;
import com.github.videogamearchive.util.Zip;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RomPatcherTest {

    String fileExt = "nes";
    Path deltaPatch = Path.of("test-data", "Lolo Test ROM v1.1.delta.bps");
    Path original = Path.of("test-data", "Kirby Test ROM (World).nes");
    Path modified = Path.of("test-data", "Lolo Test ROM (World) [Themed by Hackermans (v1.1)].nes");
    Path originalZip = Path.of("test-data", "Kirby Test ROM (World).zip");
    Path modifiedZip = Path.of("test-data", "Lolo Test ROM (World) [Themed by Hackermans (v1.1)].zip");
    Path tempFile = Path.of("temp-" + System.currentTimeMillis() + "." + fileExt);
    Path tempFileZip = Path.of("temp-" + System.currentTimeMillis() + ".zip");
    @AfterEach
    public void cleanup() throws IOException {
        if (Files.exists(tempFile)) {
            Files.delete(tempFile);
        }
        if (Files.exists(tempFileZip)) {
            Files.delete(tempFileZip);
        }
    }

    @Test
    public void testHelp() throws IOException {
        String expected = RomPatcher.helpMsg();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        System.setOut(printStream);
        RomPatcher.main(new String[] {});
        String actual = new String(byteArrayOutputStream.toByteArray());
        assertTrue(expected.length() > 0);
        assertEquals(expected, actual);
    }

    @Test
    public void testPatch() throws IOException {
        Path out = tempFile;
        RomPatcher.main(new String[] {deltaPatch.toString(), original.toString(), out.toString()});

        byte[] expected = Files.readAllBytes(modified);
        byte[] actual = Files.readAllBytes(out);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testPatchInputZip() throws IOException {
        Path out = tempFile;
        RomPatcher.main(new String[] {deltaPatch.toString(), originalZip.toString(), out.toString()});

        byte[] expected = Files.readAllBytes(modified);
        byte[] actual = Files.readAllBytes(out);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testPatchOutputZip() throws IOException {
        Path out = tempFileZip;
        RomPatcher.main(new String[] {deltaPatch.toString(), original.toString(), out.toString()});

        byte[] expected = Files.readAllBytes(modified);
        Map<String, byte[]> zipContent = Zip.readAllBytes(out);
        String actualName = zipContent.keySet().iterator().next();
        assertEquals(fileExt, PathUtil.getExtension(actualName));
        byte[] actual = zipContent.values().iterator().next();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testPatchInputOutputZip() throws IOException {
        Path out = tempFileZip;
        RomPatcher.main(new String[] {deltaPatch.toString(), originalZip.toString(), out.toString()});

        byte[] expected = Files.readAllBytes(modified);
        Map<String, byte[]> zipContent = Zip.readAllBytes(out);
        String actualName = zipContent.keySet().iterator().next();
        assertEquals(fileExt, PathUtil.getExtension(actualName));
        byte[] actual = zipContent.values().iterator().next();
        assertArrayEquals(expected, actual);
    }
}

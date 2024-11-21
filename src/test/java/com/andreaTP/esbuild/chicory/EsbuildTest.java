package com.andreaTP.esbuild.chicory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.andreaTP.esbuild.chicory.Esbuild;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

public class EsbuildTest {

    @Test
    public void shouldProcessADemo() throws IOException {
        // Arrange
        var outFile = "out.js";
        var esbuild =
                Esbuild.builder()
                        .withArgs("./app.jsx", "--bundle", "--outfile=out.js")
                        .withInputDir(Path.of("esbuild-test"))
                        .withOutputFile(outFile)
                        .build();

        // Act
        esbuild.process();

        // Assert
        assertEquals(
                new String(Files.readAllBytes(Path.of("esbuild-test", "result.js"))),
                new String(Files.readAllBytes(Path.of("esbuild-test").resolve(outFile))));
    }
}

package com.github.andreaTP.esbuild.chicory;

import com.dylibso.chicory.runtime.ImportValues;
import com.dylibso.chicory.runtime.Instance;
import com.dylibso.chicory.wasi.Files;
import com.dylibso.chicory.wasi.WasiExitException;
import com.dylibso.chicory.wasi.WasiOptions;
import com.dylibso.chicory.wasi.WasiPreview1;
import com.dylibso.chicory.wasm.Parser;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class Esbuild {
    private final Path inputDir;
    private final String outputFileName;
    private final List<String> arguments;

    private Esbuild(Path inputDir, String outputFileName, List<String> arguments) {
        this.inputDir = inputDir;
        this.outputFileName = outputFileName;
        this.arguments = arguments;
    }

    public void process() {
        try (var fs =
                Jimfs.newFileSystem(
                        Configuration.unix().toBuilder().setAttributeViews("unix").build())) {
            Path root = fs.getPath("/");
            java.nio.file.Files.list(inputDir)
                    .forEach(
                            f -> {
                                var fileName = f.getFileName().toString();
                                try {
                                    if (f.toFile().isDirectory()) {
                                        Files.copyDirectory(
                                                inputDir.resolve(fileName), root.resolve(fileName));
                                    } else {
                                        java.nio.file.Files.copy(
                                                inputDir.resolve(fileName), root.resolve(fileName));
                                    }
                                } catch (IOException ex) {
                                    throw new RuntimeException(
                                            "failed to ingest folder: " + f.getFileName(), ex);
                                }
                            });

            var wasiOpts =
                    WasiOptions.builder()
                            .withDirectory(root.toString(), root)
                            .withArguments(arguments)
                            .inheritSystem()
                            .build();

            try (var wasi = WasiPreview1.builder().withOptions(wasiOpts).build()) {
                Instance.builder(Parser.parse(Esbuild.class.getResourceAsStream("/esbuild.wasm")))
                        .withImportValues(
                                ImportValues.builder().addFunction(wasi.toHostFunctions()).build())
                        .build();
            } catch (WasiExitException exit) {
                if (exit.exitCode() != 0) {
                    throw new RuntimeException("Esbuild returned: " + exit.exitCode(), exit);
                }
            }

            // TODO: improve me
            java.nio.file.Files.copy(
                    root.resolve(outputFileName),
                    inputDir.resolve(outputFileName),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new RuntimeException("Esbuild process failed: ", ex);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Path inputDir;
        private String outputFileName;
        private List<String> arguments;

        private Builder() {}

        public Builder withInputDir(Path inputDir) {
            this.inputDir = inputDir;
            return this;
        }

        public Builder withOutputFile(String outputFileName) {
            this.outputFileName = outputFileName;
            return this;
        }

        public Builder withArgs(String... args) {
            this.arguments = new ArrayList<>();
            this.arguments.add("Esbuild");
            this.arguments.addAll(List.of(args));
            return this;
        }

        public Esbuild build() {
            return new Esbuild(inputDir, outputFileName, arguments);
        }
    }
}

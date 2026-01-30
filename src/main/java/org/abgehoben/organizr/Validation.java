package org.abgehoben.organizr;

import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.abgehoben.organizr.main.addProgressText;

public class Validation {
    public static int validate(Settings params, Stage owner) {
        int validationsFailed = 0;
        if (!isWritable(params.getInputPath(), params.getOutputPath())) {
            addProgressText("Input or output directory is not writable.");
            validationsFailed++;
        }

        if (!isEnoughSpace(params.getInputPath())) {
            addProgressText("Not enough disk space.");
            validationsFailed++;
        }

        if (!isDestinationEmpty(params.getOutputPath())) {
            addProgressText("Output directory is not empty.");
            validationsFailed++;
            if (validationsFailed == 1) {
                addProgressText("Are you sure you want to continue? This will delete existing files in the output directory.");
                CompletableFuture<Boolean> future = new CompletableFuture<>();
                Platform.runLater(() -> future.complete(main.showNotEmptyConfirmation(owner)));
                boolean result = future.join();
                if (result) validationsFailed--;
            }
            if (validationsFailed == 0) try {
                cleanDirectory(params.getOutputPath());
            } catch (IOException e) {
                addProgressText("Failed to clean output directory");
                throw new RuntimeException(e);
            }
        }

        return validationsFailed;
    }

    public static boolean isDestinationEmpty(Path out) {
        addProgressText("Checking if output directory is empty");
        try (Stream<Path> entries = Files.list(out)) {
            return entries.findAny().isEmpty();
        } catch (IOException e) {
            addProgressText("Failed to check output directory");
            throw new RuntimeException(e);
        }
    }

    public static boolean isWritable(Path in, Path out) {
        addProgressText("Checking write permissions");
        return Files.isWritable(in) && Files.isWritable(out);
    }

    public static boolean isEnoughSpace(Path in) {
        addProgressText("Checking available disk space");
        double folderSize;
        double usableSpace;
        try {
            folderSize = getFolderSize(in);
            usableSpace = in.toFile().getUsableSpace();
        } catch (IOException e) {
            addProgressText("Failed to calculate folder size");
            throw new RuntimeException(e);
        }
        return usableSpace >= folderSize;
    }

    public static long getFolderSize(Path path) throws IOException {
        final AtomicLong size = new AtomicLong(0);

        //noinspection NullableProblems
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                size.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });
        return size.get();
    }

    public static void cleanDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        //noinspection NullableProblems
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                addProgressText("Deleting file: " + file);
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if (exc != null) {
                    throw exc;
                }
                if (!dir.equals(directory)) {
                    addProgressText("Deleting directory: " + dir);
                    Files.delete(dir);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

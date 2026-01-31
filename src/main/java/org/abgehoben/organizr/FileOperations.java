package org.abgehoben.organizr;

import org.abgehoben.organizr.enums.FolderEntityType;
import org.abgehoben.organizr.records.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.abgehoben.organizr.Metadata.getFileMetadata;
import static org.abgehoben.organizr.main.addProgressText;
import static org.abgehoben.organizr.main.updateProgressBar;
import static org.abgehoben.organizr.sorting.files;

public class FileOperations {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            "mp3", "flac", "m4a", "m4p", "m4b", "mp4",
            "ogg", "oga", "opus", "wma", "wav", "aif", "aiff", "ape", "dsf"
    );

    public static HashMap<MusicFile, ArrayList<Path>> mapFilesToPaths(Settings params, FolderIndex folderIndex) {
        HashMap<MusicFile, ArrayList<Path>> fileMap = new HashMap<>();

        FolderEntityType level1Type = params.sortingScheme.firstLevel;
        FolderEntityType level2Type = params.sortingScheme.secondLevel;
        boolean hasSecondLevel = (level2Type != null);

        for (MusicFile file : files) {
            List<? extends MetadataEntity> firstLevelEntities = file.getEntities(level1Type);

            for (MetadataEntity l1Entity : firstLevelEntities) {

                Path level1Path = folderIndex.rootPaths.get(l1Entity);

                if (hasSecondLevel) {
                    List<? extends MetadataEntity> secondLevelEntities = file.getEntities(level2Type);

                    Map<MetadataEntity, Path> subMap = folderIndex.childPaths.get(l1Entity);

                    for (MetadataEntity l2Entity : secondLevelEntities) {
                        Path fullPath = subMap.get(l2Entity);
                        if (fullPath != null) {
                            fileMap.computeIfAbsent(file, k -> new ArrayList<>()).add(fullPath);
                            addProgressText("Mapped " + file.title() + " to " + fullPath);
                        }
                    }
                }
                else {
                    fileMap.computeIfAbsent(file, k -> new ArrayList<>()).add(level1Path);
                    addProgressText("Mapped " + file.title() + " to " + level1Path);
                }
            }
        }
        return fileMap;
    }

    public static HashMap<Path, MetadataEntity> createBaseFolders(Settings params, FolderIndex folderIndex) {
        HashMap<Path, MetadataEntity> createdFolders = new HashMap<>();
        switch (params.sortingScheme.firstLevel) {
            case FolderEntityType.ALBUM -> Metadata.albumCache.values().forEach((album) -> {
                Path folderPath = createFolder(album.name(), params.getOutputPath());
                createdFolders.put(folderPath, album);
                folderIndex.rootPaths.put(album, folderPath);
            });
            case FolderEntityType.ARTIST -> Metadata.artistCache.values().forEach((artist) -> {
                Path folderPath = createFolder(artist.name(), params.getOutputPath());
                createdFolders.put(folderPath, artist);
                folderIndex.rootPaths.put(artist, folderPath);
            });
            case FolderEntityType.GENRE -> Metadata.genreCache.values().forEach((genre) -> {
                Path folderPath = createFolder(genre.name(), params.getOutputPath());
                createdFolders.put(folderPath, genre);
                folderIndex.rootPaths.put(genre, folderPath);
            });
        }
        return createdFolders;
    }

    public static void createSubDirFolders(Settings params, HashMap<Path, MetadataEntity> parentFolders, FolderIndex folderIndex) {
        switch (params.sortingScheme.secondLevel) {
            case FolderEntityType.ALBUM -> parentFolders.forEach((parentPath, parentMetadataEntity) -> {
                Map<MetadataEntity, Path> childMap = folderIndex.childPaths.computeIfAbsent(parentMetadataEntity, k -> new HashMap<>());
                parentMetadataEntity.getAlbums().forEach(album -> {
                    Path fullPath = createFolder(album.name(), parentPath);
                    childMap.put(album, fullPath);
                });
            });
            case FolderEntityType.ARTIST -> parentFolders.forEach((parentPath, parentMetadataEntity) -> {
                Map<MetadataEntity, Path> childMap = folderIndex.childPaths.computeIfAbsent(parentMetadataEntity, k -> new HashMap<>());
                parentMetadataEntity.getArtists().forEach(artist -> {
                    Path fullPath = createFolder(artist.name(), parentPath);
                    childMap.put(artist, fullPath);
                });
            });
            case FolderEntityType.GENRE -> parentFolders.forEach((parentPath, parentMetadataEntity) -> {
                Map<MetadataEntity, Path> childMap = folderIndex.childPaths.computeIfAbsent(parentMetadataEntity, k -> new HashMap<>());
                parentMetadataEntity.getGenres().forEach(genre -> {
                    Path fullPath = createFolder(genre.name(), parentPath);
                    childMap.put(genre, fullPath);
                });
            });
        }
    }

    public static ArrayList<MusicFile> getFiles(Settings params) throws IOException {
        ArrayList<MusicFile> tempFiles = new ArrayList<>();
        int maxDepth = 1;
        if(params.includeSubdirs) maxDepth = 16;

        //noinspection NullableProblems
        Files.walkFileTree(params.getInputPath(), EnumSet.noneOf(FileVisitOption.class), maxDepth, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs){
                if (!isSupported(file)) return FileVisitResult.CONTINUE;
                addProgressText("Discovered: " + file);
                MusicFile metadata = getFileMetadata(file.toFile(), params);
                if (metadata != null) tempFiles.add(metadata);

                return FileVisitResult.CONTINUE;
            }
        });
        return  tempFiles;
    }

    /*
    @param folderName Name of the folder to create
    @param outputDir output directory
    @return Path of the created folder
    */
    public static Path createFolder(String folderName, Path outputDir) {
        try {
            Files.createDirectories(outputDir.resolve(folderName));
            addProgressText("Created folder: " + folderName);
            return outputDir.resolve(folderName);
        } catch (IOException e) {
            addProgressText("Failed to create folder: " + folderName);
            throw new RuntimeException(e);
        }
    }

    public static boolean isSupported(Path file) {
        String name = file.getFileName().toString();
        int len = name.length();
        int lastDot = name.lastIndexOf('.');

        if (lastDot <= 0 || lastDot > len - 4) return false;

        String ext = name.substring(lastDot + 1).toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.contains(ext);
    }

    public static void copyFilesToDestinations(HashMap<MusicFile, ArrayList<Path>> fileMap, Settings params) {
        int total = fileMap.size();
        AtomicInteger processedCount = new AtomicInteger(0);
        updateProgressBar(0, total);

        fileMap.entrySet().parallelStream().forEach(entry -> {
            int current = processedCount.incrementAndGet();
            MusicFile sourceFile = entry.getKey();
            ArrayList<Path> paths = entry.getValue();

            if (params.useSymlinks) {
                Path primaryDest = paths.getFirst();
                copyFile(sourceFile, primaryDest, params);

                for (int i = 1; i < paths.size(); i++) {
                    createSymlink(sourceFile, paths.get(i));
                }
            } else {
                for (Path destPath : paths) {
                    copyFile(sourceFile, destPath, params);
                }
            }
            if (current % 20 == 0 || current == total) {
                updateProgressBar(current, total);
            }
        });
    }

    public static void moveFilesToDestinations(HashMap<MusicFile, ArrayList<Path>> fileMap, Settings params) {
        int total = fileMap.size();
        AtomicInteger processedCount = new AtomicInteger(0);
        updateProgressBar(0, total);

        fileMap.entrySet().parallelStream().forEach(entry -> {
            int current = processedCount.incrementAndGet();
            MusicFile sourceFile = entry.getKey();
            ArrayList<Path> paths = entry.getValue();

            if (params.useSymlinks) {
                Path primaryDest = paths.getFirst();
                moveFile(sourceFile, primaryDest, params);

                for (int i = 1; i < paths.size(); i++) {
                    createSymlink(sourceFile, paths.get(i));
                }
            } else {
                for (Path destPath : paths) {
                    moveFile(sourceFile, destPath, params);
                }
            }
            if (current % 20 == 0 || current == total) {
                updateProgressBar(current, total);
            }
        });
    }
    public static void createSymlinksInDestinations(HashMap<MusicFile, ArrayList<Path>> fileMap) {
        int total = fileMap.size();
        AtomicInteger processedCount = new AtomicInteger(0);
        updateProgressBar(0, total);

        fileMap.entrySet().parallelStream().forEach(entry -> {
            int current = processedCount.incrementAndGet();
            MusicFile sourceFile = entry.getKey();
            ArrayList<Path> paths = entry.getValue();

            for (Path destPath : paths) {
                createSymlink(sourceFile, destPath);
            }
            if (current % 10 == 0 || current == total) {
                updateProgressBar(current, total);
            }
        });
    }

    public static void moveFile(MusicFile sourceFile, Path destinationDir, Settings params) {
        try {
            addProgressText("Moving file: " + sourceFile.title() + " to " + destinationDir);
            if (params.keepDate) Files.move(sourceFile.file().toPath(), destinationDir.resolve(sourceFile.title()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            else Files.move(sourceFile.file().toPath(), destinationDir.resolve(sourceFile.title()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            addProgressText("Failed to move file: " + sourceFile.title() + " to " + destinationDir);
            throw new RuntimeException(e);
        }
    }
    public static void copyFile(MusicFile sourceFile, Path destinationDir, Settings params) {
        try {
            addProgressText("Copying file: " + sourceFile.title() + " to " + destinationDir);
            if (params.keepDate) Files.copy(sourceFile.file().toPath(), destinationDir.resolve(sourceFile.title()), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            else Files.copy(sourceFile.file().toPath(), destinationDir.resolve(sourceFile.title()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            addProgressText("Failed to copy file: " + sourceFile.title() + " to " + destinationDir);
            throw new RuntimeException(e);
        }
    }
    public static void createSymlink(MusicFile sourceFile, Path destinationDir) {
        createSymlink(sourceFile.file(), destinationDir.resolve(sourceFile.title()));
    }
    public static void createSymlink(File sourceFile, Path destinationPath) {
        try {
            addProgressText("Creating symlink for file: " + sourceFile.getName() + " at " + destinationPath);
            Files.createSymbolicLink(destinationPath, sourceFile.toPath());
        } catch (IOException e) {
            addProgressText("Failed to create symlink for file: " + sourceFile.getName() + " at " + destinationPath);
            throw new RuntimeException(e);
        }
    }
}

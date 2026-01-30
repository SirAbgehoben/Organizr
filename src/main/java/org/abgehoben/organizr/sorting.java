package org.abgehoben.organizr;

import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.abgehoben.organizr.records.FolderIndex;
import org.abgehoben.organizr.records.MetadataEntity;
import org.abgehoben.organizr.records.MusicFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.abgehoben.organizr.FileOperations.*;
import static org.abgehoben.organizr.Validation.validate;
import static org.abgehoben.organizr.main.*;

public class sorting {
    public static ArrayList<MusicFile> files = new ArrayList<>();

    public static void sortFilesAsync(Settings params, Stage owner) {
        Date startTime = new Date();
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                startSort(params, owner);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            Date endTime = new Date();
            long timeDiff = endTime.getTime() - startTime.getTime();
            long seconds = (timeDiff / 1000) % 60;
            long minutes = (timeDiff / (1000 * 60)) % 60;
            long hours = (timeDiff / (1000 * 60 * 60)) % 24;
            String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            addProgressText("Completed in " + timeString);
            addProgressLabelText("finished");
            enableStartButton();
        });
        task.setOnFailed(e -> {
            addProgressText("Sorting failed: " + task.getException().getMessage());
            addProgressLabelText("There was an error during sorting.");
            enableStartButton();
        });

        new Thread(task).start();
    }
    public static void startSort(Settings params, Stage owner) {
        addProgressLabelText("Validating");
        int validationsFailed = validate(params, owner);
        if (validationsFailed > 0) {
            addProgressText(validationsFailed + " validation(s) failed. Aborting.");
            addProgressLabelText("aborted");
            return;
        }
        addProgressText("All validations passed.");
        try {
            addProgressLabelText("Getting files");
            files = getFiles(params);
        } catch (IOException e) {
            addProgressText("There was an error while getting files.");
            throw new RuntimeException(e);
        }

        FolderIndex folderIndex = new FolderIndex();
        HashMap<Path, MetadataEntity> level1Created;

        try {
            addProgressLabelText("Creating " + params.sortingScheme.firstLevel.displayname + " folders");
            level1Created = createBaseFolders(params, folderIndex);
        } catch (Exception e) {
            addProgressText("There was an error while creating" + params.sortingScheme.firstLevel.displayname + " folders.");
            throw new RuntimeException(e);
        }

        if (params.sortingScheme.secondLevel != null) {
            try {
                addProgressLabelText("Creating " + params.sortingScheme.secondLevel.displayname + " folders");
                createSubDirFolders(params, level1Created, folderIndex);
            } catch (Exception e) {
                addProgressText("There was an error while creating " + params.sortingScheme.secondLevel.displayname + " folders.");
                throw new RuntimeException(e);
            }
        }
        addProgressLabelText("Mapping files to paths");
        addProgressText("Mapping files to paths");
        HashMap<MusicFile, ArrayList<Path>> newFileMap = mapFilesToPaths(params, folderIndex);


        switch(params.movingScheme) {
            case COPY_FILES -> {
                addProgressLabelText("Copying files");
                copyFilesToDestinations(newFileMap, params);
            }
            case MOVE_FILES -> {
                addProgressLabelText("Moving files");
                moveFilesToDestinations(newFileMap, params);
            }
            case CREATE_SYMLINKS -> {
                addProgressLabelText("Creating symlinks");
                createSymlinksInDestinations(newFileMap);
            }
        }
        addProgressLabelText("finished");
    }

}

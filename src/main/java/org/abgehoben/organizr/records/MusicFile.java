package org.abgehoben.organizr;

import java.io.File;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;


public record MusicFile(
        File file,
        ArrayList<String> artists,
        String rawArtist,
        String album,
        String genre,
        String title,
        FileTime creationDate,
        FileTime modificationDate
) {}

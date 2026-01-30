package org.abgehoben.organizr.records;

import org.abgehoben.organizr.enums.FolderEntityType;

import java.io.File;
import java.util.List;



public record MusicFile(File file,
                        List<Artist> artists,
                        Album album,
                        Genre genre,
                        String title)
{
    public List<? extends MetadataEntity> getEntities(FolderEntityType type) {
        return switch (type) {
            case ALBUM -> List.of(album());
            case GENRE -> List.of(genre());
            case ARTIST -> artists();
        };
    }
}

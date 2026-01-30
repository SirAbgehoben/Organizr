package org.abgehoben.organizr.records;

import java.util.Set;

public sealed interface MetadataEntity permits Album, Artist, Genre {
    String getName();
    Set<Artist> getArtists();
    Set<Genre> getGenres();
    Set<Album> getAlbums();
}

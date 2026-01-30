package org.abgehoben.organizr.records;

import java.util.Objects;
import java.util.Set;

public record Artist(
        String name,
        Set<Album> albums,
        Set<Genre> genres
) implements MetadataEntity {
    @Override
    public String getName() {
        return name();
    }
    @Override
    public Set<Artist> getArtists() {
        return Set.of(this);
    }
    @Override
    public Set<Genre> getGenres() {
        return genres;
    }
    @Override
    public Set<Album> getAlbums() {
        return albums();
    }

    // Override to avoid recursion through albums and genres
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Artist other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "Artist[name=" + name + "]";
    }
}
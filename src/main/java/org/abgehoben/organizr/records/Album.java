package org.abgehoben.organizr.records;

import java.util.Objects;
import java.util.Set;

public record Album(
        String name,
        Set<Artist> artists,
        Set<Genre> genres
) implements MetadataEntity {
    @Override
    public String getName() {
        return name();
    }
    @Override
    public Set<Artist> getArtists() {
        return artists;
    }
    @Override
    public Set<Genre> getGenres() {
        return genres;
    }
    @Override
    public Set<Album> getAlbums() {
        return Set.of(this);
    }

    // Override to avoid recursion through artists and genres
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Album other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "Album[name=" + name + "]";
    }
}
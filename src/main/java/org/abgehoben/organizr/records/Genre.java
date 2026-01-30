package org.abgehoben.organizr.records;

import java.util.Objects;
import java.util.Set;

public record Genre(
        String name,
        Set<Artist> artists,
        Set<Album> albums
) implements MetadataEntity {
    @Override
    public String getName() {
        return name();
    }
    @Override
    public Set<Artist> getArtists() {
        return artists();
    }
    @Override
    public Set<Genre> getGenres() {
        return Set.of(this);
    }
    @Override
    public Set<Album> getAlbums() {
        return albums();
    }

    // Override to avoid recursion through artists and albums
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Genre other)) return false;
        return Objects.equals(name, other.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return "Genre[name=" + name + "]";
    }
}
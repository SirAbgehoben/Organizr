package org.abgehoben.organizr;

public enum SortingScheme {
    ARTIST_ALBUM("Artist/Album", ARTIST, ALBUM),
    ALBUM_ARTIST("Album/Artist", ALBUM, ARTIST),
    GENRE_ARTIST("Genre/Artist", GENRE, ARTIST),
    ALBUM("Album", ALBUM, null),
    ARTIST("Artist", ARTIST, null),
    GENRE("Genre", GENRE, null);

    String displayname;
    SortingScheme firstLevel;
    SortingScheme secondLevel;

    public SortingScheme(String displayname, SortingScheme firstLevel, SortingScheme secondLevel) {
        this.displayname = displayname;
        this.firstLevel = firstLevel;
        this.secondLevel = secondLevel;
    }
}

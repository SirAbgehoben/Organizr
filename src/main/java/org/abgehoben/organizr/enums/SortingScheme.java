package org.abgehoben.organizr.enums;

public enum SortingScheme {
    ARTIST_ALBUM("Artist/Album", FolderEntityType.ARTIST, FolderEntityType.ALBUM),
    ALBUM_ARTIST("Album/Artist", FolderEntityType.ALBUM, FolderEntityType.ARTIST),
    GENRE_ARTIST("Genre/Artist", FolderEntityType.GENRE, FolderEntityType.ARTIST),
    ALBUM("Album", FolderEntityType.ALBUM, null),
    ARTIST("Artist", FolderEntityType.ARTIST, null),
    GENRE("Genre", FolderEntityType.GENRE, null);

    public final String displayname;
    public final FolderEntityType firstLevel;
    public final FolderEntityType secondLevel;

    SortingScheme(String displayname, FolderEntityType firstLevel, FolderEntityType secondLevel) {
        this.displayname = displayname;
        this.firstLevel = firstLevel;
        this.secondLevel = secondLevel;
    }
}

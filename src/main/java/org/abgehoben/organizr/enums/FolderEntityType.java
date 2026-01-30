package org.abgehoben.organizr.enums;

public enum FolderEntityType {
    ARTIST("Artist"),
    ALBUM("Album"),
    GENRE("Genre");

    public final String displayname;

    FolderEntityType(String displayname) {
        this.displayname = displayname;
    }
}

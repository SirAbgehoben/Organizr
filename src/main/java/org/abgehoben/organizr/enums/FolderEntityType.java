package org.abgehoben.organizr.enums;

public enum FolderType {
    ARTIST("Artist"),
    ALBUM("Album"),
    GENRE("Genre");

    public final String displayname;

    FolderType(String displayname) {
        this.displayname = displayname;
    }
}

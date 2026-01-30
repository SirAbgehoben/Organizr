package org.abgehoben.organizr.enums;

public enum MovingScheme {
    MOVE_FILES("Move files"),
    COPY_FILES("Copy files"),
    CREATE_SYMLINKS("Create symlinks");

    public final String displayname;
    MovingScheme(String displayname) {
        this.displayname = displayname;
    }
}

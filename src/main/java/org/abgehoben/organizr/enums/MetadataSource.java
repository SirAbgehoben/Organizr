package org.abgehoben.organizr.enums;

public enum MetadataSource {
    FILE_METADATA("File Metadata"),
    NFO("NFO");

    public final String displayname;
    MetadataSource(String displayname) {
        this.displayname = displayname;
    }
}

package org.abgehoben.organizr.records;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FolderIndex {
    //Entity -> Path
    public Map<MetadataEntity, Path> rootPaths = new HashMap<>();
    //ParentEntity -> (ChildEntity -> Path)
    public Map<MetadataEntity, Map<MetadataEntity, Path>> childPaths = new HashMap<>();
}

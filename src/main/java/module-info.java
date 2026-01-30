module org.abgehoben.organizr {

    requires javafx.controls;

    requires atlantafx.base;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.materialdesign2;
    requires org.yaml.snakeyaml;
    requires javafx.graphics;
    requires jaudiotagger;
    requires java.desktop;
    requires java.sql;
    requires javafx.base;

    exports org.abgehoben.organizr;

    opens org.abgehoben.organizr to org.yaml.snakeyaml;

    // resources
    opens assets;
    opens assets.icons;
    exports org.abgehoben.organizr.enums;
    opens org.abgehoben.organizr.enums to org.yaml.snakeyaml;
    exports org.abgehoben.organizr.records;
    opens org.abgehoben.organizr.records to org.yaml.snakeyaml;
}

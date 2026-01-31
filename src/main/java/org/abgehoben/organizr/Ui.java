package org.abgehoben.organizr;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.abgehoben.organizr.enums.MetadataSource;
import org.abgehoben.organizr.enums.MovingScheme;
import org.abgehoben.organizr.enums.SortingScheme;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignC;
import org.kordamp.ikonli.materialdesign2.MaterialDesignM;
import org.kordamp.ikonli.materialdesign2.MaterialDesignS;

import java.nio.file.Path;
import java.util.function.Function;

import static org.abgehoben.organizr.Main.*;
import static org.abgehoben.organizr.Settings.loadSettings;

public class Ui extends Application {
    public static void launch(String[] args) {
        loadSettings();
        Runtime.getRuntime().addShutdownHook(new Thread(settings::saveSettings));
        Application.launch(args);
    }


    @Override
    public void start(Stage stage) {
        if (settings.darkMode) Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        else Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        var scene = new Scene(createMainPane(stage), 920, 520);
        scene.getStylesheets().add(ASSETS_DIR + "index.css");

        stage.setScene(scene);
        stage.setTitle(APP_NAME);
        stage.getIcons().add(new Image(APP_ICON_PATH));
        stage.setOnCloseRequest(_ -> Platform.exit());
        stage.setMinWidth(214);
        stage.setMinHeight(262);
        stage.setMaxWidth(1440);
        stage.setMaxHeight(1920);

        Platform.runLater(() -> {
            stage.show();
            stage.requestFocus();
        });
    }

    private Pane createMainPane(Stage stage) {
        var titleBox = new HBox(10);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(12));
        var mainIcon = new FontIcon(MaterialDesignS.SORT);
        var titleLabel = new Label(APP_NAME);
        titleLabel.getStyleClass().add("title");
        titleBox.getChildren().addAll(mainIcon, titleLabel);

        var themeBtn = new Button();
        themeBtn.setGraphic(new FontIcon(MaterialDesignM.MOON_WAXING_CRESCENT));
        themeBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;");
        themeBtn.setOnAction(_ -> {
            if (settings.darkMode) {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                themeBtn.setGraphic(new FontIcon(MaterialDesignM.MOON_FULL));
            } else {
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
                themeBtn.setGraphic(new FontIcon(MaterialDesignM.MOON_WAXING_CRESCENT));
            }
            settings.darkMode = !settings.darkMode;
            settings.saveSettings();
        });

        var settingsBtn = new Button();
        settingsBtn.setGraphic(new FontIcon(MaterialDesignC.COG));
        settingsBtn.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 6;");
        settingsBtn.setOnAction(_ -> openSettingsWindow(stage));

        var rightIcons = new HBox(6, themeBtn, settingsBtn);
        rightIcons.setAlignment(Pos.CENTER_RIGHT);

        var topBar = new BorderPane();
        topBar.setLeft(titleBox);
        topBar.setRight(rightIcons);

        var inputField = new TextField();
        inputField.setText(settings.inputDir);
        inputField.setPromptText("Select input directory containing music files");
        inputField.setPrefWidth(420);
        inputField.setEditable(true);
        var inputBtn = new Button("Choose Input");
        inputBtn.setPrefWidth(158);
        inputBtn.setMinWidth(158);
        inputBtn.setOnAction(_ -> {
            var chooser = new DirectoryChooser();
            chooser.setTitle("Select Input Directory");
            Path dir = chooser.showDialog(stage).toPath();
            inputField.setText(dir.toString());
            settings.inputDir = dir.toAbsolutePath().toString();
            settings.saveSettings();
        });
        var inputRow = new HBox(8, inputField, inputBtn);
        inputRow.setAlignment(Pos.CENTER_LEFT);

        var outputField = new TextField();
        outputField.setText(settings.outputDir);
        outputField.setPromptText("Select output directory for organized music");
        outputField.setPrefWidth(420);
        outputField.setEditable(true);
        var outputBtn = new Button("Choose Output");
        outputBtn.setPrefWidth(158);
        outputBtn.setMinWidth(158);
        outputBtn.setOnAction(_ -> {
            var chooser = new DirectoryChooser();
            chooser.setTitle("Select Output Directory");
            Path dir = chooser.showDialog(stage).toPath();
            outputField.setText(dir.toString());
            settings.outputDir = dir.toAbsolutePath().toString();
            settings.saveSettings();
        });
        var outputRow = new HBox(8, outputField, outputBtn);
        outputRow.setAlignment(Pos.CENTER_LEFT);

        startBtn = new Button("Start Sort");
        startBtn.setMinWidth(120);
        startBtn.setOnAction(_ -> {
            progressArea.getItems().clear();
            settings.inputDir = inputField.getText();
            settings.outputDir = outputField.getText();
            settings.saveSettings();
            startBtn.setDisable(true);
            Sorting.sortFilesAsync(settings.getSettings(), stage);
        });

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(288);
        progressBar.setPrefHeight(12);
        progressLabel = new Label(settings.isReady() ? "Ready to start sorting." : "select input and output directories.");

        var controls = new HBox(12, progressBar, progressLabel);
        controls.setAlignment(Pos.CENTER_LEFT);

        progressArea = new ListView<>();
        progressArea.setEditable(false);
        progressArea.setPrefHeight(1780);
        progressArea.setStyle("-fx-font-family: monospace; -fx-font-size: 11px;");
        progressArea.getStyleClass().add("console-view");
        progressArea.setFixedCellSize(16.0);


        var center = new VBox(12, inputRow, outputRow, new HBox(12, startBtn, controls), progressArea);
        center.setPadding(new Insets(12));

        var root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(center);

        return root;
    }

    private void openSettingsWindow(Stage owner) {
        var settingsStage = new Stage();
        settingsStage.initOwner(owner);
        settingsStage.initModality(Modality.WINDOW_MODAL);
        settingsStage.setTitle("Settings");

        var includeSubdirs = new CheckBox("Include subdirectories");
        includeSubdirs.setSelected(settings.includeSubdirs);
        var useSymlinks = new CheckBox("Use symlinks to safe space, only works for move and copy");
        useSymlinks.setSelected(settings.useSymlinks);
        var keepDate = new CheckBox("Keep original edit and creation date");
        keepDate.setSelected(settings.keepDate);
        var splitArtists = new CheckBox("Split songs of multiple artists into separate folders");
        splitArtists.setSelected(settings.splitArtists);
        var useEmbeddedTitle = new CheckBox("Use metadata title tag instead of filename");
        useEmbeddedTitle.setSelected(settings.splitArtists);

        var movingChoice = createChoiceBox(MovingScheme.values(), settings.movingScheme, s -> s.displayname);
        var sortingChoice = createChoiceBox(SortingScheme.values(), settings.sortingScheme, s -> s.displayname);
        var metadataChoice = createChoiceBox(MetadataSource.values(), settings.metadataSource, s -> s.displayname);


        var saveBtn = new Button("Save");
        saveBtn.setOnAction(_ -> {
            settings.includeSubdirs = includeSubdirs.isSelected();
            settings.useSymlinks = useSymlinks.isSelected();
            settings.keepDate = keepDate.isSelected();
            settings.splitArtists = splitArtists.isSelected();
            settings.useEmbeddedTitle = useEmbeddedTitle.isSelected();
            settings.movingScheme = movingChoice.getValue();
            settings.sortingScheme = sortingChoice.getValue();
            settings.metadataSource = metadataChoice.getValue();
            settings.saveSettings();
            settingsStage.close();
        });

        var closeBtn = new Button("Close");
        closeBtn.setOnAction(_ -> settingsStage.close());

        var buttons = new HBox(8, saveBtn, closeBtn);
        buttons.setAlignment(Pos.BOTTOM_RIGHT);

        var content = new VBox(10,
                new Label("General Settings"),
                includeSubdirs,
                useSymlinks,
                keepDate,
                splitArtists,
                useEmbeddedTitle,
                new Label("moving scheme:"),
                movingChoice,
                new Label("sorting scheme:"),
                sortingChoice,
                new Label("where to get metadata from:"),
                metadataChoice,
                buttons
        );
        var root = new BorderPane();
        root.setCenter(content);
        root.setBottom(buttons);
        root.setPadding(new Insets(12));

        var scene = new Scene(root, 560, 415);
        settingsStage.setScene(scene);
        settingsStage.setMinHeight(468);
        settingsStage.setMinWidth(560);
        settingsStage.show();
    }

    private <T> ChoiceBox<T> createChoiceBox(T[] items, T current, Function<T, String> labeler) {
        var box = new ChoiceBox<>(FXCollections.observableArrayList(items));
        box.setConverter(new StringConverter<>() {
            @Override public String toString(T t) { return t == null ? "" : labeler.apply(t); }
            @Override public T fromString(String s) { return null; }
        });
        box.setValue(current);
        return box;
    }

    public static boolean showNotEmptyConfirmation(Stage owner) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setTitle("Output Directory Not Empty");
        alert.setHeaderText("The output directory is not empty.");
        alert.setContentText("Are you sure you want to continue? This will delete existing files in the output directory.");

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }
}

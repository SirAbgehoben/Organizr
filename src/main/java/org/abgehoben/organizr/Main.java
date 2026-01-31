package org.abgehoben.organizr;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.abgehoben.organizr.Settings.loadSettings;
import static org.abgehoben.organizr.Ui.launch;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javafx.application.Platform;
import javafx.scene.control.*;

public class Main {

    static final String ASSETS_DIR = "/assets/";

    static final String APP_ICON_PATH = Objects.requireNonNull(
            Main.class.getResource(ASSETS_DIR + "icons/app-icon.svg")
    ).toExternalForm();

    static final String APP_PROPERTIES_PATH = "/application.properties";

    static {
        loadApplicationProperties();
    }

    public static final String APP_NAME = System.getProperty("app.name");
    public static final Path APP_DIR = getAppDir();
    public static Path CONFIG_PATH = APP_DIR.resolve(APP_NAME + ".conf");
    public static final Settings settings = loadSettings();

    private static final List<String> buffer = new ArrayList<>();
    private static boolean isBufferUpdatePending = false;

    public static ProgressBar progressBar;
    public static Label progressLabel;
    public static ListView<String> progressArea;
    public static Button startBtn;

    public static void main(String[] args) {
        loadSettings();
        Runtime.getRuntime().addShutdownHook(new Thread(settings::saveSettings));
        launch(args);
    }


    /**
     * Append a single line to the progress view.
     */
    public static void addProgressText(String text) {
        System.out.println(text);
        synchronized (buffer) {
            buffer.add(text);
            if (isBufferUpdatePending) return;
            isBufferUpdatePending = true;
        }

        Platform.runLater(() -> {
            List<String> copy;
            synchronized (buffer) {
                copy = new ArrayList<>(buffer);
                buffer.clear();
                isBufferUpdatePending = false;
            }

            progressArea.getItems().addAll(copy);

            progressArea.scrollTo(progressArea.getItems().size() - 1);
        });
    }

    /**
     * sets the progressLabel text
     */
    public static void addProgressLabelText(String text) {
        Platform.runLater(() -> {
                if (progressLabel != null) {
                    progressLabel.setText(text);
                }
        });
    }

    /**
     * Updates the progress bar.
     * @param workDone The current number of files processed.
     * @param total The total number of files. Pass -1 to make the bar wave.
     */
    public static void updateProgressBar(double workDone, double total) {
        Platform.runLater(() -> {
            if (progressBar != null) {
                if (workDone < 0) {
                    progressBar.setProgress(-1);
                } else if (total <= 0) {
                    progressBar.setProgress(0);
                } else {
                    progressBar.setProgress(workDone / total);
                }
            }
        });
    }

    public static void enableStartButton() {
        Platform.runLater(() -> startBtn.setDisable(false));
    }

    private static void loadApplicationProperties() {
        try {
            Properties properties = new Properties();
            properties.load(new InputStreamReader(
                    Objects.requireNonNull(Main.class.getResourceAsStream(APP_PROPERTIES_PATH)),
                    UTF_8
            ));
            properties.forEach((key, value) -> System.setProperty(
                    String.valueOf(key),
                    String.valueOf(value)
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Path getAppDir() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) return Path.of(System.getenv("APPDATA"), APP_NAME);
        if (os.contains("mac")) return Path.of(System.getProperty("user.home"), "Library/Application Support", APP_NAME);
        return Path.of(System.getenv().getOrDefault("XDG_CONFIG_HOME", System.getProperty("user.home") + "/.config"), APP_NAME);
    }
}

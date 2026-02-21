package org.abgehoben.organizr;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.abgehoben.organizr.Settings.loadSettings;
import static org.abgehoben.organizr.Ui.launch;

import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class Main {

    static final String ASSETS_DIR = "/assets/";

    static final String APP_ICON_PATH = Objects.requireNonNull(
            Main.class.getResource(ASSETS_DIR + "icons/app-icon.svg")
    ).toExternalForm();

    static final String APP_PROPERTIES_PATH = "/application.properties";

    static {
        loadApplicationProperties();
    }
    static Boolean HAS_GUI = true;
    static Console console;

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
        if (asList(args).contains("--no-gui")) {
            HAS_GUI = false;
        }
        if (asList(args).contains("--help") || asList(args).contains("-h")) {
            System.out.println("Usage: organizr [options]");
            System.out.println("Options:");
            System.out.println("  --no-gui      Run without GUI");
            System.out.println("  --input=DIR   Set input directory");
            System.out.println("  --output=DIR  Set output directory");
            System.out.println("  --help, -h    Show this help message");
            return;
        }
        for (String arg : args) {
            if (arg.startsWith("--input=")) {
                settings.inputDir = arg.substring("--input=".length());
            } else if (arg.startsWith("--output=")) {
                settings.outputDir = arg.substring("--output=".length());
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(settings::saveSettings));
        if (HAS_GUI) launch(args);
        else launchConsole();
    }

    public static void launchConsole() {
        console = System.console();
        if (console == null) {
            System.err.println("No console available. Cannot run in no-GUI mode.");
            return;
        }
        Sorting.sortFiles(settings);
    }


    /**
     * Append a single line to the progress view.
     */
    public static void addProgressText(String text) {
        System.out.println(text);
        if (HAS_GUI) {
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
    }

    /**
     * sets the progressLabel text
     */
    public static void addProgressLabelText(String text) {
        if (HAS_GUI) {
            Platform.runLater(() -> {
                if (progressLabel != null) {
                    progressLabel.setText(text);
                }
            });
        }
    }

    /**
     * Updates the progress bar.
     * @param workDone The current number of files processed.
     * @param total The total number of files. Pass -1 to make the bar wave.
     */
    public static void updateProgressBar(double workDone, double total) {
        if (HAS_GUI) {
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
    }

    public static void enableStartButton() {
        Platform.runLater(() -> startBtn.setDisable(false));
    }

    public static boolean showNotEmptyConfirmation(Stage owner) {
        if (HAS_GUI) {
            CompletableFuture<Boolean> future = new CompletableFuture<>();
            Platform.runLater(() -> future.complete(Ui.showNotEmptyConfirmation(owner)));
            return future.join();
        }
        else {
            String response = console.readLine(":: Delete existing files? [y/N]");
            return response.equalsIgnoreCase("y") || response.equalsIgnoreCase("yes");
        }
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

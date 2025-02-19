package me.freezy.plugins.papermc.blazesmp.module.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.logging.Logger;

public class L4M4 {
    private static final Logger LOGGER = Logger.getLogger("L4M4");
    private static final String MESSAGES_STORAGE_PATH = "plugins/BlazeSMP/storage/messages.json";

    // Map zum Speichern der geladenen Nachrichten
    private static Map<String, String> messages;

    /**
     * Initialisiert die messages.json und lädt anschließend die Nachrichten.
     * Diese Methode muss vor der Verwendung von get() aufgerufen werden.
     */
    public static void init() {
        initializeMessages();
        loadMessages();
    }

    /**
     * Initialisiert die messages.json.
     * Falls die Datei nicht existiert, wird der Standardinhalt aus den Ressourcen geladen und gespeichert.
     */
    private static void initializeMessages() {
        File messagesFile = new File(MESSAGES_STORAGE_PATH);
        if (!messagesFile.exists()) {
            // Erstelle die notwendigen Verzeichnisse
            if (messagesFile.getParentFile() != null && !messagesFile.getParentFile().exists()) {
                if (messagesFile.getParentFile().mkdirs()) {
                    LOGGER.info("Verzeichnis für messages.json erstellt: " + messagesFile.getParentFile().getAbsolutePath());
                } else {
                    LOGGER.severe("Fehler beim Erstellen des Verzeichnisses für messages.json: " + messagesFile.getParentFile().getAbsolutePath());
                    return;
                }
            }
            // Lade die Ressource als Stream
            try (InputStream in = L4M4.class.getClassLoader().getResourceAsStream("storage/messages.json")) {
                if (in == null) {
                    LOGGER.severe("Resource 'storage/messages.json' nicht gefunden!");
                    return;
                }
                // Kopiere den Inhalt der Ressource in die Zieldatei
                Files.copy(in, messagesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                LOGGER.info("Default messages.json wurde kopiert nach: " + MESSAGES_STORAGE_PATH);
            } catch (IOException e) {
                LOGGER.severe("Fehler beim Kopieren der Default messages.json: " + e.getMessage());
            }
        } else {
            LOGGER.info("messages.json existiert bereits unter: " + MESSAGES_STORAGE_PATH);
        }
    }

    /**
     * Lädt die messages.json in ein Map.
     */
    private static void loadMessages() {
        File messagesFile = new File(MESSAGES_STORAGE_PATH);
        if (!messagesFile.exists()) {
            LOGGER.severe("messages.json Datei nicht gefunden beim Laden.");
            return;
        }
        try (Reader reader = new FileReader(messagesFile)) {
            Gson gson = new Gson();
            messages = gson.fromJson(reader, new TypeToken<Map<String, String>>() {}.getType());
            LOGGER.info("messages.json wurde erfolgreich geladen.");
        } catch (IOException e) {
            LOGGER.severe("Fehler beim Laden von messages.json: " + e.getMessage());
        }
    }

    /**
     * Gibt den Wert zum gegebenen Schlüssel zurück.
     *
     * @param key der Schlüssel
     * @return der Wert, falls vorhanden, ansonsten null.
     */
    public static String get(String key) {
        if (messages == null) {
            LOGGER.warning("Messages wurden nicht geladen. Bitte init() aufrufen.");
            return "404 not found";
        }
        String value = messages.get(key);
        if (value == null) {
            LOGGER.warning("Key '" + key + "' nicht in messages.json gefunden.");
        }
        return value;
    }
}

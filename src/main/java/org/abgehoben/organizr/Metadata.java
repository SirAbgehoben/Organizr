package org.abgehoben.organizr;

import org.abgehoben.organizr.records.Album;
import org.abgehoben.organizr.records.Artist;
import org.abgehoben.organizr.records.Genre;
import org.abgehoben.organizr.records.MusicFile;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.abgehoben.organizr.main.addProgressText;

public class Metadata {

    // Bands in this list will NEVER be split
    private static final ArrayList<String> PROTECTED_ARTISTS = new ArrayList<>(Arrays.asList(
            "Earth, Wind & Fire",
            "Crosby, Stills, Nash & Young",
            "Emerson, Lake & Palmer",
            "Kool & The Gang",
            "Of Monsters and Men",
            "Brooks & Dunn",
            "Simon & Garfunkel",
            "AC/DC",
            "Peter, Bjorn and John"
    ));

    private static final Pattern PROTECTED_PATTERN;

    static {
        PROTECTED_ARTISTS.sort((a, b) -> b.length() - a.length());
        String regex = PROTECTED_ARTISTS.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|"));
        PROTECTED_PATTERN = Pattern.compile(regex);
    }

    private static final Pattern SPLIT_PATTERN = Pattern.compile(
            "(?i)\\s+(?:feat\\.?|ft\\.?|vs\\.?|with|x)\\s+|" + // Keywords
                    "\\s+(?:and|&)\\s+|" +                             // 'and' or '&'
                    "\\s*;\\s*|" +                                      // Semicolons
                    "\\s*,\\s+(?!(?:The|Da|El|La|Jr\\.?|Sr\\.?)\\b)|" + // Commas (with guards)
                    "\\s*/\\s*"                                         // Slashes
    );

    public static final Map<String, Artist> artistCache = new HashMap<>();
    public static final Map<String, Album> albumCache = new HashMap<>();
    public static final Map<String, Genre> genreCache = new HashMap<>();

    public static MusicFile getFileMetadata(File file, Settings params) {
        AudioFile audioFile;
        try {
            audioFile = AudioFileIO.read(file);
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            addProgressText("There was an error reading file: " + file.getName() + ". Skipping.");
            return null;
        }
        Tag tag = audioFile.getTag();

        String title = params.useEmbeddedTitle ? extractTitle(tag) : extractName(file);
        String albumName = extractAlbum(tag);
        String genreName = extractGenre(tag);
        ArrayList<String> ArtistNames = params.splitArtists ? extractArtists(tag) : extractArtist(tag);

        Album album = albumCache.computeIfAbsent(albumName, n -> new Album(n, new HashSet<>(), new HashSet<>()));
        Genre genre = genreCache.computeIfAbsent(genreName, n -> new Genre(n, new HashSet<>(), new HashSet<>()));

        List<Artist> artists = new ArrayList<>();
        for (String name : ArtistNames) {
            Artist artist = artistCache.computeIfAbsent(name, n -> new Artist(n, new HashSet<>(), new HashSet<>()));
            artists.add(artist);

            // Link Artist <-> Album
            artist.albums().add(album);
            album.artists().add(artist);

            // Link Artist <-> Genre
            artist.genres().add(genre);
            genre.artists().add(artist);
        }

        // Link Album <-> Genre
        album.genres().add(genre);
        genre.albums().add(album);

        return new MusicFile(file,
                artists,
                album,
                genre,
                title);
    }
    public static ArrayList<String> extractArtists(Tag tag) {
        return getArtists(getTagOrUnknown(tag, FieldKey.ARTIST));
    }
    public static ArrayList<String> extractArtist(Tag tag) {
        return new ArrayList<>(List.of(getTagOrUnknown(tag, FieldKey.ARTIST)));
    }
    public static String extractAlbum(Tag tag) {
        return getTagOrUnknown(tag, FieldKey.ALBUM);
    }
    public static String extractTitle(Tag tag) {
        return getTagOrUnknown(tag, FieldKey.TITLE);
    }
    public static String extractGenre(Tag tag) {
        return getTagOrUnknown(tag, FieldKey.GENRE);
    }
    public static String extractName(File file) {
        return file.getName();
    }

    private static String getTagOrUnknown(Tag tag, FieldKey key) {
        String val = tag.getFirst(key);
        return val == null || val.trim().isEmpty() ? "Unknown" : sanitize(val);
    }

    public static MusicFile getNfoMetadata(File file) { //TODO
        // Placeholder for NFO metadata extraction logic
        Album unknownAlbum = albumCache.computeIfAbsent("Unknown", n -> new Album(n, new HashSet<>(), new HashSet<>()));
        Genre unknownGenre = genreCache.computeIfAbsent("Unknown", n -> new Genre(n, new HashSet<>(), new HashSet<>()));
        Artist unknownArtist = artistCache.computeIfAbsent("Unknown", n -> new Artist(n, new HashSet<>(), new HashSet<>()));

        return new MusicFile(file, new ArrayList<>(List.of(unknownArtist)), unknownAlbum, unknownGenre, "Unknown");
    }

    public static ArrayList<String> getArtists(String rawArtists) {
        Map<String, String> tokenMap = new HashMap<>();

        //find ALL protected Artists
        Matcher matcher = PROTECTED_PATTERN.matcher(rawArtists);
        StringBuilder sb = new StringBuilder();
        int tokenIndex = 0;

        while (matcher.find()) {
            String foundBand = matcher.group();
            String token = "###P" + tokenIndex++ + "###";

            tokenMap.put(token, foundBand);

            matcher.appendReplacement(sb, token);
        }
        matcher.appendTail(sb);

        String tempString = sb.toString().replace("(", " ").replace(")", " ");
        String[] rawSplits = SPLIT_PATTERN.split(tempString);

        ArrayList<String> result = new ArrayList<>();

        for (String s : rawSplits) {
            String candidate = s.trim();
            if (candidate.isEmpty()) continue;

            // restore the artist name if it's a token
            result.add(tokenMap.getOrDefault(candidate, candidate));
        }

        return result;
    }

    public static String sanitize(String name) {
        return name.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}

package com.reader.util;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * Factory that selects the appropriate ComicParser for a given file.
 */
@Component
public class ComicParserFactory {

    private final List<ComicParser> parsers;

    public ComicParserFactory() {
        // Order matters: specific formats before generic folder parser
        this.parsers = List.of(
                new ZipComicParser(),
                new PdfComicParser(),
                new FolderComicParser()
        );
    }

    /**
     * Returns the first ComicParser that supports the given file, or null if none match.
     */
    public ComicParser getParser(File file) {
        for (ComicParser parser : parsers) {
            if (parser.supports(file)) {
                return parser;
            }
        }
        return null;
    }
}

# Organizr

A comprehensive Java-based music library organizer. This tool helps you maintain a well-organized music library with
proper metadata and folder structure

## Features

- Sorting by different sorting schemes (Artist -> Album, Album -> Artist, Genre -> Artist, Album, Artist and Genre)
- Different Moving Schemes (Moving files, Copying files and Creating Symlinks)
- Copying with old Edit / Creation dates
- Splitting up multiple artists of a single Song
- Automatic file renaming based on metadata
- Using Symlinks to safe on space if a song needs to be copied multiple times
- Modern JavaFx based Gui
- Dark / Light mode

## Prerequisites

- A computer
- An operating system
- Java is needed for the universal .jar

## Installation
1. Get the right Artifact for your operating system from the [releases](https://github.com/SirAbgehoben/Organizr/releases)
2. Install / Launch it

## File Structure

- `Main.java`: Core functionality for the initialization and updating of Ui elements
- `FileOperations.java`: Handles File operations like moving of files, creating of folders and indexing
- `Metadata.java`: Gets Metadata and sanitizes it
- `Settings.java`: Stores the users Settings
- `Sorting.java`: Handles the sort
- `Validation.java`: Validates for example if there is enough space or if the output directory is not writable
- `Ui.java`: Responsible for creating the Ui elements
- `recods`: Here are the record(and simmilar) classes stored
- `enums`: Here are the enums classes stored, for example the MetadataSource
   
## Notes

- Getting Metadata from Nfo's is currently not implemented
- Special characters are handled and cleaned in filenames

## Contributing

Feel free to submit issues and enhancement requests!

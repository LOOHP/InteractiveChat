/*
 * This file is part of InteractiveChat.
 *
 * Copyright (C) 2020 - 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2020 - 2025. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactivechat.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtils {

    private static final ClassLoader NULL_CLASSLOADER = null;

    public static void removeFolderRecursively(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    removeFolderRecursively(file);
                } else {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                }
            }
            if (!folder.delete()) {
                folder.deleteOnExit();
            }
        }
    }

    public static long copy(File from, File to) throws IOException {
        FileInputStream stream = new FileInputStream(from);
        long result = Files.copy(stream, to.toPath());
        stream.close();
        return result;
    }

    public static long copy(InputStream from, File to) throws IOException {
        return Files.copy(from, to.toPath());
    }

    public static void copyZipEntry(File zipFile, String fileName, File outputFile) throws IOException {
        try (FileSystem fileSystem = FileSystems.newFileSystem(zipFile.toPath(), NULL_CLASSLOADER)) {
            Path fileToExtract = fileSystem.getPath(fileName);
            Files.copy(fileToExtract, outputFile.toPath());
        }
    }

}

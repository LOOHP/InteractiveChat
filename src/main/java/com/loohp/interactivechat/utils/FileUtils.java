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

package com.loohp.interactivechat.Utils;

import java.io.File;

public class FileUtils {

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

}

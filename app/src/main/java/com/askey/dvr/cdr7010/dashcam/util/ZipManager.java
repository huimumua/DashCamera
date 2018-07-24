package com.askey.dvr.cdr7010.dashcam.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipManager {
	private static final String LOG_TAG = "ZipManager";
	private static final int BUFFER = 80000;

	public static void zip(List<String> zipFilePathList, String zipFileName) {
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(zipFileName);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
			byte data[] = new byte[BUFFER];
			for (int i = 0; i < zipFilePathList.size(); i++) {
				String filePath = zipFilePathList.get(i);
				Logg.v(LOG_TAG, "zip: " + filePath);
				FileInputStream fi = new FileInputStream(filePath);
				origin = new BufferedInputStream(fi, BUFFER);

				String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
				String fileParentPath = filePath.substring(0, filePath.lastIndexOf("/"));
				String fileParentName = fileParentPath.substring(fileParentPath.lastIndexOf("/") + 1);
				String zipInFileName = fileParentName + "_" + fileName;

				ZipEntry entry = new ZipEntry(zipInFileName);
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void unzip(String _zipFile, String _targetLocation) {
		//create target location folder if not exist
		dirChecker(_targetLocation);
		try {
			FileInputStream fin = new FileInputStream(_zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				//create dir if required while unzipping
				if (ze.isDirectory()) {
					dirChecker(ze.getName());
				} else {
					FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read()) {
						fout.write(c);
					}
					zin.closeEntry();
					fout.close();
				}
			}
			zin.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private static void dirChecker(String dir) {
		File f = new File(dir);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}

}
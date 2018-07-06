package com.askey.dvr.cdr7010.dashcam.util;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * File Utils
 */
public class FileUtils {

    public final static String FILE_EXTENSION_SEPARATOR = ".";
    private static final String LOG_TAG = FileUtils.class.getSimpleName();
    private static final int THUMB_WIDTH = 88;
    private static final int THUMB_HEIGHT = 88;
    private static final int BYTE = 1024;
    private static final int THUMB_MAX_SIZE = 10;
    private static final int THUMB_WIDTH_HEIGHT_DECREASE = 8;

    private FileUtils() {
        throw new AssertionError();
    }


    /**
     * android 移动文件方法 File renameTo
     */
    public static String renameTo(String sourceFilePath, String destFilePath) {
        if (TextUtils.isEmpty(sourceFilePath) || TextUtils.isEmpty(destFilePath)) {
            throw new RuntimeException("Both sourceFilePath and destFilePath cannot be null.");
        }
        //moveFile(new File(sourceFilePath), new File(destFilePath));
        File srcFile = new File(sourceFilePath);
        if (!srcFile.exists() || !srcFile.isFile())
            return null;

        File destDir = new File(destFilePath);
        if (!destDir.exists())
            destDir.mkdirs();

        String dst = destFilePath + File.separator + srcFile.getName();
        if (srcFile.renameTo(new File(dst)))
            return dst;
        else
            return null;
    }

    public static String renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath) || TextUtils.isEmpty(newPath)) {
            throw new RuntimeException("Both sourceFilePath and destFilePath cannot be null.");
        }
        File file = new File(oldPath);
        if (!file.exists() || !file.isFile())
            return null;
        if (file.renameTo(new File(newPath))) {
            return newPath;
        } else {
            return null;
        }
    }

    /**
     * read file
     *
     * @param filePath
     * @param charsetName The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator BufferedReader
     */
    public static StringBuilder readFile(String filePath, String charsetName) {
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);//参数charset设置编码例如UTF-8
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            try {
                if(reader!=null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * write file
     *
     * @param filePath
     * @param content
     * @param append   is append, if true, write to the end of file, else clear content of file and write into it
     * @return return false if content is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, String content, boolean append) {
        if(filePath ==null || content ==null){
            return false;
        }
        if (content.isEmpty()) {
            return false;
        }
        boolean ret = true;
        OutputStreamWriter out = null;
        Logg.d("FileUtils","content1="+content);
        try {
            makeDirs(filePath);
            out = new OutputStreamWriter(new FileOutputStream(filePath, append),"UTF-8");
            Logg.d("FileUtils","content="+content);
            out.write(content);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
            ret = false;
        } finally {
            try {
                if(out!=null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    /**
     * write file
     *
     * @param filePath
     * @param contentList
     * @param append      is append, if true, write to the end of file, else clear content of file and write into it
     * @return return false if contentList is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, List<String> contentList, boolean append) {

        if (contentList.isEmpty()) {
            return false;
        }
        OutputStreamWriter out = null;
        try {
            makeDirs(filePath);
            out = new OutputStreamWriter(new FileOutputStream(filePath, append),"UTF-8");
            int i = 0;
            for (String line : contentList) {
                if (i++ > 0) {
                    out.write("\r\n");
                }
                out.write(line);
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            try {
                if(out!=null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * write file, the string will be written to the begin of the file
     *
     * @param filePath
     * @param content
     * @return
     */
    public static boolean writeFile(String filePath, String content) {
        return writeFile(filePath, content, false);
    }

    /**
     * write file, the string list will be written to the begin of the file
     *
     * @param filePath
     * @param contentList
     * @return
     */
    public static boolean writeFile(String filePath, List<String> contentList) {
        return writeFile(filePath, contentList, false);
    }

    /**
     * write file, the bytes will be written to the begin of the file
     *
     * @param filePath
     * @param stream
     * @return
     * @see {@link #writeFile(String, InputStream, boolean)}
     */
    public static boolean writeFile(String filePath, InputStream stream) {
        return writeFile(filePath, stream, false);
    }

    /**
     * write file
     *
     * @param stream the input stream
     * @param append if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(String filePath, InputStream stream, boolean append) {
        return writeFile(filePath != null ? new File(filePath) : null, stream, append);
    }

    /**
     * write file, the bytes will be written to the begin of the file
     *
     * @param file
     * @param stream
     * @return
     * @see {@link #writeFile(File, InputStream, boolean)}
     */
    public static boolean writeFile(File file, InputStream stream) {
        return writeFile(file, stream, false);
    }

    /**
     * write file
     *
     * @param file   the file to be opened for writing.
     * @param stream the input stream
     * @param append if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(File file, InputStream stream, boolean append) {
        OutputStream o = null;
        try {
            makeDirs(file.getAbsolutePath());
            o = new FileOutputStream(file, append);
            byte data[] = new byte[1024];
            int length = -1;
            while ((length = stream.read(data)) != -1) {
                o.write(data, 0, length);
            }
            o.flush();
            return true;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            try {
                if(o!=null) {
                    o.close();
                }
                if(stream!=null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * move file
     *
     * @param sourceFilePath
     * @param destFilePath
     */
    public static void moveFile(String sourceFilePath, String destFilePath) {
        if (TextUtils.isEmpty(sourceFilePath) || TextUtils.isEmpty(destFilePath)) {
            throw new RuntimeException("Both sourceFilePath and destFilePath cannot be null.");
        }
        moveFile(new File(sourceFilePath), new File(destFilePath));
    }

    /**
     * move file
     *
     * @param srcFile
     * @param destFile
     */
    public static void moveFile(File srcFile, File destFile) {
        boolean rename = srcFile.renameTo(destFile);
        if (!rename) {
            copyFile(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
            deleteFile(srcFile.getAbsolutePath());
        }
    }

    /**
     * copy file
     *
     * @param sourceFilePath
     * @param destFilePath
     * @return
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean copyFile(String sourceFilePath, String destFilePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sourceFilePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        }
        return writeFile(destFilePath, inputStream);
    }

    /**
     * read file to string list, a element of list is a line
     *
     * @param filePath
     * @param charsetName The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator BufferedReader
     */
    public static List<String> readFileToList(String filePath, String charsetName) {
        File file = new File(filePath);
        List<String> fileContent = new ArrayList<String>();
        if (file == null || !file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContent.add(line);
            }
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get file name from path, not include suffix
     * <p>
     * <pre>
     *      getFileNameWithoutExtension(null)               =   null
     *      getFileNameWithoutExtension("")                 =   ""
     *      getFileNameWithoutExtension("   ")              =   "   "
     *      getFileNameWithoutExtension("abc")              =   "abc"
     *      getFileNameWithoutExtension("a.mp3")            =   "a"
     *      getFileNameWithoutExtension("a.b.rmvb")         =   "a.b"
     *      getFileNameWithoutExtension("c:\\")              =   ""
     *      getFileNameWithoutExtension("c:\\a")             =   "a"
     *      getFileNameWithoutExtension("c:\\a.b")           =   "a"
     *      getFileNameWithoutExtension("c:a.txt\\a")        =   "a"
     *      getFileNameWithoutExtension("/home/admin")      =   "admin"
     *      getFileNameWithoutExtension("/home/admin/a.txt/b.mp3")  =   "b"
     * </pre>
     *
     * @param filePath
     * @return file name from path, not include suffix
     * @see
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (filePath.isEmpty()) {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            return (extenPosi == -1 ? filePath : filePath.substring(0, extenPosi));
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1, extenPosi) : filePath.substring(filePosi + 1));
    }

    /**
     * get file name from path, include suffix
     * <p>
     * <pre>
     *      getFileName(null)               =   null
     *      getFileName("")                 =   ""
     *      getFileName("   ")              =   "   "
     *      getFileName("a.mp3")            =   "a.mp3"
     *      getFileName("a.b.rmvb")         =   "a.b.rmvb"
     *      getFileName("abc")              =   "abc"
     *      getFileName("c:\\")              =   ""
     *      getFileName("c:\\a")             =   "a"
     *      getFileName("c:\\a.b")           =   "a.b"
     *      getFileName("c:a.txt\\a")        =   "a"
     *      getFileName("/home/admin")      =   "admin"
     *      getFileName("/home/admin/a.txt/b.mp3")  =   "b.mp3"
     * </pre>
     *
     * @param filePath
     * @return file name from path, include suffix
     */
    public static String getFileName(String filePath) {
        if (filePath.isEmpty()) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    /**
     * get folder name from path
     * <p>
     * <pre>
     *      getFolderName(null)               =   null
     *      getFolderName("")                 =   ""
     *      getFolderName("   ")              =   ""
     *      getFolderName("a.mp3")            =   ""
     *      getFolderName("a.b.rmvb")         =   ""
     *      getFolderName("abc")              =   ""
     *      getFolderName("c:\\")              =   "c:"
     *      getFolderName("c:\\a")             =   "c:"
     *      getFolderName("c:\\a.b")           =   "c:"
     *      getFolderName("c:a.txt\\a")        =   "c:a.txt"
     *      getFolderName("c:a\\b\\c\\d.txt")    =   "c:a\\b\\c"
     *      getFolderName("/home/admin")      =   "/home"
     *      getFolderName("/home/admin/a.txt/b.mp3")  =   "/home/admin/a.txt"
     * </pre>
     *
     * @param filePath
     * @return
     */
    public static String getFolderName(String filePath) {
        if (filePath.isEmpty()) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    /**
     * get suffix of file from path
     * <p>
     * <pre>
     *      getFileExtension(null)               =   ""
     *      getFileExtension("")                 =   ""
     *      getFileExtension("   ")              =   "   "
     *      getFileExtension("a.mp3")            =   "mp3"
     *      getFileExtension("a.b.rmvb")         =   "rmvb"
     *      getFileExtension("abc")              =   ""
     *      getFileExtension("c:\\")              =   ""
     *      getFileExtension("c:\\a")             =   ""
     *      getFileExtension("c:\\a.b")           =   "b"
     *      getFileExtension("c:a.txt\\a")        =   ""
     *      getFileExtension("/home/admin")      =   ""
     *      getFileExtension("/home/admin/a.txt/b")  =   ""
     *      getFileExtension("/home/admin/a.txt/b.mp3")  =   "mp3"
     * </pre>
     *
     * @param filePath
     * @return
     */
    public static String getFileExtension(String filePath) {
        if (filePath.isEmpty()) {
            return filePath;
        }

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (extenPosi == -1) {
            return "";
        }
        return (filePosi >= extenPosi) ? "" : filePath.substring(extenPosi + 1);
    }

    /**
     * Creates the directory named by the trailing filename of this file, including the complete directory path required
     * to create this directory. <br/>
     * <br/>
     * <ul>
     * <strong>Attentions:</strong>
     * <li>makeDirs("C:\\Users\\Trinea") can only create users folder</li>
     * <li>makeFolder("C:\\Users\\Trinea\\") can create Trinea folder</li>
     * </ul>
     *
     * @param filePath
     * @return true if the necessary directories have been created or the target directory already exists, false one of
     * the directories can not be created.
     * <ul>
     * <li>if {@link FileUtils#getFolderName(String)} return null, return false</li>
     * <li>if target directory already exists, return true</li>
     * </ul>
     */
    public static boolean makeDirs(String filePath) {
        String folderName = getFolderName(filePath);
        if (folderName.isEmpty()) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }

    /**
     * @param filePath
     * @return
     * @see #makeDirs(String)
     */
    public static boolean makeFolders(String filePath) {
        return makeDirs(filePath);
    }

    /**
     * Indicates if this file represents a file on the underlying file system.
     *
     * @param filePath
     * @return
     */
    public static boolean isFileExist(String filePath) {
        if (filePath.isEmpty()) {
            return false;
        }

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     * Indicates if this file represents a directory on the underlying file system.
     *
     * @param directoryPath
     * @return
     */
    public static boolean isFolderExist(String directoryPath) {
        if (directoryPath.isEmpty()) {
            return false;
        }

        File dire = new File(directoryPath);
        return (dire.exists() && dire.isDirectory());
    }

    /**
     * delete file or directory
     * <ul>
     * <li>if path is null or empty, return true</li>
     * <li>if path not exist, return true</li>
     * <li>if path exist, delete recursion. return true</li>
     * <ul>
     *
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) {
        if (path.isEmpty()) {
            return true;
        }

        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        if(file.listFiles()!=null&&file.listFiles().length>0) {
            for (File f : file.listFiles()) {
                if (f.isFile()) {
                    f.delete();
                } else if (f.isDirectory()) {
                    deleteFile(f.getAbsolutePath());
                }
            }
        }
        return file.delete();
    }

    public static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            throw new IllegalArgumentException("directory not exist.");
        }

        for (File file: directory.listFiles()) {
            if (file.isDirectory())
                cleanDirectory(file);
            else
                file.delete();
        }
    }

    /**
     * get file size
     * <ul>
     * <li>if path is null or empty, return -1</li>
     * <li>if path exist and it is a file, return file size, else return -1</li>
     * <ul>
     *
     * @param path
     * @return returns the length of this file in bytes. returns -1 if the file does not exist.
     */
    public static long getFileSize(String path) {
        if (path.isEmpty()) {
            return -1;
        }

        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

    //判断sd卡的剩余内存的大小
    public static boolean isSpaceEnough(String availableMemory) {//eg 10MB
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long size = availableBlocks * blockSize;
        String str = availableMemory.substring(0, availableMemory.length() - 2);
        if (availableMemory.endsWith("MB")) {
            return size > Long.parseLong(str) * 1024 * 1024;
        } else if (availableMemory.endsWith("KB")) {
            return size > Long.parseLong(str) * 1024;
        }

        return false;
    }

    /**
     * 创建一张视频的缩略图
     * 如果视频已损坏或者格式不支持可能返回null
     * ThumbnailUtils.createVideoThumbnail
     *  kind可以为MINI_KIND或MICRO_KIND
     *
     */
    public static Boolean createThumbnail(String videoPath, String savePath) {//例如 savePath为test.png
        Logg.v(LOG_TAG, "createThumbnail start  videoPath===" + videoPath);//  /storage/emulated/0/DCIM/Camera/VID_20160110_021210.mp4
//                bitMap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
        Bitmap bitMap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
//        bitMap = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MICRO_KIND);
        if (bitMap == null || getBitmapSize(bitMap) == 0 ) {
            Logg.e(LOG_TAG, "createThumbnail Input file create bitmap failure.");
            return false;
        } else {
            Logg.i(LOG_TAG, "createThumbnail Input file create bitmap succ");
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitMap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                double size = bytes.length / BYTE;
                int thumbWidth = THUMB_WIDTH;
                int thumbHeight = THUMB_HEIGHT;
                while (size >= THUMB_MAX_SIZE) {
                    thumbWidth -= THUMB_WIDTH_HEIGHT_DECREASE;
                    thumbHeight -= THUMB_WIDTH_HEIGHT_DECREASE;
                    bitMap = ThumbnailUtils.extractThumbnail(bitMap, thumbWidth, thumbHeight);
                    if (bitMap == null  || getBitmapSize(bitMap) == 0 ) {
                        Logg.e(LOG_TAG, "createThumbnail extractThumbnail() return null.");
                        return false;
                    }
                    byteArrayOutputStream.reset();
                    bitMap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    bytes = byteArrayOutputStream.toByteArray();
                    size = bytes.length / BYTE;
                }

                if(bytes ==null || bytes.length == 0 ){
                    return false;
                }
                File file = new File(savePath);
                if (file.exists()) {
                    file.delete();
                } else {
                    file.getParentFile().mkdir();
                }
                FileOutputStream outputStream = new FileOutputStream(file);
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                Logg.e(LOG_TAG, "createThumbnail Exception", e);
                return false;
            }
        }
        return true;
    }

    /**
     * 创建一个指定大小居中的缩略图
     * extractThumbnail(source, width, height, options):
     * 源文件(Bitmap类型)
     *  输出缩略图的宽度
     *  输出缩略图的高度
     * 如果options定义为OPTIONS_RECYCLE_INPUT,则回收
     *
     */
    public static Boolean createThumbnail1(Bitmap bitMap, String savePath) {//例如 savePath为test.png
        Logg.i(LOG_TAG, "createThumbnail Input file create bitmap succ");
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Bitmap thumbImageResize = ThumbnailUtils.extractThumbnail(bitMap, THUMB_WIDTH, THUMB_HEIGHT);
            if (thumbImageResize == null || thumbImageResize.getByteCount() == 0 ) {
                Logg.e(LOG_TAG, "createThumbnail extractThumbnail() return null.");
                return false;
            }

            thumbImageResize.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] bytes = byteArrayOutputStream.toByteArray();
            double size = bytes.length / BYTE;
            int thumbWidth = THUMB_WIDTH;
            int thumbHeight = THUMB_HEIGHT;
            while (size >= THUMB_MAX_SIZE) {
                thumbWidth -= THUMB_WIDTH_HEIGHT_DECREASE;
                thumbHeight -= THUMB_WIDTH_HEIGHT_DECREASE;
                thumbImageResize = ThumbnailUtils.extractThumbnail(bitMap,
                        thumbWidth, thumbHeight);
                if (thumbImageResize == null  || thumbImageResize.getByteCount() == 0 ) {
                    Logg.e(LOG_TAG, "createThumbnail extractThumbnail() return null.");
                    return false;
                }

                byteArrayOutputStream.reset();
                thumbImageResize.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                bytes = byteArrayOutputStream.toByteArray();
                size = bytes.length / BYTE;
            }

            File file = new File(savePath);
            if (file.exists()) {
                file.delete();
            } else {
                file.getParentFile().mkdir();
            }
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(byteArrayOutputStream.toByteArray());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Logg.e(LOG_TAG, "createThumbnail Exception", e);
            return false;
        }
        return true;
    }


    /**
     * 用来获取缩略图大小的方法
     * */
    public long getBitmapsize(Bitmap bitmap){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            return bitmap.getByteCount();
        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public static int getBitmapSize(Bitmap bitmap){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){    //API 19
            return bitmap.getAllocationByteCount();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){//API 12
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
    }

    /**
     * 将文件按时间降序排列
     */
    public static List<File> fileComparatorByDeDate(String dirPath) {
        List<File> fileList = new ArrayList<File>();
        File file = new File(dirPath);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            for (File mFile : files) {
                fileList.add(mFile);
            }
            Collections.sort(fileList, new FileComparatorByDeDate());
        }
        return fileList;
    }
    /**
     * 将文件按时间升序排列
     */
    public static List<File> FileComparatorByInDate(List<File> files) {
        List<File> fileList = new ArrayList<File>();
        if (files.size() >0) {
            for (File mfile : files) {
                fileList.add(mfile);
            }
            Collections.sort(fileList, new FileComparatorByInDate());
            return fileList;
        }
        return null;
    }

    /**
     * 将文件按时间降序排列
     */
    private static class FileComparatorByDeDate implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
//            if (file1.lastModified() < file2.lastModified()) {
//                return 1;// 最后修改的文件在前
//            } else if(file1.lastModified()> file2.lastModified()){
//                return -1;
//            }else return 0;
            if (getStringToDate(file1) < getStringToDate(file2)) {
                return 1;// 最后修改的文件在前
            } else if(getStringToDate(file1)> getStringToDate(file2)){
                return -1;
            }else return 0;
        }
    }
    /**
     * 将文件按时间升序排列
     */
    private static class FileComparatorByInDate implements Comparator<File> {

        @Override
        public int compare(File file1, File file2) {
//            if (file1.lastModified() > file2.lastModified()) {
//                return 1;// 最早修改的文件在前
//            } else if(file1.lastModified() < file2.lastModified()){
//                return -1;
//            }else return 0;


            long fileTimeStamp = getStringToDate(file1);
            long fileTimeStamp2 = getStringToDate(file2);
            if (fileTimeStamp > fileTimeStamp2) {
                return 1;// 最早修改的文件在前
            } else if(fileTimeStamp < fileTimeStamp2){
                return -1;
            }else return 0;
        }
    }
    /**
     * 将日期格式转化为时间戳格式
     */
    public static long getStringToDate(File file) {
        String fileName=file.getName();
        String time=fileName.substring(0,fileName.length()-4);
        // "2017-03-27_035219" yyyy-MM-dd_HHmmssSSS
        SimpleDateFormat sdf;
        if(fileName.contains(".PNG")){
            sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmssSSS");
        }else{
            sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        }
        Date date = new Date();
        try{
            date = sdf.parse(time);
        } catch(ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date.getTime();
    }
    /**
     * 获取SD path
     */
    public static String getSDPath() {
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
        if (sdCardExist) {
            File sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
            return sdDir.toString();
        }
        return null;
    }

    //判断文件是否存在
    public static boolean fileIsExists(String strFile) {
        try {
            File f = new File(strFile);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static int filesNumber(String dirPath) {
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        int number = files.length;
        return number;
    }

    public static LinkedList<File> getAllFiles(String[] strPath, String type) {
        LinkedList<File> files = new LinkedList<File>();
        int k = 0;
        String suf = "";
        for (int j = 0; j < strPath.length; j++) {
            LinkedList<File> list = new LinkedList<File>();
            if (null != strPath[j] && strPath[j].length() > 0) {
                File dir = new File(strPath[j]);
                File[] file = dir.listFiles();
                if (file != null && file.length > 0) {
                    for (File mFile : file) {
                        if (mFile.isDirectory())
                            list.add(mFile);
                        else {
                            k = mFile.getName().lastIndexOf(".");
                            suf = mFile.getName().substring(k + 1);
                            if (suf.equalsIgnoreCase(type)) {
                                files.add(mFile);
                            }
                        }
                    }
                    File tmp;
                    while (!list.isEmpty()) {
                        tmp = list.removeFirst();
                        if (tmp.isDirectory()) {
                            file = tmp.listFiles();
                            if (file == null)
                                continue;
                            for (File mFile : file) {
                                if (mFile.isDirectory())
                                    list.add(mFile);
                                else {
                                    k = mFile.getName().lastIndexOf(".");
                                    suf = mFile.getName().substring(k + 1);
                                    if (suf.equalsIgnoreCase(type)) {
                                        files.add(mFile);
                                    }
                                }
                            }
                        } else {
                            k = tmp.getName().lastIndexOf(".");
                            suf = tmp.getName().substring(k + 1);
                            if (suf.equalsIgnoreCase(type)) {
                                files.add(tmp);
                            }
                        }
                    }
                }
            }
        }
        return files;
    }
    public static void writeSysFile(String sys_path,String value){

        Process p = null;
        DataOutputStream os = null;
        try {
            p = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo 0 > "+sys_path + "\n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Logg.e("FileUtils", " can't write " + sys_path+e.getMessage());
        } finally {
            if(p != null){
                p.destroy();
            }
            if(os != null){
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); // 切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }
    public static String execRootCmd(String cmd) {
        String result = "";
        DataOutputStream dos = null;
        DataInputStream dis = null;

        try {
            Process p = Runtime.getRuntime().exec("su");
            dos = new DataOutputStream(p.getOutputStream());
            dis = new DataInputStream(p.getInputStream());

            dos.writeBytes(cmd + "\n");
            dos.flush();
            dos.writeBytes("exit\n");
            dos.flush();
            String line = null;
            while ((line = dis.readLine()) != null) {
                result += line+"\r\n";
            }
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dos != null) {
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (dis != null) {
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }
}

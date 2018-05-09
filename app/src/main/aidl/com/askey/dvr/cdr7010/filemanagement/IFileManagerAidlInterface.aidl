// IFileManagerAidlInterface.aidl
package com.askey.dvr.cdr7010.filemanagement;

import com.askey.dvr.cdr7010.filemanagement.ItemData;
import com.askey.dvr.cdr7010.filemanagement.SdcardInfo;
// Declare any non-default types here with import statements

interface IFileManagerAidlInterface {

      /**
      *  Purpose: 1.Choice folderType to openfile
                  2.Get file_num from "Table.config"
                  3.If "Free" folder have extension for folderType, use it to open file.
         Input:  mount path, open filename,
                 folderType: Event, Manual, Normal, Parking, Picture, System
         Output: FILE Pointer
        ** If file number > folderType file_num, return NULL; ***/
     String openSdcard(String filename, String folderType);

     /**
     *  Purpose: Close opened file
       Input: Opened FILE Pointer
       Output: bool, true = 1, false = 0;
     * */
     boolean closeSdcard();

    /**
    * Type : NORMAL EVENT PARKING PICTURE SYSTEM
    * */
   List <String> getAllFilesByType(String type);

    /**
    * Type : NORMAL EVENT PARKING PICTURE SYSTEM
    * */
   List <ItemData> getAllFileByType(String type);

    /**
    * path : file Absolute Path
    * */
    boolean deleteFile(String path);

    /**
    * pathList : files Absolute Path
    *     //in表明是由客户端到服务端
          //out表明是由服务端到客户端
          //inout表明可以双向通信
    * */
    boolean deleteFileByGroup(inout List<String> pathArray);

    /**
    * pathList : files Absolute Path
    * */
    boolean deleteFileByFolder(String type);

    /**
    * 为setting界面提供sdcardInfo信息
    * */
     List <SdcardInfo> getSdcardInfo();

     /**
     * 检测sdcard中是否有update.zip文件
     * */
     boolean checkNewSystemVersion();





}

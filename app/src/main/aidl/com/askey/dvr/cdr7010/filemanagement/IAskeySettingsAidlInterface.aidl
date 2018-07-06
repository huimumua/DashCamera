// IAskeySettingsAidlInterface.aidl
package com.askey.dvr.cdr7010.filemanagement;

// Declare any non-default types here with import statements

interface IAskeySettingsAidlInterface {

    void init (String userId);

    void sync();

    void write (String key,String value);


}

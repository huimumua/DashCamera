package com.jvckenwood.communication;

import com.jvckenwood.communication.IMainAppCallback;

/**
 * MainAPP向けAIDL
 * <p>
 * MainAPP からの通知を受けるIF
 * </p>
 *
 * @author FSI
 * @version 0.1 WIP
 */
interface IMainApp {
    /**
     * コールバック登録
     * @param callback 登録するコールバック
     * @since 0.1 WIP
     */
    oneway void registerCallback(IMainAppCallback callback);
    
    /**
     * コールバック解除
     * @param callback 解除するコールバック
     * @since 0.1 WIP
     */
    oneway void unregisterCallback(IMainAppCallback callback);

    /**
     * 設定変更要求
     * <p>
     * ドラレコの設定をサーバーに送信する
     * </p>
     * @param settings 設定のJSON
     * @since 0.1 WIP
     */
    void settingsUpdateRequest(String settings);


    /**
     * 手動アップロードキャンセル
     * <p>
     * 手動アップロードのキャンセル操作を通知する
     * </p>
     * @param cancel 0:中断 1:再開 2:キャンセル
     * @since 0.1 WIP
     */
    void manualUploadCancel(int cancel);


    /**
     * ログファイル送信要求
     * <p>
     * WaaSサーバーにドラレコのログを送信する
     * </p>
     * @param zipPath MainAPPで収集したログをアーカイブしたファイルパス
     * @since 0.1 WIP
     */
    void logUpload(String zipPath);
}


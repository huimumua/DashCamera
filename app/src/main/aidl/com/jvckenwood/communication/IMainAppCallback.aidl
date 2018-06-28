package com.jvckenwood.communication;


/**
 * MainAppコールバックAIDL
 * <p>
 * MainAppへのコールバックIF
 * </p>
 *
 * @author JVCKENWOOD
 * @version 0.1 WIP
 */
interface IMainAppCallback {

    /**
     * 証券有効確認の結果
     * <p>
     * WaaSサーバーとの証券有効確認の結果をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param response サーバーからのレスポンスのJSON
     * @since 0.1 WIP
     */
    void reportInsuranceTerm(int oos, String response);


    /**
     * ユーザー一覧取得の結果
     * <p>
     * WaaSサーバーから取得したデフォルトユーザーとユーザー一覧をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param defaultUser デフォルトユーザー変更フラグ 0:変更なし 1:変更有
     * @param selectUser 選択ユーザー変更フラグ 0:変更なし 1:変更有
     * @param userList ユーザー一覧変更フラグ 0:変更なし 1:変更有
     * @param response サーバーからのレスポンスのJSON
     * @since 0.1 WIP
     */
    void reportUserList(int oos, String response);


    /**
     * ドラレコ設定情報取得(管理者)の結果
     * <p>
     * WaaSサーバーから取得したドラレコ設定情報(管理者)をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param response サーバーからのレスポンスのJSON
     * @since 0.1 WIP
     */
    void reportSystemSettings(int oos, String response);


    /**
     * ドラレコ設定情報取得の結果
     * <p>
     * WaaSサーバーから取得したドラレコ設定情報をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param response サーバーからのレスポンスのJSON
     * @since 0.1 WIP
     */
    void reportUserSettings(int oos, String response);


    /**
     * ドラレコ設定情報登録の結果
     * <p>
     * WaaSサーバーから取得したドラレコ設定情報登録の結果をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param response サーバーからのレスポンスのJSON
     * @since 0.1 WIP
     */
    void reportSettingsUpdate(int oos, String response);


    /**
     * 運転レポート取得の結果
     * <p>
     * WaaSサーバーから取得した運転レポート取得の結果をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param response サーバーからのレスポンスのJSON
     * @since 0.1 WIP
     */
    void reportDrivingReport(int oos, String response);


    /**
     * 月間運転レポート取得の結果
     * <p>
     * WaaSサーバーから取得した月間運転レポート取得の結果をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param response サーバーからのレスポンスのJSON
     * @since 0.1 WIP
     */
    void reportManthlyDrivingReport(int oos, String response);


    /**
     * お知らせ取得の結果
     * <p>
     * WaaSサーバーから取得したお知らせの結果をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param response サーバーからのレスポンスのJSON
     * @since 0.1 WIP
     */
    void reportServerNotifocation(int oos, String response);


    /**
     * 運転前アドバイス取得の結果
     * <p>
     * WaaSサーバーから取得した運転前アドバイスの結果をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param response サーバーからのレスポンスのJSON
     * @since 0.1 WIP
     */
    void reportDrivingAdvice(int oos, String response);


    /**
     * イベントデータ登録のアップロード進捗報告
     * <p>
     * WaaSサーバーへのイベントデータアップロードの進捗を各zipファイルの送信完了ごとにMainAPPに通知する。<br>
     * サーバーからのレスポンスは下記の値になる<br>
     * 0:正常 -1:想定外の例外 -100:トリップIDが未入力、シーケンス番号が未入力<br>
     * -300:デコード失敗<br>
     * -400:ファイルがnull、存在しない、ファイルではない、0バイト、
     *      デコード用ディレクトリ作成失敗、入出力エラー、ファイルが見つからない<br>
     * -700:ユーザ情報が未登録<br>
     * </p>
     * @param path イベントデータ登録を行ったファイルのパス(動画1ファイル、静止画最大3ファイル))
     * @param result 送信結果
     * @since 0.1 WIP
     */
    void reportTxEventProgress(inout List<String> path, int result);


    /**
     * イベントデータ登録のアップロード進捗報告
     * <p>
     * イベントデータ登録の中間応答をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param progress1 ファイル1の送信量
     * @param total1 ファイル1のデータサイズ
     * @param progress2 ファイル2の送信量
     * @param total2 ファイル2のデータサイズ
     * @since 0.1 WIP
     */
    void reportTxManualProgress(int progress1, int total1, int progress2, int total2);


    /**
     * ログアップロードの結果
     * <p>
     * WaaSサーバーから取得した運転前アドバイスの結果をMainAPPに通知する<br>
     * 圏外の場合は取得できなかったことを通知する
     * </p>
     * @param oos 圏外フラグ 0:成功 1:圏外 2:処理中
     * @param result サーバーからの結果通知のJSON
     * @since 0.1 WIP
     */
    void logUploadResult(int oos, String result);
}


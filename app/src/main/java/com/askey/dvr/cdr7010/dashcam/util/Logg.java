package com.askey.dvr.cdr7010.dashcam.util;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logg {

    public static final boolean DEBUG = true;

    public static void cstdr(String TAG, String msg) {
        if (DEBUG) {
            android.util.Log.i("cstdr", TAG + "~~~" + msg);
        }
    }

    /**
     * 执行 命令
     * */
    public static void execShell(String cmd){
        try{
            //权限设置
            Process p = Runtime.getRuntime().exec("su");
            //获取输出流
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
            //将命令写入
            dataOutputStream.writeBytes(cmd);
            //提交命令
            dataOutputStream.flush();
            //关闭流操作
            dataOutputStream.close();
            outputStream.close();
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
    }

    public static String exec(String command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行 命令
     * */
    public static void execCommand(String command) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        Process proc = runtime.exec(command);
        try {
            if (proc.waitFor() != 0) {//这句有问题
                System.err.println("exit value = " + proc.exitValue());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream(), "UTF-8"));
            StringBuffer stringBuffer = new StringBuffer();
            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line+" ");
            }
            System.out.println(stringBuffer.toString());

        } catch (InterruptedException e) {
            System.err.println(e);
        }finally{
            try {
                proc.destroy();
            } catch (Exception e2) {
            }
        }
    }


    /**
     * @return void </br>
     * @throws </br>
     * @about version 1.0
     * @Description Send an INFO log message
     */
    public static void i(String tag, String msg) {

        if (DEBUG) {
            android.util.Log.i(tag, msg);
        }
    }

    /**
     * @return void </br>
     * @throws </br>
     * @about version 1.0
     * @Description Send a DEBUG log message
     */
    public static void d(String tag, String msg) {

        if (DEBUG) {
            android.util.Log.d(tag, msg);
        }
    }


    /**
     * @return void </br>
     * @throws </br>
     * @about version 1.0
     * @Description Send a WARN log message.
     */
    public static void w(String tag, String msg) {

        if (DEBUG) {
            android.util.Log.w(tag, msg);
        }
    }

    /**
     * @return void </br>
     * @throws </br>
     * @about version 1.0
     * @Description Send an ERROR log message.
     */
    public static void e(String tag, String msg) {

        if (DEBUG) {
            android.util.Log.e(tag, msg);
        }
    }

    /**
     * @return void </br>
     * @throws </br>
     * @about version 1.0
     * @Description Send an ERROR log message.
     */
    public static void e(String tag, String msg, Throwable tr) {

        if (DEBUG) {
            android.util.Log.e(tag, msg, tr);
        }
    }

    /**
     * @return void </br>
     * @throws </br>
     * @about version 1.0
     * @Description Send a VERBOSE log message.
     */
    public static void v(String tag, String msg) {

        if (DEBUG) {
            android.util.Log.v(tag, msg);
        }
    }

    public static void showToast(final Context mContext, final String content) {
        ((Activity) mContext).runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast toast = Toast.makeText(mContext, content,
                        Toast.LENGTH_SHORT);
                toast.show();

            }
        });

    }

    public static void setTextViewContent(final Activity activity,
                                          final TextView textView, final String content) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (textView != null) {
                    textView.setText(content);
                }
            }
        });
    }

    public static void resizePerformanceResultToSdCard(String result, String inputUri, String outputUri) {
        String filePath = Environment.getExternalStorageDirectory() + "/NasGwServer/ResizePerformance.txt";
        File out = new File(filePath);
        out.getParentFile().mkdir();

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(out, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);
            String str = "----------" + df.format(date) + " (" + inputUri + ") " + "-----START------\n";
            str += df.format(date) + " (" + outputUri + ") :" + result + "\n";
            str += "----------" + df.format(date) + " (" + inputUri + ") " + "------END-------\n";
            bw.write(str);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
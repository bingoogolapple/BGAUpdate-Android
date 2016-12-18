package cn.bingoogolapple.update;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static cn.bingoogolapple.update.AppUtil.sApp;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/12/16 上午11:59
 * 描述:
 */
class StorageUtil {
    private static final String DIR_NAME_APK = "upgrade_apk";

    private StorageUtil() {
    }

    /**
     * 获取 apk 文件夹
     *
     * @return
     */
    static File getApkFileDir() {
        return sApp.getExternalFilesDir(DIR_NAME_APK);
    }

    /**
     * 获取 apk 文件
     *
     * @param version
     * @return
     */
    static File getApkFile(String version) {
        String appName;
        try {
            appName = sApp.getPackageManager().getPackageInfo(sApp.getPackageName(), 0).applicationInfo.loadLabel(sApp.getPackageManager()).toString();
        } catch (Exception e) {
            // 利用系统api getPackageName()得到的包名，这个异常根本不可能发生
            appName = "";
        }
        return new File(getApkFileDir(), appName + "_v" + version + ".apk");
    }

    /**
     * 保存 apk 文件
     *
     * @param is
     * @param version
     * @return
     */
    static File saveApk(InputStream is, String version) {
        File file = getApkFile(version);

        if (writeFile(file, is)) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * 根据输入流，保存文件
     *
     * @param file
     * @param is
     * @return
     */
    static boolean writeFile(File file, InputStream is) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            byte data[] = new byte[1024];
            int length = -1;
            while ((length = is.read(data)) != -1) {
                os.write(data, 0, length);
            }
            os.flush();
            return true;
        } catch (Exception e) {
            if (file != null && file.exists()) {
                file.deleteOnExit();
            }
            e.printStackTrace();
        } finally {
            closeStream(os);
            closeStream(is);
        }
        return false;
    }

    /**
     * 删除文件或文件夹
     *
     * @param file
     */
    static void deleteFile(File file) {
        try {
            if (file == null || !file.exists()) {
                return;
            }

            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        if (f.exists()) {
                            if (f.isDirectory()) {
                                deleteFile(f);
                            } else {
                                f.delete();
                            }
                        }
                    }
                }
            } else {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 关闭流
     *
     * @param closeable
     */
    static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

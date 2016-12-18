package cn.bingoogolapple.update;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/12/16 上午10:48
 * 描述:应用升级工具类
 */
public class BGAUpgradeUtil {
    private static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
    private static final String DIR_NAME_APK = "apk";

    public static final Application sApp;

    static {
        Application app = null;
        try {
            app = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
            if (app == null)
                throw new IllegalStateException("Static initialization of Applications must be on main thread.");
        } catch (final Exception e) {
            Log.e(BGAUpgradeUtil.class.getSimpleName(), "Failed to get current application from AppGlobals." + e.getMessage());
            try {
                app = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            } catch (final Exception ex) {
                Log.e(BGAUpgradeUtil.class.getSimpleName(), "Failed to get current application from ActivityThread." + e.getMessage());
            }
        } finally {
            sApp = app;
        }
    }

    private BGAUpgradeUtil() {
    }

    /**
     * 监听下载进度
     *
     * @return
     */
    public static Observable<BGADownloadProgressEvent> getDownloadProgressEventObservable() {
        return RxUtil.getDownloadEventObservable();
    }

    /**
     * apk 文件是否已经下载过，如果已经下载过就直接安装
     *
     * @param version 新 apk 文件版本号
     * @return
     */
    public static boolean isApkFileDownloaded(String version) {
        File apkFile = getApkFile(version);
        if (apkFile.exists()) {
            installApk(apkFile);
            return true;
        }
        return false;
    }

    /**
     * 下载新版 apk 文件
     *
     * @param url     apk 文件路径
     * @param version 新 apk 文件版本号
     * @return
     */
    public static Observable<File> downloadApkFile(final String url, final String version) {
        return Observable.defer(new Func0<Observable<InputStream>>() {
            @Override
            public Observable<InputStream> call() {
                try {
                    return Observable.just(Engine.getInstance().getDownloadApi().downloadFile(url).execute().body().byteStream());
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        }).map(new Func1<InputStream, File>() {
            @Override
            public File call(InputStream inputStream) {
                return saveApk(inputStream, version);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 安装 apk 文件
     *
     * @param apkFile
     */
    public static void installApk(File apkFile) {
       /* Intent installApkIntent = new Intent();
        installApkIntent.setAction(Intent.ACTION_VIEW);
        installApkIntent.addCategory(Intent.CATEGORY_DEFAULT);
        installApkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installApkIntent.setDataAndType(Uri.fromFile(apkFile), MIME_TYPE_APK);

        if (sApp.getPackageManager().queryIntentActivities(installApkIntent, 0).size() > 0) {
            sApp.startActivity(installApkIntent);
        }*/
       Toast.makeText(sApp,apkFile.getPath(),Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(sApp, "cn.bingoogolapple.update.fileprovider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (sApp.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            sApp.startActivity(intent);
        }
    }

    /**
     * 删除之前升级时下载的老的 apk 文件
     */
    public static void deleteOldApk() {
        StorageUtil.deleteFile(sApp.getExternalFilesDir(DIR_NAME_APK));
    }

    /**
     * 获取 apk 文件
     *
     * @param version 新 apk 文件版本号
     * @return
     */
    private static File getApkFile(String version) {
        return new File(sApp.getExternalFilesDir(DIR_NAME_APK), getAppName() + "_v" + version + ".apk");
    }

    /**
     * 保存 apk 文件
     *
     * @param is
     * @param version
     * @return
     */
    private static File saveApk(InputStream is, String version) {
        File file = getApkFile(version);

        if (StorageUtil.writeFile(file, is)) {
            return file;
        } else {
            return null;
        }
    }

    /**
     * 获取应用名称
     *
     * @return
     */
    private static String getAppName() {
        try {
            return sApp.getPackageManager().getPackageInfo(sApp.getPackageName(), 0).applicationInfo.loadLabel(sApp.getPackageManager()).toString();
        } catch (Exception e) {
            // 利用系统api getPackageName()得到的包名，这个异常根本不可能发生
            return "";
        }
    }
}

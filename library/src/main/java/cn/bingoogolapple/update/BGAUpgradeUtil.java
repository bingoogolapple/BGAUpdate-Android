package cn.bingoogolapple.update;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.InputStream;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static cn.bingoogolapple.update.AppUtil.sApp;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/12/16 上午10:48
 * 描述:应用升级工具类
 */
public class BGAUpgradeUtil {
    private static final String MIME_TYPE_APK = "application/vnd.android.package-archive";


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
        File apkFile = StorageUtil.getApkFile(version);
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
                return StorageUtil.saveApk(inputStream, version);
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
        Intent installApkIntent = new Intent();
        installApkIntent.setAction(Intent.ACTION_VIEW);
        installApkIntent.addCategory(Intent.CATEGORY_DEFAULT);
        installApkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            installApkIntent.setDataAndType(FileProvider.getUriForFile(sApp, getFileProviderAuthority(), apkFile), MIME_TYPE_APK);
            installApkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            installApkIntent.setDataAndType(Uri.fromFile(apkFile), MIME_TYPE_APK);
        }

        if (sApp.getPackageManager().queryIntentActivities(installApkIntent, 0).size() > 0) {
            sApp.startActivity(installApkIntent);
        }
    }

    /**
     * 获取FileProvider的auth
     *
     * @return
     */
    private static String getFileProviderAuthority() {
        try {
            for (ProviderInfo provider : sApp.getPackageManager().getPackageInfo(sApp.getPackageName(), PackageManager.GET_PROVIDERS).providers) {
                if (FileProvider.class.getName().equals(provider.name) && provider.authority.endsWith(".bga_update.file_provider")) {
                    return provider.authority;
                }
            }
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        return null;
    }

    /**
     * 删除之前升级时下载的老的 apk 文件
     */
    public static void deleteOldApk() {
        File apkDir = StorageUtil.getApkFileDir();
        if (apkDir == null || apkDir.listFiles() == null || apkDir.listFiles().length == 0) {
            return;
        }

        // 删除文件
        StorageUtil.deleteFile(apkDir);
    }
}

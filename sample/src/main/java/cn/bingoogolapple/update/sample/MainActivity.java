package cn.bingoogolapple.update.sample;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.io.File;
import java.util.List;

import cn.bingoogolapple.update.BGADownloadProgressEvent;
import cn.bingoogolapple.update.BGAUpgradeUtil;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import rx.Subscriber;
import rx.functions.Action1;

public class MainActivity extends RxAppCompatActivity implements EasyPermissions.PermissionCallbacks {
    /**
     * 下载文件权限请求码
     */
    private static final int RC_PERMISSION_DOWNLOAD = 1;
    /**
     * 删除文件权限请求码
     */
    private static final int RC_PERMISSION_DELETE = 2;

    private DownloadingDialog mDownloadingDialog;

    private String mNewVersion = "1.0.1";
    private String mApkUrl = "http://7xk9dj.com1.z0.glb.clouddn.com/BGAUpdateSample_v1.0.1_debug.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 监听下载进度
        BGAUpgradeUtil.getDownloadProgressEventObservable()
                .compose(this.<BGADownloadProgressEvent>bindToLifecycle())
                .subscribe(new Action1<BGADownloadProgressEvent>() {
                    @Override
                    public void call(BGADownloadProgressEvent downloadProgressEvent) {
                        if (mDownloadingDialog != null && mDownloadingDialog.isShowing() && downloadProgressEvent.isNotDownloadFinished()) {
                            mDownloadingDialog.setProgress(downloadProgressEvent.getProgress(), downloadProgressEvent.getTotal());
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        finish();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn_main_delete) {
            deleteApkFile();
        } else if (v.getId() == R.id.btn_main_download) {
            downloadApkFile();
        }
    }

    /**
     * 删除之前升级时下载的老的 apk 文件
     */
    @AfterPermissionGranted(RC_PERMISSION_DELETE)
    public void deleteApkFile() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 删除之前升级时下载的老的 apk 文件
            BGAUpgradeUtil.deleteOldApk();
        } else {
            EasyPermissions.requestPermissions(this, "使用 BGAUpdateDemo 需要授权读写外部存储权限!", RC_PERMISSION_DELETE, perms);
        }
    }

    /**
     * 下载新版 apk 文件
     */
    @AfterPermissionGranted(RC_PERMISSION_DOWNLOAD)
    public void downloadApkFile() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            // 如果新版 apk 文件已经下载过了，直接 return，此时不需要开发者调用安装 apk 文件的方法，在 isApkFileDownloaded 里已经调用了安装」
            if (BGAUpgradeUtil.isApkFileDownloaded(mNewVersion)) {
                return;
            }

            // 下载新版 apk 文件
            BGAUpgradeUtil.downloadApkFile(mApkUrl, mNewVersion)
                    .subscribe(new Subscriber<File>() {
                        @Override
                        public void onStart() {
                            showDownloadingDialog();
                        }

                        @Override
                        public void onCompleted() {
                            dismissDownloadingDialog();
                        }

                        @Override
                        public void onError(Throwable e) {
                            dismissDownloadingDialog();
                        }

                        @Override
                        public void onNext(File apkFile) {
                            if (apkFile != null) {
                                BGAUpgradeUtil.installApk(apkFile);
                            }
                        }
                    });
        } else {
            EasyPermissions.requestPermissions(this, "使用 BGAUpdateDemo 需要授权读写外部存储权限!", RC_PERMISSION_DOWNLOAD, perms);
        }
    }

    /**
     * 显示下载对话框
     */
    private void showDownloadingDialog() {
        if (mDownloadingDialog == null) {
            mDownloadingDialog = new DownloadingDialog(this);
        }
        mDownloadingDialog.show();
    }

    /**
     * 隐藏下载对话框
     */
    private void dismissDownloadingDialog() {
        if (mDownloadingDialog != null) {
            mDownloadingDialog.dismiss();
        }
    }
}

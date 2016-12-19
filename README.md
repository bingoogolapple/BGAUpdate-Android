:running:BGAUpdate-Android:running:
============

## 功能介绍

- [x] 适配 Android 7.+ 系统
- [x] 检测新版 apk 文件是否已经下载过
- [x] RxJava + Retrofit 下载新版 apk 文件
- [x] RxBus 监听下载进度
- [x] 安装 apk 文件
- [x] 删除之前升级时下载的老的 apk 文件

## 效果图与示例 apk

![BGAUpdateDemo](https://cloud.githubusercontent.com/assets/8949716/21256759/256dce3e-c3af-11e6-98b3-373afcfa4cce.gif)

[点击下载 BGAUpdateDemo.apk](http://fir.im/BGAUpdateDemo) 或扫描下面的二维码安装

![BGABannerDemo apk文件二维](https://cloud.githubusercontent.com/assets/8949716/21256883/db23d4b2-c3af-11e6-9793-7ac5c6624e25.png)

## 使用

### 1.添加 Gradle 依赖
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.bingoogolapple/bga-update/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.bingoogolapple/bga-update) bga-update 后面的「latestVersion」指的是左边这个 maven-central 徽章后面的「数字」，请自行替换。

```groovy
dependencies {
    compile 'cn.bingoogolapple:bga-update:latestVersion@aar'

    // 换成己工程里依赖的 rxjava 和 retrofit 版本
    compile 'io.reactivex:rxjava:1.2.2'
    compile 'io.reactivex:rxandroid:1.2.1'
    compile 'com.squareup.retrofit2:retrofit:2.1.0'
    compile 'com.squareup.retrofit2:converter-gson:2.1.0'
    compile 'com.squareup.retrofit2:adapter-rxjava:2.1.0'
}
```

### 2.在 Activity 的 onCreate 方法中监听下载进度。demo 里引用了 rxlifecycle 这个库来防止 Activity 内存泄漏

```java
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
```

### 3.下载新版 apk 文件。注意先申请 WRITE_EXTERNAL_STORAGE 权限

```java
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
```

### 4.下次进入应用时删除之前升级时下载的老的 apk 文件

```
BGAUpgradeUtil.deleteOldApk();
```

## 代码是最好的老师，更多详细用法请查看 [demo](https://github.com/bingoogolapple/BGAUpdate-Android/tree/master/demo):feet:

## 贡献者

* [bingoogolapple](https://github.com/bingoogolapple)
* [chenfei0928](https://github.com/chenfei0928)
* [HYVincent](https://github.com/HYVincent)

## 关于我

| 新浪微博 | 个人主页 | 邮箱 | BGA系列开源库QQ群
| ------------ | ------------- | ------------ | ------------ |
| <a href="http://weibo.com/bingoogol" target="_blank">bingoogolapple</a> | <a  href="http://www.bingoogolapple.cn" target="_blank">bingoogolapple.cn</a>  | <a href="mailto:bingoogolapple@gmail.com" target="_blank">bingoogolapple@gmail.com</a> | ![BGA_CODE_CLUB](http://7xk9dj.com1.z0.glb.clouddn.com/BGA_CODE_CLUB.png?imageView2/2/w/200) |

## 打赏支持

如果您觉得 BGA 系列开源库帮你节省了大量的开发时间，请扫描下方的二维码随意打赏，要是能打赏个 10.24 :monkey_face:就太:thumbsup:了。您的支持将鼓励我继续创作:octocat:

如果您目前正打算购买通往墙外的梯子，可以使用我的邀请码「YFQ9Q3B」购买 [Lantern](https://github.com/getlantern/forum)，双方都赠送三个月的专业版使用时间:beers:

<p align="center">
  <img src="http://7xk9dj.com1.z0.glb.clouddn.com/bga_pay.png" width="450">
</p>

## License

    Copyright 2015 bingoogolapple

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

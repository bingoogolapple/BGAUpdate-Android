package cn.bingoogolapple.update.sample;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;

import cn.bingoogolapple.progressbar.BGAProgressBar;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/12/16 下午3:34
 * 描述:下载进度对话框，进度条使用的是 https://github.com/bingoogolapple/BGAProgressBar-Android
 */
public class DownloadingDialog extends AppCompatDialog {
    private BGAProgressBar mProgressBar;

    public DownloadingDialog(Context context) {
        super(context, R.style.AppDialogTheme);
        setContentView(R.layout.dialog_downloading);
        mProgressBar = (BGAProgressBar) findViewById(R.id.pb_downloading_content);
        setCancelable(false);
    }

    public void setProgress(long progress, long maxProgress) {
        mProgressBar.setMax((int) maxProgress);
        mProgressBar.setProgress((int) progress);
    }

    @Override
    public void show() {
        super.show();
        mProgressBar.setMax(100);
        mProgressBar.setProgress(0);
    }
}

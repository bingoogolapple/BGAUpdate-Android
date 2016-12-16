package cn.bingoogolapple.update;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/12/16 上午11:41
 * 描述:下载进度事件对象
 */
public class BGADownloadProgressEvent {
    /**
     * 文件总大小
     */
    private long mTotal;
    /**
     * 当前下载进度
     */
    private long mProgress;

    public BGADownloadProgressEvent(long total, long progress) {
        mTotal = total;
        mProgress = progress;
    }

    /**
     * 获取文件总大小
     *
     * @return
     */
    public long getTotal() {
        return mTotal;
    }

    /**
     *
     * @return
     */
    public long getProgress() {
        return mProgress;
    }

    /**
     * 是否还没有下载完成
     *
     * @return
     */
    public boolean isNotDownloadFinished() {
        return mTotal != mProgress;
    }
}

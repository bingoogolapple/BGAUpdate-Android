package cn.bingoogolapple.update;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/12/16 上午10:58
 * 描述:
 */
class DownloadResponseBody extends ResponseBody {
    private final ResponseBody mResponseBody;
    private BufferedSource mBufferedSource;

    public DownloadResponseBody(ResponseBody responseBody) {
        mResponseBody = responseBody;
    }

    @Override
    public MediaType contentType() {
        return mResponseBody.contentType();
    }

    @Override
    public long contentLength() {
        return mResponseBody.contentLength();
    }

    @Override
    public BufferedSource source() {
        if (mBufferedSource == null) {
            mBufferedSource = Okio.buffer(source(mResponseBody.source()));
        }
        return mBufferedSource;
    }

    private Source source(Source source) {
        return new ForwardingSource(source) {
            private long mProgress = 0;
            private long mLastSendTime = 0;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead = super.read(sink, byteCount);
                mProgress += bytesRead == -1 ? 0 : bytesRead;

                if (System.currentTimeMillis() - mLastSendTime > 500) {
                    RxUtil.send(new BGADownloadProgressEvent(contentLength(), mProgress));
                    mLastSendTime = System.currentTimeMillis();
                } else if (mProgress == contentLength()) {
                    Observable.just(mProgress).delaySubscription(500, TimeUnit.MILLISECONDS, Schedulers.io()).subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            RxUtil.send(new BGADownloadProgressEvent(contentLength(), mProgress));
                        }
                    });
                }

                return bytesRead;
            }
        };
    }

}
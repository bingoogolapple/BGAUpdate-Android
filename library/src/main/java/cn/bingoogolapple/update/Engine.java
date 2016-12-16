package cn.bingoogolapple.update;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com
 * 创建时间:16/12/16 上午10:53
 * 描述:
 */
class Engine {
    private static Engine sInstance;

    private DownloadApi mDownloadApi;

    static final Engine getInstance() {
        if (sInstance == null) {
            synchronized (Engine.class) {
                if (sInstance == null) {
                    sInstance = new Engine();
                }
            }
        }
        return sInstance;
    }

    private Engine() {
        // 这里的 url 我随便填的，反正 DownloadApi 的 downloadFile 方法传的是绝对路径进来
        mDownloadApi = new Retrofit.Builder()
                .baseUrl("https://github.com/bingoogolapple/")
                .client(getDownloadOkHttpClient())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()
                .create(DownloadApi.class);
    }

    private static OkHttpClient getDownloadOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder().body(new DownloadResponseBody(originalResponse.body())).build();
                    }
                });

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[0];
                }
            }};
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory).hostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    DownloadApi getDownloadApi() {
        return mDownloadApi;
    }

    public interface DownloadApi {
        @Streaming
        @GET
        Call<ResponseBody> downloadFile(@Url String url);
    }
}

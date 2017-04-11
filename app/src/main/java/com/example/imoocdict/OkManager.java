package com.example.imoocdict;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created  on 17/4/11.
 * gradle添加依赖  compile 'com.squareup.okhttp3:okhttp:3.6.0'
 */

public class OkManager {
    private OkHttpClient client;

    //private final String TAG = OkManager.class.getSimpleName();//获得类名
    private final String TAG = "GsonUtils";


    private OkManager() {
        client = new OkHttpClient();
        //设置连接超时时间为10s
        client.newBuilder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS);
    }

    //private volatile static OkManager okManager;
    //采用单例模式获取对象
   /* public static OkManager getInstance() {
        OkManager instance = null;
        if (null == okManager) {
            //同步锁
            synchronized (OkManager.class) {
                if (null == instance) {
                    instance = new OkManager();
                    okManager = instance;
                }
            }
        }
        return instance;
    }*/


    private static OkManager okManager;
    //创建 单例模式（OkHttp官方建议如此操作）
    public static OkManager getInstance() {
        if (null == okManager) {
            synchronized (OkManager.class) {
                okManager = new OkManager();
            }
        }
        return okManager;
    }

    /**
     * 同步请求，在android开发中不常用，因为会阻塞UI线程
     *
     * @param url
     * @return
     */
    public String syncGetByURL(String url) {
        Request request = new Request.Builder().url(url).build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
            if (null != response && response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
        return null;
    }


    /**
     * 异步get请求方式获取bitmap,通过接口回调获取
     *
     * @param url
     */
    public void getBitmap(String url) {
        final Request request = new Request.Builder().get().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure_IOException" + e.toString());
                if (null != okManagerBitmapListner) {
                    okManagerBitmapListner.onResponseFailure(call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "onResponse=");
                if (response != null && response.isSuccessful()) {
                    byte[] bytes = response.body().bytes();
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    //方法二,可以获取字节流,然后转换成图片
                    //InputStream inputStream = response.body().byteStream();
                    //Bitmap bitmap1 = BitmapFactory.decodeStream(inputStream);
                    if (null != okManagerBitmapListner) {
                        okManagerBitmapListner.onResponseBitmap(bitmap);
                    }
                }
            }
        });
    }

    /**
     * 异步get请求获取String类型的json数据
     *
     * @param url
     * @return
     */
    public void getString(String url) {

        final Request request = new Request.Builder().url(url).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "IOException" + e.toString());
                if (null != okManagerStringListner) {
                    okManagerStringListner.onResponseFailure(call, e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (null != response && response.isSuccessful()) {
                    ResponseBody body = response.body();
                    String string = body.string();
                    Log.d(TAG, "string=" + string);
                    if (null != okManagerStringListner) {
                        okManagerStringListner.onResponseString(string);
                    }
                }
            }
        });

    }

    /**
     * post表单请求提交,获取json字符串
     *
     * @param url
     * @param params
     */
    public void postComplexForm(String url, Map<String, String> params) {

        FormBody.Builder builder = new FormBody.Builder();//表单对象，包含以input开始的对象，以html表单为主
        //把map集合中的参数添加到FormBody表单中.
        if (null != params && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
            RequestBody requestBody = builder.build();//创建请求体
            Request request = new Request.Builder().url(url).post(requestBody).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "IOException" + e.toString());
                    if (null != okManagerStringListner) {
                        okManagerStringListner.onResponseFailure(call, e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (null != response && response.isSuccessful()) {
                        String string = response.body().string();
                        Log.d(TAG, "string=" + string);
                        if (null != okManagerStringListner) {
                            okManagerStringListner.onResponseString(string);
                        }
                    }
                }
            });

        }
    }

    /**
     * Bitmap的接口
     */
    interface OkManagerBitmapListner {
        void onResponseBitmap(Bitmap bitmap);

        void onResponseFailure(Call call, IOException e);
    }

    public OkManagerBitmapListner getOkManagerBitmapListner() {
        return okManagerBitmapListner;
    }

    public void setOkManagerBitmapListner(OkManagerBitmapListner okManagerListner) {
        this.okManagerBitmapListner = okManagerListner;
    }

    private OkManagerBitmapListner okManagerBitmapListner;


    /**
     * String的接口
     */
    interface OkManagerStringListner {
        void onResponseString(String string);

        void onResponseFailure(Call call, IOException e);
    }

    private OkManagerStringListner okManagerStringListner;

    public OkManagerStringListner getOkManagerStringListner() {
        return okManagerStringListner;
    }

    public void setOkManagerStringListner(OkManagerStringListner okManagerStringListner) {
        this.okManagerStringListner = okManagerStringListner;
    }


}

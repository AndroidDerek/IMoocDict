package com.example.imoocdict;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * 使用okhttp进行图片下载，上传，get,post网络请求
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GsonUtils";
    private static final int SUCCESS_STATUS = 1;
    private static final int ERROR_STATUS = 0;
    private TextView voice;
    private EditText input;
    private Button find;
    private LinearLayout activity_main;
    private String findWords;
    private String string;
    private Button syncgetbyurl;
    private Button syncgetdrawableurl;
    private ImageView imageView;
    private OkManager okManager;
    private TextView translate;
    private Button upload;
    private Looper mainLooper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        mainLooper = Looper.getMainLooper();
        //okManager = OkManager.getInstance();
    }

    private void initView() {
        voice = (TextView) findViewById(R.id.voice);
        input = (EditText) findViewById(R.id.input);
        find = (Button) findViewById(R.id.find);
        activity_main = (LinearLayout) findViewById(R.id.activity_main);

        find.setOnClickListener(this);
        syncgetbyurl = (Button) findViewById(R.id.syncgetbyurl);
        syncgetbyurl.setOnClickListener(this);
        syncgetdrawableurl = (Button) findViewById(R.id.syncgetdrawableurl);
        syncgetdrawableurl.setOnClickListener(this);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnClickListener(this);
        translate = (TextView) findViewById(R.id.translate);
        translate.setOnClickListener(this);
        upload = (Button) findViewById(R.id.upload);
        upload.setOnClickListener(this);
    }

    String rootPath = "http://fanyi.youdao.com/openapi.do";
    /**
     * 有道词典提供的api查询接口,q=后面拼接的是需要查询的单词
     * 参考网站
     * 例如：查询words，对应的http://fanyi.youdao.com/openapi.do?keyfrom=imoocdict123456&key=324273592
     * &type=data&doctype=json&version=1.1&q=words
     */
    String path = "http://fanyi.youdao.com/openapi.do?keyfrom=imoocdict123456&key=324273592&" +
            "type=data&doctype=json&version=1.1&q=";


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //图片上传成功的标识
            if (msg.what == SUCCESS_STATUS) {
                Log.d(TAG, "SUCCESS_STATUS=");
                findString = "图片上传成功，返回的json为:" + (String) msg.obj;
                translate.setText(findString);
            }
            if (msg.what == ERROR_STATUS) {
                Log.d(TAG, "ERROR_STATUS=");
                translate.setText("网络异常，服务器繁忙");
                //Toast.makeText(MainActivity.this, "网络异常，服务器繁忙", Toast.LENGTH_SHORT).show();
            }
        }
    };
    String findString;

    @Override
    public void onClick(View v) {
        submit();
        switch (v.getId()) {
            case R.id.find:
                final String findUrl = path + findWords;
                if (null == okManager) {
                    okManager = OkManager.getInstance();
                }
                Log.d(TAG, "findUrl=" + findUrl);
                Log.d(TAG, "okManager=" + okManager);
                okManager.setOkManagerStringListner(new OkManager.OkManagerStringListner() {
                    @Override
                    public void onResponseString(String string) {
                        findString = string;
                        Log.d(TAG, "findString=" + findString);
                        //在ui主线程中更新ui
                       /* handler.post(new Runnable() {
                            @Override
                            public void run() {
                                translate.setText(findString);
                            }
                        });*/
                        Message message = Message.obtain();
                        message.what = SUCCESS_STATUS;
                        message.obj = string;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onResponseFailure(Call call, IOException e) {
                        //Toast需要在ui主线程中Looper中执行
                        //                        mainLooper.prepare();
                        //                        new Handler().post(new Runnable() {
                        //                            @Override
                        //                            public void run() {
                        //                                Toast.makeText(MainActivity.this,
                        // "网络异常，服务器繁忙", Toast.LENGTH_SHORT).show();
                        //                            }
                        //                        });
                        //
                        //                        mainLooper.loop();
                        //错误：Can't create handler inside thread that has not called Looper.prepare()

                       /* MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "网络异常，服务器繁忙", Toast.LENGTH_SHORT).show();
                            }
                        });*/
                        Message message = Message.obtain();
                        message.what = ERROR_STATUS;
                        handler.sendMessage(message);
                    }
                });
                //进行get请求
                okManager.getString(findUrl);

                break;
            case R.id.syncgetbyurl:
                if (null == okManager) {
                    okManager = OkManager.getInstance();
                }
                okManager.setOkManagerStringListner(new OkManager.OkManagerStringListner() {
                    @Override
                    public void onResponseString(String string) {
                        findString = string;
                        Log.d(TAG, "findString=" + findString);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                translate.setText(findString);
                            }
                        });
                    }

                    @Override
                    public void onResponseFailure(Call call, IOException e) {

                    }
                });
                Map<String, String> params = new HashMap<>();
                params.put("keyfrom", "imoocdict123456");
                params.put("key", "324273592");
                params.put("type", "data");
                params.put("doctype", "json");
                params.put("version", "1.1");
                params.put("q", findWords);
                //进行post请求
                okManager.postComplexForm(rootPath, params);
                break;
            case R.id.syncgetdrawableurl:
                if (null == okManager) {
                    okManager = OkManager.getInstance();
                }
                String imageUrl = "https://www.baidu.com/img/bd_logo1.png";
                okManager.setOkManagerBitmapListner(new OkManager.OkManagerBitmapListner() {
                    @Override
                    public void onResponseBitmap(Bitmap bitmap) {
                        Log.d(TAG, "imageBitmap");
                        imageBitmap = bitmap;
                        //在主线程中更新ui
                        MainActivity.this.runOnUiThread(imageRunnable);
                    }

                    @Override
                    public void onResponseFailure(Call call, IOException e) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "网络异常，服务器繁忙",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                //加载网络图片
                okManager.getBitmap(imageUrl);
                break;
            case R.id.upload:
                if (null == okManager) {
                    okManager = OkManager.getInstance();
                }
                //从资源中获取的Drawable --> Bitmap
                Resources resources = getResources();
                Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.login_bg);
                /***开发中使用bitmap转换成base64格式的字符串，提交给服务器***/
                //String base64 = "/9j/4AAQSkZJRgABAQAAAQABAAD";
                String base64 = bitmapToBase64(bitmap);
                Log.d(TAG, "base64=" + base64);
                // 这是本地服务器的上传测试地址
                String uploadUrl = "http://192.168.2.241/upark/upload";
                Map<String, String> loadParams = new HashMap<>();
                loadParams.put("id", "-1");
                loadParams.put("base64", base64);
                loadParams.put("type", "png");
                loadParams.put("model", "groupService");
                loadParams.put("serverCode", "other");
                okManager.setOkManagerStringListner(new OkManager.OkManagerStringListner() {
                    @Override
                    public void onResponseString(String string) {
                        //图片上传的成功回调
                        findString = string;
                        Log.d(TAG, "findString=" + findString);
                        Message message = Message.obtain();
                        message.what = SUCCESS_STATUS;
                        message.obj = findString;
                        handler.sendMessage(message);
                    }

                    @Override
                    public void onResponseFailure(Call call, IOException e) {
                        //Toast需要在ui主线程中Looper中执行
                        mainLooper.prepare();
                        Toast.makeText(MainActivity.this, "网络异常，服务器繁忙",
                                Toast.LENGTH_SHORT).show();
                        mainLooper.loop();
                    }
                });
                //进行图片的上传
                okManager.postComplexForm(uploadUrl, loadParams);
                break;
        }
    }

    private Bitmap imageBitmap;
    //展示加载的图片
    Runnable imageRunnable = new Runnable() {
        @Override
        public void run() {
            imageView.setImageBitmap(imageBitmap);
        }
    };

    private void submit() {
        // validate
        String inputString = input.getText().toString().trim();
        if (TextUtils.isEmpty(inputString)) {
            Toast.makeText(this, "inputString不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        findWords = inputString;
        // TODO validate success, do something
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public String bitmapToBase64(Bitmap bitmap) {
        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}

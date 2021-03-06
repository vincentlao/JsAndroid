package com.example.cheng.js;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.zxing.activity.CaptureActivity;

public class MainActivity extends Activity {
    WebView webView;
    // String url = "file:///android_asset/index.html";
    String url = "file:///android_asset/test.html";
    private final static int SCANNIN_GREQUEST_CODE = 1;

    private static String scanResult;
    public final static String LONGITUDE = "longitude";// 经度
    public final static String LATITUDE = "latitude";// 维度
    TextView txt;
    private Handler hd = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        DensityUtil.px2dip(this,216);
        DensityUtil.px2dip(this,226);
        if(null!=savedInstanceState){
            webView.restoreState(savedInstanceState);
            Log.i(TAG, "restore state");
        }
    }
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 处理扫描结果（在界面上显示）
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            scanResult = bundle.getString("result");
            txt.setText("从js获取到得数据为：" + scanResult);
            webView.loadUrl("javascript:setScanResult('" + getJsonStr() + "')");
        }
    }
	
    private void init() {
        txt = (TextView) findViewById(R.id.textView2);
        webView = (WebView) this.findViewById(R.id.wv);
        setUpWebViewDefaults(webView);	    
	    
//	webView.setWebChromeClient(new MyWebChromeClient());
        //通过单纯js来操纵安卓
        webView.addJavascriptInterface(new DemoJavascriptInterface(), "demo");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            //通过shoultOverrideUrlLoading方法判断url信息来操纵安卓，并传递信息
            public boolean shouldOverrideUrlLoading(WebView view,
                                                    final String url) {
                Log.e("myZxing_first", url);
                if (url.startsWith("startscanning://")) {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(),
                            CaptureActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                    return true;
                }else if (url.startsWith("startrouting:///")) {
                    Log.e("myZxing_startscanning", url);
                    String s = url.substring(16, url.length());
                    String[] str = s.split("\\/");
                    Log.e("str[0]", str[0]);
/*					Intent intent = new Intent().setClass(
							getApplicationContext(), PoiActivity.class);
					intent.putExtra(LONGITUDE, Double.valueOf(str[0]));
					intent.putExtra(LATITUDE, Double.valueOf(str[1]));
					startActivity(intent);*/
                    txt.setText("从js获取到得数据为："+str[0]+"    "+str[1]);
                    return true;
                }
                return false;
            }
		
	    @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
		
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
		//优化图片加载
                if(!webView.getSettings().getLoadsImagesAutomatically()) {
                    webView.getSettings().setLoadsImagesAutomatically(true);
                }
	    }
        });
        url = "https://teste.csc108.com/fmall/main";
        webView.loadUrl(url);
        Log.e("xdb", "url:" + url);

        //安卓给js数据
        findViewById(R.id.btn1).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                webView.loadUrl("javascript:wave('"+setJsData()+"')");
                startActivity(new Intent().setClass(MainActivity.this,TouguShowH5Activity.class));
            }
        });


        //重置数据
        findViewById(R.id.btn2).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                txt.setText("从js获取到得数据为：");
                webView.loadUrl("javascript:reset()");
            }
        });
    }	
	
    /**
     * Convenience method to set some generic defaults for a
     * given WebView
     *
     * @param webView
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setUpWebViewDefaults(WebView webView) {
	//开启硬件加速后，WebView渲染页面更加快速，拖动也更加顺滑。
        // 但有个副作用就是容易会出现页面加载白块同时界面闪烁现象。
        // 解决这个问题的方法是设置WebView暂时关闭硬件加速
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && webView.isHardwareAccelerated()) {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }        
        WebSettings settings = webView.getSettings();
        // 设置字符集编码
        settings.setDefaultTextEncodingName("UTF-8");
        //支持数据保存
        settings.setSavePassword(true);
        settings.setSaveFormData(true);
        //支持缩放
        settings.setSupportZoom(true);
        // Enable Javascript
        settings.setJavaScriptEnabled(true);
        // Use WideViewport and Zoom out if there is no viewport defined
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
	settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);//自适应屏幕
	//图片加载优化    
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            settings.setLoadsImagesAutomatically(true);//图片自动缩放 打开
        } else {
            settings.setLoadsImagesAutomatically(false);//图片自动缩放 关闭
        }
        // Enable pinch to zoom without the zoom buttons
        settings.setBuiltInZoomControls(true);
	//隐藏缩放控件
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            settings.setDisplayZoomControls(false);
        }
        // Enable remote debugging via chrome://inspect
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
	//如果SDK版本大于19则使用缓存数据    
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }	    
	//支持localStorage
        settings.setDomStorageEnabled(true);
        settings.setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
    }	

    public String setJsData(){
        return "从安卓获取到得数据为：安卓数据";
    }	
	
    public static String getJsonStr() {
        return scanResult;
    }

    // 重写返回键，让网页返回正常化
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // 自定义javascript接口用于给网页js调用安卓代码
    final class DemoJavascriptInterface {
        @JavascriptInterface
        public void clickOnAndroid() {
            hd.post(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(),
                            CaptureActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
                }
            });
        }

        //js调用安卓并把数据传给安卓端
        @JavascriptInterface
        public void getHtmlJson(final String json) {
            // TODO Auto-generated method stub
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txt.setText("从html获取到得数据：" + json);
                }
            });
        }
    }
}

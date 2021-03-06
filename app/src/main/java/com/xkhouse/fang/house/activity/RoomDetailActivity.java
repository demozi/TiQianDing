package com.xkhouse.fang.house.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

//import com.squareup.okhttp.Call;
//import com.squareup.okhttp.Callback;
//import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.Response;
import com.xkhouse.fang.R;
import com.xkhouse.fang.app.activity.AppBaseActivity;
import com.xkhouse.fang.app.activity.CalculatorActivity;
import com.xkhouse.fang.app.config.Preference;
import com.xkhouse.fang.app.view.SharePopupWindow;
import com.xkhouse.fang.money.activity.CustomerAddActivity;
import com.xkhouse.fang.user.activity.LoginActivity;
import com.xkhouse.frame.log.Logger;
import com.xkhouse.lib.utils.NetUtil;
import com.xkhouse.lib.utils.StringUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/** 
 * @Description: 户型（房源）详情页 
 * @author wujian  
 * @date 2015-10-13 下午4:02:09  
 */
public class RoomDetailActivity extends AppBaseActivity {
	
	private View root_view;
	
	private ImageView iv_head_left;
	private TextView tv_head_title;
	private ImageView iv_head_share;		//分享
	
	private String title;

    private ProgressBar pb;
	private WebView webView;
    private LinearLayout error_lay;
	private String currentUrl;
	private String projectId;
	private String roomId;
	
	public static final int ROOM_TYPE_HX = 0;   //户型
	public static final int ROOM_TYPE_FY = 1;   //房源
	
	private int roomType;   //新房  二手房  租房
	
	private SharePopupWindow popWindow;
	private String webTitle;
    private String webDescription;
    private String imageUrl;

    private boolean isFromList = false; //判断是否是从新房户型列表页进入


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void setContentView() {
		super.setContentView();
		
		setContentView(R.layout.activity_room_detail);
	}
	
	@Override
	protected void init() {
		super.init();
		
		projectId = getIntent().getExtras().getString("projectId");
		roomId = getIntent().getExtras().getString("roomId");
		roomType = getIntent().getExtras().getInt("roomType");
        isFromList = getIntent().getExtras().getBoolean("isFromList", false);

		if(roomType == ROOM_TYPE_HX){
            if(isFromList){
                currentUrl = modelApp.getNewHouseDetail() + projectId +"/huxing/" + roomId + ".html?houselist=1";
            }else{
                currentUrl = modelApp.getNewHouseDetail() + projectId +"/huxing/" + roomId + ".html";
            }

			title = "户型详情";
			
		}else if(roomType == ROOM_TYPE_FY) {
            currentUrl = modelApp.getNewHouseDetail() + projectId +"/fangyuan/" + roomId + ".html";
			title = "房源详情";
		}
	}
	
	@Override
	protected void findViews() {
		super.findViews();
		
		initTitle();

        error_lay = (LinearLayout) findViewById(R.id.error_lay);
        error_lay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWebData(currentUrl);
            }
        });

        pb = (ProgressBar) findViewById(R.id.pb);
		webView = (WebView) findViewById(R.id.webview);
		
		WebSettings webSettings = webView.getSettings();
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        String str = webSettings.getUserAgentString();
        webSettings.setUserAgentString(str+"XKAPP");
        
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setWebViewClient(new MyWebClient());

        loadWebData(currentUrl);
        htmlParse(currentUrl);
	}

    private void loadWebData(String url){
        if (NetUtil.detectAvailable(mContext)){
            webView.setVisibility(View.VISIBLE);
            error_lay.setVisibility(View.GONE);
            loadDataFromNet(url);
        }else{
            webView.setVisibility(View.GONE);
            error_lay.setVisibility(View.VISIBLE);
        }
    }

	
	private void initTitle() {
		root_view = findViewById(R.id.root_view);
		iv_head_left = (ImageView) findViewById(R.id.iv_head_left);
		tv_head_title = (TextView) findViewById(R.id.tv_head_title);
		iv_head_share = (ImageView) findViewById(R.id.iv_head_share);
		
		tv_head_title.setText(title);
		iv_head_left.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    finish();
                }
            }
        });
		
		iv_head_share.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popWindow == null) {
                    popWindow = new SharePopupWindow(mContext);
                }
                popWindow.showAtLocation(webTitle, webDescription, imageUrl, webView.getUrl(), false,
                        root_view, Gravity.BOTTOM, 0, 0);
            }
        });
	}

    private void htmlParse(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document document=  Jsoup.connect(url).get();

                    Elements metas = document.select("meta");
                    for (Element meta : metas){
                        if("description".equals(meta.attr("name").toLowerCase())){
                            webDescription = meta.attr("content");
                        }
                    }
                    Elements images = document.select("img");
                    if(images != null && images.size() > 0){
                        imageUrl = images.get(0).attr("src");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

	
	final class MyWebChromeClient extends WebChromeClient {
		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			webTitle = title;
		}

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            pb.setProgress(newProgress);
            if(newProgress==100){
                pb.setVisibility(View.GONE);
            }else {
                if (pb.getVisibility() == View.GONE)
                    pb.setVisibility(View.VISIBLE);
                pb.setProgress(newProgress);
            }
            super.onProgressChanged(view, newProgress);
        }

	}
	
	final class MyWebClient extends WebViewClient{
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);

		}
		
		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
            htmlParse(url);
		}
	
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (url.startsWith("tel:")){ 
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)); 
                startActivity(intent);
                return true;
                
            }else if (!StringUtil.isEmpty(url) && url.contains("tools/counter")){ 
                startActivity(new Intent(mContext, CalculatorActivity.class));
                return true;
                
            }else if(!StringUtil.isEmpty(url) && url.contains("dongtai.html")){
				//跳转到销售动态
				Intent intent = new Intent(mContext, HouseDynamicActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("projectId", projectId);
				intent.putExtras(bundle);
				startActivity(intent);
                return true;
				
			}else if(!StringUtil.isEmpty(url) && url.contains("huxing/")) {
				//跳转到主力户型
				Intent intent = new Intent(mContext, MainHouseTypeActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("projectId", projectId);
				intent.putExtras(bundle);
				startActivity(intent);
                return true;
				
			}else if(!StringUtil.isEmpty(url) && url.contains("fangyuan/")) {
				//跳转到在售房源
				Intent intent = new Intent(mContext, OnsaleHouseActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("projectId", projectId);
				intent.putExtras(bundle);
				startActivity(intent);
                return true;
				
			}else if(!StringUtil.isEmpty(url) && url.contains("/Recommend/")){
				
				if(Preference.getInstance().readIsLogin()){
					Intent intent = new Intent(mContext, CustomerAddActivity.class);
					startActivity(intent);
                    return true;
				}else {
					Intent intent = new Intent(mContext, LoginActivity.class);
					intent.putExtra("classStr", CustomerAddActivity.class);
					startActivity(intent);
                    return true;
				}
			}else if(url.contains("/xiangce") || url.contains("/imgDetail/")){
                Intent intent = new Intent(mContext, HouseAlbumActivity.class);
                Bundle data = new Bundle();
                data.putString("url", url);
                intent.putExtras(data);
                startActivity(intent);
                return true;

            }else if(!StringUtil.isEmpty(url) && url.contains("/newhouse/")){
                String params[] = url.split("/");
                if(params != null && params.length == 5){
                    Intent intent = new Intent(mContext, HouseDetailActivity.class);
                    Bundle data = new Bundle();
                    data.putInt("houseType", MapHousesActivity.HOUSE_TYPE_NEW);
                    data.putString("projectId", params[params.length-1]);
                    data.putString("projectName", "");
                    intent.putExtras(data);
                    startActivity(intent);
                    return true;
                }
            }

            if (!NetUtil.detectAvailable(mContext)){
                webView.setVisibility(View.GONE);
                error_lay.setVisibility(View.VISIBLE);
                currentUrl = url;
                return true;
            }

            loadDataFromNet(url);

			return true;
		}

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            Logger.e("", " onReceivedError ");
        }

	}


    /**
     * 解决500， 404 等等错误，webview拦截不到回调
     * @param url
     */
    private void loadDataFromNet(final String url) {
//        try{
//            //创建okHttpClient对象
//            OkHttpClient mOkHttpClient = new OkHttpClient();
//            //创建一个Request
//            Request request = new Request.Builder()
//                    .url(url)
//                    .build();
//            //new call
//            Call call = mOkHttpClient.newCall(request);
//            //请求加入调度
//            call.enqueue(new Callback() {
//                @Override
//                public void onFailure(Request request, IOException e) {
//                    runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            webView.setVisibility(View.GONE);
//                            error_lay.setVisibility(View.VISIBLE);
//                        }
//                    });
//                }
//
//                @Override
//                public void onResponse(final Response response) throws IOException {
//                    final String htmlStr = response.body().string();
//                    runOnUiThread(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            webView.loadUrl(url);
//                            webView.setVisibility(View.VISIBLE);
//                            error_lay.setVisibility(View.GONE);
//                        }
//                    });
//                }
//            });
//        }catch (Exception e){
//            e.printStackTrace();
//        }

    }



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(webView.canGoBack()){
				webView.goBack();
				return true;
			}
			if (popWindow != null && popWindow.isShowing()) {
				popWindow.dismiss();
			} else {
				finish();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}

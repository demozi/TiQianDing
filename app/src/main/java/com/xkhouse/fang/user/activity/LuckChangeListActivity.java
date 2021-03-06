package com.xkhouse.fang.user.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xkhouse.fang.R;
import com.xkhouse.fang.app.activity.AppBaseActivity;
import com.xkhouse.fang.app.callback.RequestListener;
import com.xkhouse.fang.app.config.Constants;
import com.xkhouse.fang.user.adapter.LuckChangeAdapter;
import com.xkhouse.fang.user.entity.MSGNews;
import com.xkhouse.fang.user.task.MessageDetailListRequest;
import com.xkhouse.fang.widget.loading.RotateLoading;
import com.xkhouse.fang.widget.xlist.XListView;
import com.xkhouse.fang.widget.xlist.XListView.IXListViewListener;
import com.xkhouse.lib.utils.NetUtil;

import java.util.ArrayList;

/**
 * 抽奖机会明细
 */
public class LuckChangeListActivity extends AppBaseActivity {
	
	private ImageView iv_head_left;
	private TextView tv_head_title;
	
	private XListView msg_listView;
	private LuckChangeAdapter adapter;
	private int currentPageIndex = 1;  //分页索引
	private int pageSize = 10; //每次请求10条数据
	private boolean isPullDown = false; // 下拉

    private LinearLayout notice_lay;
    private RotateLoading rotate_loading;
    private LinearLayout error_lay;

	private MessageDetailListRequest listRequest;
	private ArrayList<MSGNews> newsList = new ArrayList<MSGNews>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		startDataTask(1, true);
        fillData();
	}
	
	
	@Override
	protected void setContentView() {
		setContentView(R.layout.activity_luck_change_list);
	}

	@Override
	protected void init() {
		super.init();
	}

	@Override
	protected void findViews() {
		initTitle();
		
		msg_listView = (XListView) findViewById(R.id.msg_listView);
        rotate_loading = (RotateLoading) findViewById(R.id.rotate_loading);
        error_lay = (LinearLayout) findViewById(R.id.error_lay);
        notice_lay = (LinearLayout) findViewById(R.id.notice_lay);
		
	}

	private void initTitle() {
		iv_head_left = (ImageView) findViewById(R.id.iv_head_left);
		tv_head_title = (TextView) findViewById(R.id.tv_head_title);
		tv_head_title.setText("活动机会明细");
		iv_head_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				finish();
			}
		});
	}
	
	@Override
	protected void setListeners() {
        error_lay.setOnClickListener(this);

		msg_listView.setPullLoadEnable(true);
		msg_listView.setPullRefreshEnable(true);
		msg_listView.setXListViewListener(new IXListViewListener() {

            @Override
            public void onRefresh() {
                isPullDown = true;
//                startDataTask(1, false);
            }

            @Override
            public void onLoadMore() {
//                startDataTask(currentPageIndex, false);
            }
        }, R.id.favorite_listView);
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
        switch (v.getId()){
            case R.id.error_lay:
//                startDataTask(1, true);
                break;
        }
	}
	
	
	private void fillData(){

        for (int i = 0; i < 3; i++) {
            newsList.add(new MSGNews());
        }

		if(newsList == null) return;
		if(adapter == null ){
			adapter = new LuckChangeAdapter(mContext, newsList);
			msg_listView.setAdapter(adapter);
		}else {
			adapter.setData(newsList);
		}
	}
	
	private void startDataTask(int page, boolean showLoading){
		if (NetUtil.detectAvailable(mContext)) {
			if(listRequest == null){
				listRequest = new MessageDetailListRequest("", "",modelApp.getSite().getSiteId(),
						11, page, pageSize, new RequestListener() {
					
					@Override
					public void sendMessage(Message message) {

                        rotate_loading.stop();
                        rotate_loading.setVisibility(View.GONE);

                        if (isPullDown){
                            currentPageIndex = 1;
                        }

						switch (message.what) {
						case Constants.ERROR_DATA_FROM_NET:
                            if (newsList == null || newsList.size() == 0){
                                msg_listView.setVisibility(View.GONE);
                                notice_lay.setVisibility(View.GONE);
                                error_lay.setVisibility(View.VISIBLE);
                            }else{
                                Toast.makeText(mContext, R.string.service_error, Toast.LENGTH_SHORT).show();
                            }
							break;
							
						case Constants.NO_DATA_FROM_NET:
                            error_lay.setVisibility(View.GONE);
                            notice_lay.setVisibility(View.GONE);
                            msg_listView.setVisibility(View.VISIBLE);
                            if(newsList == null || newsList.size() ==0){
                                msg_listView.setVisibility(View.GONE);
                                notice_lay.setVisibility(View.VISIBLE);
                            }
							break;
							
						case Constants.SUCCESS_DATA_FROM_NET:
                            msg_listView.setVisibility(View.VISIBLE);
                            error_lay.setVisibility(View.GONE);
                            notice_lay.setVisibility(View.GONE);

							ArrayList<MSGNews> temp = (ArrayList<MSGNews>) message.obj;
							//根据返回的数据量判断是否隐藏加载更多
							if(temp.size() < pageSize){
								msg_listView.setPullLoadEnable(false);
							}else{
								msg_listView.setPullLoadEnable(true);
							}
							//如果是下拉刷新则索引恢复到1，并且清除掉之前数据
							if(isPullDown && newsList != null){
								newsList.clear();
								currentPageIndex = 1;
							}
							newsList.addAll(temp);
                            if(currentPageIndex == 1 && (temp == null || temp.size() ==0)){
                                msg_listView.setVisibility(View.GONE);
                                notice_lay.setVisibility(View.VISIBLE);
                                return;
                            }

							fillData();
                            if (currentPageIndex > 1 && message.arg1 == newsList.size()){
                                Toast.makeText(mContext, R.string.data_load_end, Toast.LENGTH_SHORT).show();
                            }
                            currentPageIndex++;
							break;
						}
						isPullDown = false;
						msg_listView.stopRefresh();
						msg_listView.stopLoadMore();
					}
				});
			}else {
				listRequest.setData("", "", modelApp.getSite().getSiteId(),
						11, page, pageSize);
			}
			if (showLoading){
                msg_listView.setVisibility(View.GONE);
                error_lay.setVisibility(View.GONE);
                notice_lay.setVisibility(View.GONE);
                rotate_loading.setVisibility(View.VISIBLE);
                rotate_loading.start();
            }
			listRequest.doRequest();
		}else {
			isPullDown = false;
			msg_listView.stopRefresh();
			msg_listView.stopLoadMore();
            if (newsList == null || newsList.size() == 0){
                msg_listView.setVisibility(View.GONE);
                rotate_loading.setVisibility(View.GONE);
                notice_lay.setVisibility(View.GONE);
                error_lay.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(mContext, R.string.net_warn, Toast.LENGTH_SHORT).show();
            }
		}
	}

}

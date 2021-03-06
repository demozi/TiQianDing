package com.xkhouse.fang.house.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.xkhouse.fang.R;
import com.xkhouse.fang.app.activity.ModelApplication;
import com.xkhouse.fang.app.callback.RequestListener;
import com.xkhouse.fang.app.config.Constants;
import com.xkhouse.fang.house.adapter.HouseDynamicAdapter;
import com.xkhouse.fang.house.entity.XKDynamic;
import com.xkhouse.fang.house.task.HouseDynamicRequest;
import com.xkhouse.fang.widget.CustomDialog;
import com.xkhouse.fang.widget.xlist.XListView;
import com.xkhouse.fang.widget.xlist.XListView.IXListViewListener;
import com.xkhouse.lib.utils.NetUtil;

import java.util.ArrayList;

/**
 * 
* @Description: 楼盘销售动态
* @author wujian  
* @date 2015-10-8 下午2:29:58
 */
public class HouseDynamicView {

	private Context context;
	private View rootView;
	
	private XListView house_listView;
	private int currentPageIndex = 2;  //分页索引
	private int pageSize = 10; //每次请求10条数据
	private boolean isPullDown = false; // 下拉
	
	private HouseDynamicAdapter adapter;
	private ArrayList<XKDynamic> dynamicList = new ArrayList<XKDynamic>();
	
	private HouseDynamicRequest request;
	private String projectId = "";
	private String year;
	
	private ModelApplication modelApp;
	
	public View getView() {
        return rootView;
    } 
	
	public HouseDynamicView(Context context, String year, String projectId) {
		this.context = context;
		this.year = year;
		this.projectId = projectId;
		modelApp = (ModelApplication) ((Activity) context).getApplication();
		initView();
		setListener();
	}
	
	private void setListener(){
		house_listView.setPullLoadEnable(true);
		house_listView.setPullRefreshEnable(true);
		house_listView.setXListViewListener(new IXListViewListener() {

            @Override
            public void onRefresh() {
                isPullDown = true;
                startDataTask(1, false);
            }

            @Override
            public void onLoadMore() {
                startDataTask(currentPageIndex, false);
            }
        }, R.id.house_listView);
	}
	
	private void initView() {
		rootView = LayoutInflater.from(context).inflate(R.layout.view_house_dynamic, null);
		house_listView = (XListView) rootView.findViewById(R.id.house_listView);
		
	}
	
	public void fillData(ArrayList<XKDynamic> dynamicList){
		if(dynamicList == null) return;
		
		this.dynamicList = dynamicList;
		if(adapter == null){
			adapter = new HouseDynamicAdapter(context, dynamicList);
			house_listView.setAdapter(adapter);
		}else{
			adapter.notifyDataSetChanged();
		}
	}
	
	public void refreshView() {
		if(dynamicList == null || dynamicList.size() < 1){
			startDataTask(1, true);
		}
	}
	
	
	/*******************************  网络数据请求   ***************************************/
	private RequestListener listener = new RequestListener() {
		
		@Override
		public void sendMessage(Message message) {
			hideLoadingDialog();
			switch (message.what) {
			case Constants.ERROR_DATA_FROM_NET:
			case Constants.NO_DATA_FROM_NET:
				Toast.makeText(context, R.string.service_error, Toast.LENGTH_SHORT).show();
				break;
				
			case Constants.SUCCESS_DATA_FROM_NET:
				Bundle data = message.getData();
				ArrayList<XKDynamic> temp = (ArrayList<XKDynamic>) data.getSerializable("dynamicList");
				if(temp != null){
					//根据返回的数据量判断是否隐藏加载更多
					if(temp.size() < pageSize){
						house_listView.setPullLoadEnable(false);
					}else{
						house_listView.setPullLoadEnable(true);
					}
					
					//如果是下拉刷新则索引恢复到1，并且清除掉之前数据
					if(isPullDown && dynamicList != null){
						dynamicList.clear();
						currentPageIndex = 1;
					}
					currentPageIndex++;
					dynamicList.addAll(temp);
					fillData(dynamicList);
				} 
				break;
			}
			isPullDown = false;
			house_listView.stopRefresh();
			house_listView.stopLoadMore();
		}
	};
	
	private void startDataTask(int page, boolean showLoading){
		if(NetUtil.detectAvailable(context)){
			if(request == null){
				request = new HouseDynamicRequest(projectId, year, page, pageSize, listener);
			}else {
				request.setData(projectId, year, page, pageSize);
			}
            if(showLoading){
                showLoadingDialog(R.string.data_loading);
            }

			request.doRequest();
			
		}else {
			Toast.makeText(context, R.string.net_warn, Toast.LENGTH_SHORT).show();
			isPullDown = false;
			house_listView.stopRefresh();
			house_listView.stopLoadMore();
		}
	}
	
	
	
	CustomDialog dialog;

    public void showLoadingDialog(int showMessage) {
        if (dialog == null) {
            dialog = new CustomDialog(context, showMessage,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        if (!((Activity) context).isFinishing() && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void showLoadingDialog(String showMessage) {
        if (dialog == null) {
            dialog = new CustomDialog(context, showMessage,
                    android.R.style.Theme_Translucent_NoTitleBar);
        }
        if (!((Activity) context).isFinishing() && !dialog.isShowing()) {
            dialog.show();
        }
    }
    
    /**
     * 隐藏正在提交疑问的环形进度条
     */
    public void hideLoadingDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}

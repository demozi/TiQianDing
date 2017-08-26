package com.xkhouse.fang.user.view;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.xkhouse.fang.R;
import com.xkhouse.fang.app.activity.ModelApplication;
import com.xkhouse.fang.app.callback.RequestListener;
import com.xkhouse.fang.app.config.Constants;
import com.xkhouse.fang.user.activity.MyRecommendActivity;
import com.xkhouse.fang.user.adapter.CJListAdapter;
import com.xkhouse.fang.user.adapter.XKRecommendListAdapter;
import com.xkhouse.fang.user.entity.XKRecommend;
import com.xkhouse.fang.user.task.RecommendAgainRequest;
import com.xkhouse.fang.user.task.XKRecommendListRequest;
import com.xkhouse.fang.widget.loading.RotateLoading;
import com.xkhouse.fang.widget.xlist.XListView;
import com.xkhouse.fang.widget.xlist.XListView.IXListViewListener;
import com.xkhouse.lib.utils.NetUtil;
import com.xkhouse.lib.utils.StringUtil;

import java.util.ArrayList;

/**
 * 抽奖列表
 */
public class ItemCJListView {

	private Context context;
	private View rootView;

    private LinearLayout content_lay;
    private RotateLoading rotate_loading;
    private LinearLayout error_lay;

	private XListView recommend_listView;
	private CJListAdapter adapter;
	private int currentPageIndex = 1;  //分页索引
	private int pageSize = 10; //每次请求10条数据
	private boolean isPullDown = false; // 下拉

	private XKRecommendListRequest recommendListRequest;
	private ArrayList<XKRecommend> recommendList = new ArrayList<XKRecommend>();

	private String status;

	private ModelApplication modelApp;


	public View getView() {
        return rootView;
    }

	public ItemCJListView(Context context, String status) {
		this.context = context;
		this.status = status;
		modelApp = (ModelApplication) ((Activity) context).getApplication();
		
		initView();
		setListener();

	}
	
	private void initView() {
		rootView = LayoutInflater.from(context).inflate(R.layout.view_recommend_list, null);

		recommend_listView = (XListView) rootView.findViewById(R.id.recommend_listView);

        content_lay = (LinearLayout) rootView.findViewById(R.id.content_lay);
        rotate_loading = (RotateLoading) rootView.findViewById(R.id.rotate_loading);
        error_lay = (LinearLayout) rootView.findViewById(R.id.error_lay);
	}
	
	public void refreshView(){
		isPullDown = true;
		startRecommendListTask(1, true);
	}
	
	private void setListener() {
		recommend_listView.setPullLoadEnable(true);
		recommend_listView.setPullRefreshEnable(true);
		recommend_listView.setXListViewListener(new IXListViewListener() {

            @Override
            public void onRefresh() {
                isPullDown = true;
                startRecommendListTask(1, false);
            }

            @Override
            public void onLoadMore() {
                startRecommendListTask(currentPageIndex, false);
            }
        }, R.id.recommend_listView);

        error_lay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPullDown = true;
                startRecommendListTask(1, true);
            }
        });
	}
	
	
	private void startRecommendListTask(int page, boolean showLoading){
		if (NetUtil.detectAvailable(context)) {
			if(recommendListRequest == null){
				recommendListRequest = new XKRecommendListRequest("1000", status, page, pageSize,
						new RequestListener() {
					
					@Override
					public void sendMessage(Message message) {

                        rotate_loading.stop();
                        rotate_loading.setVisibility(View.GONE);
                        if (isPullDown){
                            currentPageIndex = 1;
                        }

						switch (message.what) {
						case Constants.ERROR_DATA_FROM_NET:
                            if (recommendList == null || recommendList.size() == 0){
                                content_lay.setVisibility(View.GONE);
                                rotate_loading.setVisibility(View.GONE);
                                error_lay.setVisibility(View.VISIBLE);
                            }else{
                                Toast.makeText(context, R.string.service_error, Toast.LENGTH_SHORT).show();
                            }
							break;
							
						case Constants.NO_DATA_FROM_NET:
                            error_lay.setVisibility(View.GONE);
                            content_lay.setVisibility(View.VISIBLE);
                            if ("0".equals(status)){
                                Toast.makeText(context, "您还没有推荐，还在等什么！", Toast.LENGTH_SHORT).show();
                            }
							break;
							
						case Constants.SUCCESS_DATA_FROM_NET:
                            content_lay.setVisibility(View.VISIBLE);
                            error_lay.setVisibility(View.GONE);

							ArrayList<XKRecommend> temp = (ArrayList<XKRecommend>) message.obj;
							//根据返回的数据量判断是否隐藏加载更多
							if(temp.size() < pageSize){
								recommend_listView.setPullLoadEnable(false);
							}else{
								recommend_listView.setPullLoadEnable(true);
							}
							
							//如果是下拉刷新则索引恢复到1，并且清除掉之前数据
							if(isPullDown && recommendList != null){
								recommendList.clear();
								currentPageIndex = 1;
							}
							recommendList.addAll(temp);
							fillRecommendData();
                            if (currentPageIndex > 1 && message.arg1 == recommendList.size()){
                                Toast.makeText(context, R.string.data_load_end, Toast.LENGTH_SHORT).show();
                            }
                            currentPageIndex++;
							break;
						}
						isPullDown = false;
						recommend_listView.stopRefresh();
						recommend_listView.stopLoadMore();
					}
				});
			}else {
				recommendListRequest.setData("100", status, page, pageSize);
			}
			if (showLoading) {
                content_lay.setVisibility(View.GONE);
                error_lay.setVisibility(View.GONE);
                rotate_loading.setVisibility(View.VISIBLE);
                rotate_loading.start();
            }
			recommendListRequest.doRequest();

		}else {
			isPullDown = false;
			recommend_listView.stopRefresh();
			recommend_listView.stopLoadMore();

            if (recommendList == null || recommendList.size() == 0){
                content_lay.setVisibility(View.GONE);
                rotate_loading.setVisibility(View.GONE);
                error_lay.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(context, R.string.net_warn, Toast.LENGTH_SHORT).show();
            }
		}

        fillRecommendData();
	}
	
	
	private void fillRecommendData() {
		if(recommendList == null) return;
		
		if(adapter == null){
			adapter = new CJListAdapter(context, recommendList);
			recommend_listView.setAdapter(adapter);
		}else {
			adapter.setData(recommendList);
		}
	}
	
	
	


    
}
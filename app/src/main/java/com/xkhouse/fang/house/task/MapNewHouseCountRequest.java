package com.xkhouse.fang.house.task;

import android.os.Message;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.xkhouse.fang.app.callback.RequestListener;
import com.xkhouse.fang.app.config.Constants;
import com.xkhouse.fang.app.entity.Area;
import com.xkhouse.frame.activity.BaseApplication;
import com.xkhouse.frame.log.Logger;
import com.xkhouse.lib.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @Description: 获取各区域内楼盘个数 (新房地图找房) 
* @author wujian  
* @date 2015-9-28 下午3:21:10
 */
public class MapNewHouseCountRequest {

	private String TAG = "MapNewHouseCountRequest";
	private RequestListener requestListener;
	
	private String siteId;  //站点 
	private String searchContent;
	
	private String code;	//返回状态
	private String msg;		//返回提示语
	
	private ArrayList<Area> areaList = new ArrayList<Area>();
	
	public MapNewHouseCountRequest(String siteId, String searchContent, RequestListener requestListener) {
		this.siteId = siteId;
		this.searchContent = searchContent;
		this.requestListener = requestListener;
	}
	
	public void setData(String siteId, String searchContent) {
		this.siteId = siteId; 
		this.searchContent = searchContent;
	}
	
	public void doRequest(){
		areaList.clear();
		Map<String, String> params = new HashMap<String, String>();
		params.put("siteId", siteId);
		String url = StringUtil.getRequestUrl(Constants.MAP_AREA_LIST, params) + searchContent;
		Logger.d(TAG, url);
		
		 
		StringRequest request = new StringRequest(url, new Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Logger.d(TAG, response);
                       
                        parseResult(response);
                        
                        Message message = new Message();
                        if(Constants.SUCCESS_CODE_OLD.equals(code)){
                        	message.obj = areaList;
                        	message.what = Constants.SUCCESS_DATA_FROM_NET;
                        }else{
                           message.obj = msg;
                           message.what = Constants.NO_DATA_FROM_NET;
                        }
                        requestListener.sendMessage(message);
                    }
                }, new ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                    	Logger.e(TAG, error.toString());
                        Message message = new Message();
                        message.what = Constants.ERROR_DATA_FROM_NET;
                        requestListener.sendMessage(message);
                    }
                }){
            @Override
            public RetryPolicy getRetryPolicy() {
                return new DefaultRetryPolicy(Constants.VOLLEY_TIME_OUT,
                        Constants.VOLLEY_MAX_NUM_RETRIES, Constants.VOLLEY_BACKOFF_MULTIPLIER);
            }
        };

        BaseApplication.getInstance().getRequestQueue().add(request);
	}
	
	
	public void parseResult(String result) {
		if (StringUtil.isEmpty(result)) {
            return;
        }

        try {
        	if(result != null && result.startsWith("\ufeff")){
        		result =  result.substring(1);
        	}
        	
            JSONObject jsonObject = new JSONObject(result);
            if (jsonObject != null) {
            	code = jsonObject.optString("code");
            	
                if (!Constants.SUCCESS_CODE_OLD.equals(code)) {
                	msg = jsonObject.optString("msg");
                    return;
                }
                
                JSONArray jsonArray = jsonObject.optJSONArray("data");
               
	        	if (jsonArray != null && jsonArray.length() > 0) {
	                for (int i = 0; i <= jsonArray.length() - 1; i++) {
	                	JSONObject areaJson = jsonArray.getJSONObject(i);
	                	
	                	Area area = new Area();
	                	area.setAreaId(areaJson.optString("areaId"));
	                	area.setAreaName(areaJson.optString("areaName"));
	                	area.setLatitude(areaJson.optString("latitude"));
	                	area.setLongitude(areaJson.optString("longitude"));
	                	area.setCount(areaJson.optString("projectCount"));
	                	
	                	areaList.add(area);
	                }
	        	}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}

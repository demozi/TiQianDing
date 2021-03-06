package com.xkhouse.fang.booked.task;

import android.os.Bundle;
import android.os.Message;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.xkhouse.fang.app.callback.RequestListener;
import com.xkhouse.fang.app.config.Constants;
import com.xkhouse.fang.booked.entity.AccountInfo;
import com.xkhouse.frame.activity.BaseApplication;
import com.xkhouse.frame.log.Logger;
import com.xkhouse.lib.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
* @Description: 获取账户明细--员工
* @author wujian  
* @date 2015-9-21 下午2:20:00
 */
public class EmployeeAccountInfoListRequest {

	private String TAG = EmployeeAccountInfoListRequest.class.getSimpleName();
	private RequestListener requestListener;

	private String token;  //站点
	private int page;	//分页索引
	private int num;	//每页个数

	private String code;	//返回状态
	private String msg;		//返回提示语

    private String balance;  //余额

	private ArrayList<AccountInfo> accountInfoList = new ArrayList<>();

	public EmployeeAccountInfoListRequest(String token, int page, int num, RequestListener requestListener) {
		this.token = token;
		this.page = page;
		this.num = num;
		this.requestListener = requestListener;
	}
	
	public void setData(String business_id, int page, int num) {
		this.token = business_id;
		this.page = page;
		this.num = num;
	}
	
	public void doRequest(){
		accountInfoList.clear();
		Map<String, String> params = new HashMap<String, String>();
		params.put("token", token);
        params.put("page", String.valueOf(page));
        params.put("pagenum", String.valueOf(num));

        String url = StringUtil.getRequestUrl(Constants.EMPLOYEE_ACCOUNT_LIST, params);
        Logger.d(TAG, url);
        
		StringRequest request = new StringRequest(url, new Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Logger.d(TAG, response);
                       
                        parseResult(response);
                        
                        Message message = new Message();
                        if(Constants.SUCCESS_CODE.equals(code)){
                            Bundle data = new Bundle();
                            data.putSerializable("accountInfoList", accountInfoList);
                        	data.putString("balance", balance);
                            message.setData(data);
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
            	code = jsonObject.optString("status");
            	
                if (!Constants.SUCCESS_CODE.equals(code)) {
                	msg = jsonObject.optString("msg");
                    return;
                }

                JSONObject dataObj = jsonObject.optJSONObject("data");

                JSONArray jsonArray = dataObj.optJSONArray("list");
               
	        	if (jsonArray != null && jsonArray.length() > 0) {
	                for (int i = 0; i <= jsonArray.length() - 1; i++) {
	                	JSONObject json = jsonArray.getJSONObject(i);

                        AccountInfo accountInfo = new AccountInfo();

                        accountInfo.setId(json.optString("id"));
                        accountInfo.setType(json.optString("type"));
                        accountInfo.setNumber(json.optString("number"));
                        accountInfo.setMoney(json.optString("money"));
                        accountInfo.setPay_type(json.optString("pay_type"));
                        accountInfo.setTradeno(json.optString("tradeno"));
                        accountInfo.setStatus(json.optString("status"));
                        accountInfo.setMember_id(json.optString("member_id"));
                        accountInfo.setCreate_time(json.optString("create_time"));
                        accountInfo.setReason(json.optString("reason"));

	                	accountInfoList.add(accountInfo);
	                }
	        	}

                balance = dataObj.optString("account_balance");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}

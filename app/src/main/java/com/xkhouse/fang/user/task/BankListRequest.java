package com.xkhouse.fang.user.task;

import android.os.Message;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.xkhouse.fang.app.callback.RequestListener;
import com.xkhouse.fang.app.config.Constants;
import com.xkhouse.fang.user.entity.XKBank;
import com.xkhouse.frame.activity.BaseApplication;
import com.xkhouse.frame.log.Logger;
import com.xkhouse.lib.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


/**
* @Description: 星空宝--申请提现--银行接口 
* @author wujian  
* @date 2015-10-30 下午4:35:08
 */
public class BankListRequest {

	private String TAG = "WalletListRequest";
	private RequestListener requestListener;
	
	private String code;	//返回状态
	private String msg;		//返回提示语
	
	private ArrayList<XKBank> bankList = new ArrayList<XKBank>();
	
	
	public BankListRequest(RequestListener requestListener) {
		this.requestListener = requestListener;
	}
	
	public void doRequest(){
		bankList.clear();
		
		StringRequest request = new StringRequest(Constants.XKB_BANK_LIST, new Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Logger.d(TAG, response);
                       
                        parseResult(response);
                        
                        Message message = new Message();
                        if(Constants.SUCCESS_CODE_OLD.equals(code)){
                        	message.obj = bankList;
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
                
                JSONObject dataObj = jsonObject.optJSONObject("data");
                JSONArray jsonArray = dataObj.optJSONArray("list");
               
	        	if (jsonArray != null && jsonArray.length() > 0) {
	                for (int i = 0; i <= jsonArray.length() - 1; i++) {
	                	JSONObject bankJson = jsonArray.getJSONObject(i);
	                	
	                	XKBank bank = new XKBank();
	                	bank.setBankIcon(bankJson.optString("imgUrl"));
	                	bank.setBankId(bankJson.optString("bId"));
	                	bank.setBankName(bankJson.optString("cn"));
	                	bankList.add(bank);
	                }
	        	}
	        	
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}

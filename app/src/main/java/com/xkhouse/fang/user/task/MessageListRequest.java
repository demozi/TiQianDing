package com.xkhouse.fang.user.task;

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
import com.xkhouse.fang.app.entity.BookedInfo;
import com.xkhouse.fang.user.entity.MessageInfo;
import com.xkhouse.frame.activity.BaseApplication;
import com.xkhouse.frame.log.Logger;
import com.xkhouse.lib.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
* @Description: 我的消息列表
* @author wujian  
* @date 2015-10-22 下午2:54:43
 */
public class MessageListRequest {

	private String TAG = MessageListRequest.class.getSimpleName();
    private RequestListener requestListener;

    private String token;  	//用户id
    private int num;		//条数
    private int page;		//页数

    private String code;	//返回状态
    private String msg;		//返回提示语
    private int count;

    private ArrayList<MessageInfo> messageList = new ArrayList<>();

    public MessageListRequest(String token, int page, int num, RequestListener requestListener) {

        this.token = token;
        this.num = num;
        this.page = page;

        this.requestListener = requestListener;
    }


    public void setData(String token, int page, int num) {
        this.token = token;
        this.num = num;
        this.page = page;
    }


    public void doRequest(){

        messageList.clear();

        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        params.put("pagenum", String.valueOf(num));
        params.put("page", String.valueOf(page));

        String url = StringUtil.getRequestUrl(Constants.USER_MESSAGE_LIST, params);
        Logger.d(TAG, url);

        StringRequest request = new StringRequest(url, new Listener<String>() {

            @Override
            public void onResponse(String response) {
                Logger.d(TAG, response);

                parseResult(response);

                Message message = new Message();
                if(Constants.SUCCESS_CODE.equals(code)){
                    Bundle data = new Bundle();
                    data.putSerializable("messageList", messageList);
                    message.setData(data);
                    message.arg1 = count;
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

                count = Integer.parseInt(dataObj.optString("count"));

                //新房
                JSONArray favList = dataObj.optJSONArray("list");
                if(favList != null && favList.length() > 0) {
                    for (int i = 0; i < favList.length(); i++) {
                        JSONObject messageJson = favList.getJSONObject(i);

                        MessageInfo messageInfo = new MessageInfo();
                        messageInfo.setId(messageJson.optString("id"));
                        messageInfo.setMember_id(messageJson.optString("member_id"));
                        messageInfo.setType(messageJson.optString("type"));
                        messageInfo.setCreate_time(messageJson.optString("create_time"));
                        messageInfo.setContent(messageJson.optString("content"));
                        messageInfo.setStatus(messageJson.optString("status"));

                        messageList.add(messageInfo);
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

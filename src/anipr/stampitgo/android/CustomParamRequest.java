package anipr.stampitgo.android;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;

public class CustomParamRequest extends StringRequest{
	
	String mCookie;
	Map<String, String> mParams = new HashMap<String, String>();
	String mBody;
	Priority priority = Priority.NORMAL;
	public CustomParamRequest(int method, String url,String cookie,Map<String, String> params,String body, Listener<String> listener,
			ErrorListener errorListener) {
		super(method, url, listener, errorListener);
		mCookie = cookie;
		if(params!=null){
			mParams.putAll(params);
		}
		mBody = body;
	}
	public CustomParamRequest(int method, String url,String cookie,Map<String, String> params,String body, Listener<String> listener,
			ErrorListener errorListener , Priority priority) {
		super(method, url, listener, errorListener);
		mCookie = cookie;
		if(params!=null){
			mParams.putAll(params);
		}	mBody = body;
		this.priority = priority;
		
	}
	
	@Override
	public com.android.volley.Request.Priority getPriority() {
		return priority;
	}
	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		
		Map<String, String> map = new HashMap<String, String>();
        map.put("Cookie", mCookie);
        return map;
	}
	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		Log.d("Added", mParams.size()+" params");
		return mParams;
	}
	@Override
	public byte[] getBody() throws AuthFailureError {
		if(mBody != null)
			return mBody.getBytes();
		else
			return super.getBody();
	}
	@Override
	public String getBodyContentType() {
		if(mParams.size()==0){
		return "application/json; charset=utf-8";
		}else{
			return "charset=utf-8";
		}
	}
	
}

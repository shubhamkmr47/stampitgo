package anipr.stampitgo.android;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

public class UpdateNearByStores  extends JsonObjectRequest {
	
	String mCookie;
	public UpdateNearByStores(int method, String url,String cookie, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, jsonRequest, listener, errorListener);
		mCookie = cookie;
	}
	
	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		Map<String, String> map = new HashMap<String, String>();
        map.put("Cookie", mCookie);
        return map;
		
	}
	
}

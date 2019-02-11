package com.example.kurtalang.youtube_mp3_ripper_frontend;

import android.util.Log;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class MetaRequest extends JsonObjectRequest {

    final String TAG = "MetaRequest.parseNetworkResponse";

    public MetaRequest(int method, String url, JSONObject jsonRequest, Response.Listener
            <JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
    }

    public MetaRequest(String url, JSONObject jsonRequest, Response.Listener<JSONObject>
            listener, Response.ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            Log.d(TAG,"Response is: " + response);
            Log.d(TAG,"Response is: " + response.headers);
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers, PROTOCOL_CHARSET));
            Log.d(TAG,"jsonString is: " + jsonString);

            JSONObject jsonResponse;
            if (jsonString.equals("")) {
                jsonResponse = new JSONObject();
            }
            else {
                jsonResponse = new JSONObject(jsonString);
            }

            Log.d(TAG,"jsonResponse is: " + jsonString);
            jsonResponse.put("headers", new JSONObject(response.headers));
            Log.d(TAG,"jsonResponse after put call is: " + jsonString);
            return Response.success(jsonResponse,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }
}

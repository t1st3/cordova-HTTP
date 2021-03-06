/**
 * A HTTP plugin for Cordova / Phonegap
 */
package com.synconset;

import java.net.UnknownHostException;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.SSLHandshakeException;

import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;
 
public class CordovaHttpPostBody extends CordovaHttp implements Runnable {
    public CordovaHttpPostBody(String urlString, String bodyParam, Map<String, String> headers, CallbackContext callbackContext) {
        super(urlString, bodyParam, headers, callbackContext);
    }
    
    @Override
    public void run() {
        try {
            HttpRequest request = HttpRequest.post(this.getUrlString());
            this.setupSecurity(request);
            request.acceptCharset(CHARSET);
            request.headers(this.getHeaders());
            request.send(this.getBodyParam());
            int code = request.code();
            JSONObject responseHeaders = new JSONObject();
            responseHeaders.put("Date", request.header("Date"));
            responseHeaders.put("Pragma", request.header("Pragma"));
            responseHeaders.put("Content-Type", request.header("Content-Type"));
            responseHeaders.put("Set-Cookie", request.header("Set-Cookie"));
            String body = request.body(CHARSET);
            JSONObject response = new JSONObject();
            response.put("status", code);
            response.put("headers", responseHeaders);
            if (code >= 200 && code < 300) {
                response.put("data", body);
                this.getCallbackContext().success(response);
            } else {
                response.put("error", body);
                this.getCallbackContext().error(response);
            }
        } catch (JSONException e) {
            this.respondWithError("There was an error generating the response");
        }  catch (HttpRequestException e) {
            if (e.getCause() instanceof UnknownHostException) {
                this.respondWithError(0, "The host could not be resolved");
            } else if (e.getCause() instanceof SSLHandshakeException) {
                this.respondWithError("SSL handshake failed");
            } else {
                this.respondWithError("There was an error with the request");
            }
        }
    }
}

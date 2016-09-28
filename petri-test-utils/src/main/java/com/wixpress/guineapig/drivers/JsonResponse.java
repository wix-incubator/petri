package com.wixpress.guineapig.drivers;

import net.sf.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class JsonResponse {
    ResponseEntity<String> response;

    private JSONObject jsonBody = null;

    public JsonResponse(String response) {
        this.response = ResponseEntity.ok().body(response);
    }

    public String getBodyRaw() {
        return response.getBody();
    }

    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }

    public HttpStatus getStatusCode() {
        return response.getStatusCode();
    }

    public String getContentTypeFromResponse() {
        return response.getHeaders().getContentType().toString();
    }

    public String getRedirectUrl() {
        return response.getHeaders().getLocation().toString();
    }

    public JSONObject getBodyJson() {
        if (jsonBody == null) {
            String body = getBodyRaw();
            jsonBody = JSONObject.fromObject(body);
        }
        return jsonBody;
    }

    public boolean getSuccess() {
        String value = getValue("result", "success", false);
        if (value == null)
            return true;
        else return Boolean.parseBoolean(value);
    }

    public int getErrorCode() {
        String value = getValue("result", "errorCode", false);
        if (value == null)
            return 0;
        else
            return Integer.parseInt(value);
    }

    public String getErrorDescription() {
        return getValue("result", "errorDescription", false);
    }


    private String getValue(String node, String attribute) {
        JSONObject body = getBodyJson();
        return body.getString(attribute);
    }

    private String getValue(String node, String attribute, boolean required) {
        JSONObject body = getBodyJson();
        if (!required && !body.containsKey(attribute))
            return null;
        return body.getString(attribute);
    }

    // payload level
    public String getPayload() {
        return getPayloadAsJson().toString();
    }

    public JSONObject getPayloadAsJson() {
        JSONObject body = getBodyJson();
        return body.getJSONObject("payload");
    }
}

package com.calix.tools.param;

import java.io.Serializable;

/**
 * Created by calix on 17-6-16.
 * 请求结果
 */
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 4152347612735015467L;

    private String requestId;

    private String errorMsg;

    private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}

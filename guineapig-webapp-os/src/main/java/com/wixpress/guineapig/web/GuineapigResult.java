package com.wixpress.guineapig.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "result")
@SuppressWarnings("unused")
public class GuineapigResult<P>
{
    @XmlAttribute
    private int errorCode = 0;

    @XmlAttribute
    private String errorDescription = "OK";

    @XmlAttribute
    private boolean success;

    @XmlAnyElement (lax = true)
    private P payload;

    /**
     * The HTTP status that accompanies this result
     */
    private transient HttpStatus httpStatus = HttpStatus.OK;

    public GuineapigResult()
    {
        this.success = true;
    }

    public GuineapigResult(P payload)
    {
        this.payload = payload;
        this.success = true;
    }

    public GuineapigResult(int errorCode, String errorDescription)
    {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
        this.success = false;
    }

    public int getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getErrorDescription()
    {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription)
    {
        this.errorDescription = errorDescription;
    }

    public P getPayload()
    {
        return payload;
    }

    public void setPayload(P payload)
    {
        this.payload = payload;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @JsonIgnore
    public HttpStatus getHttpStatus()
    {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus)
    {
        this.httpStatus = httpStatus;
    }

    @Override
    public String toString()
    {
        return "GuineapigResult{" +
                "errorCode=" + errorCode +
                ", errorDescription='" + errorDescription + '\'' +
                ", success=" + success +
                ", payload=" + payload +
                ", httpStatus=" + httpStatus +
                '}';
    }
}

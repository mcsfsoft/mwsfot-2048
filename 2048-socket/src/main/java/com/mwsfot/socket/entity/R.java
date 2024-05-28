package com.mwsfot.socket.entity;

import com.mwsfot.socket.constants.SubscribeConstants;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

/**
 * 响应信息主体
 *
 * @param <T>
 * @author lengleng
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private String msg;

    @Getter
    @Setter
    private T data;

    public static <T> R<T> ok() {
        return restResult(null, SubscribeConstants.SUCCESS, null);
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, SubscribeConstants.SUCCESS, null);
    }

    public static <T> R<T> ok(T data, String msg) {
        return restResult(data, SubscribeConstants.SUCCESS, msg);
    }

    public static <T> R<T> failed() {
        return restResult(null, SubscribeConstants.FAIL, null);
    }

    public static <T> R<T> failed(String msg) {
        return restResult(null, SubscribeConstants.FAIL, msg);
    }

    public static <T> R<T> failed(T data) {
        return restResult(data, SubscribeConstants.FAIL, null);
    }

    public static <T> R<T> failed(T data, String msg) {
        return restResult(data, SubscribeConstants.FAIL, msg);
    }

    public static <T> R<T> restResult(T data, int code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
    public static <T> R<T> restResult( int code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(null);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public static <T> R<T> parameterErrorResponse(List<ObjectError> allErrors) {
        StringBuilder message = new StringBuilder();
        for (ObjectError objectError : allErrors) {
            message.append(objectError.getDefaultMessage());
            message.append("[");
            String key = objectError instanceof FieldError ? ((FieldError) objectError).getField() : objectError.getObjectName();
            message.append(key);
            message.append("]");
            message.append("; ");
        }
        R<T> apiResult = new R<>();
        apiResult.setCode(SubscribeConstants.FAIL);
        apiResult.setMsg(message.toString());
        return apiResult;
    }
}

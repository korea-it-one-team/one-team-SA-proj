package com.lyj.proj.oneteamsaproj.vo;

import java.util.Map;
import com.lyj.proj.oneteamsaproj.Util.Ut;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResultData<DT> {

    private String ResultCode;
    private String msg;
    private DT data1;
    private String data1Name;
    private Object data2;
    private String data2Name;

    private Map<String, Object> body;

    public ResultData(String ResultCode, String msg, Object... args) {
        this.ResultCode = ResultCode;
        this.msg = msg;
        this.body = Ut.mapOf(args);
    }

    public static <DT> ResultData<DT> from(String ResultCode, String msg) {
        return from(ResultCode, msg, null, null);
    }

    public static <DT> ResultData<DT> from(String ResultCode, String msg, String data1Name, DT data1) {
        ResultData<DT> rd = new ResultData<>();
        rd.ResultCode = ResultCode;
        rd.msg = msg;
        rd.data1 = data1;
        rd.data1Name = data1Name;

        return rd;
    }

    public static <DT> ResultData<DT> from(String resultCode, String msg, String data1Name, DT data1, String data2Name, DT data2) {
        ResultData<DT> rd = new ResultData<>();
        rd.ResultCode = resultCode;
        rd.msg = msg;
        rd.data1Name = data1Name;
        rd.data1 = data1;
        rd.data2Name = data2Name;
        rd.data2 = data2;

        return rd;
    }

    public boolean isSuccess() {
        return ResultCode.startsWith("S-");
    }

    public boolean isFail() {
        return !isSuccess();
    }

    public static <DT> ResultData<DT> newData(ResultData rd, String dataName, DT newData) {
        return from(rd.getResultCode(), rd.getMsg(), dataName, newData);
    }

    public void setData2(String data2Name, Object data2) {
        this.data2 = data2;
        this.data2Name = data2Name;
    }
}

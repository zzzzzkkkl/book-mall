package org.example.bookmall.utils;

//通用工具类，统一接口的返回格式，让返回格式更规范，在浏览器看结果更清晰
public class ResultUtil {
    private Integer code; //成功失败的标志 200=成功，500=失败
    private String msg;//成功或失败的原因
    private Object data;//结果数据

    //成功，接受要返回的数据：登录成功的用户信息、搜索到的图书列表等
    //注意：
    //static静态方法，直接类名.方法名调用，不用再new对象
    public static ResultUtil success(Object data) {
        //新建一个结果包
        ResultUtil r = new ResultUtil();
        //给结果设置code，表示成功
        r.setCode(200);
        //给结果包说明：操作成功
        r.setMsg("操作成功");
        //把成功得到的数据放到结果包里
        r.setData(data);
        //把这个打包的结果返回出去
        return r;
    }

    //失败
    public static ResultUtil fail(String msg) {
        ResultUtil r = new ResultUtil();
        r.setCode(500);
        r.setMsg(msg);
        return r;
    }

    // Getter+Setter
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}

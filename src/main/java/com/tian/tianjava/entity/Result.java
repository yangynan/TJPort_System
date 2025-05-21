package com.tian.tianjava.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//统一响应结果
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer code;//业务状态码  0-成功  1-失败
    private String meg;//提示信息
    private Object data;//响应数据

    //增删改 成功响应
    public static Result success(){
        return new Result(1,"success",null);
    }
    //查询 成功响应
    public static Result success(Object data){
        return new Result(1,"success",data);
    }
    //失败响应
    public static Result error(String msg){
        return new Result(0,msg,null);
    }
//    //快速返回操作成功响应结果(带响应数据)
//    public static <E> Result<E> success(E data) {
//        return new Result<>(0, "操作成功", data);
//    }
////
//    //快速返回操作成功响应结果
//    public static Result success() {
//        return new Result(0, "操作成功", null);
//    }
//
//    public static Result error(String message) {
//        Result res =  new Result<>(1, message, null);
//        return  res;
//    }

}
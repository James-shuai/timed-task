package com.qujie.timedtask.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by WeiQinglin on 17/9/14.
 * gson工具类
 * 负责json数据格式转换
 */
public class GsonUtils {

    /**
     * object ->> String
     * @param bean
     * @return
     */
    public static String toJSONString(Object bean) {
        return getGson().toJson(bean);
    }

    /**
     * String ->> object
     * @param json
     * @param classType
     * @param <T>
     * @return
     */
    public static <T> T parseObject(String json,Type classType) {
        Gson gson = getGson();
        T res = (T) gson.fromJson(json, classType);
        return res;
    }

    public static <T> List<T> parseArray(String json, Type classType){
        List<T> newResult=GsonUtils.parseObject(json,new ArrayList<T>().getClass());
        List<T> result=new ArrayList<>();
        for (T item : newResult){
            String str=toJSONString(item);
            T obj=parseObject(str,classType);
            result.add(obj);
        }
        return result;
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .create();
    }
}

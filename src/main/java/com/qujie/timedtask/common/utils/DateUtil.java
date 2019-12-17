package com.qujie.timedtask.common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by WeiQinglin on 17/9/27.
 */
public class DateUtil {

    /**
     * 获取时间戳
     */
    public static long getTs(Date date){
        return date.getTime();
    }

    /**
     * 获取当前时间戳-->>毫秒
     * @return
     */
    public static long getDateTs(){
        Date date=new Date();
        return date.getTime();
    }

    /**
     * 获取时间对象
     * @return
     */
    public static Date getDate(){
        return new Date();
    }

    /**
     * 将TS转换为Date对象->>毫秒
     * @param ts
     * @return
     */
    public static Date createDate(long ts){
        return new Date(ts);
    }

    /**
     * 获取当前时间的字符串
     * @return
     */
    public static String getDateStr(String pattern){
        String str= (new SimpleDateFormat(pattern)).format(new Date());
        return str;
    }
    /**
     * 获取特定时间的字符串
     * @return
     */
    public static String getDateStr(Date date,String pattern){
        if (date==null)
        {
            return "";
        }
        String str= (new SimpleDateFormat(pattern)).format(date);
        return str;
    }

    /**
     * 字符串转时间
     * @param str yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Date strToDate(String str){
        Date date = new Date();
        //注意format的格式要与日期String的格式相匹配
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = sdf.parse(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 字符串转时间long
     * @param str
     * @return
     */
    public static long strToLong(String str){
        Date date = new Date();
        //注意format的格式要与日期String的格式相匹配
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            date = sdf.parse(str);
            System.out.println(date.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    /**
     * 时间字符串转换成整型
     * @param time
     * @return
     */
    public static Integer convertTimeToInt(String time){
        Integer intResult = 0;
        try{
            intResult = Integer.parseInt(time.replace(":",""));
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return intResult;
    }

    /**
     * 时间整型转成字符串
     * @param time
     * @return
     */
    public static String convertTimeToString(Integer time){
        String strResult = "";
        try {
            String temTime = String.format("%04d", time);
            if (!temTime.isEmpty()) {
                if (temTime.length() == 3) {
                    strResult = "0" + temTime.substring(0, 1) + ":" + temTime.substring(1, 3);
                } else if (temTime.length() == 4) {
                    strResult = temTime.substring(0, 2) + ":" + temTime.substring(2, 4);
                }
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
        }
        return strResult;
    }


    /**
     * 获取当前日期的星期
     * @param date
     * @return
     */
    public static String getWeek(Date date){
        //7:"星期日",1:"星期一",2:"星期二",3:"星期三",4:"星期四",5:"星期五",6"星期六"
        String[] weeks = {"7","1","2","3","4","5","6"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if(week_index<0){
            week_index = 0;
        }
        return weeks[week_index];
    }

    public static int getCurrentTime(){
        Date now=new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HHmm");
        String tablename=dateFormat.format(now);
        return Integer.parseInt(tablename);
    }

    /**
     * 当前系统时间加天数
     * @param days
     * @return
     */
    public static Date getDateByAddDays(int days){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH,days);
        return calendar.getTime();
    }

    /**
     * 根据时间戳获取时间日期
     * @param time
     * @return
     */
    public static String getDate(long time){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sd = sdf.format(new Date(time)); // 时间戳转换日期
        return sd;
    }

    /**
     * 获取过去第几天的日期
     *
     * @param past
     * @return
     */
    public static Date getPastDate(int past) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
        Date today = calendar.getTime();
        return today;
    }

    /**
     * 验证当前时间是否在指定区间
     * date1<date2
     * @return
     */
    public static boolean CheckPart(Date date1,Date date2){
        boolean result = true;
        Date now = new Date();
        if(now.getTime()<date1.getTime()){
            return false;
        }
        if(now.getTime()>date2.getTime()){
            return false;
        }
        return result;
    }

    /**
     * 在指定日期基础上加几天
     * @param date
     * @param day
     * @return
     */
    public static Date addDate(Date date,int day) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.DATE, day);
        Date retDate = ca.getTime();
        return retDate;
    }

    /**
     * 在指定日期基础上加几分钟
     * @param date
     * @param min
     * @return
     */
    public static Date addMin(Date date,int min) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.add(Calendar.MINUTE, min);
        Date retDate = ca.getTime();
        return retDate;
    }

}

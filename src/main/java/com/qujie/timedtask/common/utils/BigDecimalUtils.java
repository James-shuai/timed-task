package com.qujie.timedtask.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class BigDecimalUtils {

    /**
     *
     * @param v1
     * @param v2
     * @return
     */
    public static BigDecimal add(BigDecimal v1, BigDecimal v2) {

        BigDecimal n1 = new BigDecimal(v1.toString());
        BigDecimal n2 = new BigDecimal(v2.toString());

        return n1.add(n2);
    }

    /**
     * 小数点后为零则显示整数否则保留两位小数
     * @param d
     * @return
     */
    public static String formatDouble(double d) {
        BigDecimal bg = new BigDecimal(d).setScale(2, BigDecimal.ROUND_HALF_UP);
        double num = bg.doubleValue();
        if (Math.round(num) - num == 0) {
            return String.valueOf((long) num);
        }
        return String.valueOf(num);
    }

    /**
     * 小数点后为零则显示整数否则保留三位小数
     * @param d
     * @return
     */
    public static String formatDoublethree(double d) {
        BigDecimal bg = new BigDecimal(d).setScale(3, BigDecimal.ROUND_HALF_UP);
        double num = bg.doubleValue();
        if (Math.round(num) - num == 0) {
            return String.valueOf((long) num);
        }
        return String.valueOf(num);
    }

    public static double formatDouble(String dou){
        return new BigDecimal(Double.parseDouble(dou)).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static String withoutZero(Double d){
        NumberFormat nf = NumberFormat.getInstance();
        return nf.format(d);
    }



}

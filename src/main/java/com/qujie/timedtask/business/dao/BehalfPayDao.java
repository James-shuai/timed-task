package com.qujie.timedtask.business.dao;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 代付dao
 * @author ys
 * @date 2019/10/14 14:28
 */
@Component
@Mapper
public interface BehalfPayDao {

    /**
     * 获取没有代付的订单
     * @return
     */
    @Select("select st.id,st.shopid,isnull(st.newuserreduceamount+st.vipmemberreduceamount,0) vipmemberreduceamount,isnull(st.payamount,0) payamount,st.ordernum,st.txnTime,isnull(st.payfee,0) payfee,wsc.cardnum,wsc.bankacctname,wsc.bankCode,wsc.bankname,wss.merid,wsc.accounttype from smpay_traderecord st\n" +
            "inner join wm_shop_collectionaccount wsc on wsc.shopid=st.shopid\n" +
            "inner join wm_shop_shopMerIdRecord wss on wss.shopid=st.shopid\n" +
            "where st.paystatus=1 and st.ispaybehalf=0 and convert(varchar(100),st.paysuccesstime,120)>=convert(varchar(100),DATEADD(DAY,-1,GETDATE()),23)+' 22:40:00' and convert(varchar(100),st.paysuccesstime,120)<=convert(varchar(100),GETDATE(),23)+' 00:50:00'\n")
    List<Map<String,Object>> smpayTraderecordNoList();


    @Select("select st.id,st.shopid,isnull(st.vipmemberreduceamount,0) vipmemberreduceamount,st.ordernum,st.txnTime,isnull(st.newuserreduceamount,0) newuserreduceamount,wsc.cardnum,wsc.bankacctname,wsc.bankname,wss.merid,wsc.accounttype from smpay_traderecord st\n" +
            "inner join wm_shop_collectionaccount wsc on wsc.shopid=st.shopid\n" +
            "inner join wm_shop_shopMerIdRecord wss on wss.shopid=st.shopid\n" +
            "where st.paystatus=1 and st.czpaybehalfstatus=0 and convert(varchar(100),st.addtime,120)>=convert(varchar(100),DATEADD(DAY,-1,GETDATE()),23)+' 22:40:00' and convert(varchar(100),st.addtime,120)<=convert(varchar(100),GETDATE(),23)+' 00:55:00'\n")
    List<Map<String,Object>> RechargesmpayTraderecordNoList();


    @Update("update smpay_traderecord set czpaybehalfstatus=2 where id=#{id}")
    int updateSmpayTraderecord(@Param("id") String id);

    @Update("update smpay_traderecord set ispaybehalf=2 where id=#{id}")
    int updateSmpayTraderecordispaybehalf(@Param("id") String id);

    /**
     * 获取没有天行代发的订单
     * @return
     */
    @Select("select st.id,st.shopid,isnull(st.newuserreduceamount+st.vipmemberreduceamount,0) paymoney,st.ordernum,wsc.cardnum from smpay_traderecord st\n" +
            "inner join wm_shop_collectionaccount wsc on wsc.shopid=st.shopid\n" +
            "where st.paystatus=1 and st.thpaybehalfstatus=0 and convert(varchar(100),st.addtime,120)>=convert(varchar(100),DATEADD(DAY,-1,GETDATE()),23)+' 22:40:00' and convert(varchar(100),st.addtime,120)<=convert(varchar(100),GETDATE(),23)+' 00:20:00'")
    List<Map<String,Object>> smpayTraderecordNoPayList();

    /**
     * 更改状态
     */
    @Update("update smpay_traderecord set ispaybehalf=1,queryId=#{queryId},paybehalftime=#{date} where ordernum=#{ordernum}")
    int updateTraderecordIspaybehalf(@Param("ordernum") String ordernum, @Param("queryId") String queryId, @Param("date") String date);

    @Insert("insert into YSBehalfPayRecord(id,orderId,payment,msg,addtime,success,type,description) values(#{id},#{orderId},#{payment},#{msg},#{addtime},#{success},#{type},#{description})")
    int saveYSBehalfPayRecord(Map<String, Object> param);

    @Select("SELECT st.ordernum,st.payamount,st.vipmemberreduceamount,st.newuserreduceamount,wsc.cardnum,wsc.bankacctname,wsc.accounttype,st.payfee FROM smpay_traderecord st \n" +
            "LEFT JOIN wm_shop_collectionaccount wsc ON st.shopid=wsc.shopid\n" +
            "WHERE st.ordernum =#{orderNum}")
    Map<String,Object> informationList(@Param("orderNum") String orderNum);


    @Update("update smpay_traderecord set ispaybehalf=1,czpaybehalfstatus=3,paybehalftime=#{date} where ordernum=#{ordernum}")
    int updateTraderecordIspayYsbehalf(@Param("ordernum") String ordernum, @Param("date") String date);

    @Update("update smpay_traderecord set isrebate=1,ispaybehalf=1,czpaybehalfstatus=3,paybehalftime=#{date} where ordernum=#{ordernum}")
    int updateTraderecordIspayYsbehalfFL(@Param("ordernum") String ordernum, @Param("date") String date);

    @Update("update smpay_traderecord set czpaybehalfstatus=1,czpaybehalftime=#{date} where ordernum=#{ordernum}")
    int rechargeUpdateTraderecordIspayYsbehalf(@Param("ordernum") String ordernum, @Param("date") String date);

    @Select("select id from smpay_traderecord where ordernum=#{ordernum}")
    String orderNumByOrderId(@Param("ordernum") String ordernum);

    @Select("select ys.orderId,st.id from YSBehalfPayRecord ys\n" +
            "inner join smpay_traderecord st on ys.orderId=st.ordernum\n" +
            "where ys.success=1 and (convert(varchar(100),ys.addtime,23)=convert(varchar(100),DATEADD(DAY,-1,GETDATE()),23) or convert(varchar(100),ys.addtime,23)=convert(varchar(100),GETDATE(),23))")
    List<Map<String,Object>> orderNumList();

    @Update("update smpay_traderecord set thpaybehalfstatus=1 and queryId=#{queryId} where ordernum=#{ordernum}")
    int updateThpaybehalfstatus(@Param("ordernum") String ordernum, @Param("queryId") String queryId);

    @Select("select couponid from MarketingVouchersTemporary")
    List<String> MarketingVouchersTemporaryList();

    @Update("update smpay_shop_marketingactivity set activitystatus=7,isremovedthenextday=0 where id=#{id}")
    int updateActivitystatus(@Param("id") String id);

    @Delete("delete MarketingVouchersTemporary where couponid=#{couponid}")
    int deleteMarketingVouchersTemporary(@Param("couponid") String couponid);

    @Select("select count(1) from smpay_traderecord where ordernum=#{orderNum} and paystatus=1 and ispaybehalf=0")
    int getPayCount(@Param("orderNum") String orderNum);

    /**
     * 保存代付发送记录
     * @param param
     * @return
     */
    @Insert("insert into YSBehalfPaySendRecord(id,orderId,actualPayment,subsidyPayment,sumPayment,requestMsg,responseMsg,success,addtime,rebatePayment) values(#{id},#{orderId},#{actualPayment},#{subsidyPayment},#{sumPayment},#{requestMsg},#{responseMsg},#{success},#{addtime},#{rebatePayment})")
    int insertYSBehalfPaySendRecord(Map<String, Object> param);
}

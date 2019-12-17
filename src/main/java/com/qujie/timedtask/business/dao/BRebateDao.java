package com.qujie.timedtask.business.dao;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 返利金
 * @author ys
 * @date 2019/11/11 13:02
 */
@Component
@Mapper
public interface BRebateDao {

    @Select("select CONVERT(varchar(100),paysuccesstime,112) date from smpay_traderecord where paystatus=1 and ispaybehalf=0 and ordernum=#{ordernum}")
    String queryOrder(@Param("ordernum") String ordernum);

    @Select("select shopid,isnull(payamount+newuserreduceamount+vipmemberreduceamount) money from smpay_traderecord where paystatus=1 and ispaybehalf=1 and ordernum=#{ordernum}")
    Map<String,Object> selectShopid(@Param("ordernum") String ordernum);

    @Select("select isnull(payamount+newuserreduceamount+vipmemberreduceamount,0) money from smpay_traderecord where ordernum=#{ordernum}")
    String getOrderMoney(@Param("ordernum") String ordernum);

    @Select("select ID from T_Shop where DelStatus=1 and ShopType in (SELECT shoptype from servicefee_rebate_configurationactivity_type) and SMTypeID in (SELECT smallshoptype from servicefee_rebate_configurationactivity_type)")
    List<String> getShopId();

    /**
     * 获取首页金额
     * @param shopid
     * @return
     */
    @Select("select CONVERT(VARCHAR(100),CONVERT(FLOAT,isnull(money,0))) money from rebateTransactionsTotalSum where shopid=#{shopid}")
    String getHomeMoney(@Param("shopid") String shopid);

    @Select("select src.startdate from activity at\n" +
            "left join servicefee_rebate_configurationactivity src on at.id=src.activityid\n" +
            "where at.avtivitytype=#{avtivitytype} and src.activitytype=0 and src.enddate>GETDATE()")
    String getDate(@Param("avtivitytype") String avtivitytype);

    @Select("select paysuccesstime from smpay_traderecord where ordernum=#{ordernum}")
    String smpayDate(@Param("ordernum") String ordernum);

    /**
     * 获取首页列表
     * @param param
     * @return
     */
    @Select("<script>"+"SELECT * FROM (\n" +
            "SELECT ROW_NUMBER() OVER(ORDER BY addtime desc) num,id,shopid,rebateType,\n" +
            "CASE WHEN rebateType<![CDATA[<>]]>3 THEN 1 ELSE 0 END plusorminus,CONVERT(VARCHAR(100),CONVERT(FLOAT,isnull(money,0))) money,rebateTypeDescribe,\n" +
            "CASE WHEN rebateType<![CDATA[<>]]>3 THEN CONVERT(VARCHAR(100), addtime, 102)+'  '+LEFT(CONVERT(VARCHAR(100),addtime,8),5)+'存入' ELSE CONVERT(VARCHAR(100), addtime, 102)+'  '+LEFT(CONVERT(VARCHAR(100),addtime,8),5)+'提现' END date\n" +
            " from rebateTransactions where shopid=#{shopid} and money>0\n" +
            ") t WHERE t.num<![CDATA[>]]>#{startNum} AND t.num<![CDATA[<=]]>#{endNum}"+"</script>")
    List<Map<String,Object>> homeList(Map<String, Object> param);

    /**
     * 提现详情
     * @param shopid
     * @return
     */
    @Select("SELECT '天津银行（'+RIGHT(wsc.cardnum,4)+')' cardnum,CONVERT(VARCHAR(100),CONVERT(FLOAT,isnull(rs.money,0))) money FROM rebateTransactionsTotalSum rs\n" +
            "LEFT JOIN wm_shop_collectionaccount wsc ON rs.shopid=wsc.shopid\n" +
            "WHERE rs.shopid=#{shopid}")
    Map<String,Object> detailsOfPresentation(@Param("shopid") String shopid);

    /**
     * 获取当日提现次数
     * @param shopid
     * @return
     */
    @Select("SELECT COUNT(1) FROM rebateTransactions WHERE rebateType=3 AND shopid=#{shopid} AND CONVERT(varchar(100),addtime,23)=CONVERT(varchar(100),GETDATE(), 23)")
    int getDateNum(@Param("shopid") String shopid);

    @Select("select CONVERT(VARCHAR(100),CONVERT(FLOAT,isnull(money,0))) money from rebateTransactionsTotalSum where shopid=#{shopid}")
    String getMoney(@Param("shopid") String shopid);

    //获取银行预留手机号
    @Select("SELECT bankcardphone from wm_shop_collectionaccount where shopid=#{shopId}")
    String getBankcardphone(@Param("shopId") String shopId);

    /**
     * 获取账户信息
     */
    @Select("SELECT rs.money,wsc.cardnum,wsc.bankacctname,wsc.accounttype FROM rebateTransactionsTotalSum rs \n" +
            "LEFT JOIN wm_shop_collectionaccount wsc ON rs.shopid=wsc.shopid\n" +
            "WHERE rs.shopid=#{shopid}")
    Map<String,Object> getCollectionaccount(@Param("shopid") String shopid);

    /**
     * 插入返利金
     * @param param
     * @return
     */
    @Insert("INSERT INTO rebateTransactions(id,shopid,money,rebateType,rebateTypeDescribe,transactionNum,cashnum,cashWithdrawalState,addtime,isread) VALUES(#{id},#{shopid},#{money},3,'返利金提现',#{transactionNum},#{cashnum},0,GETDATE(),1)")
    int insertrebateTransactions(Map<String, Object> param);

    /**
     * 修改奖励金金额
     * @param shopid
     * @param money
     * @return
     */
    @Update("update rebateTransactionsTotalSum set money=money-#{money},updatetime=getdate() where shopid=#{shopid}")
    int updaterebateTransactionsTotalSum(@Param("shopid") String shopid, @Param("money") String money);

    /**
     * 修改提现状态为提现成功
     * @param transactionNum
     * @return
     */
    @Update("update rebateTransactions set cashWithdrawalState=1 where transactionNum=#{transactionNum}")
    int updaterebateTransactions(@Param("transactionNum") String transactionNum);

    /**
     * 通过交易流水查询订单是否存在
     * @param transactionNum
     * @return
     */
    @Select("select count(1) from rebateTransactions where transactionNum=#{transactionNum}")
    int selectrebateTransactions(@Param("transactionNum") String transactionNum);

    /**
     * 获取提现成功页面信息
     * @param id
     * @return
     */
    @Select("SELECT  '天津银行（'+RIGHT(cashnum,4)+')' cardnum,CONVERT(VARCHAR(100), addtime, 102)+' '+LEFT(CONVERT(VARCHAR(100),addtime,8),5) date,CONVERT(VARCHAR(100),CONVERT(FLOAT,isnull(money,0))) money FROM rebateTransactions WHERE rebateType=3 AND id=#{id}")
    Map<String,Object> getCollectionaccounts(@Param("id") String id);

    /**
     * 获取服务返利详情
     * @param rebateTransactionsId
     * @return
     */
    @Select("SELECT CONVERT(varchar(100),TransactionTime, 102) TransactionTime,CONVERT(VARCHAR(100),CONVERT(FLOAT,ISNULL(SUM(paymoney),0))) paymoney,\n" +
            "CONVERT(VARCHAR(100),CONVERT(FLOAT,ISNULL(SUM(serviceFee),0))) serviceFee,CONVERT(VARCHAR(100),CONVERT(FLOAT,ISNULL(SUM(serviceFeeRebate),0))) serviceFeeRebate\n" +
            " FROM serviceFeerebateTransactions\n" +
            " WHERE rebateTransactionsId=#{rebateTransactionsId} and isDeposit=1\n" +
            "GROUP BY CONVERT(varchar(100),TransactionTime, 102)")
    Map<String,Object> getserviceFeerebateTransactions(@Param("rebateTransactionsId") String rebateTransactionsId);

    @Select("SELECT CONVERT(VARCHAR(100), addtime, 102)+'  '+LEFT(CONVERT(VARCHAR(100), addtime,8),5)+'存入' date,\n" +
            "CONVERT(VARCHAR(100),CONVERT(FLOAT,isnull(money,0))) money,rebateTypeDescribe\n" +
            " FROM rebateTransactions WHERE id=#{rebateTransactionsId}")
    Map<String,Object> getrebateTransactions(@Param("rebateTransactionsId") String rebateTransactionsId);

    /**
     * 获取费率
     * @return
     */
    @Select("SELECT Value FROM dbo.SysConfig WHERE [Key]='smpayfee'")
    String getSysConfig();
    @Select("SELECT Value FROM dbo.SysConfig WHERE [Key]='serviceFeeRebateAmount'")
    String getServiceFeeRebateAmount();

    /**
     * 获取手续费
     * @param avtivitytype
     * @return
     */
    @Select("select isnull(rebatescale,0) rebatescale from activity at\n" +
            "left join servicefee_rebate_configurationactivity src on src.activityid=at.id\n" +
            "where at.avtivitytype=1")
    String getrebatescale(@Param("avtivitytype") String avtivitytype);

    /**
     * 获取服务费返利
     * @return
     */
    @Select("select shopid,ISNULL(SUM(serviceFeeRebate),0) serviceFeeRebate from serviceFeerebateTransactions WHERE CONVERT(VARCHAR(100),TransactionTime,23) = CONVERT(VARCHAR(100),DATEADD(dd,-1,GETDATE()),23) AND isDeposit<>1  GROUP BY shopid\n")
    List<Map<String,Object>> getServiceFee();


    @Select("select shopid,ISNULL(serviceFeeRebate,0) serviceFeeRebate from serviceFeerebateTransactions WHERE id=#{id}\n")
    Map<String,Object> getShopServiceFee(@Param("id") String id);

    @Select("SELECT Value FROM dbo.SysConfig WHERE [Key]='smpayupperlimit'")
    String getSmpayupperlimit();

    /**
     * 获取本月所有返利金和
     * @return
     */
    @Select("select isnull(sum(money),0) money from rebateTransactions where rebateType=1 AND YEAR(addtime)=YEAR(GETDATE()) AND MONTH(addtime) =MONTH(GETDATE())")
    String getSameMonthMoney();

    @Select("SELECT Value FROM dbo.SysConfig WHERE [Key]='smpaycashmoney'")
    String getSmpaycashmoney();

    /**
     * 根据shopid获取商户收款信息
     * @param shopid
     * @return
     */
    @Select("select * from wm_shop_collectionaccount where shopid=#{shopid}")
    Map<String,Object> getAccount(@Param("shopid") String shopid);

    @Insert("insert into rebateTransactions(id,shopid,money,rebateType,rebateTypeDescribe,addtime,isread) values(#{id},#{shopid},#{money},#{rebateType},#{rebateTypeDescribe},#{addtime},0)")
    int insertRebateTransactions(Map<String, Object> param);

    /**
     * 修改服务费返利表
     * @param rebateTransactionsId
     * @param shopid
     * @return
     */
    @Update(" UPDATE serviceFeerebateTransactions SET isDeposit=1,rebateTransactionsId=#{rebateTransactionsId} WHERE shopid=#{shopid} and CONVERT(VARCHAR(100),TransactionTime,23) = CONVERT(VARCHAR(100),DATEADD(dd,-1,GETDATE()),23) AND isDeposit<>1")
    int updateServiceFeerebateTransactions(@Param("rebateTransactionsId") String rebateTransactionsId, @Param("shopid") String shopid);

    /**
     * 获取需要代付的返利金订单信息
     * @return
     */
    @Select("SELECT rt.money,rt.transactionNum,wsc.cardnum,wsc.bankCode,wsc.bankacctname,wsc.accounttype FROM rebateTransactions rt\n" +
            " LEFT JOIN wm_shop_collectionaccount wsc ON wsc.shopid=rt.shopid\n" +
            " WHERE rt.cashWithdrawalState=0\n" +
            " AND convert(varchar(100),rt.addtime,120)>=convert(varchar(100),DATEADD(DAY,-1,GETDATE()),23)+' 22:40:00' \n" +
            " AND convert(varchar(100),rt.addtime,120)<=convert(varchar(100),GETDATE(),23)+' 00:45:00'")
    List<Map<String,Object>> getBehalfPay();

    @Delete("DELETE rebateTransactions WHERE rebateType=3 AND shopid=#{shopid}")
    int delete(@Param("shopid") String shopid);

    @Insert("insert into rebateTransactionsTotalSum(id,shopid,money,addtime) values(NEWID(),#{shopid},#{money},getdate())")
    int insertRebateTransactionsTotalSum(@Param("shopid") String shopid, @Param("money") String money);

    @Update("update rebateTransactionsTotalSum set money=money+#{money} where shopid=#{shopid}")
    int updateRebateTransactionsTotalSum(@Param("shopid") String shopid, @Param("money") String money);

    @Select("select count(1) from rebateTransactionsTotalSum where shopid=#{shopid}")
    int countRebateTransactionsTotalSum(@Param("shopid") String shopid);

    /**
     * 获取兑现时间
     * @return
     */
    @Select("SELECT CONVERT(VARCHAR(100),src.rebatecashtime,120) rebatecashtime,src.id FROM activity at\n" +
            "INNER JOIN servicefee_rebate_configurationactivity src ON at.id=src.activityid\n" +
            "WHERE at.avtivitytype=1")
    Map<String,Object> getRebatecashtime();

    /**
     * 查询活动是否停止
     * @param id
     * @return
     */
    @Select("select count(1) from servicefee_rebate_configurationactivity where id=#{id} and activitytype=1")
    int getActivitytype(@Param("id") String id);


    /**
     * 查询是否停止定时任务
     * @param id
     * @return
     */
    @Select("select count(1) from servicefee_rebate_configurationactivity where id=#{id} and isstop=1")
    int getIsStop(@Param("id") String id);

    /**
     * 停止定时任务
     * @param id
     * @return
     */
    @Update("update servicefee_rebate_configurationactivity set isstop=1 where id=#{id}")
    int updateIsStopone(@Param("id") String id);


    /**
     * 获取返利月上限额度
     * @param avtivitytype
     * @return
     */
    @Select("SELECT isnull(src.rebatemonthup,0) rebatemonthup,src.id,isnull(src.rebateup,0) rebateup FROM activity at\n" +
            "INNER JOIN servicefee_rebate_configurationactivity src ON at.id=src.activityid\n" +
            "WHERE at.avtivitytype=#{avtivitytype}")
    Map<String,Object> getRebatemonthup(@Param("avtivitytype") String avtivitytype);


    /**
     * 获取一二级行业及月流水上限金额
     * @param servicefeeid
     * @return
     */
    @Select("select isnull(rebatemonthup,0) rebatemonthup,shoptype,smallshoptype,id,servicefeeid from servicefee_rebate_configurationactivity_type where servicefeeid=#{servicefeeid} and shoptype=#{shoptype} and smallshoptype=#{smallshoptype}")
    Map<String,Object> getShopTypeList(@Param("servicefeeid") String servicefeeid, @Param("shoptype") String shoptype, @Param("smallshoptype") String smallshoptype);

    /**
     * 根据id获取商户一二级行业
     * @param shopid
     * @return
     */
    @Select("select ID,ShopType,SMTypeID from T_Shop where ID=#{shopid}")
    Map<String,Object> getShopType(@Param("shopid") String shopid);


    @Select("select st.shopid,ts.ShopType,ts.SMTypeID,ts.CityCode,ts.DistrictCode,ts.BusinessCircleID,CONVERT(VARCHAR(100),st.addtime,120) ztime from smpay_traderecord st\n" +
            "left join T_Shop ts on ts.ID=st.shopid\n" +
            "where ordernum=#{ordernum}")
    Map<String,Object> getShopTypeSMTypeID(@Param("ordernum") String ordernum);

    @Select("select src.id from activity at\n" +
            "left join servicefee_rebate_configurationactivity src on src.activityid=at.id\n" +
            "where at.avtivitytype=#{avtivitytype}")
    String getservicefeeid(@Param("avtivitytype") String avtivitytype);

    /**
     * 通过一二级行业查询是否存在
     * @param shoptype
     * @param smallshoptype
     * @param servicefeeid
     * @return
     */
    @Select("select count(1) from servicefee_rebate_configurationactivity_type where shoptype=#{shoptype} and smallshoptype=#{smallshoptype} and servicefeeid=#{servicefeeid}")
    int isCount(@Param("shoptype") String shoptype,@Param("smallshoptype") String smallshoptype,@Param("servicefeeid") String servicefeeid);

    /**
     * 判断活动是否存在上限金额
     * @return
     */
    @Select("select count(1) from servicefee_rebate_configurationactivity where isup=1")
    int getservicefee_rebate_configurationactivityCount();

    /**
     * 获取商户当月流水交易总金额
     * @param shopid
     * @return
     */
    @Select("SELECT ISNULL(SUM(payamount),0) payamount FROM smpay_traderecord WHERE YEAR(addtime)=YEAR(GETDATE()) AND MONTH(addtime) =MONTH(GETDATE()) and isrebate=1 AND shopid=#{shopid} and paystatus=1")
    String getShopMoney(@Param("shopid") String shopid);

    /**
     * 获取所有商户返利金额
     * @return
     */
    @Select("SELECT ISNULL(SUM(serviceFeeRebate),0) serviceFeeRebate FROM serviceFeerebateTransactions WHERE isDeposit<>0")
    String getAllMoney();

    /**
     * 停止活动
     * @param id
     * @return
     */
    @Update("update servicefee_rebate_configurationactivity set activitytype=1 where id=#{id}")
    int updateActivitytype(@Param("id") String id);

    /**
     * 获取前一日扫码支付订单金额
     * @return
     */
    @Select("SELECT st.id,st.shopid,ISNULL(st.payamount,0) payamount,CONVERT(VARCHAR(100),st.addtime,120) ztime,ts.ShopType,ts.SMTypeID,ts.CityCode,ts.DistrictCode,ts.BusinessCircleID FROM dbo.smpay_traderecord st\n" +
            "LEFT JOIN dbo.T_Shop ts ON st.shopid=ts.ID\n" +
            " WHERE st.paystatus=1 AND CONVERT(VARCHAR(100),st.addtime,23) = CONVERT(VARCHAR(100),DATEADD(DAY,-1,GETDATE()),23)\n" +
            " AND ts.ShopType IN (\n" +
            " SELECT shoptype FROM activity at\n" +
            " LEFT JOIN servicefee_rebate_configurationactivity src ON at.id=src.activityid\n" +
            " LEFT JOIN servicefee_rebate_configurationactivity_type srct ON src.id=srct.servicefeeid\n" +
            " WHERE at.avtivitytype=#{avtivitytype}\n" +
            " )\n" +
            " AND ts.SMTypeID IN (\n" +
            "  SELECT smallshoptype FROM activity at\n" +
            " LEFT JOIN servicefee_rebate_configurationactivity src ON at.id=src.activityid\n" +
            " LEFT JOIN servicefee_rebate_configurationactivity_type srct ON src.id=srct.servicefeeid\n" +
            " WHERE at.avtivitytype=#{avtivitytype}\n" +
            " ) and st.isrebate=0 order by st.addtime asc")
    List<Map<String,Object>> getSmpayPayMoney(@Param("avtivitytype") String avtivitytype);


    /**
     * 判断单个商户是否支持返利
     * @param shopid
     * @return
     */
    @Select("select count(1) from servicefee_rebate_shop_exclude where shopid=#{shopid}")
    int getServicefee_rebate_shop_exclude(@Param("shopid") String shopid);

    /**
     * 判断免支付手续费返利活动配置表 是否存在数据
     * @return
     */
    @Select("SELECT COUNT(1) FROM activity ai\n" +
            "INNER JOIN servicefee_rebate_configurationactivity src ON src.activityid=ai.id\n" +
            "WHERE ai.avtivitytype=1 AND src.startdate<=GETDATE() AND src.enddate>GETDATE() AND src.activitytype=0")
    int getCountservicefee_rebate_configurationactivity();


    /**
     * 获取城市区域商圈
     * @param avtivitytype
     * @return
     */
    @Select("SELECT citycode,districtcode,businesscircleid FROM activity at\n" +
            " LEFT JOIN servicefee_rebate_configurationactivity src ON at.id=src.activityid\n" +
            " LEFT JOIN servicefee_rebate_configurationactivity_city srcc ON src.id=srcc.servicefeeid\n" +
            " WHERE at.avtivitytype=#{avtivitytype}")
    List<Map<String,Object>> getservicefee_rebate_configurationactivity_city(@Param("avtivitytype") String avtivitytype);


    @Insert("INSERT INTO serviceFeerebateTransactions(id,shopid,TransactionTime,paymoney,serviceFee,serviceFeeRebate,addtime,isDeposit) VALUES(#{id},#{shopid},#{TransactionTime},#{paymoney},#{serviceFee},#{serviceFeeRebate},GETDATE(),0)")
    int insertserviceFeerebateTransactions(Map<String, Object> param);

    @Update("update serviceFeerebateTransactions set isDeposit=1,ordernum=#{ordernum} where id=#{id}")
    int updateserviceFeerebateTransactions(@Param("id") String id,@Param("ordernum") String ordernum);

    @Insert("INSERT INTO serviceFeerebateTransactions(id,shopid,TransactionTime,paymoney,serviceFee,serviceFeeRebate,addtime,isDeposit) VALUES(#{id},#{shopid},#{TransactionTime},#{paymoney},#{serviceFee},#{serviceFeeRebate},GETDATE(),2)")
    int insertserviceFeerebateTransactionsIsTwo(Map<String, Object> param);

    /**
     * 修改扫码记录表为返利
     * @param id
     * @return
     */
    @Update("update smpay_traderecord set isrebate=1 where id=#{id}")
    int updatesmpay_traderecord(@Param("id") String id);

    /**
     * 修改扫码记录表为返利
     * @param id
     * @return
     */
    @Update("update smpay_traderecord set isrebate=1 where ordernum=#{ordernum}")
    int updatesmpay_traderecords(@Param("ordernum") String ordernum);


}

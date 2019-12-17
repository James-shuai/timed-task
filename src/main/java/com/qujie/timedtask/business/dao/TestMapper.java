package com.qujie.timedtask.business.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author ys
 * @date 2019/12/13 15:33
 */
@Component
@Mapper
public interface TestMapper {

    @Select("select ts.ID,ts.ShopType,ts.SMTypeID,st.* from smpay_traderecord st \n" +
            "left join T_Shop ts on ts.ID=st.shopid\n" +
            "where convert(varchar(100),st.paysuccesstime,23)='2019-12-13' and st.paystatus=1 and st.ispaybehalf=1 and ts.DelStatus=1 and st.isrebate=1 order by st.paysuccesstime desc")
    List<Map<String,Object>> Fselect();

    @Select("select ts.ID,ts.ShopType,ts.SMTypeID,st.* from smpay_traderecord st \n" +
            "left join T_Shop ts on ts.ID=st.shopid\n" +
            "where convert(varchar(100),st.paysuccesstime,23)='2019-12-13' and st.paystatus=1 and st.ispaybehalf=1 and ts.DelStatus=1 and st.isrebate=0 order by st.paysuccesstime desc")
    List<Map<String,Object>> Wselect();

    @Select("select payment from YSBehalfPayRecord where success=1 and convert(varchar(100),addtime,23)='2019-12-13' and orderid=#{ordernum}")
    String selectYS(@Param("ordernum") String ordernum);

    @Select("select count(1) from servicefee_rebate_configurationactivity_type where shoptype=#{shoptype} and smallshoptype=#{smallshoptype}")
    int count(@Param("shoptype") String shoptype,@Param("smallshoptype") String smallshoptype);

}

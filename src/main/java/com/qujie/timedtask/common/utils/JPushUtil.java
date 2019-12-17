package com.qujie.timedtask.common.utils;

import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.notification.AndroidNotification;
import cn.jpush.api.push.model.notification.IosNotification;
import cn.jpush.api.push.model.notification.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JPushUtil {
    public static Logger log = LoggerFactory.getLogger(JPushUtil.class);


    private static Boolean ApnsProduction =true;


    /**
     * 推送给设备标识参数的用户
     * @param aliasList 别名或别名组
     * @param msg_content 消息内容
     * @param extrasparam 扩展字段
     * @return 0推送失败，1推送成功
     */
    public static int sendToAliasList(JPushClient jPushClient, List<String> aliasList, String msg_content, String extraKey, String extrasparam, String notification_title )  {
        if(ApnsProduction)log.debug("这是生产环境");


        int result = 0;

        try {
            PushPayload pushPayload= buildPushObject_all_aliasList_alertWithTitle(aliasList,msg_content,extraKey,extrasparam,notification_title);
            log.info("推送给设备标识参数的用户"+pushPayload);
            PushResult pushResult = jPushClient.sendPush(pushPayload);
            log.info("推送结果"+pushResult);
            if(pushResult.getResponseCode()==200){
                result=1;
            }
        } catch (APIConnectionException e) {
            log.error("极光推送给设备标识参数的用户方法异常："+e.getMessage());
        } catch (APIRequestException e) {
            log.error("极光推送给设备标识参数的用户方法异常："+e.getMessage());
        }
        return result;
    }

    /**
     * 发送给所有用户
     * @param msg_content 消息内容
     * @param extrasparam 扩展字段
     * @return 0推送失败，1推送成功
     */
    public static int sendToAll(JPushClient jPushClient,String msg_content,String extraKey, String extrasparam,String notification_title) {
        int result = 0;
        try {
            PushPayload pushPayload= buildPushObject_android_and_ios(msg_content,extraKey,extrasparam,notification_title);
            log.info("推送参数"+pushPayload);
            PushResult pushResult=jPushClient.sendPush(pushPayload);
            log.info("推送结果"+pushResult);
            if(pushResult.getResponseCode()==200){
                result=1;
            }
        } catch (Exception e) {

            log.error("极光发送给所有用户异常："+e.getMessage());
        }

        return result;
    }

    /**
     * 推送给Tag参数的用户
     * @param tagsList Tag或Tag组
     * @param msg_content 消息内容
     * @param extrasparam 扩展字段
     * @return 0推送失败，1推送成功
     */
    public static int sendToTagList(JPushClient jPushClient,List<String> tagsList, String msg_content,String extra, String extrasparam,String notification_title) {
        int result = 0;
        try {
            PushPayload pushPayload= buildPushObject_all_tagList_alertWithTitle(tagsList,msg_content,extra,extrasparam,notification_title);
            log.info("极光推送参数"+pushPayload);
            PushResult pushResult=jPushClient.sendPush(pushPayload);
            log.info("极光推送结果："+pushResult);
            if(pushResult.getResponseCode()==200){
                result=1;
            }
        } catch (APIConnectionException e) {
            log.error("极光推送给Tag参数的用户异常："+e.getMessage());

        } catch (APIRequestException e) {
            log.error("极光推送给Tag参数的用户异常："+e.getMessage());
        }

        return result;
    }

    /**
     * 发送给所有安卓用户
     * @param notification_title 通知内容标题
     * @param msg_content 消息内容
     * @param extrasparam 扩展字段
     * @return 0推送失败，1推送成功
     */
    public static int sendToAllAndroid(JPushClient jPushClient, String notification_title,  String msg_content,String extraKey, String extrasparam) {
        int result = 0;
        try {
            PushPayload pushPayload= buildPushObject_android_all_alertWithTitle(notification_title,msg_content,extraKey,extrasparam);
            log.info("推送参数："+pushPayload);
            PushResult pushResult=jPushClient.sendPush(pushPayload);
            log.info("推送结果："+pushResult);
            if(pushResult.getResponseCode()==200){
                result=1;
            }
        } catch (Exception e) {

            log.error("极光发送给所有安卓用户异常："+e.getMessage());
        }

        return result;
    }

    /**
     * 发送给所有IOS用户
     * @param msg_content 消息内容
     * @param extrasparam 扩展字段
     * @return 0推送失败，1推送成功
     */
    public static int sendToAllIos(JPushClient jPushClient, String msg_content,String extraKey, String extrasparam) {
        int result = 0;
        try {
            PushPayload pushPayload= buildPushObject_ios_all_alertWithTitle(msg_content,extraKey,extrasparam);
            log.info("推送参数："+pushPayload);
            PushResult pushResult=jPushClient.sendPush(pushPayload);
            log.info("推送结果："+pushResult);
            if(pushResult.getResponseCode()==200){
                result=1;
            }
        } catch (Exception e) {

            log.error("极光发送给所有IOS用户异常："+e.getMessage());
        }

        return result;
    }




    /**
     * 向所有平台单个或多个指定别名用户推送消息
     * @param aliasList
     * @param msg_content
     * @param extrasparam
     * @return
     */
    public static PushPayload buildPushObject_all_aliasList_alertWithTitle(List<String> aliasList, String msg_content, String extraKey, String extrasparam, String notification_title) {

        log.info("----------向所有平台单个或多个指定别名用户推送消息中......");
        log.debug("当前推送环境"+ApnsProduction);
        //创建一个IosAlert对象，可指定APNs的alert、title等字段
        //IosAlert iosAlert =  IosAlert.newBuilder().setTitleAndBody("title", "alert body").build();
        String sound="";
        String[] temextra = extrasparam.split("#");
        return PushPayload.newBuilder()
                //指定要推送的平台，all代表当前应用配置了的所有平台，也可以传android等具体平台
                .setPlatform(Platform.all())
                //指定推送的接收对象，all代表所有人，也可以指定已经设置成功的tag或alias或该应应用客户端调用接口获取到的registration id
                .setAudience(Audience.alias(aliasList))
                //.setAudience(Audience.tag(aliasList))
                //jpush的通知，android的由jpush直接下发，iOS的由apns服务器下发，Winphone的由mpns下发
                .setNotification(Notification.newBuilder()
                        //指定当前推送的android通知
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(msg_content)
                                .setTitle(notification_title)
                                //此字段为透传字段，不会显示在通知栏。用户可以通过此字段来做一些定制需求，如特定的key传要指定跳转的页面（value）
                                .addExtra(extraKey,extrasparam)
                                .build())
                        //指定当前推送的iOS通知
                        .addPlatformNotification(IosNotification.newBuilder()
                                //传一个IosAlert对象，指定apns title、title、subtitle等
                                .setAlert(msg_content)
                                //直接传alert
                                //此项是指定此推送的badge自动加1
                                .incrBadge(1)
                                //此字段的值default表示系统默认声音；传sound.caf表示此推送以项目里面打包的sound.caf声音来提醒，
                                // 如果系统没有此音频则以系统默认声音提醒；此字段如果传空字符串，iOS9及以上的系统是无声音提醒，以下的系统是默认声音
                                .setSound(sound)
                                //此字段为透传字段，不会显示在通知栏。用户可以通过此字段来做一些定制需求，如特定的key传要指定跳转的页面（value）
                                .addExtra(extraKey,extrasparam)
                                //此项说明此推送是一个background推送，想了解background看：http://docs.jpush.io/client/ios_tutorials/#ios-7-background-remote-notification
                                //取消此注释，消息推送时ios将无法在锁屏情况接收
                                .setContentAvailable(true)
                                .setMutableContent(true)

                                .build())

                        .build())
                //Platform指定了哪些平台就会像指定平台中符合推送条件的设备进行推送。 jpush的自定义消息，
                // sdk默认不做任何处理，不会有通知提示。建议看文档http://docs.jpush.io/guideline/faq/的
                // [通知与自定义消息有什么区别？]了解通知和自定义消息的区别
                .setMessage(Message.newBuilder()
                .setMsgContent(msg_content)

                        .setTitle(msg_content)
                 .addExtra("type",extrasparam)
                        .build())

                .setOptions(Options.newBuilder()
                        //此字段的值是用来指定本推送要推送的apns环境，false表示开发，true表示生产；对android和自定义消息无意义
                        .setApnsProduction(ApnsProduction)
                        //此字段是给开发者自己给推送编号，方便推送者分辨推送记录
                        .setSendno(1)
                        //此字段的值是用来指定本推送的离线保存时长，如果不传此字段则默认保存一天，最多指定保留十天；
                        .setTimeToLive(86400)
                        .build())
                .build();
    }


    /**
     * 向所有平台所有用户推送消息
     * @param msg_content
     * @param extrasparam
     * @return
     */
    public static PushPayload buildPushObject_android_and_ios(String msg_content,String extraKey, String extrasparam,String notification_title) {
        log.info("----------向所有平台所有用户推送消息中......");
        return PushPayload.newBuilder()
                .setPlatform(Platform.android_ios())
                .setAudience(Audience.all())
                .setNotification(Notification.newBuilder()
                                .setAlert(msg_content)
                                .addPlatformNotification(AndroidNotification.newBuilder()
                                        .setTitle(notification_title)
                                        //此字段为透传字段，不会显示在通知栏。用户可以通过此字段来做一些定制需求，如特定的key传要指定跳转的页面（value）
                                        .addExtra(extraKey,extrasparam)
                                        .build()
                                )
                                .addPlatformNotification(IosNotification.newBuilder()
//                                //传一个IosAlert对象，指定apns title、title、subtitle等
//                                .setAlert(notification_title)
                                                //直接传alert
                                                //此项是指定此推送的badge自动加1
                                                .incrBadge(1)
                                                //此字段的值default表示系统默认声音；传sound.caf表示此推送以项目里面打包的sound.caf声音来提醒，
                                                // 如果系统没有此音频则以系统默认声音提醒；此字段如果传空字符串，iOS9及以上的系统是无声音提醒，以下的系统是默认声音
                                                .setSound("default")
                                                //此字段为透传字段，不会显示在通知栏。用户可以通过此字段来做一些定制需求，如特定的key传要指定跳转的页面（value）
                                                .addExtra(extraKey,extrasparam)
                                                //此项说明此推送是一个background推送，想了解background看：http://docs.jpush.io/client/ios_tutorials/#ios-7-background-remote-notification
                                                // .setContentAvailable(true)

                                                .build()
                                )
                                .build()
                )
                //Platform指定了哪些平台就会像指定平台中符合推送条件的设备进行推送。 jpush的自定义消息，
                // sdk默认不做任何处理，不会有通知提示。建议看文档http://docs.jpush.io/guideline/faq/的
                // [通知与自定义消息有什么区别？]了解通知和自定义消息的区别
                /*.setMessage(Message.newBuilder()
                        .setMsgContent(msg_content)
                        .setTitle(msg_title)
                        .addExtra("message extras key",extrasparam)
                        .build())*/

                .setOptions(Options.newBuilder()
                        //此字段的值是用来指定本推送要推送的apns环境，false表示开发，true表示生产；对android和自定义消息无意义
                        .setApnsProduction(ApnsProduction)
                        //此字段是给开发者自己给推送编号，方便推送者分辨推送记录
                        .setSendno(1)
                        //此字段的值是用来指定本推送的离线保存时长，如果不传此字段则默认保存一天，最多指定保留十天，单位为秒
                        .setTimeToLive(86400)
                        .build()
                )
                .build();
    }

    /**
     *向所有平台单个或多个指定Tag用户推送消息
     * @param tagsList
     * @param msg_content
     * @param extrasparam
     * @return
     */
    private static PushPayload buildPushObject_all_tagList_alertWithTitle(List<String> tagsList,  String msg_content,String extraKey, String extrasparam,String notification_title) {

        log.info("----------向所有平台单个或多个指定Tag用户推送消息中.......");
        String sound="default";
        String[] temextra = extrasparam.split("#");
        String type = temextra[0];
        switch (type)//小二外卖订单语音播报
        {
            case "15"://新订单
                sound = "xindingdanmp.wav";
                break;
            case "16"://预约订单
                sound = "yudingdingdanmp.wav";
                break;
            case "17"://接单超时
                sound = "xindingdanchaoshimp.wav";
                break;
            case "18"://退单
                sound = "tuikuanmp.wav";
                break;
            case "19"://催单
                sound = "cuidanmp.wav";
                break;
            case "20"://小二外卖有订单即将超时啦
                sound = "peisongchaoshimp.wav";
                break;
            case "21"://小二外卖有订单配送超时，请尽快处理
                sound = "dingdanpeisongchaoshimp.wav";
                break;
            default:
                sound = "default";
                break;
        }
        //创建一个IosAlert对象，可指定APNs的alert、title等字段
        //IosAlert iosAlert =  IosAlert.newBuilder().setTitleAndBody("title", "alert body").build();
        return PushPayload.newBuilder()
                //指定要推送的平台，all代表当前应用配置了的所有平台，也可以传android等具体平台
                .setPlatform(Platform.all())
                //指定推送的接收对象，all代表所有人，也可以指定已经设置成功的tag或alias或该应应用客户端调用接口获取到的registration id
                .setAudience(Audience.tag(tagsList))
                //jpush的通知，android的由jpush直接下发，iOS的由apns服务器下发，Winphone的由mpns下发
                .setNotification(Notification.newBuilder()
                        //指定当前推送的android通知
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(msg_content)
                                .setTitle(notification_title)
                                //此字段为透传字段，不会显示在通知栏。用户可以通过此字段来做一些定制需求，如特定的key传要指定跳转的页面（value）
                                .addExtra(extraKey,extrasparam)
                                .build())
                        //指定当前推送的iOS通知
                        .addPlatformNotification(IosNotification.newBuilder()
                                //传一个IosAlert对象，指定apns title、title、subtitle等
                                .setAlert(msg_content)
                                //直接传alert
                                //此项是指定此推送的badge自动加1
                                .incrBadge(1)
                                //此字段的值default表示系统默认声音；传sound.caf表示此推送以项目里面打包的sound.caf声音来提醒，
                                // 如果系统没有此音频则以系统默认声音提醒；此字段如果传空字符串，iOS9及以上的系统是无声音提醒，以下的系统是默认声音
                                .setSound(sound)
                                //此字段为透传字段，不会显示在通知栏。用户可以通过此字段来做一些定制需求，如特定的key传要指定跳转的页面（value）
                                .addExtra(extraKey,extrasparam)
                                //此项说明此推送是一个background推送，想了解background看：http://docs.jpush.io/client/ios_tutorials/#ios-7-background-remote-notification
                                //取消此注释，消息推送时ios将无法在锁屏情况接收
                                .setContentAvailable(true)
                                .build())
                        .build())
                //Platform指定了哪些平台就会像指定平台中符合推送条件的设备进行推送。 jpush的自定义消息，
                // sdk默认不做任何处理，不会有通知提示。建议看文档http://docs.jpush.io/guideline/faq/的
                // [通知与自定义消息有什么区别？]了解通知和自定义消息的区别
                /*.setMessage(Message.newBuilder()

                        .setMsgContent(msg_content)

                        .setTitle(msg_title)

                        .addExtra("message extras key",extrasparam)

                        .build())*/

                .setOptions(Options.newBuilder()
                        //此字段的值是用来指定本推送要推送的apns环境，false表示开发，true表示生产；对android和自定义消息无意义
                        .setApnsProduction(ApnsProduction)
                        //此字段是给开发者自己给推送编号，方便推送者分辨推送记录
                        .setSendno(1)
                        //此字段的值是用来指定本推送的离线保存时长，如果不传此字段则默认保存一天，最多指定保留十天；
                        .setTimeToLive(86400)

                        .build())

                .build();

    }

    /**
     * 向android平台所有用户推送消息
     * @param notification_title
     * @param msg_content
     * @param extrasparam
     * @return
     */
    private static PushPayload buildPushObject_android_all_alertWithTitle(String notification_title,  String msg_content,String extraKey, String extrasparam) {
        log.info("----------向android平台所有用户推送消息中......");
        return PushPayload.newBuilder()
                //指定要推送的平台，all代表当前应用配置了的所有平台，也可以传android等具体平台
                .setPlatform(Platform.android())
                //指定推送的接收对象，all代表所有人，也可以指定已经设置成功的tag或alias或该应应用客户端调用接口获取到的registration id
                .setAudience(Audience.all())
                //jpush的通知，android的由jpush直接下发，iOS的由apns服务器下发，Winphone的由mpns下发
                .setNotification(Notification.newBuilder()
                        //指定当前推送的android通知
                        .addPlatformNotification(AndroidNotification.newBuilder()
                                .setAlert(msg_content)
                                .setTitle(notification_title)
                                //此字段为透传字段，不会显示在通知栏。用户可以通过此字段来做一些定制需求，如特定的key传要指定跳转的页面（value）
                                .addExtra(extraKey,extrasparam)
                                .build())
                        .build()
                )
                //Platform指定了哪些平台就会像指定平台中符合推送条件的设备进行推送。 jpush的自定义消息，
                // sdk默认不做任何处理，不会有通知提示。建议看文档http://docs.jpush.io/guideline/faq/的
                // [通知与自定义消息有什么区别？]了解通知和自定义消息的区别
                /*.setMessage(Message.newBuilder()
                        .setMsgContent(msg_content)
                        .setTitle(msg_title)
                        .addExtra("message extras key",extrasparam)
                        .build())*/

                .setOptions(Options.newBuilder()
                        //此字段的值是用来指定本推送要推送的apns环境，false表示开发，true表示生产；对android和自定义消息无意义
                        .setApnsProduction(ApnsProduction)
                        //此字段是给开发者自己给推送编号，方便推送者分辨推送记录
                        .setSendno(1)
                        //此字段的值是用来指定本推送的离线保存时长，如果不传此字段则默认保存一天，最多指定保留十天，单位为秒
                        .setTimeToLive(86400)
                        .build())
                .build();
    }

    /**
     * 向ios平台所有用户推送消息
     * @param msg_content
     * @param extrasparam
     * @return
     */
    private static PushPayload buildPushObject_ios_all_alertWithTitle(  String msg_content,String extraKey, String extrasparam) {
        log.info("----------向ios平台所有用户推送消息中.......");
        return PushPayload.newBuilder()
                //指定要推送的平台，all代表当前应用配置了的所有平台，也可以传android等具体平台
                .setPlatform(Platform.ios())
                //指定推送的接收对象，all代表所有人，也可以指定已经设置成功的tag或alias或该应应用客户端调用接口获取到的registration id
                .setAudience(Audience.all())
                //jpush的通知，android的由jpush直接下发，iOS的由apns服务器下发，Winphone的由mpns下发
                .setNotification(Notification.newBuilder()
                        //指定当前推送的android通知
                        .addPlatformNotification(IosNotification.newBuilder()
                                //传一个IosAlert对象，指定apns title、title、subtitle等
                                .setAlert(msg_content)
                                //直接传alert
                                //此项是指定此推送的badge自动加1
                                .incrBadge(1)
                                //此字段的值default表示系统默认声音；传sound.caf表示此推送以项目里面打包的sound.caf声音来提醒，
                                // 如果系统没有此音频则以系统默认声音提醒；此字段如果传空字符串，iOS9及以上的系统是无声音提醒，以下的系统是默认声音
                                .setSound("default")
                                //此字段为透传字段，不会显示在通知栏。用户可以通过此字段来做一些定制需求，如特定的key传要指定跳转的页面（value）
                                .addExtra(extraKey,extrasparam)
                                //此项说明此推送是一个background推送，想了解background看：http://docs.jpush.io/client/ios_tutorials/#ios-7-background-remote-notification
                                // .setContentAvailable(true)

                                .build())
                        .build()
                )
                //Platform指定了哪些平台就会像指定平台中符合推送条件的设备进行推送。 jpush的自定义消息，
                // sdk默认不做任何处理，不会有通知提示。建议看文档http://docs.jpush.io/guideline/faq/的
                // [通知与自定义消息有什么区别？]了解通知和自定义消息的区别
                /*.setMessage(Message.newBuilder()
                        .setMsgContent(msg_content)
                        .setTitle(msg_title)
                        .addExtra("message extras key",extrasparam)
                        .build())*/

                .setOptions(Options.newBuilder()
                        //此字段的值是用来指定本推送要推送的apns环境，false表示开发，true表示生产；对android和自定义消息无意义
                        .setApnsProduction(ApnsProduction)
                        //此字段是给开发者自己给推送编号，方便推送者分辨推送记录
                        .setSendno(1)
                        //此字段的值是用来指定本推送的离线保存时长，如果不传此字段则默认保存一天，最多指定保留十天，单位为秒
                        .setTimeToLive(86400)
                        .build())
                .build();
    }
}

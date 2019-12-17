--扫码支付--聚合码表
create table smpay_qrcode
(
	id uniqueidentifier primary key default newid(),--主键ID
	shopid int,--店铺ID
	qrcode nvarchar(64),--二维码编码
	qrcodeurl nvarchar(512),--二维码url
	qrpath nvarchar(512),--二维码下载路径
	bindtime datetime,--二维码绑定时间
	addtime datetime
)
--扫码支付--用户与三方平台绑定表
create table smpay_threeplatform_binduser
(
	id uniqueidentifier primary key default newid(),--主键ID
	openid nvarchar(128),--三方平台
	fromtype int ,--来源 1：微信，2：支付宝，3：云闪付
	telphone nvarchar(16),--手机号
	userid int,--用户ID
	easyopenid nvarchar(128),--易生云闪付openid
	addtime datetime
)
--扫码支付--商家支付营销活动
create table smpay_shop_marketingactivity
(
	id uniqueidentifier primary key default newid(),--主键ID
	shopid int,--店铺ID
	activitytype int,--活动类型 1：买单优惠类  2：兑换类
	coupontype int,--券类型 1：满减券，2：折扣券，3：赠品券 5体验券 6代金券
	startdate datetime,--活动开始时间
	enddate datetime,--活动结束时间
	effecttype int,--生效类型 1：立即生效，2：次日生效
	limitvalidity int,--券生效时间 （领取后多少天内有效）
	weekdays nvarchar(16),--可用时间（星期）格式 1,2,3,4,5,6,7
	isweekdays int, --1周一到周日2自定义
	couponnum int,--券数量
	fullamount decimal(18,2),--满减券 满金额
	reduceamount decimal(18,2),--满减券 减金额
	discount decimal(18,2),--折扣券 折扣力度 格式 0.1==10%
	productname nvarchar(64),--赠品名称
	experienceticketname nvarchar(64),--体验券名称
	activitystatus int,--活动状态 0：保存，1：等待商家确认 2：商家驳回 3：等待后台审核 4：后台审核驳回 5：后台审核通过，等待上线 6：进行中，7：已下架 8：已到期 9：已领完
	delstatus int,--删除状态 0：否，1：是
	updatetime datetime,--修改时间
	oldid uniqueidentifier,--修改进行中订单的主键id
	rejectreson   nvarchar(255),   --驳回原因
	rejectname  nvarchar(16), --驳回人
	addresource  int,        --活动新增来源  1后台添加 2商家添加
	addpersionid   int,  --添加人
	holiday int, --节假日 0不可用 1可用
    usagerules nvarchar(255), --使用规则
    customusagerules nvarchar(255), --自定义使用规则
    isremovedthenextday int, --标识次日下架状态 0否1是
    isread int,--标识待审核标志0未读1已读
    --v3--->add
    isnewcustomer int --0老客福利1新客专享2异业营销券
    maxdiscountamount decimal(18,2),--满减券 最高优惠金额
    voucheramount decimal(18,2),--代金券金额
    startingpoint decimal(18,2),--代金券使用起点
    maxnumber int --代金券最多使用张数
	addtime datetime--添加时间
)


--扫码支付--商家支付营销活动，节假日不可用时间段
create table smpay_shop_holidaynotavailable_timeslot
(
	id uniqueidentifier primary key default newid(),--主键ID
	shopid int,--店铺ID
	couponid uniqueidentifier,--(smpay_shop_marketingactivity)表主键ID
	starttime date,--不可用开始时间
	endtime date,--不可用结束时间
	addtime datetime
)



--扫码支付--商家支付营销活动，券可用时间段
create table smpay_shop_marketingcoupon_timeslot
(
	id uniqueidentifier primary key default newid(),--主键ID
	shopid int,--店铺ID
	couponid uniqueidentifier,--(smpay_shop_marketingactivity)表主键ID
	starttime int,--可用开始时间（格式：800）
	endtime int,--可用结束时间（格式：1400）
	addtime datetime
)
--扫码支付--用户领取券表
create table smpay_user_getcoupon
(
	id uniqueidentifier primary key default newid(),--主键ID
	marketingactivityid uniqueidentifier,--(smpay_shop_marketingactivity)表主键ID
	getcoupontype int,--券类型 1：满减券，2：折扣券，3：赠品券，4：立减券（新客立减）5体验券 6代金券
	shopid int,--店铺ID
	userid int,--用户id
	couponcode nvarchar(64),--券码
	couponname nvarchar(64),--券名
	aountlimit decimal(18,2),--使用起点 商家券默认起点为0
	reduceamount decimal(18,2),--减免金额/折扣
	checkstatus int,--验证状态 0：否，1：是
	checktime datetime,--验证时间
	startdate datetime,--有效期 开始时间
	enddatet datetime,--有效期 结束时间
	weekdays nvarchar(16),--可用时间（星期）格式 1,2,3,4,5,6,7
	isnewcustomer int, --0老客福利1新客专享2异业营销券
	maxdiscountamount decimal(18,2),--满减券 最高优惠金额
    maxnumber int --代金券最多使用张数
	addtime datetime--添加时间
)
--扫码支付--用户领取券-券使用时间
create table smpay_user_getcoupon_timeslot
(
	id uniqueidentifier primary key default newid(),--主键ID
	couponid uniqueidentifier,--(smpay_user_getcoupon)表主键ID
	starttime int,--可用开始时间（格式：800）
	endtime int,--可用结束时间（格式：1400）
	addtime datetime
)
--扫码支付--交易记录
create table smpay_traderecord
(
	id uniqueidentifier primary key default newid(),--主键ID
	shopid int,--店铺id
	userid int,--用户ID
	openid nvarchar(64),--openid
	easyopid nvarchar(64),--易生openid
	tradetype int,--交易类型 1：固码支付 2：
	ordernum nvarchar(64),--支付订单号
	tradechannel int,--交易渠道 1：微信，2：支付宝，3：云闪付
	originalamount decimal(18,2),--原支付金额
	payamount decimal(18,2),--实际支付金额
	newuserreduceamount decimal(18,2),--新客立减金额
	vipmemberreduceamount decimal(18,2),--会员减免金额(平台)
	vipmemberreducedifference decimal(18,2),--会员减免金额(商户差额)
	shopreduceamount decimal(18,2),--店铺减免金额
	couponid uniqueidentifier,--商户券--用户领取券表(smpay_user_getcoupon)主键id
	newusercouponid uniqueidentifier,--立减券ID--用户领取券表(smpay_user_getcoupon)主键id
	vipmembercouponid uniqueidentifier,--会员券表(life_vipTickets)主键id
	discountname nvarchar(128),--使用优惠
	payfee decimal(18,2),--交易结算手续费（实际支付）
  tradefee decimal(18,2),--支付手续费（商家收款金额）
  subsidyfee decimal(18,2),--补贴手续费（平台金额）
	trade_code nvarchar(64),--微信/支付宝/云闪付流水号
	paystatus int,--支付状态 0：等待支付 1：支付成功 2： 支付失败 3：半小时未支付，订单作废
	paysuccesstime datetime,--支付成功时间
	qrcode nvarchar(64),--收款码编号
	merid nvarchar(64),--商户号
	terminalnumber nvarchar(64),--交易终端号
	soundterminalnumber nvarchar(64),--音响终端号
	transfercard nvarchar(64),--转入卡号
	soundversion nvarchar(16),--音响版本 wifi:04,2g：待定
	addtime datetime,
	queryId nvarchar(255), --代付交易流水（银联与天行共用，如果是易生代付可不传）
	txnTime nvarchar(255), --银联代付交易发送时间yyyyMMddHHmmss
	ispaybehalf int default 0  not null, --是否完成联机代付交易 0：否，1：是
	paybehalftime datetime, --联机代付完成时间
	czpaybehalfstatus int default 0  not null, --是否完成充值代付交易 0：否，1：是
	czpaybehalftime datetime, --充值代付完成时间
	thpaybehalfstatus int default 0  not null, --天津银行代发结果 0：否，1：是
  thpaybehalftime datetime,
  isrebate int default 0,--是否返利，0：否，1：是
)
--扫码支付--扫码记录
create table smpay_smrecord
(
	id uniqueidentifier primary key default newid(),--主键ID
	tradechannel int,--交易渠道 1：微信，2：支付宝，3：云闪付
	openid nvarchar(64),--openid
	shopid int,--店铺id
	addtime datetime
)
-- 易生代付记录
create table YSBehalfPayRecord
(
	id uniqueidentifier primary key default newid(),--主键ID
	orderId nvarchar(255), --订单ID（人工代付这里是交易流水）
	payment decimal(18,2),--代发金额
	msg nvarchar(1000),--内容
	success int,--1:成功 0：失败,2:异常
	type int,--0:订单代付，1：人工代付，2：返利金代付
	description nvarchar(255),--描述
	addtime datetime--时间
)

--易生代付发送记录
create table YSBehalfPaySendRecord
(
	id uniqueidentifier primary key default newid(),--主键ID
	orderId nvarchar(255), --订单编号
	actualPayment decimal(18,2),--实际支付金额
	subsidyPayment decimal(18,2),--补贴金额
	rebatePayment decimal(18,2),--返利金金额
	sumPayment decimal(18,2),--总代发金额
	requestMsg nvarchar(1000),--发送报文
	responseMsg nvarchar(1000),--响应报文
	success int,--1:发送成功 0：发送失败
	addtime datetime--时间
)


-- 营销券下架临时表
create table MarketingVouchersTemporary
(
	id uniqueidentifier primary key default newid(),--主键ID
	couponid uniqueidentifier, --smpay_shop_marketingactivity主键ID
	shopid int,--店铺ID
	addtime datetime--时间
)

create table shop_message
(
 id uniqueidentifier primary key default newid(),--主键ID
 shopid int,
 message nvarchar(256), --消息内容
 messagetype int, --消息类型
 isread int, --0未读  1已读
 addtime datetime
)
--返利金交易记录表
create table rebateTransactions
(
id uniqueidentifier primary key default newid(),--主键ID
shopid int,
money decimal(18,2),--交易金额
rebateType int,--返利类型 1：服务费返利，2：云闪付拉新奖励，3：返利金提现
rebateTypeDescribe nvarchar(256),--返利类型描述
transactionNum nvarchar(256),--提现交易流水
cashnum nvarchar(256),--提现卡号
cashWithdrawalState int,--提现到账状态0：等待到账，1：已到账
isread int,--是否查看，0：未读，1：已读
addtime datetime--添加时间
)

--返利金金额
create table rebateTransactionsTotalSum
(
id uniqueidentifier primary key default newid(),--主键ID
shopid int,
money decimal(18,2),--总金额
addtime datetime,--添加时间
updatetime datetime--修改时间
)

--服务费返利表
create table serviceFeerebateTransactions
(
id uniqueidentifier primary key default newid(),--主键ID
shopid int,
TransactionTime datetime,--交易日期
paymoney decimal(18,2),--用户支付总金额
serviceFee decimal(18,2),--服务费
serviceFeeRebate decimal(18,2),--服务费返利金金额
addtime datetime,--添加时间
isDeposit int,--是否存入商户 0：否，1：是，2：临时
ordernum nvarchar(256),--订单编号
rebateTransactionsId uniqueidentifier,--返利金交易记录表ID
)

--云闪付拉新奖励表
create table cloudFlashoverLaNewAward
(
id uniqueidentifier primary key default newid(),--主键ID
shopid int,
lachinenum int,--拉新人数
bonusPrice decimal(18,2),--奖励金单价
bonusmoney decimal(18,2),--云闪付拉新奖励金
isDeposit int,--是否存入商户 0：否，1：是
rebateTransactionsId uniqueidentifier,--返利金交易记录表ID
addtime datetime,--添加时间
)

ALTER TABLE smpay_traderecord ADD isrebate int default 0--是否返利，0：否，1：是
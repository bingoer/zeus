# 数字货币交易所实时行情 Springboot + Netty

> 目前支持🔥火币数据解析 后续会逐步添加解析

- websocket连接火币服务器
- K线作为历史数据写入mongoDB
- 获取okex最新USD汇率写入redis
- 支持http查询历史K线数据
- 支持配置文件订阅交易对
- 支持其它交易所扩展
- 支持websocket本地连接
- 自定义协议


> 服务端连接方式  ws://ip:port/


# Usage

本地客户端在与服务端握手时，服务器会为客户端分配一个唯一识别码UUID，在接下来的所有请求中都必须带上该ID

#### 订阅

- K线
- 24小时成交详情
- 深度
- 最新成交



> K线

```json
{
  "event":"sub",
  "id": "723c9150-e143-4d80-84fc-6d0acdcba8f5",
  "channel":["market.btcusdt.kline.1min"],   //1min 5min 15min 30min 60min 1day 1mon..
  "type":"kline"
}
```

```json
返回
```



> 24小时成交详情

```json
{
  "event":"sub",
  "id": "723c9150-e143-4d80-84fc-6d0acdcba8f5",
  "channel":["market.btcusdt.detail"],
  "type":"detail"
}
```

```json
返回
```



> 深度

```json
{
  "event":"sub",
  "id": "723c9150-e143-4d80-84fc-6d0acdcba8f5",
  "channel":["market.btcusdt.depth.step1"],
  "type":"depth"
}
```

```json
返回
```



> 最新成交

```json
{
  "event":"sub",
  "id": "723c9150-e143-4d80-84fc-6d0acdcba8f5",
  "channel":"market.btcusdt.trade.detail",
  "type":"trade"
}
```

```json
返回
```

> 取消订阅所有
```json
{
  "event":"un_sub",
  "id": "723c9150-e143-4d80-84fc-6d0acdcba8f5",
  "type":"all"
}

```




#### 请求

- 历史K线初始化
- 历史K线分页加载



> 历史K线初始化

```json
{
  "event":"req",
  "channel":["market.kline.btcusdt.1min"]
}
```

```json
返回
```



> 历史K线分页加载

```json
{
  "event":"req",
  "channel":["market.kline.btcusdt.1min.page.1571199360"]
}
```

```json
返回
```







## 数据
![image](https://github.com/wangbinzero/zeus/blob/master/image/deal.png)

![image](https://github.com/wangbinzero/zeus/blob/master/image/depth.png)

![image](https://github.com/wangbinzero/zeus/blob/master/image/kline.png)

![image](https://github.com/wangbinzero/zeus/blob/master/image/http_kline.png)



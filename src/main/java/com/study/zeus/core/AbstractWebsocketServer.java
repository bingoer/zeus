package com.study.zeus.core;

import com.study.zeus.proto.Request;
import com.study.zeus.utils.StringUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.tomcat.util.net.NioSelectorPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * websocket 服务端
 */
public abstract class AbstractWebsocketServer {

    private static final Logger logger = LoggerFactory.getLogger(AbstractWebsocketServer.class);
    private final Integer maxFrameSize = 2 << 20;
    private final Integer maxContentLength = 2 << 13;


    /**
     * -------------------------
     * depthPool    盘口
     * klinePool    K线
     * detailPool   24小时成交
     * <p>
     * sub_channel 订阅频道
     * --------------------------
     */
    public static Map<String, NioSocketChannel> depthPool = new ConcurrentHashMap<>();
    public static Map<String, NioSocketChannel> klinePool = new ConcurrentHashMap<>();
    public static Map<String, NioSocketChannel> detailPool = new ConcurrentHashMap<>();
    private static Map<String, Set<String>> sub_channel = new ConcurrentHashMap<>();
    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workGroup;

    public void init(int port, SimpleChannelInboundHandler handler) {
        try {
            bossGroup = new NioEventLoopGroup();
            workGroup = new NioEventLoopGroup();
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new HttpServerCodec());
                    pipeline.addLast(new ChunkedWriteHandler());
                    pipeline.addLast(new HttpObjectAggregator(maxContentLength));
                    pipeline.addLast(new WebSocketServerCompressionHandler());
                    pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true, maxFrameSize));
                    pipeline.addLast(handler);
                }
            });

            Channel channel = server.bind(new InetSocketAddress(port)).sync().channel();
            if (channel.isOpen()) {
                logger.info("服务端启动成功,端口:[{}],连接方式: [{}]", port, "ws://ip:port/");
            }
        } catch (Exception e) {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
            }
            if (workGroup != null) {
                workGroup.shutdownGracefully();
            }
        }
    }


    /**
     * 订阅频道
     *
     * @param clientId 客户端ID
     * @param chan     socket通道
     * @param pool     连接池
     * @param channel  订阅频道
     */
    public synchronized static void subChannel(String clientId, NioSocketChannel chan, Map<String, NioSocketChannel> pool, String... channel) {
        if (null != pool) {
            pool.put(clientId, chan);
            Set<String> sets = sub_channel.get(clientId);
            if (null != sets) {
                for (int i = 0; i < channel.length; i++) {
                    sets.add(channel[i]);
                }
            } else {
                Set<String> newSet = new HashSet<>();
                for (int i = 0; i < channel.length; i++) {
                    newSet.add(channel[i]);
                }
                sub_channel.put(clientId, newSet);
            }
        }
    }


    /**
     * 取消订阅
     *
     * @param clientId 客户端ID
     * @param channel  订阅频道
     */
    public synchronized static void unSubChannel(String clientId, Map<String, NioSocketChannel> map, String... channel) {
        Set<String> set = sub_channel.get(clientId);
        List<String> channList = Arrays.asList(channel);
        if (null != set) {
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String value = iterator.next();
                if (channList.contains(value)) {
                    set.remove(value);
                }
            }
            if (!iterator.hasNext()) {
                map.remove(clientId);
            }
        }
    }

    public synchronized static void unSubAllChannel(String clientId) {
        depthPool.remove(clientId);
        klinePool.remove(clientId);
        detailPool.remove(clientId);
        sub_channel.remove(clientId);
    }

    public abstract void run(int port);

    public abstract void onReceiveMessage(Request req, NioSocketChannel channel);

    public abstract void onLine(ChannelHandlerContext ctx);

    public abstract void offLine(ChannelHandlerContext ctx);
}

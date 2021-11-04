package cn.yuyake.common.utils;

import com.alibaba.fastjson.JSON;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GameHttpClient {

    private static Logger logger = LoggerFactory.getLogger(GameHttpClient.class);
    // 连接池管理
    private static PoolingHttpClientConnectionManager poolConnManager = null;
    // 它是线程安全的，所有的线程都可以使用它一起发送http请求
    private static CloseableHttpClient httpClient;

    static {
        try {
            logger.debug("GameHttpClient初始化开始");
            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build());
            // 配置同时支持 HTTP 和 HTTPS
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslsf)
                    .build();
            // 初始化连接管理器
            poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // 同时最多连接数
            poolConnManager.setMaxTotal(640);
            // 设置最大路由
            poolConnManager.setDefaultMaxPerRoute(320);
            httpClient = getConnection();
            logger.debug("GameHttpClient初始化成功");
        } catch (Exception e) {
            logger.error("GameHttpClient初始化失败", e);
        }
    }

    /**
     * 获取连接
     */
    private static CloseableHttpClient getConnection() {
        // 统一设置连接参数，也可能修改为通过配置传入参数
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(poolConnManager) // 设置连接池管理
                .setDefaultRequestConfig(config) // 设置配置
                .setRetryHandler(new DefaultHttpRequestRetryHandler(2, false)).build(); // 设置重试次数
        return httpClient;
    }

    public static String post(String uri, Object params, Header... heads) {
        HttpPost httpPost = new HttpPost(uri);
        CloseableHttpResponse response = null;
        try {
            StringEntity paramEntity = new StringEntity(JSON.toJSONString(params));
            paramEntity.setContentEncoding("UTF-8");
            paramEntity.setContentType("application/json");
            httpPost.setEntity(paramEntity);
            if (heads != null) {
                httpPost.setHeaders(heads);
            }
            response = httpClient.execute(httpPost);
            int code = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity());
            if (code == HttpStatus.SC_OK) {
                return result;
            } else {
                logger.error("请求{}返回错误码:{},请求参数:{},{}", uri, code, params, result);
                return null;
            }
        } catch (IOException e) {
            logger.error("收集服务配置http请求异常", e);
        } finally {
            if(response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}

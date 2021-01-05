package com.xiaoshu.config;

import com.xiaoshu.properties.MqttCommonProperties;
import com.xiaoshu.properties.MqttInboundProperties;
import com.xiaoshu.properties.MqttOutboundProperties;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : function description
 * ---------------------------------
 * @Author : deane
 * @Date : Create in 2020/3/2 19:49
 * <p>
 * Copyright (C)2013-2020 小树盛凯科技 All rights reserved.
 */
@ConditionalOnClass(MqttConnectOptions.class)
@Configuration
@EnableConfigurationProperties({MqttInboundProperties.class, MqttOutboundProperties.class, MqttCommonProperties.class})
public class MqttClientAutoConfiguration {

    public static MqttPahoMessageDrivenChannelAdapter adapter;

    public static String CHARSET_UTF8 = "UTF-8";

    @Autowired
    MqttInboundProperties mqttInboundProperties;

    @Autowired
    MqttOutboundProperties mqttOutboundProperties;

    @Autowired
    MqttCommonProperties mqttCommonProperties;

    @Value("${spring.mqtt.cleanSession:false}")
    boolean cleanSession;

    @Value("${spring.mqtt.defaultRetain:false}")
    boolean defaultRetain;

    @Value("${spring.mqtt.defaultQosInbound:1}")
    int defaultQosInbound;

    @Value("${spring.mqtt.defaultQosOutbound:1}")
    int defaultQosOutbound;

    /**
     * 消息处理器
     */
    @Autowired(required = false)
    @Qualifier(value = "messageHandler")
    private MessageHandler messageHandler;

    /**
     * 消息处理器
     */
    @Resource
    @Qualifier(value = "defaultMessageHandler")
    private MessageHandler defaultMessageHandler;

    /**
     * 配置mqtt连接信息
     * @return
     */
    @Bean
    public MqttConnectOptions mqttConnectOptions(){
        MqttConnectOptions mqttConnectOptions= new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(cleanSession);
        mqttConnectOptions.setUserName(mqttCommonProperties.getUserName());
        mqttConnectOptions.setPassword(mqttCommonProperties.getPassword().toCharArray());
        mqttConnectOptions.setServerURIs(new String[]{mqttCommonProperties.getUrl()});
        //解决too many publishes in progress error的问题，调整飞行窗口的大小，默认值是10
        mqttConnectOptions.setMaxInflight(10000);
        // 设置超时时间 TODO 这两个参数设入properties中
        mqttConnectOptions.setConnectionTimeout(20);
        mqttConnectOptions.setKeepAliveInterval(20);
        mqttConnectOptions.setAutomaticReconnect(true);
        return mqttConnectOptions;
    }

    /**
     * mqtt客户端工厂类
     * @param mqttConnectOptions
     * @return
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory(@Autowired MqttConnectOptions mqttConnectOptions) {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(mqttConnectOptions);
        return factory;
    }

    /**
     * 发送消息处理器（推送）,配置发送消息客户端
     * @param mqttPahoClientFactory
     * @return
     * @throws UnsupportedEncodingException
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(@Autowired MqttPahoClientFactory mqttPahoClientFactory) throws UnsupportedEncodingException {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(new String(mqttOutboundProperties.getClientId().getBytes(),CHARSET_UTF8), mqttPahoClientFactory);
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(mqttOutboundProperties.getTopics());
        messageHandler.setCompletionTimeout(30000);
        messageHandler.setDefaultRetained(defaultRetain);
        messageHandler.setDefaultQos(defaultQosOutbound);
        // 设置转换器，发送bytes
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);
        messageHandler.setConverter(converter);
        return messageHandler;
    }

    /**
     * 发送通道（消息生产者）
     * @return
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * 接收通道（消息消费者）
     * @return
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 配置消息适配器，配置订阅客户端
     * @param mqttClientFactory
     * @return
     * @throws UnsupportedEncodingException
     */
    @Bean
    public MessageProducer inbound(@Autowired MqttPahoClientFactory mqttClientFactory) throws UnsupportedEncodingException {
        String[] topics = mqttInboundProperties.getTopics().split(",");
        adapter = new MqttPahoMessageDrivenChannelAdapter(new String(mqttInboundProperties.getClientId().getBytes(),CHARSET_UTF8), mqttClientFactory, topics);
        // 设置转换器，接收bytes
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);
        adapter.setConverter(converter);
        adapter.setQos(defaultQosInbound);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    /**
     * 通过通道获取数据, 接收消息处理器（订阅）
     * @return
     */
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        if(ObjectUtils.isEmpty(messageHandler)){
            return defaultMessageHandler;
        }
        return messageHandler;
    }
}

package com.xiaoshu.config;

import com.xiaoshu.properties.MqttCommonProperties;
import com.xiaoshu.properties.MqttInboundProperties;
import com.xiaoshu.properties.MqttOutboundProperties;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    @Autowired
    MqttInboundProperties mqttInboundProperties;


    @Autowired
    MqttOutboundProperties mqttOutboundProperties;

    @Autowired
    MqttCommonProperties mqttCommonProperties;

    // 消息处理器
    @Autowired(required = false)
    @Qualifier(value = "messageHandler")
    private MessageHandler messageHandler;

    // 消息处理器
    @Resource
    @Qualifier(value = "defaultMessageHandler")
    private MessageHandler defaultMessageHandler;

    // 配置mqtt连接信息
    @Bean
    public MqttConnectOptions mqttConnectOptions(){
        MqttConnectOptions mqttConnectOptions= new MqttConnectOptions();
        mqttConnectOptions.setUserName(mqttCommonProperties.getUserName());
        mqttConnectOptions.setPassword(mqttCommonProperties.getPassword().toCharArray());
        mqttConnectOptions.setServerURIs(new String[]{mqttCommonProperties.getUrl()});
        mqttConnectOptions.setKeepAliveInterval(2);
        return mqttConnectOptions;
    }

    // mqtt客户端工厂类
    @Bean
    public MqttPahoClientFactory mqttClientFactory(@Autowired MqttConnectOptions mqttConnectOptions) {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        factory.setConnectionOptions(mqttConnectOptions);
        return factory;
    }

    // 发送消息处理器（推送）,配置发送消息客户端
    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound(@Autowired MqttConnectOptions mqttConnectOptions) {
        MqttPahoMessageHandler messageHandler =  new MqttPahoMessageHandler(mqttOutboundProperties.getClientId(), mqttClientFactory(mqttConnectOptions));
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(mqttOutboundProperties.getTopics());
        // 设置转换器，发送bytes
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);
        messageHandler.setConverter(converter);
        return messageHandler;
    }

    // 发送通道（消息生产者）
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    // 接收通道（消息消费者）
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // 配置消息适配器，配置订阅客户端
    @Bean
    public MessageProducer inbound(@Autowired MqttConnectOptions mqttConnectOptions) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(mqttInboundProperties.getClientId(), mqttClientFactory(mqttConnectOptions), mqttInboundProperties.getTopics());
        // 设置转换器，接收bytes
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);
        adapter.setConverter(converter);
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    // 通过通道获取数据
    // 接收消息处理器（订阅）
    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        if(ObjectUtils.isEmpty(messageHandler)){
            return defaultMessageHandler;
        }
        return messageHandler;
    }
}

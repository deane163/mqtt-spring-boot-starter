package com.xiaoshu.service;

import com.xiaoshu.annotaion.MessagehandlerAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

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
 * @Date : Create in 2020/3/2 18:42
 * <p>
 * Copyright (C)2013-2020 小树盛凯科技 All rights reserved.
 */
@Component(value = "defaultMessageHandler")
public class DefaultReceivedMessageHandler implements MessageHandler {

    @Override
    public void handleMessage(Message<?> message) {
        System.out.println("收到订阅消息: ==>" + message);
        String topic = message.getHeaders().get("mqtt_receivedTopic").toString();
        System.out.println("消息主题：==>" + topic);
        Object payLoad = message.getPayload();
        // 如果不设置转换器这里强转byte[]会报错
        byte[] data = (byte[]) payLoad;
        System.out.println("Message is : ==> "  + new String(data));
    }
}
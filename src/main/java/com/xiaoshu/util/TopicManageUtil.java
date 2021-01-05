package com.xiaoshu.util;

import com.xiaoshu.config.MqttClientAutoConfiguration;

/**
 * @function：topic manage util;
 * (1) dynamic add topic listen;
 * (2) dynamic delete topic listen;
 *
 * @ com.xiaoshu.util
 * <p>
 * Original @Author: deane.jia-贾亮亮,@2021/1/5@19:06
 * <p>
 * Copyright (C)2012-@2021 深圳优必选科技 All rights reserved.
 */
public class TopicManageUtil {

    /**
     * 动态添加Topic监听
     * @param topicNames
     */
    public static void addTopic(String... topicNames){
        if(null != topicNames && topicNames.length > 0){
            MqttClientAutoConfiguration.adapter.addTopic(topicNames);
        }
    }

    /**
     * 动态删除Topic监听；
     * @param topicName
     */
    public static void removeTopic(String... topicName){
        if(null != topicName && topicName.length > 0){
            MqttClientAutoConfiguration.adapter.removeTopic(topicName);
        }
    }

}

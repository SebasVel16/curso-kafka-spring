package com.dev4j.cursokafkaspring.config;

import com.dev4j.cursokafkaspring.models.Product;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableScheduling
public class KafkaConfiguration {

    @Value("${application.url}")
    private String url;

    public Map<String,Object> producerProperties(){
        Map<String,Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, url);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }
    public Map<String,Object> consumerProperties(){
        Map<String,Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,url);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,"devs4j-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return props;
    }
    @Bean
    public KafkaTemplate<String,Product> kafkaTemplate(){
        DefaultKafkaProducerFactory<String,Product> defaultKafkaProducerFactory = new DefaultKafkaProducerFactory<>(producerProperties());
        KafkaTemplate<String,Product> template = new KafkaTemplate<>(defaultKafkaProducerFactory);
        return template;
    }

    @Bean
    public ConsumerFactory<String,Product> consumerFactory(){
        return new DefaultKafkaConsumerFactory<>(consumerProperties(),new StringDeserializer(),new JsonDeserializer<>(Product.class));
    }

    @Bean(name = "listenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String,Product> listenerContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,Product> listenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        listenerContainerFactory.setConsumerFactory(consumerFactory());
        //Configurando el batchListener
        //listenerContainerFactory.setBatchListener(true);
        //Multithreading
        listenerContainerFactory.setConcurrency(3);
        return listenerContainerFactory;
    }

    /*
    @Bean
    public MeterRegistry meterRegistry(){
        PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        return meterRegistry;
    }
     */
}

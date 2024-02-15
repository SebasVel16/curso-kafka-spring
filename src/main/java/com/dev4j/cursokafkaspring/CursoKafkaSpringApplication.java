package com.dev4j.cursokafkaspring;

import ch.qos.logback.core.util.FixedDelay;
import io.micrometer.core.instrument.MeterRegistry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootApplication
public class CursoKafkaSpringApplication {

	@Autowired
	private MeterRegistry meterRegistry;

	private static final Logger log = LoggerFactory.getLogger(CursoKafkaSpringApplication.class);

	@Autowired
	private KafkaTemplate<String,String> kafkaTemplate;

	@KafkaListener(topics = "devs4j-topic",containerFactory = "listenerContainerFactory",groupId = "devs4j-group", properties = {
			"max.poll.interval.ms:4000",
			"max.poll.records:50"
	})
	public void listen(List<ConsumerRecord<String,String>> messages) {
		log.info("Start messaging");
		for (ConsumerRecord<String,String> message : messages) {
			log.info("Message received = {}, partition = {}, offset = {}", message.value(), message.partition(), message.offset());
		}
		log.info("Batch Complete");
	}

	public static void main(String[] args) {
		SpringApplication.run(CursoKafkaSpringApplication.class, args);
	}

	@Scheduled(fixedDelay = 2000,initialDelay = 100)
	public void sendKafkaMessages() {
		for (int i= 0 ;i<100;i++){
			kafkaTemplate.send("devs4j-topic",String.valueOf(i), String.format("Sample Message = %d", i));
		}
	}


	/* For command Line runner
	@Override
	public void run(String... args) throws Exception {
		for (int i = 0; i<100;i++){
			kafkaTemplate.send("devs4j-topic", String.valueOf(i) ,String.format("Sample message %d",i));
		}

		AsynCallBacks (We use CompletableFuture since ListenableFuture is deprecated
		CompletableFuture<SendResult<String,String>> future = kafkaTemplate.send("devs4j-topic","Sample-Message");
		future.whenComplete((result, ex) -> {
			log.info("Message sent", result.getRecordMetadata().offset());
		});

		// Sync Producer
		//kafkaTemplate.send("devs4j-topic","Sample-Message").get();
		// Sync Producer Timeout exception if message wasn't delivered on specified time
		//kafkaTemplate.send("devs4j-topic","Sample-Message").get(100,TimeUnit.MILLISECONDS);


	}
	*/

}

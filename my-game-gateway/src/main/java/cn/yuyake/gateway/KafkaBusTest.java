package cn.yuyake.gateway;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class KafkaBusTest {
    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @PostConstruct
    public void init() {
        String str = "你好，kafka";
        ProducerRecord<String, byte[]> record = new ProducerRecord<>("KafkaTestTopic", "hello", str.getBytes());
        kafkaTemplate.send(record);
    }

    @KafkaListener(topics = {"KafkaTestTopic"}, groupId = "my-game")
    public void receiver(ConsumerRecord<String, byte[]> record) {
        byte[] body = record.value();
        String value = new String(body);
        System.err.println("收到Kafka的消息：" + value);
    }
}

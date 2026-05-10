// package vn.edu.ptit.config;

// import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
// import org.apache.kafka.clients.admin.AdminClientConfig;
// import org.apache.kafka.clients.admin.NewTopic;
// import org.apache.kafka.clients.consumer.ConsumerConfig;
// import org.apache.kafka.clients.producer.ProducerConfig;
// import org.apache.kafka.common.config.TopicConfig;
// import org.apache.kafka.common.serialization.StringSerializer;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import
// org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
// import org.springframework.kafka.config.TopicBuilder;
// import org.springframework.kafka.core.*;
// import org.springframework.kafka.support.serializer.JsonDeserializer;
// import org.springframework.kafka.support.serializer.JsonSerializer;

// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// @Configuration
// public class KafkaConfig {

// @Bean
// public KafkaAdmin kafkaAdmin() {
// Map<String, Object> config = new HashMap<>();
// config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
// return new KafkaAdmin(config);
// }

// @Bean
// public NewTopic topic1() {
// return TopicBuilder
// .name("topic1")
// .partitions(10)
// .replicas(1)
// .compact()
// .build();
// }

// @Bean
// public NewTopic topic2() {
// return TopicBuilder
// .name("topic2")
// .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "zstd")
// .build();
// }

// @Bean
// public NewTopic topic3() {
// return TopicBuilder
// .name("topic3")
// .partitions(3)
// .replicas(1)
// .build();
// }

// //Producer
// @Bean
// public ProducerFactory<String, Object> producerFactory() {
// Map<String, Object> config = new HashMap<>();
// config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
// config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
// StringSerializer.class);
// config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
// JsonSerializer.class);
// return new DefaultKafkaProducerFactory<>(config);
// }

// @Bean
// public KafkaTemplate<String, Object> kafkaTemplate() {
// return new KafkaTemplate<>(producerFactory());
// }

// //Consumer

// @Bean
// public ConsumerFactory<String, Object> consumerFactory() {
// Map<String, Object> config = new HashMap<>();
// config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
// config.put(ConsumerConfig.GROUP_ID_CONFIG, "statistical-group");
// config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
// StringDeserializer.class);
// config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
// JsonDeserializer.class);
// return new DefaultKafkaConsumerFactory<>(config);
// }

// @Bean
// public ConcurrentKafkaListenerContainerFactory<String, Object>
// kafkaListenerContainerFactory() {
// ConcurrentKafkaListenerContainerFactory<String, Object> factory = new
// ConcurrentKafkaListenerContainerFactory<>();
// factory.setConsumerFactory(consumerFactory());
// return factory;
// }
// }

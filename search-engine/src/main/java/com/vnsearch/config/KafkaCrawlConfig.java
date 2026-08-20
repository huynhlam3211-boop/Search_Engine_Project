package com.vnsearch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vnsearch.crawler.DnsResolver;
import com.vnsearch.crawler.LinkExtractor;
import com.vnsearch.crawler.UrlFilter;
import com.vnsearch.crawler.UrlSeenFilter;
import com.vnsearch.crawler.bus.CrawlEventBus;
import com.vnsearch.crawler.bus.KafkaCrawlEventBus;
import com.vnsearch.crawler.modular.CrawlAnalyticsService;
import com.vnsearch.crawler.modular.ImageDownloadService;
import com.vnsearch.crawler.modular.UrlExtractorService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "app.crawler.bus", havingValue = "kafka")
public class KafkaCrawlConfig { 
    private static final Logger log = LoggerFactory.getLogger(KafkaCrawlConfig.class);

    private static final int MAX_MESSAGE_BYTES = 4*1024*1024;

    @Value("${app.crawler.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.crawler.kafka.topic.pages}")
    private String pagesTopic;

    @Value("${app.crawler.kafka.topic.urls}")
    private String urlsTopic;

    @Value("${app.crawler.kafka.topic.outlinks}")
    private String outlinksTopics;

    @Value("${app.crawler.kafka.topic.images}")
    private String imagesTopic;

    @Value("${app.crawler.kafka.topic.partition:12}")
    private int partitions;

    @Value("${app.crawler.kafka.replication-factor:1}")
    private short replicationFactor;

    @Bean
    public KafkaAdmin crawlKafkaAdmin() {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        KafkaAdmin admin = new KafkaAdmin(props);
        admin.setFatalIfBrokerNotAvailable(true);
        return admin;
    }

    @Bean
    public NewTopic pagesTopic() {

    }

    @Bean
    public NewTopic urlsTopic() {

    }

    @Bean
    public NewTopic outlinksTopic() {

    }

    @Bean 
    public NewTopic imageTopic() {

    }

    @Bean
    public NewTopic deadLetterTopic() {

    }




    @Bean
    public ObjectMapper crawlEventObjectMapper() {

    }

    @Bean
    public ProducerFactory<String, Object> crawlProducerFactory(ObjectMapper crawlerEventObjectMapper) {

    }

    @Bean
    public ConsumerFactory<String, String> crawlConsumerFactory() {

    }

    @Bean
    public KafkaTemplate<String, Object> crawlKafkaTemplate() {

    }

    @Bean
    public CrawlEventBus crawlEventBus() {

    }



    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> crawlListenerContainerFactory() {

    }

    @Bean
    public UrlFilter workerUrlFilter() {

    }

    @Bean
    public UrlSeenFilter workerUrlSeenFilter() {

    }

    @Bean 
    public UrlExtractorService UrlExtractorService() {

    }

    @Bean 
    public ImageDownloadService imageDownloadService() {

    }

    @Bean
    public CrawlAnalyticsService crawlAnalyticsService() {

    }

    @Bean
    public MeterBinder crawlBusMetrics(CrawlEventBus crawlEventBus) {
        
    }

}
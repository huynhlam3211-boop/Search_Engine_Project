package com.vnsearch.crawler.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("kafka-it")
class KafkaCrawlBusIT { 
    private static final int MAX_MESSAGE_BYTES = 4 * 1024 * 1024;

    private static KafkaContainer kafka;
    private static ObjectMapper mapper;
    private String pagesTopic;
    private String urlsTopic;
    private String outlinksTopic;
    private String imagesTopic;

    @BeforeEach
    void freshTopics() {

    }

    @BeforeAll
    static void startBroker() {

    }

    @AfterAll
    static void stopBroker() {

    }

    private KafkaCrawlEventBus newBus() {

    }

    private static KafkaConsumer<String, String> newConsumer(String topic) {

    }

    private static PageEvent page(String url, String host, String html) {

    }

    @Test
    void pageEventSurvivesARoundTripThroughKafka() throws Exception {

    }

    @Test
    void vietnameseDiacriticsSurviveSerialization() throws Exception {

    }

    @Test
    void allUrlsOfOneHostLandOnTheSamePartition() {

    }

    @Test
    void differentHostsSpreadAcrossPartitions() {

    }

    @Test
    void largePageWithinTheRaisedLimitIsAccepted() throws Exception {

    }

    @Test
    void oversizedMessageIsCountedNotThrown() {

    }

    @Test
    void eachChannelGoesToItsOwnTopic() throws Exception {

    }

    private static ConsumerRecord<String, String> pollOne(KafkaConsumer<String, String> consumer) {
        
    }
}
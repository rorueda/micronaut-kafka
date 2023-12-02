package io.micronaut.configuration.kafka.offsets

import io.micronaut.configuration.kafka.AbstractEmbeddedServerSpec
import io.micronaut.configuration.kafka.ConsumerRegistry
import io.micronaut.configuration.kafka.annotation.KafkaClient
import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetStrategy
import io.micronaut.configuration.kafka.annotation.Topic
import io.micronaut.context.annotation.Requires
import io.micronaut.messaging.annotation.MessageBody
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.consumer.OffsetCommitCallback
import org.apache.kafka.common.TopicPartition

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

import static io.micronaut.configuration.kafka.annotation.OffsetReset.EARLIEST
import static io.micronaut.configuration.kafka.config.AbstractKafkaConfiguration.EMBEDDED_TOPICS

class FromAnotherThreadManualOffsetCommitSpec extends AbstractEmbeddedServerSpec {
    private static final String CONSUMER_ID = "fruit-client"
    private static final String TOPIC = "fruit"

    protected Map<String, Object> getConfiguration() {
        super.configuration +
                ['micrometer.metrics.enabled' : true,
                 'endpoints.metrics.sensitive': false,
                 (EMBEDDED_TOPICS)            : [TOPIC]]
    }

    void "test manual commits from thread != listener thread"() {
        given:
        ConsumerRegistry registry = context.getBean(ConsumerRegistry)
        FruitClient client = context.getBean(FruitClient)
        FruitListener listener = context.getBean(FruitListener)
        TopicPartition topicPartition = new TopicPartition(TOPIC, 0)

        when:
        Consumer consumer = registry.getConsumer(CONSUMER_ID)

        then:
        consumer

        when: "one item is added"
        client.send("test", "Apple")

        then:
        conditions.eventually {
            listener.fruits.size() == 1
        }

        and:
        listener.offsets.isEmpty()

        when: "commit is added for the item"
        Map<TopicPartition, OffsetAndMetadata> offsetsToCommit = [(topicPartition): new OffsetAndMetadata(2)]
        registry.commitAsync(CONSUMER_ID, offsetsToCommit)

        then:
        conditions.eventually {
            listener.offsets == offsetsToCommit
        }

        when: "two items are added"
        client.send("test", "Banana")
        client.send("test", "Orange")

        then:
        conditions.eventually {
            listener.fruits.size() == 3
        }

        and:
        listener.offsets == offsetsToCommit

        when: "commit is added for only one item"
        offsetsToCommit = [(topicPartition): new OffsetAndMetadata(3)]
        new Thread(() -> {
            Thread.sleep(500)
            registry.commitAsync(CONSUMER_ID, offsetsToCommit)
        }).start()

        then:
        conditions.eventually {
            listener.offsets == offsetsToCommit
        }
    }

    @Requires(property = 'spec.name', value = 'FromAnotherThreadManualOffsetCommitSpec')
    @KafkaClient
    static interface FruitClient {
        @Topic(TOPIC)
        void send(@KafkaKey String company, @MessageBody String fruit)
    }

    @Requires(property = 'spec.name', value = 'FromAnotherThreadManualOffsetCommitSpec')
    @KafkaListener(clientId = CONSUMER_ID, offsetReset = EARLIEST, offsetStrategy = OffsetStrategy.ASYNC_MANUAL)
    static class FruitListener implements OffsetCommitCallback {
        Set<String> fruits = new ConcurrentSkipListSet<>()
        Map<TopicPartition, OffsetAndMetadata> offsets = new ConcurrentHashMap<>()

        @Topic(TOPIC)
        void receive(@MessageBody String name) {
            fruits << name
        }

        @Override
        void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception exception) {
            this.offsets << offsets
        }
    }
}

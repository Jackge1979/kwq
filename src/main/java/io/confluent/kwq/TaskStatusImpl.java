package io.confluent.kwq;

import io.confluent.kwq.streams.TaskStatsCollector;
import io.confluent.kwq.streams.model.TaskStats;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

public class TaskStatusImpl implements TaskStatus {

  private static final String TASK_STATUS_TOPIC = "taskStatusTopic";
  private final KafkaProducer<String, Task> producer;
  private TaskStatsCollector taskStatsCollector;

  public TaskStatusImpl(String bootstrapServers){
    producer = new KafkaProducer<>(producerProperties(bootstrapServers), new StringSerializer(), new TaskSerDes());

    startStreamsJobs(bootstrapServers);
  }

  private void startStreamsJobs(String bootstrapServers) {
    // TODO: Inject collection of streams apps
    taskStatsCollector = new TaskStatsCollector(TASK_STATUS_TOPIC, streamsProperties(bootstrapServers, "total-events"), 60);
    taskStatsCollector.start();
  }


  @Override
  public void update(Task task) {
    producer.send(new ProducerRecord<>(TASK_STATUS_TOPIC, task.getId(), task));
  }

  @Override
  public TaskStats getStats() {
    return taskStatsCollector.getCurrentStats();
  }

  public long getTotalThroughputPer() {
    return taskStatsCollector.getLastWindowStats().getTotal();
  }
//  public long getRunningCountPer() {
//    return taskStatsCollector.getRunning();
//  }
//  public long getErrorCountPer() {
//    return taskStatsCollector.getError();
//  }
//  public long getCompleted() {
//    return taskStatsCollector.getCompleted();
//  }



  private Properties producerProperties(String bootstrapServers) {
    Properties producerConfig = new Properties();
    producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServers);
    producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
    producerConfig.put(ProducerConfig.RETRIES_CONFIG, 0);
    return producerConfig;
  }


  private StreamsConfig streamsProperties(String bootstrapServers, String applicationId){
    Properties config = new Properties();
    config.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
    config.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    config.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, StringSerializer.class);
    config.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, TaskSerDes.class);

    return new StreamsConfig(config);
  }

}

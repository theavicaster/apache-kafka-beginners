package basics;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerDemo {
	
	public static void main(String args[]) {
		
		String bootstrapServers = "127.0.0.1:9092";
		
		//Create producer properties
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		
		//Create the producer
		KafkaProducer<String, String> producer = new KafkaProducer<String,String>(properties);
		
		//Create a producer record
		ProducerRecord<String,String> record = 
				new ProducerRecord<String,String>("first_topic", "hello world!");
		
		//Send data - Asynchronous
		//Adds to a buffer of messages to be sent
		producer.send(record);
		
		//Flush data
		//Actually sends forward those messages from the buffer
		producer.flush();
		
		//Close producer
		//This is to prevent resource leakage
		producer.close();
		
	}

}

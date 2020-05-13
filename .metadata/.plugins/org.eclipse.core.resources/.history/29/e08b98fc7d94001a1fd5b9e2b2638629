package consumer;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParser;

public class ElasticSearchConsumer {
	
	   public static RestHighLevelClient createClient(){
	
	        String hostname = "kafka-consumer-123473657.ap-southeast-2.bonsaisearch.net"; 
	        String username = "hp7pv4bnu3"; 
	        String password = "he70gasby0"; 
	
	        // credentials provider help supply username and password
	        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	        credentialsProvider.setCredentials(AuthScope.ANY,
	                new UsernamePasswordCredentials(username, password));
	
	        RestClientBuilder builder = RestClient.builder(
	                new HttpHost(hostname, 443, "https"))
	                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
	                    @Override
	                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
	                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
	                    }
	                });
	
	        RestHighLevelClient client = new RestHighLevelClient(builder);
	        return client;
	    }
	   
	   public static KafkaConsumer<String,String> createConsumer(String topic) {
		   
		    String bootstrapServers = "127.0.0.1:9092";
		    String groupId = "kafka-demo-elasticsearch";
		
		    //Create consumer configurations
		    Properties properties = new Properties();
		    properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		    properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		    properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		    properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		    properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); //manually commit offsets
		    properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10");
		
		    // create consumer
		    KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
		    
		    consumer.subscribe(Arrays.asList(topic));
		    
		    return consumer;
		    		   
	   }
	   
	   private static JsonParser jsonParser = new JsonParser();
	   
	   private static String extractIdFromTweet(String jsonTweet) {
		   
		   return jsonParser.parse(jsonTweet)
		   			 .getAsJsonObject()
		   			 .get("id_str")
		   			 .getAsString();
		   
	   }
	   
	   public static void main(String[] args) throws IOException {
		
		   Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class.getName());
		   
		   RestHighLevelClient client = createClient();
		   
		   KafkaConsumer<String,String> consumer = createConsumer("twitter_tweets");
		   
	        // poll for new data
	        while(true){
	            ConsumerRecords<String, String> records =
	                    consumer.poll(Duration.ofMillis(100)); // new in Kafka 2.0.0

	            Integer recordCount = records.count();
	            logger.info("Received "+ recordCount +" records");
	            
	            BulkRequest bulkRequest = new BulkRequest();
	            
	            for (ConsumerRecord<String, String> record : records){
	            	
	               //2 Strategies for making id
	               //Kafka generic ID
	               // String id = record.topic() + "_" + record.partition + "_" + record.offset();
	            	
	            	try {
						String id = extractIdFromTweet(record.value());
						
			            //Insert value into ElasticSearch	               
			 		   IndexRequest indexRequest = new IndexRequest(
										   "twitter",
										   "tweets",
										   id //ID to make consumer idempotent
									).source(record.value(), XContentType.JSON);
			 		   
			 		   bulkRequest.add(indexRequest);
								} catch (NullPointerException e) {
									// TODO Auto-generated catch block
									logger.warn("skipping bad data "+ record.value());
								}
	    		   
	    		   
	            }
	            
	            if(recordCount > 0)
	            {
		            BulkResponse bulkItemResponses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
		            
		            logger.info("Committing offsets");
		            consumer.commitSync();
		            logger.info("Offsets commited");
		            try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	        }
		   
		   //client.close();   
		   
	   }
	

}

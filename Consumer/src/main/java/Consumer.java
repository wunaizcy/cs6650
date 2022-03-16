import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {
  private final static String QUEUE_NAME = "threadExQ";
  private final static Integer NUM_THREAD = 100;
  public static void main(String[] argv) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("52.35.92.145");
    factory.setUsername("test");
    factory.setPassword("test");
    final Connection connection = factory.newConnection();
    Map<String,List<String>> map = new ConcurrentHashMap<>();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, true, false, false, null);
          // max one message per receiver
          channel.basicQos(1);
          System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            String[] messageParts = message.split("/");
            //put data in map with key = skierId and value to be a list of json body.
            putInMap(map ,messageParts[0],messageParts[1]);
          };
          // process messages
          channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> { });
        } catch (IOException ex) {
          Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    };
    // start threads and block to receive messages
    for(int i=0;i < NUM_THREAD;i++){
      Thread t = new Thread(runnable);
      t.start();
    }
  }

  private static synchronized void putInMap(Map<String,List<String>> map,String skiId, String jsonBody){
    if(!(map.containsKey(skiId))){
      List<String> list = new CopyOnWriteArrayList<>();
      list.add(jsonBody);
      map.put(skiId, list);
    } else{
      map.get(skiId).add(jsonBody);
    }
  }
}

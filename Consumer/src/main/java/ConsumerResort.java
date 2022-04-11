import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class ConsumerResort {
  private final static String QUEUE_NAME = "threadExQ2";
  private final static Integer NUM_THREAD = 200;
  public static void main(String[] argv) throws Exception {
//    JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
//    jedisPoolConfig.setBlockWhenExhausted(true);
//    jedisPoolConfig.setMaxTotal(5000);
//    jedisPoolConfig.setMaxIdle(5000);
//    JedisPool jedisPool = new JedisPool(jedisPoolConfig, "34.218.200.31", 6379);
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost("34.209.86.209");
    factory.setUsername("test");
    factory.setPassword("test");
    final Connection connection = factory.newConnection();
    Map<String,List<ArrayList<Integer>>> map = new ConcurrentHashMap<>();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          Jedis jedis = new Jedis("52.33.175.251", 6379);
          final Channel channel = connection.createChannel();
          channel.queueDeclare(QUEUE_NAME, true, false, false, null);
          // max one message per receiver
          channel.basicQos(1);
          System.out.println("Resort [*] Thread waiting for messages. To exit press CTRL+C");

          DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            String[] messageParts = message.split("/");
            ArrayList<Integer> clearnedData = cleanData(messageParts[1]);
            //put data in map with key = skierId and value to be a list of json body.
            writeIntoRedisResort(messageParts[0],clearnedData,jedis);
            //putInMap(map ,messageParts[0],clearnedData);
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
  private static void writeInRedis(String skiId,ArrayList<Integer> data, Jedis jedis){
    //[skier + id]: set(day)
    //[skier + id + day + vertical] : lift * 10
    //[skier + id + day + lift] : set(lift id)
    String skierDay = "skier" + skiId;
    String skierDayVertical = "skier" + skiId + "day" + data.get(4) + "vertical";
    String skierDayLift = "skier" + skiId + "day" + data.get(4) + "lift";
    //System.out.println(skierDay + ',' + skierDayVertical + ',' + skierDayLift);
    //write days for a skier
    jedis.sadd(skierDay, String.valueOf(data.get(4)));

    //write vertical total for each ski day
    jedis.incrBy(skierDayVertical, data.get(1) * 10);
    //jedis.sadd(skierDayVertical,String.valueOf(data.get(1)*10));
//    if (jedis.exists(skierDayVertical)){
//      jedis.set(skierDayVertical,String.valueOf(Integer.parseInt(jedis.get(skierDayVertical)) + data.get(1) * 10));
//    }else{
//      jedis.set(skierDayVertical,String.valueOf(data.get(1)*10));
//    }
//
//    //write lifts skier ride on each day
    jedis.sadd(skierDayLift,String.valueOf(data.get(1)));

  }
  private static void writeIntoRedisResort(String skiId,ArrayList<Integer> data, Jedis jedis){
    // [day + $day + resort + resort id] = set(skiid)
    // [day + $day + lift + liftid] = incr
    // [day + $day + hour] = incr
    String dayResortVisited = "day" + data.get(4) + "resort" + data.get(3);
    String dayLiftHappened = "day" + data.get(4) + "lift" + data.get(1);
    String dayLiftHour = "day" + data.get(4) + "hour" + Math.floor(data.get(0)/60);

    //write unique skier visited resort x on day N
    jedis.sadd(dayResortVisited, skiId);

    //write rides on lift N on day N

    jedis.incr(dayLiftHappened);

    //write lift rides for each hour on day N

    jedis.incr(dayLiftHour);
  }
  private static ArrayList<Integer> cleanData(String jasonBody){
    //[0] = time, [1] = liftId, [2] = wait time [3] = resortId [4] = days
    ArrayList<Integer> temp = new ArrayList<>();
    String[] splitJasonBody = jasonBody.split(",");
    for (String s: splitJasonBody) {
      String stringNumber = s.replaceAll("[^0-9]","");
      temp.add(Integer.parseInt(stringNumber));
    }
    return temp;
  }

  private static synchronized void putInMap(Map<String,List<ArrayList<Integer>>> map,String skiId, ArrayList<Integer> clearnedData){
    System.out.println(skiId + ": " + clearnedData.toString());
    if(!(map.containsKey(skiId))){
      List<ArrayList<Integer>> list = new CopyOnWriteArrayList<>();
      list.add(clearnedData);
      map.put(skiId, list);
    } else{
      map.get(skiId).add(clearnedData);
    }
  }

}

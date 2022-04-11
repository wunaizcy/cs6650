import java.util.List;
import java.util.Set;
import redis.clients.jedis.Jedis;

public class RedisTest {

  public static void main(String[] args){
    Jedis jedis = new Jedis("52.33.175.251" ,6379);
    Jedis jedis2 = new Jedis("34.218.200.31" ,6379);
    //[skier + id]: day
    //[skier + id + day + vertical] : lift * 10
    //[skier + id + day + lift] : set(lift id)
    System.out.println(jedis2.ping());
    Set<String> set = jedis2.keys("*");
    Set<String> set2 = jedis.keys("*");
    System.out.println("ski: " + set.size());
    System.out.println("day " + set2.size());
//    for(String s: set){
//      if (jedis2.type(s).equals("set")){
//        System.out.println("Key " + s + ":" + jedis2.smembers(s));
//      }
//      else{
//        System.out.println("Key " + s + ":" + jedis2.get(s));
//      }
//    }
    //jedis.sadd("test","1","2","3");
//    jedis.sadd("skier" + String.valueOf(5), String.valueOf(56));
//    jedis.sadd("skier" + String.valueOf(5), String.valueOf(58));
//    //jedis.set("test1",String.valueOf(5 * 100));
//    System.out.println(jedis.get("test2"));
//    //jedis.set("test1",String.valueOf(Integer.parseInt(jedis.get("test2")) + 3 * 100));
//    System.out.println(jedis.get("test2"));
//
//    Set<String> set = jedis.smembers("skier" + String.valueOf(5));
//    System.out.println(set.size());
    jedis.flushAll();
    jedis2.flushAll();
  }
}

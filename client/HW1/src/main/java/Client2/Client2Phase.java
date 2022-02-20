package Client2;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Client2Phase {
  private static final SecureRandom randomId = new SecureRandom();
  private static final SecureRandom randomTime = new SecureRandom();
  public static List<Long> response = new ArrayList<>();
  public static List<List<String>> records = new ArrayList<>();
  public static int totalRequest;

  public static void sortList(){
    if (Client2Phase.response != null || Client2Phase.response.size() != 0){
      Collections.sort(Client2Phase.response);
    }
  }

  public static long findAverage(){
    long sum = 0;
    for (long i: Client2Phase.response){
      sum += i;
    }
    return sum/Client2Phase.response.size();
  }

  public static long percentile(double percentile) {
    sortList();
    int index = (int) Math.ceil(percentile / 100.0 * response.size());
    return response.get(index - 1);
  }

  public void startPost(int numThreads,int numRuns, int minutesHigh,
      int minutesLow, int numberOfSkier, int nthThread, final String url)
      throws InterruptedException {
    int idBase1 = numberOfSkier / numThreads;
    int idLowBound = 1 + nthThread * idBase1;
    int idHighBound = idBase1 * (nthThread + 1);
    CountDownLatch complete = new CountDownLatch(numRuns);
    for (int i = 0; i < numRuns; i ++){
      Runnable thread = () -> {
        int skiId = randomId.nextInt((idHighBound - idHighBound + 1)) + idLowBound;
        int time = randomTime.nextInt((minutesHigh - minutesLow + 1)) + minutesLow;
        post(skiId, time, url);
        complete.countDown();
      };
      new Thread(thread).start();
    }
    complete.await();
  }

  public void post(int skiId, int time, String url){
    SkiersApi apiInstance = new SkiersApi();
    apiInstance.getApiClient().setBasePath(url);
    Integer resortID = 56; // Integer | ID of the resort of interest
    String seasonID = "56"; // Integer | ID of the resort of interest
    String dayID = "56"; // Integer | ID of the resort of interest
    Integer skierID = skiId;
    LiftRide liftRide = new LiftRide();
    liftRide.setLiftID(new Random().nextInt(200));
    liftRide.setTime(time);
    liftRide.setWaitTime(new Random().nextInt(10));
    List<String> record = new ArrayList<>();
    long start = System.currentTimeMillis();
    record.add(String.valueOf(start));
    record.add("post");
    int responseCode;
    try {
      ApiResponse result = apiInstance.writeNewLiftRideWithHttpInfo(liftRide, resortID, seasonID,dayID,skierID);
      responseCode = result.getStatusCode();
      System.out.println(result.getStatusCode());
    } catch (ApiException e) {
      System.err.println("Exception when calling ResortsApi#getResortSkiersDay");
      e.printStackTrace();
      responseCode = 500;
    }
    long end = System.currentTimeMillis();
    record.add(String.valueOf(end - start));
    record.add(String.valueOf(responseCode));
    appendResponse(end - start);
    appendRecord(record);
    Client2Phase.totalRequest += 1;
  }
  synchronized public void appendResponse(long response){
    Client2Phase.response.add(response);
  }

  synchronized public void appendRecord(List<String> record){
    Client2Phase.records.add(record);
  }

}

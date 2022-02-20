package Client1;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class SampleTest {
  final static private int NUMTHREADS = 10000;

  public void post(){
    SkiersApi apiInstance = new SkiersApi();
    apiInstance.getApiClient().setBasePath("http://54.187.46.172:8080/HW1Server_war/skiers");
    Integer resortID = 56; // Integer | ID of the resort of interest
    String seasonID = "56"; // Integer | ID of the resort of interest
    String dayID = "56"; // Integer | ID of the resort of interest
    Integer skierID = 123;
    try {
      ApiResponse result = apiInstance.writeNewLiftRideWithHttpInfo(new LiftRide(), resortID, seasonID,dayID,skierID);
      System.out.println(result.getStatusCode());
    } catch (ApiException e) {
      System.err.println("Exception when calling ResortsApi#getResortSkiersDay");
      e.printStackTrace();

    }
  }
  public static void main(String[] arg) throws InterruptedException {
    final SampleTest sampleTest = new SampleTest();
    CountDownLatch countDownLatch = new CountDownLatch(NUMTHREADS);
    long start = System.currentTimeMillis();
    
    for (int i = 0; i < NUMTHREADS; i++){
      Runnable thread = () -> {
        sampleTest.post();
        countDownLatch.countDown();
      };
      new Thread(thread).start();
    }
    countDownLatch.await();
    long end = System.currentTimeMillis();
    System.out.println("Total Request: " + NUMTHREADS);
    System.out.println("Took " + (end - start)/1000 + " second");
    System.out.println("Number of threads / single thread latency: " + NUMTHREADS/((end - start)/1000));
  }
}

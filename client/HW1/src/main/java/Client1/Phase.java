package Client1;

import com.sun.xml.bind.v2.TODO;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class Phase {
  private static final SecureRandom randomId = new SecureRandom();
  private static final SecureRandom randomTime = new SecureRandom();
  public static int success = 0;
  public static int fail = 0;
  public void startPost(int numThreads,int numRuns, int minutesHigh,
      int minutesLow, int numberOfSkier, int nthThread, final String url)
      throws InterruptedException {
    int idBase1 = numberOfSkier / numThreads;
    int idLowBound = 1 + nthThread * idBase1;
    int idHighBound = idBase1 * (nthThread + 1);
    CountDownLatch complete = new CountDownLatch(numRuns);
    System.out.println(numRuns);
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
    try {
      ApiResponse result = apiInstance.writeNewLiftRideWithHttpInfo(liftRide, resortID, seasonID,dayID,skierID);
      System.out.println(result.getStatusCode());
      Phase.success += 1;
    } catch (ApiException e) {
      System.err.println("Exception when calling ResortsApi#getResortSkiersDay");
      e.printStackTrace();
      Phase.fail += 1;
    }
  }


}

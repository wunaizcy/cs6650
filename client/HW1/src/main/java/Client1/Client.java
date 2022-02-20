package Client1;

import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SkiersApi;
import io.swagger.client.model.LiftRide;
import java.util.concurrent.CountDownLatch;


public class Client {

  public static void main(String[] args) throws InterruptedException {
    int numberThreads = 1024;
    if (numberThreads >Integer.parseInt(args[0])){
      numberThreads = Integer.parseInt(args[0]);
    }
    int numberOfSkier = 100000;
    if (numberOfSkier  >Integer.parseInt(args[1])){
      numberOfSkier = Integer.parseInt(args[1]);
    }

    int numLifts = 40;
    if (Integer.parseInt(args[2]) > 5 && Integer.parseInt(args[2]) < 60) {
      numLifts = Integer.parseInt(args[2]);
    }

    int numRuns;
    if (Integer.parseInt(args[3]) > 20){
      numRuns = 10;
    }else{
      numRuns = 20;
    }
    String url = args[4];
    long start = System.currentTimeMillis();
    int numberOfRunsFactor = numRuns *(numberOfSkier/numberThreads);
    CountDownLatch countDownLatchTotal = new CountDownLatch((int)( numberThreads/4 + numberThreads + numberThreads/10));
    CountDownLatch countDownLatch1 = new CountDownLatch((int)(0.2*numberThreads/4));
    for (int i = 0; i < numberThreads/4; i++) {
      int finalI = i;
      int finalNumberThreads = numberThreads;
      int finalNumberOfSkier = numberOfSkier;
      Runnable thread = () ->{
      Phase phase1 = new Phase();
        try {
          phase1.startPost(finalNumberThreads /4,
              (int) (0.2* numberOfRunsFactor*4),
              90, 1
              , finalNumberOfSkier, finalI, url);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        countDownLatch1.countDown();
        countDownLatchTotal.countDown();
    };
      new Thread(thread).start();
    }

    countDownLatch1.await();
    CountDownLatch countDownLatch2 = new CountDownLatch((int)(numberThreads*0.2));
    for (int j=0; j< numberThreads;j++){
      int finalJ = j;
      int finalNumberThreads1 = numberThreads;
      int finalNumberOfSkier1 = numberOfSkier;
      Runnable thread = () ->{
        Phase phase2 = new Phase();
        try{
          phase2.startPost(finalNumberThreads1,(int)(0.6*numberOfRunsFactor)
              ,360,91, finalNumberOfSkier1,finalJ,url );
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        countDownLatch2.countDown();
        countDownLatchTotal.countDown();
      };
      new Thread(thread).start();
    }
    countDownLatch2.await();

    CountDownLatch countDownLatch3 = new CountDownLatch((int)(numberThreads*0.1));
    for (int m=0; m< numberThreads/10;m++){
      int finalM = m;
      int finalNumberThreads2 = numberThreads;
      int finalNumberOfSkier2 = numberOfSkier;
      Runnable thread = () ->{
        Phase phase3 = new Phase();
        try{
          phase3.startPost(finalNumberThreads2,(int)(0.1*numRuns)
              ,420,361, finalNumberOfSkier2,finalM,url );
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        countDownLatch3.countDown();
        countDownLatchTotal.countDown();
      };
      new Thread(thread).start();
    }
    countDownLatch3.await();
    countDownLatchTotal.await();
    long end = System.currentTimeMillis();
    int totalRequest = Phase.success + Phase.fail;
    long totalTime = (end - start)/1000;
    System.out.println("Total Threads: " + numberThreads);
    System.out.println("Total skiers: " + numberOfSkier);
    System.out.println("Total success requests: " + Phase.success);
    System.out.println("Total failed requests: " + Phase.fail);
    System.out.println("took " + totalTime + " seconds");
    System.out.println("Wall Time: " + (totalRequest/totalTime));
    }
}

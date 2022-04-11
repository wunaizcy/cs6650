package Servlet;

import PooledChannel.ChannelService;
import PooledChannel.Send;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;


public class SkierServlet extends javax.servlet.http.HttpServlet {
  private final static String QUEUE_NAME = "threadExQ";
  private final static String QUEUE_TWO_NAME = "threadExQ2";
  private ChannelService channelService;

  @Override
  public void init(ServletConfig config) throws ServletException{
    super.init(config);
    channelService = ChannelService.getInstance();
 }

  protected void doPost(javax.servlet.http.HttpServletRequest req,
      javax.servlet.http.HttpServletResponse res)
      throws javax.servlet.ServletException, IOException {
    res.setContentType("application/json");
    //get urlPath and parse body
    String urlPath = req.getPathInfo();
    StringBuilder jsonSb = new StringBuilder();
    String s;
    while ((s = req.getReader().readLine()) != null) {
      jsonSb.append(s);
    }
    // check we have a URL!
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_OK);
      res.getWriter().write("missing paramterers");
      return;
    }

    String[] urlParts = urlPath.split("/");
    // and now validate url path and return the response status code
    // (and maybe also some value if input is valid)

    if (!isUrlValid(urlParts)) {
      res.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
      res.getWriter().write("Invalid data.");
    } else {
      // try send to channel
     try {
        String newJsonBody = jsonSb.toString() + ',' + urlParts[2] + ','+ urlParts[6];
        sendToChannel(String.valueOf(urlParts[8]), newJsonBody);
      } catch (IOException | TimeoutException ex) {
       Logger.getLogger(SkierServlet.class.getName()).log(Level.SEVERE, null, ex);
    }
      res.setStatus(HttpServletResponse.SC_CREATED);
    }
  }
  private void sendToChannel(String skiId, String json) throws IOException, TimeoutException {
    try {
      // channel per thread
      Channel channel = channelService.getChannel();
      String fanoutExchange = "fanout-exchange";
      channel.exchangeDeclare(fanoutExchange, BuiltinExchangeType.FANOUT,true);
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
      channel.queueBind(QUEUE_NAME, fanoutExchange,"");
      channel.queueDeclare(QUEUE_TWO_NAME,true,false,false,null);
      channel.queueBind(QUEUE_TWO_NAME,fanoutExchange,"");
      String message = skiId +"/" + json;
      channel.basicPublish(fanoutExchange, "", null, message.getBytes(StandardCharsets.UTF_8));
      channelService.returnChannel(channel);

    } catch (IOException | TimeoutException ex) {
      Logger.getLogger(Send.class.getName()).log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void doGet(javax.servlet.http.HttpServletRequest request,
      javax.servlet.http.HttpServletResponse response)
      throws javax.servlet.ServletException, IOException {

  }

  private boolean isUrlValid(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    for (int i = 2; i < urlPath.length; i+=2){
      if (!isNumeric(urlPath[i])){
        System.out.println(urlPath[i]);
        return false;
      }
    }
    if (!(urlPath[3].equals("seasons")) || !(urlPath[5].equals("days"))|| !(urlPath[7].equals("skiers"))){
      return false;
    }
    return true;
  }

  private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

  public boolean isNumeric(String strNum) {
    if (strNum == null) {
      return false;
    }
    return pattern.matcher(strNum).matches();
  }
}

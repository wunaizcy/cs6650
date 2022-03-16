package PooledChannel;
import Servlet.SkierServlet;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class PoolChannelFactory implements PooledObjectFactory<Channel> {
  private Connection connection;


  public PoolChannelFactory() {
    try{
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("52.35.92.145");
      factory.setUsername("test");
      factory.setPassword("test");
      this.connection = factory.newConnection();
    }catch (IOException | TimeoutException e){
    e.printStackTrace();}
  }


  @Override
  public void activateObject(PooledObject<Channel> pooledObject) throws Exception {

  }

  @Override
  public void destroyObject(PooledObject<Channel> pooledObject) throws Exception {

  }

  @Override
  public PooledObject<Channel> makeObject() throws Exception {
    return new DefaultPooledObject<>(connection.createChannel());
  }

  @Override
  public void passivateObject(PooledObject<Channel> pooledObject) throws Exception {

  }

  @Override
  public boolean validateObject(PooledObject<Channel> pooledObject) {
    return false;
  }
}

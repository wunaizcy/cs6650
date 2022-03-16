package PooledChannel;

import com.rabbitmq.client.Channel;

public class ChannelService {
  private ChannelPool pool;

  public static ChannelService getInstance(){
    return SingletonHolder.INSTANCE;
  }

  public Channel getChannel() throws Exception{
    return pool.borrowObject();
  }

  public void returnChannel(Channel channel){
    pool.returnObject(channel);
  }

  private static class SingletonHolder{
    private  final static ChannelService INSTANCE = new ChannelService();
  }

  private ChannelService(){
    initPool();
  }

  private void initPool(){
    PoolChannelFactory factory = new PoolChannelFactory();
    pool = new ChannelPool(factory);
  }
}

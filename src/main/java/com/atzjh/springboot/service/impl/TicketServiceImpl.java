package com.atzjh.springboot.service.impl;

import cn.hutool.json.JSON;
import com.alibaba.fastjson.JSON;
import com.atzjh.springboot.api.vo.ResultInfo;
import com.atzjh.springboot.entity.Ticket;
import com.atzjh.springboot.service.TicketService;
import com.atzjh.springboot.vo.ResultInfo;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 车票服务接口
 *
 * @author zhangjh
 * @date 2020-03-01
 */
@Service
public class TicketServiceImpl implements TicketService {

    private final static String STOCKLOCKKEY = "stock-lock-key-";

    private final static String REDISLOCKKEY = "redis-lock-key-";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private Redisson redisson;

    /**
     * 重置库存
     *
     * @param ticket
     * @return
     */
    @Override
    public Object add(Ticket ticket) {
        redisTemplate.opsForValue().set(STOCKLOCKKEY + JSON.toJSONString(ticket.getId()), JSON.toJSONString(ticket));
        return new ResultInfo<>();
    }

    /**
     * 当高并发情况下,会同时执行查询库存操作,会发生超卖;程序挂机也会出现死锁
     * --获取当前剩余车票库存
     * --Object result = redisTemplate.opsForValue().get(stock + id);
     * --Ticket ticket = JSON.parseObject(result.toString(), Ticket.class);
     * --long stock = ticket.getInventory();
     *
     * @param id
     * @return
     */
    @Override
    public Object get(long id) {
        try {
            // 获取当前剩余车票库存
            Object result = redisTemplate.opsForValue().get(STOCKLOCKKEY + id);
            Ticket ticket = JSON.parseObject(result.toString(), Ticket.class);
            long currentStock = ticket.getInventory();// 当前库存
            if (currentStock > 0) {
                // 将库存减去1
                ticket.setInventory(currentStock - 1);
                // 将剩余票的信息放入到缓存中
                redisTemplate.opsForValue().set(STOCKLOCKKEY + JSON.toJSONString(ticket.getId()), JSON.toJSONString(ticket));
                System.out.println("出票成功,id:" + id + ", 车票编号:" + currentStock + ", 库存剩余:" + (currentStock - 1));
                return new ResultInfo<>(returnMap(ticket, currentStock));
            }
            // 无票操作
            else {
                System.out.println("出票失败,库存扣减失败,暂时无票");
                return new ResultInfo<>("1", "出票失败,库存扣减失败,暂时无票", returnMap(null, 0));
            }
            // 有票操作
        } catch (Exception e) {
            return new ResultInfo<>("1", JSON.toJSONString(e));
        }
    }

    /**
     * SETNX key value:
     * --当键key不存在时,将key的值设置为value,
     * --当键key存在时,则setnx不做任何操作
     * --SETNX是[set if Not exists](如果不存在，则set的简写)
     * <p>
     * 问题：
     * 1、锁误删。A线程加的锁时间到期,导致锁失效,则B线程可直接进来。线程B刚进来,然后先进来的线程A(它的锁已经到期)将B加的锁删除掉,循环往复
     * 2、锁失效。A线程业务执行时间太长,导致A加的锁时间过期,还是会导致锁失效。以至于A线程还没执行完,B线程就进来了,循环往复
     * 3、锁同步不及时：redis集群模式下,当A线程加的锁存到master节点上时,master节点挂掉,此时数据还未同步到它的slave节点,导致slave节点也没锁数据
     * ---------------这时B线程或者其它的线程发现redis中没有锁,则会进行争抢,这时A线程未执行完,其它线程就进来了
     *
     * @param id
     * @return
     */
    @Override
    public Object getLock(long id) {
        try {
            // 执行SETNX操作,进来先加锁,枷锁之后,下一个进来的直接返回
            // 下边加锁成功返回true,如果已存在锁则不做任何操作,返回false
            // 相当于jedis的先set在expire
            Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("basic_lock_key", "basic_lock_key", 30, TimeUnit.SECONDS);
            if (!aBoolean) {
                return new ResultInfo<>("1", "请重试...");
            }

            // 获取当前剩余车票库存
            Object result = redisTemplate.opsForValue().get(STOCKLOCKKEY + id);
            Ticket ticket = JSON.parseObject(result.toString(), Ticket.class);
            // 当前库存
            long currentStock = ticket.getInventory();
            // 有票操作
            if (currentStock > 0) {
                // 将库存减去1
                ticket.setInventory(currentStock - 1);
                // 将剩余票的信息放入到缓存中
                redisTemplate.opsForValue().set(STOCKLOCKKEY + JSON.toJSONString(ticket.getId()), JSON.toJSONString(ticket));
                System.out.println("出票成功,id:" + id + ", 车票编号:" + currentStock + ", 库存剩余:" + (currentStock - 1));
                return new ResultInfo<>(returnMap(ticket, currentStock));
            } else {
                // 无票操作
                System.out.println("出票失败,库存扣减失败,暂时无票");
                return new ResultInfo<>("1", "出票失败,库存扣减失败,暂时无票", returnMap(null, 0));
            }

        } catch (Exception e) {
            return new ResultInfo<>("1", JSON.toJSONString(e));
        } finally {
            // 释放锁
            redisTemplate.delete("basic_lock_key");
        }
    }

    /**
     * 解决了锁误删,为解决锁失效
     * <p>
     * 问题：
     * 1、锁失效。A线程业务执行时间太长,导致A加的锁时间过期,还是会导致锁失效。以至于A线程还没执行完,B线程就进来了,循环往复
     * 2、锁同步不及时：redis集群模式下,当A线程加的锁存到master节点上时,master节点挂掉,此时数据还未同步到它的slave节点,导致slave节点也没锁数据
     * ---------------这时B线程或者其它的线程发现redis中没有锁,则会进行争抢,这时A线程未执行完,其它线程就进来了
     *
     * @param id
     * @return
     */
    @Override
    public Object getLockClientId(long id) {
        // 随机生成一个uuid
        // 每个线程都是独立的,每个线程都会生成clientId,这个clientId并不是公有的
        String clientId = UUID.randomUUID().toString();
        try {
            // 执行SETNX操作,进来先加锁,枷锁之后,下一个进来的直接返回
            // 相当于jedis的先set在expire
            Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent("basic_lock_key", clientId, 30, TimeUnit.SECONDS);
            if (!aBoolean) {
                return new ResultInfo<>("1", "请重试...");
            }

            // 获取当前剩余车票库存
            Object result = redisTemplate.opsForValue().get(STOCKLOCKKEY + id);
            Ticket ticket = JSON.parseObject(result.toString(), Ticket.class);
            // 当前库存
            long currentStock = ticket.getInventory();
            // 有票操作
            if (currentStock > 0) {
                // 将库存减去1
                ticket.setInventory(currentStock - 1);
                // 将剩余票的信息放入到缓存中
                redisTemplate.opsForValue().set(STOCKLOCKKEY + JSON.toJSONString(ticket.getId()), JSON.toJSONString(ticket));
                System.out.println("出票成功,id:" + id + ", 车票编号:" + currentStock + ", 库存剩余:" + (currentStock - 1));
                return new ResultInfo<>(returnMap(ticket, currentStock));
            } else {
                // 无票操作
                System.out.println("出票失败,库存扣减失败,暂时无票");
                return new ResultInfo<>("1", "出票失败,库存扣减失败,暂时无票", returnMap(null, 0));
            }

        } catch (Exception e) {
            return new ResultInfo<>("1", JSON.toJSONString(e));
        } finally {
            // 释放锁
            // 每个线程都是独立的,每个线程都会生成clientId,这个clientId并不是公有的
            // 所以可以用当前线程生成的clientId跟公有的锁值进行比较,如果相同则认为是自己加的锁,如果不同则不是自己加的
            if (clientId.equals(redisTemplate.opsForValue().get("basic_lock_key"))) {
                redisTemplate.delete("basic_lock_key");
            }
        }
    }

    /**
     * 问题：
     * 1、锁同步不及时：redis集群模式下,当A线程加的锁存到master节点上时,master节点挂掉,此时数据还未同步到它的slave节点,导致slave节点也没锁数据
     * ------------这时B线程或者其它的线程发现redis中没有锁,则会进行争抢,这时A线程未执行完,其它线程就进来了
     * 这个问题存在于redis cluster集群和redis主从复制集群中
     * <p>
     * 解决方式zookeeper
     *
     * @param id
     * @return
     */
    @Override
    public Object getLockRedission(long id) {
        // 锁的key
        String key = "redission_key";
        // reddisson生成锁对象
        RLock redissonLock = redisson.getLock(key);
        try {

            // 1、这步加锁类似于(redisTemplate.opsForValue().setIfAbsent(key, "value", 30, TimeUnit.SECONDS);
            // 2、同时redisson底层开始fork一个分线程,只要锁未被删掉,就会对锁进行超时时间的续命
            // 3、这个方法会允许第一个线程进来,其它的线程则进行while死循环,尝试加锁(相当于自旋),直到所有线程拿到锁为止;
            // 注意这第3个面试问过：其它的线程未抢到锁如何处理的?。则可回答第三个
            //存在锁互斥和可重入锁机制，与自动延期机制
            //redisson实现通过lua脚本，保证复杂逻辑业务的原子性
            redissonLock.lock(30, TimeUnit.SECONDS);

            // 获取当前剩余车票库存
            Object result = redisTemplate.opsForValue().get(STOCKLOCKKEY + id);
            Ticket ticket = JSON.parseObject(result.toString(), Ticket.class);
            long currentStock = ticket.getInventory();// 当前库存
            // 有票操作
            if (currentStock > 0) {
                // 将库存减去1
                ticket.setInventory(currentStock - 1);
                // 将剩余票的信息放入到缓存中
                redisTemplate.opsForValue().set(STOCKLOCKKEY + JSON.toJSONString(ticket.getId()), JSON.toJSONString(ticket));
                System.out.println("出票成功,id:" + id + ", 车票编号:" + currentStock + ", 库存剩余:" + (currentStock - 1));
                return new ResultInfo<>(returnMap(ticket, currentStock));
            } else {
                // 无票操作
                System.out.println("出票失败,库存扣减失败,暂时无票");
                return new ResultInfo<>("1", "出票失败,库存扣减失败,暂时无票", returnMap(null, 0));
            }

        } catch (Exception e) {
            return new ResultInfo<>("1", JSON.toJSONString(e));
        } finally {
            // 释放锁
            redissonLock.unlock();
        }
    }


    /**
     * 封装的抢票返回值
     *
     * @param ticket       为null时，抢票失败
     * @param currentStock
     * @return
     */
    private Map<String, Object> returnMap(Ticket ticket, long currentStock) {
        Map<String, Object> map = new HashMap<>();
        if (ticket != null) {
            map.put("ticket", ticket);
            Map<String, String> generate = generate();
            map.put("msg", "出票成功,您的车票编号是：" + currentStock + " ,车厢编号为：" + generate.get("j") + "车厢 ,座位号为：" + generate.get("i") + "座");
            return map;
        }
        map.put("msg", "出票失败,当前车次已售完......");
        return map;
    }

    /**
     * 随机生成一个座位
     */
    private Map<String, String> generate() {
        //创建Random类对象,生成随机座位
        Random random = new Random();
        int i = random.nextInt(6);
        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");
        list.add("D");
        list.add("E");
        list.add("F");
        Map<String, String> map = new HashMap<>();
        map.put("i", list.get(i));
        // 生成随机车厢
        Random randomCar = new Random();
        int j = randomCar.nextInt(12);
        map.put("j", JSON.toJSONString(j));
        return map;
    }

}

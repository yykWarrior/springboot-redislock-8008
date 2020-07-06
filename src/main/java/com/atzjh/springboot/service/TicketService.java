package com.atzjh.springboot.service;

import com.atzjh.springboot.entity.Ticket;

/**
 * 车票服务接口
 *
 * @author zhangjh
 * @date 2020-03-01
 */
public interface TicketService {

    Object add(Ticket ticket);

    Object get(long id);

    Object getLock(long id);

    Object getLockClientId(long id);

    Object getLockRedission(long id);
}

package com.atzjh.springboot.controller;

import com.atzjh.springboot.entity.Ticket;
import com.atzjh.springboot.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 抢票
 *
 * @author zhangjh
 * @date 2020-03-01
 */
@RestController
@RequestMapping(value = "/ticket")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    /**
     * 模拟抢票
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/get/{id}", method = RequestMethod.GET)
    public Object getTicket(@PathVariable long id) {
        return ticketService.get(id);
    }

    @RequestMapping(value = "/getLock/{id}", method = RequestMethod.GET)
    public Object getLock(@PathVariable long id) {
        return ticketService.getLock(id);
    }

    @RequestMapping(value = "/getLockClient/{id}", method = RequestMethod.GET)
    public Object getLockClient(@PathVariable long id) {
        return ticketService.getLockClientId(id);
    }

    @RequestMapping(value = "/getRedissionLock/{id}", method = RequestMethod.GET)
    public Object getRedissionLock(@PathVariable long id) {
        return ticketService.getLockRedission(id);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public Object addTicket(@RequestBody Ticket ticket) {

        return ticketService.add(ticket);
    }


}

package com.atzjh.springboot.entity;

import lombok.*;

/**
 * 车票实体类
 *
 * @author zhangjh
 * @date 202-03-01
 */
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    /**
     * id
     */
    private long id;

    /**
     * 车票名
     */
    private String name;

    /**
     * 所属公司
     */
    private String company;

    /**
     * 站点起始位置
     */
    private String locationStart;

    /**
     * 站点终止位置
     */
    private String locationEnd;

    /**
     * 始发时间
     */
    private String tsStart;

    /**
     * 结束时间
     */
    private String tsEnd;

    /**
     * 库存
     */
    private long inventory;

    /**
     * 分段锁
     */
    private String lockedKey;



}

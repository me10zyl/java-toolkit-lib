package toolkit.scanner.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 对应数据库表 job_scanner
 * 用于存储 Job 调度的配置和状态信息
 */
@Data // 自动生成 getter, setter, toString, equals, hashCode
@Accessors(chain = true) // 启用链式调用 set 方法 (optional)
@TableName("job_scanner")
public class JobScannerDO {

    /**
     * job_id: bigint auto_increment primary key
     */
    @TableId
    private Long jobId;

    /**
     * job_name: varchar(50) not null comment 'job名称'
     */
    private String jobName;

    /**
     * execute_interval: int default 5 not null comment '执行间隔（秒）'
     */
    private long executeInterval;

    /**
     * last_execute_time: datetime null comment 'job最后执行时间'
     */
    private LocalDateTime lastExecuteTime;

    /**
     * last_execute_result: text null comment '最后执行结果'
     * 注意：对应数据库的TEXT类型，Java中使用 String
     */
    private String lastExecuteResult;

    /**
     * last_execute_trace_id: varchar(32) null comment '最够执行的traceid'
     */
    private String lastExecuteTraceId;

    /**
     * max_times: int default 5 not null comment '最大执行次数'
     */
    private Integer maxTimes;

    /**
     * job_desc: varchar(100) null comment 'job描述'
     */
    private String jobDesc;

    /**
     * created_at: datetime default CURRENT_TIMESTAMP not null comment '创建时间'
     * 建议使用 LocalDateTime 映射 MySQL 的 DATETIME
     */
    private LocalDateTime createdAt;

    /**
     * update_time: datetime default CURRENT_TIMESTAMP not null comment '更新时间'
     */
    private LocalDateTime updateTime;
     /**
      * next_execute_time: datetime null comment '下次执行时间'
      */
    private LocalDateTime nextExecuteTime;

     /**
      * last_execute_ip: varchar(20) null comment '执行ip'
      */
    private String lastExecuteIp;
}
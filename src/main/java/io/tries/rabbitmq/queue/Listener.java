package io.tries.rabbitmq.queue;

import io.tries.rabbitmq.domain.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Listener {

    @RabbitListener(queues = "queue.audit.log")
    public void listen(MessageProperties properties, AuditLog auditLog) {
        log.debug("properties are : {}", properties);
        log.debug("message is : {}", auditLog);
    }
}

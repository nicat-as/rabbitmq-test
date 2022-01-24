package io.tries.rabbitmq.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class BrokerConfig {
    private static final String DEAD_LETTER_PREFIX = "dl.";
    private static final String DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    private static final String DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    private final BrokerProperties properties;
    private final ConfigurableBeanFactory beanFactory;

    @Bean
    public void defineBindings() {
        defineExchanges();
        defineQueues();
        properties.getBrokers()
                .forEach(property -> {
                    var binding = BindingBuilder
                            .bind(beanFactory.getBean(property.getQueue(), Queue.class))
                            .to(beanFactory.getBean(property.getExchange(), DirectExchange.class))
                            .with(property.getRoutingKey());

                    beanFactory.registerSingleton(binding.getExchange() + binding.getRoutingKey(), binding);

                    if (property.isDeadLetter()) {
                        var dlBinding = BindingBuilder
                                .bind(beanFactory.getBean(deadLetter(property.getQueue()), Queue.class))
                                .to(beanFactory.getBean(deadLetter(property.getExchange()), DirectExchange.class))
                                .with(deadLetter(property.getRoutingKey()));

                        beanFactory.registerSingleton(
                                dlBinding.getExchange() + dlBinding.getRoutingKey(),
                                dlBinding
                        );
                    }

                });
    }

    @Bean
    public RabbitTemplate configureRabbitTemplate(ConnectionFactory connectionFactory) {
        var rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(getMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter getMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private void defineExchanges() {
        properties.getBrokers()
                .forEach(property -> {
                    var exchange = ExchangeBuilder
                            .directExchange(property.getExchange())
                            .durable(true)
                            .build();

                    beanFactory.registerSingleton(exchange.getName(), exchange);

                    if (property.isDeadLetter()) {
                        var dlExchange = ExchangeBuilder
                                .directExchange(deadLetter(property.getExchange()))
                                .durable(true)
                                .build();

                        beanFactory.registerSingleton(dlExchange.getName(), dlExchange);
                    }
                });
    }

    private void defineQueues() {
        properties.getBrokers()
                .forEach(property -> {
                    var queueBuilder = QueueBuilder.durable(property.getQueue());

                    if (property.isDeadLetter()) {
                        queueBuilder.withArgument(DEAD_LETTER_EXCHANGE, deadLetter(property.getExchange()))
                                .withArgument(DEAD_LETTER_ROUTING_KEY, deadLetter(property.getRoutingKey()));
                    }

                    var queue = queueBuilder.build();

                    beanFactory.registerSingleton(queue.getName(), queue);

                    if (property.isDeadLetter()) {
                        var dlQueue = QueueBuilder.durable(deadLetter(property.getQueue()))
                                .build();

                        beanFactory.registerSingleton(dlQueue.getName(), dlQueue);
                    }
                });

    }

    private String deadLetter(String suffix) {
        return DEAD_LETTER_PREFIX + suffix;
    }
}

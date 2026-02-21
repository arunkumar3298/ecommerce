package com.arun.ecommerce.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ── OTP Email ─────────────────────────────────────────────
    public static final String OTP_QUEUE       = "email.otp.queue";
    public static final String OTP_ROUTING_KEY = "email.otp";

    // ── Order Confirmation Email ──────────────────────────────
    public static final String ORDER_QUEUE       = "email.order.queue";
    public static final String ORDER_ROUTING_KEY = "email.order";

    // ── Shared Exchange ───────────────────────────────────────
    public static final String EMAIL_EXCHANGE = "email.exchange";

    @Bean
    public Queue otpQueue() {
        return new Queue(OTP_QUEUE, true);
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding otpBinding() {
        return BindingBuilder
                .bind(otpQueue())
                .to(emailExchange())
                .with(OTP_ROUTING_KEY);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder
                .bind(orderQueue())
                .to(emailExchange())
                .with(ORDER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}

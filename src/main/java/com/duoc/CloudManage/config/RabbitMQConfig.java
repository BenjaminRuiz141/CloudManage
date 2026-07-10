package com.duoc.CloudManage.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class RabbitMQConfig {

    public static final String COLA_GUIAS = "guias-queue";
    public static final String COLA_ERRORES = "guias-error-queue";
    public static final String EXCHANGE_PRINCIPAL = "guias-exchange";
    public static final String EXCHANGE_DLX = "guias-dlx-exchange";
    public static final String ROUTING_KEY_GUIAS = "guias-queue";
    public static final String ROUTING_KEY_ERRORES = "guias-error-queue";

    @Bean
    public Queue colaGuias() {
        log.info("Inicializando cola RabbitMQ '{}' con DLX '{}' y routing key '{}'",
                COLA_GUIAS, EXCHANGE_DLX, ROUTING_KEY_ERRORES);
        return QueueBuilder.durable(COLA_GUIAS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_DLX)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_ERRORES)
                .build();
    }

    @Bean
    public Queue colaErrores() {
        log.info("Inicializando cola de errores RabbitMQ '{}'", COLA_ERRORES);
        return QueueBuilder.durable(COLA_ERRORES).build();
    }

    @Bean
    public DirectExchange exchangePrincipal() {
        log.info("Inicializando exchange principal RabbitMQ '{}'", EXCHANGE_PRINCIPAL);
        return new DirectExchange(EXCHANGE_PRINCIPAL);
    }

    @Bean
    public DirectExchange exchangeDlx() {
        log.info("Inicializando exchange DLX RabbitMQ '{}'", EXCHANGE_DLX);
        return new DirectExchange(EXCHANGE_DLX);
    }

    @Bean
    public Binding bindingPrincipal() {
        log.info("Creando binding principal: cola '{}' -> exchange '{}' con routing key '{}'",
                COLA_GUIAS, EXCHANGE_PRINCIPAL, ROUTING_KEY_GUIAS);
        return BindingBuilder.bind(colaGuias()).to(exchangePrincipal()).with(ROUTING_KEY_GUIAS);
    }

    @Bean
    public Binding bindingDlx() {
        log.info("Creando binding DLX: cola '{}' -> exchange '{}' con routing key '{}'",
                COLA_ERRORES, EXCHANGE_DLX, ROUTING_KEY_ERRORES);
        return BindingBuilder.bind(colaErrores()).to(exchangeDlx()).with(ROUTING_KEY_ERRORES);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        log.info("Configurando convertidor JSON para mensajes RabbitMQ");
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        log.info("RabbitTemplate configurado con convertidor JSON y ConnectionFactory activa");
        return template;
    }
}
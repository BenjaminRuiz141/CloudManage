package com.duoc.CloudManage.service;

import com.duoc.CloudManage.config.RabbitMQConfig;
import com.duoc.CloudManage.model.GuiaDespacho;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GuiaProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publicarGuia(GuiaDespacho guia) {
        try {
            log.info("Publicando guía en RabbitMQ. numeroGuia={}, transportista={}, estado={}, exchange={}, routingKey={}",
                guia.getNumeroGuia(), guia.getTransportista(), guia.getEstado(),
                RabbitMQConfig.EXCHANGE_PRINCIPAL, RabbitMQConfig.ROUTING_KEY_GUIAS);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_PRINCIPAL,
                    RabbitMQConfig.ROUTING_KEY_GUIAS,
                    guia);
            log.info("Guía publicada correctamente en RabbitMQ. numeroGuia={}, colaDestino={}",
                guia.getNumeroGuia(), RabbitMQConfig.COLA_GUIAS);
        } catch (AmqpException e) {
            log.error("Error publicando guía en RabbitMQ. numeroGuia={}, transportista={}, estado={}",
                guia.getNumeroGuia(), guia.getTransportista(), guia.getEstado(), e);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_DLX,
                    RabbitMQConfig.ROUTING_KEY_ERRORES,
                    guia);
            log.warn("Guía enviada a cola de errores. numeroGuia={}, exchange={}, routingKey={}",
                guia.getNumeroGuia(), RabbitMQConfig.EXCHANGE_DLX, RabbitMQConfig.ROUTING_KEY_ERRORES);
            throw new RuntimeException("No se pudo publicar la guía en la cola principal, enviada a cola de errores",
                    e);
        }
    }
}
package com.duoc.CloudManage.service;

import com.duoc.CloudManage.model.GuiaDespacho;
import com.duoc.CloudManage.model.GuiaProcesada;
import com.duoc.CloudManage.repository.GuiaProcesadaRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Component
@Slf4j
public class GuiaQueueConsumer {

    @Autowired
    private GuiaProcesadaRepository guiaProcesadaRepository;

    @RabbitListener(queues = "guias-queue")
    public void procesarGuia(GuiaDespacho guia) {
        log.info("Mensaje recibido desde RabbitMQ. numeroGuia={}, transportista={}, estado={}, fecha={}",
            guia.getNumeroGuia(), guia.getTransportista(), guia.getEstado(), guia.getFecha());
        GuiaProcesada registro = new GuiaProcesada(
                guia.getNumeroGuia(),
                guia.getTransportista(),
                guia.getEstado().toString(),
                LocalDateTime.now());
        log.info("Persistiendo procesamiento de guía en DB. numeroGuia={}, estadoOrigen={}, fechaProcesado={}",
            registro.getNumeroGuia(), registro.getEstadoOrigen(), registro.getFechaProcesado());
        guiaProcesadaRepository.save(registro);
        log.info("Procesamiento de guía persistido correctamente. numeroGuia={}, idRegistro={}",
            registro.getNumeroGuia(), registro.getId());
    }
}
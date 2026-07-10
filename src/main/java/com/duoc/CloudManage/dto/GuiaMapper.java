package com.duoc.CloudManage.dto;

import com.duoc.CloudManage.model.GuiaDespacho;

import java.util.List;

public final class GuiaMapper {

    private GuiaMapper() {
    }

    public static GuiaResponseDTO toResponse(GuiaDespacho guia) {
        if (guia == null) {
            return null;
        }

        return GuiaResponseDTO.builder()
                .id(guia.getId())
                .numeroGuia(guia.getNumeroGuia())
                .transportista(guia.getTransportista())
                .fecha(guia.getFecha())
                .estado(guia.getEstado())
                .rutaEfs(guia.getRutaEfs())
                .rutaS3(guia.getRutaS3())
                .creadoEn(guia.getCreadoEn())
                .build();
    }

    public static List<GuiaResponseDTO> toResponseList(List<GuiaDespacho> guias) {
        return guias.stream()
                .map(GuiaMapper::toResponse)
                .toList();
    }
}
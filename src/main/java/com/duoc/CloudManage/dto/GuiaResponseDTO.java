package com.duoc.CloudManage.dto;

import com.duoc.CloudManage.model.GuiaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuiaResponseDTO {
    private String id;
    private String numeroGuia;
    private String transportista;
    private LocalDate fecha;
    private GuiaStatus estado;
    private String rutaEfs;
    private String rutaS3;
    private LocalDateTime creadoEn;
}
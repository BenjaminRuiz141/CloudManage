package com.duoc.CloudManage.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "guias_despacho")
@Data
@NoArgsConstructor
public class GuiaDespacho {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String numeroGuia;

    @Column(nullable = false)
    private String transportista;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuiaStatus estado = GuiaStatus.PENDIENTE;

    private String rutaEfs;  // ruta temporal en EFS
    private String rutaS3;   // key en S3

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;
}
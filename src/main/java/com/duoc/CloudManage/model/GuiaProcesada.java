package com.duoc.CloudManage.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "guias_procesadas", uniqueConstraints = @UniqueConstraint(columnNames = "numeroGuia"))
public class GuiaProcesada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroGuia;
    private String transportista;
    private String estadoOrigen;
    private LocalDateTime fechaProcesado;

    public GuiaProcesada() {
    }

    public GuiaProcesada(String numeroGuia, String transportista, String estadoOrigen, LocalDateTime fechaProcesado) {
        this.numeroGuia = numeroGuia;
        this.transportista = transportista;
        this.estadoOrigen = estadoOrigen;
        this.fechaProcesado = fechaProcesado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroGuia() {
        return numeroGuia;
    }

    public void setNumeroGuia(String numeroGuia) {
        this.numeroGuia = numeroGuia;
    }

    public String getTransportista() {
        return transportista;
    }

    public void setTransportista(String transportista) {
        this.transportista = transportista;
    }

    public String getEstadoOrigen() {
        return estadoOrigen;
    }

    public void setEstadoOrigen(String estadoOrigen) {
        this.estadoOrigen = estadoOrigen;
    }

    public LocalDateTime getFechaProcesado() {
        return fechaProcesado;
    }

    public void setFechaProcesado(LocalDateTime fechaProcesado) {
        this.fechaProcesado = fechaProcesado;
    }
}
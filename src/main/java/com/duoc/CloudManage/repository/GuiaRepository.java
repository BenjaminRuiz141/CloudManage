package com.duoc.CloudManage.repository;


import com.duoc.CloudManage.model.GuiaDespacho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GuiaRepository extends JpaRepository<GuiaDespacho, String> {

    List<GuiaDespacho> findByTransportista(String transportista);

    List<GuiaDespacho> findByFecha(LocalDate fecha);

    List<GuiaDespacho> findByTransportistaAndFecha(
            String transportista, LocalDate fecha);

    Optional<GuiaDespacho> findByNumeroGuia(String numeroGuia);
}

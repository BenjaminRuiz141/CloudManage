package com.duoc.CloudManage.aws.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.duoc.CloudManage.aws.exceptions.EfsException;

@Service
public class EfsService {

    @Value("${cloud.efs.mount-path}")
    private String mountPath;

    // Guarda el PDF en EFS y retorna la ruta completa
    public String guardar(String numeroGuia, byte[] contenido) {
        try {
            Path directorio = Paths.get(mountPath);
            Files.createDirectories(directorio); // crea la carpeta si no existe

            Path archivo = directorio.resolve(numeroGuia + ".pdf");
            Files.write(archivo, contenido,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);

            return archivo.toString();
        } catch (IOException e) {
            throw new EfsException("Error al guardar en EFS: " + e.getMessage());
        }
    }

    // Lee el PDF desde EFS y retorna los bytes
    public byte[] leer(String rutaEfs) {
        try {
            Path archivo = Paths.get(rutaEfs);
            if (!Files.exists(archivo))
                throw new EfsException("Archivo no encontrado en EFS: " + rutaEfs);
            return Files.readAllBytes(archivo);
        } catch (IOException e) {
            throw new EfsException("Error al leer desde EFS: " + e.getMessage());
        }
    }

    // Retorna la referencia al archivo físico en EFS
    public File obtenerArchivo(String rutaEfs) {
        Path path = Paths.get(rutaEfs);
        if (!Files.exists(path))
            throw new EfsException("Archivo no encontrado en EFS: " + rutaEfs);
        return path.toFile();
    }

    // Retorna el archivo basándose solo en el número de guía
    public File obtenerArchivoPorNumero(String numeroGuia) {
        Path path = Paths.get(mountPath).resolve(numeroGuia + ".pdf");
        if (!Files.exists(path))
            throw new EfsException("Archivo no encontrado en EFS: " + path.toString());
        return path.toFile();
    }

    // Elimina el archivo del EFS (opcional, para limpieza)
    public void eliminar(String rutaEfs) {
        try {
            Files.deleteIfExists(Paths.get(rutaEfs));
        } catch (IOException e) {
            throw new EfsException("Error al eliminar de EFS: " + e.getMessage());
        }
    }
}

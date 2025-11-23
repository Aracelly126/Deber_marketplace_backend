package com.multicompany.sales_system.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface FileStorageService {

    /**
     * Almacenar un archivo de imagen
     * 
     * @param file      Archivo a almacenar
     * @param productId ID del producto al que pertenece la imagen
     * @return Ruta relativa donde se guardó el archivo
     */
    String storeFile(MultipartFile file, Long productId) throws IOException;

    /**
     * Cargar un archivo como recurso
     * 
     * @param filename Nombre del archivo
     * @return Recurso del archivo
     */
    Resource loadFileAsResource(String filename) throws IOException;

    /**
     * Eliminar un archivo
     * 
     * @param filename Nombre del archivo a eliminar
     */
    void deleteFile(String filename) throws IOException;

    /**
     * Validar que el archivo sea una imagen
     * 
     * @param file Archivo a validar
     * @return true si es una imagen válida
     */
    boolean isValidImage(MultipartFile file);

    /**
     * Validar el tamaño del archivo (máximo 10MB)
     * 
     * @param file Archivo a validar
     * @return true si el tamaño es válido
     */
    boolean isValidSize(MultipartFile file);

    /**
     * Obtener la ruta completa del directorio de almacenamiento
     * 
     * @return Path del directorio
     */
    Path getStorageLocation();
}

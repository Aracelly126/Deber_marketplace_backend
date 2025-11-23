package com.multicompany.sales_system.service.impl;

import com.multicompany.sales_system.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp");

    public FileStorageServiceImpl() {
        this.fileStorageLocation = Paths.get("C:/productos").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Directorio de almacenamiento creado/verificado: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo crear el directorio de almacenamiento de archivos.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, Long productId) throws IOException {
        // Validar que el archivo no esté vacío
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        // Validar que sea una imagen
        if (!isValidImage(file)) {
            throw new RuntimeException("El archivo debe ser una imagen válida (JPG, PNG, GIF, WEBP)");
        }

        // Validar el tamaño
        if (!isValidSize(file)) {
            throw new RuntimeException("El archivo excede el tamaño máximo permitido de 10MB");
        }

        // Obtener la extensión del archivo original
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";

        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generar nombre único para el archivo: producto_{productId}_{uuid}.{extension}
        String newFilename = "producto_" + productId + "_" + UUID.randomUUID().toString() + fileExtension;

        // Copiar archivo a la ubicación de destino
        Path targetLocation = this.fileStorageLocation.resolve(newFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        log.info("Archivo guardado exitosamente: {}", newFilename);

        return newFilename;
    }

    @Override
    public Resource loadFileAsResource(String filename) throws IOException {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Archivo no encontrado: " + filename, ex);
        }
    }

    @Override
    public void deleteFile(String filename) throws IOException {
        if (filename == null || filename.trim().isEmpty()) {
            return;
        }

        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("Archivo eliminado exitosamente: {}", filename);
        } catch (IOException ex) {
            log.error("Error al eliminar el archivo: {}", filename, ex);
            throw new RuntimeException("No se pudo eliminar el archivo: " + filename, ex);
        }
    }

    @Override
    public boolean isValidImage(MultipartFile file) {
        // Validar por content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            return false;
        }

        // Validar por extensión del archivo
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return false;
        }

        String extension = "";
        if (originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }

        return ALLOWED_EXTENSIONS.contains(extension);
    }

    @Override
    public boolean isValidSize(MultipartFile file) {
        return file.getSize() <= MAX_FILE_SIZE;
    }

    @Override
    public Path getStorageLocation() {
        return this.fileStorageLocation;
    }
}

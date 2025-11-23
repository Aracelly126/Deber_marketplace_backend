package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;
import com.multicompany.sales_system.service.PhotoService;
import com.multicompany.sales_system.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PhotoController {

    private final PhotoService photoService;
    private final FileStorageService fileStorageService;

    /**
     * Subir una sola foto para un producto
     * POST /api/photos/upload/{productId}
     */
    @PostMapping(value = "/upload/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponseDTO> uploadPhoto(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file) {
        try {
            PhotoResponseDTO uploadedPhoto = photoService.uploadPhoto(productId, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(uploadedPhoto);
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage(), e);
        }
    }

    /**
     * Subir múltiples fotos para un producto (máximo 5)
     * POST /api/photos/upload-multiple/{productId}
     */
    @PostMapping(value = "/upload-multiple/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadMultiplePhotos(
            @PathVariable Long productId,
            @RequestPart("files") List<MultipartFile> files) {
        try {
            List<PhotoResponseDTO> uploadedPhotos = photoService.uploadMultiplePhotos(productId, files);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Fotos subidas exitosamente");
            response.put("cantidad", uploadedPhotos.size());
            response.put("fotos", uploadedPhotos);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            throw new RuntimeException("Error al subir las fotos: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener una imagen por nombre de archivo
     * GET /api/photos/image/{filename}
     */
    @GetMapping("/image/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Resource resource = fileStorageService.loadFileAsResource(filename);

            // Determinar el tipo de contenido
            String contentType = "image/jpeg"; // Por defecto
            if (filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filename.toLowerCase().endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener información de una foto por ID
     * GET /api/photos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponseDTO> getPhotoById(@PathVariable Long id) {
        PhotoResponseDTO photo = photoService.getPhotoById(id);
        return ResponseEntity.ok(photo);
    }

    /**
     * Obtener todas las fotos
     * GET /api/photos
     */
    @GetMapping
    public ResponseEntity<List<PhotoResponseDTO>> getAllPhotos() {
        List<PhotoResponseDTO> photos = photoService.getAllPhotos();
        return ResponseEntity.ok(photos);
    }

    /**
     * Obtener todas las fotos de un producto
     * GET /api/photos/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<PhotoResponseDTO>> getPhotosByProductId(@PathVariable Long productId) {
        List<PhotoResponseDTO> photos = photoService.getPhotosByProductId(productId);
        return ResponseEntity.ok(photos);
    }

    /**
     * Eliminar una foto (elimina el archivo y el registro)
     * DELETE /api/photos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePhoto(@PathVariable Long id) {
        try {
            photoService.deletePhoto(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Foto eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar la foto: " + e.getMessage(), e);
        }
    }

    /**
     * Eliminar todas las fotos de un producto
     * DELETE /api/photos/product/{productId}
     */
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Map<String, String>> deletePhotosByProductId(@PathVariable Long productId) {
        try {
            photoService.deletePhotosByProductId(productId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Todas las fotos del producto fueron eliminadas exitosamente");
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            throw new RuntimeException("Error al eliminar las fotos: " + e.getMessage(), e);
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
}

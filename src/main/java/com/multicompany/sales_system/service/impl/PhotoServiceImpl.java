package com.multicompany.sales_system.service.impl;

import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;
import com.multicompany.sales_system.model.FotoProducto;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.repository.PhotoRepository;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.service.PhotoService;
import com.multicompany.sales_system.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    @Override
    public PhotoResponseDTO uploadPhoto(Long productId, MultipartFile file) throws IOException {
        // Validar que el producto exista
        Producto producto = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productId));

        // Validar que sea una imagen válida
        if (!fileStorageService.isValidImage(file)) {
            throw new RuntimeException("El archivo debe ser una imagen válida (JPG, PNG, GIF, WEBP)");
        }

        // Validar el tamaño
        if (!fileStorageService.isValidSize(file)) {
            throw new RuntimeException("El archivo excede el tamaño máximo permitido de 10MB");
        }

        // Guardar el archivo físicamente
        String filename = fileStorageService.storeFile(file, productId);

        // Crear el registro en la base de datos
        FotoProducto foto = new FotoProducto();
        foto.setUrl(filename); // Ahora guardamos el nombre del archivo, no una URL externa
        foto.setProducto(producto);

        FotoProducto savedPhoto = photoRepository.save(foto);
        log.info("Foto guardada exitosamente para producto {}: {}", productId, filename);

        return convertToResponseDTO(savedPhoto);
    }

    @Override
    public List<PhotoResponseDTO> uploadMultiplePhotos(Long productId, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new RuntimeException("Debe proporcionar al menos un archivo");
        }

        // Validar cantidad máxima de fotos
        if (files.size() > 5) {
            throw new RuntimeException("No se pueden subir más de 5 fotos a la vez");
        }

        List<PhotoResponseDTO> uploadedPhotos = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                PhotoResponseDTO photo = uploadPhoto(productId, file);
                uploadedPhotos.add(photo);
            } catch (Exception e) {
                // Si falla alguna foto, eliminar las que ya se subieron
                log.error("Error al subir foto, revertiendo cambios: {}", e.getMessage());

                for (PhotoResponseDTO uploadedPhoto : uploadedPhotos) {
                    try {
                        deletePhoto(uploadedPhoto.getIdFoto());
                    } catch (Exception ex) {
                        log.error("Error al eliminar foto durante rollback: {}", ex.getMessage());
                    }
                }

                throw new RuntimeException("Error al subir las fotos: " + e.getMessage(), e);
            }
        }

        return uploadedPhotos;
    }

    @Override
    @Transactional(readOnly = true)
    public PhotoResponseDTO getPhotoById(Long id) {
        FotoProducto foto = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + id));
        return convertToResponseDTO(foto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoResponseDTO> getAllPhotos() {
        return photoRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoResponseDTO> getPhotosByProductId(Long productId) {
        List<FotoProducto> fotos = photoRepository.findByProductoIdProducto(productId);
        return fotos.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deletePhoto(Long id) throws IOException {
        FotoProducto foto = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto no encontrada con ID: " + id));

        // Eliminar el archivo físico
        String filename = foto.getUrl();
        if (filename != null && !filename.trim().isEmpty()) {
            try {
                fileStorageService.deleteFile(filename);
                log.info("Archivo físico eliminado: {}", filename);
            } catch (Exception e) {
                log.error("Error al eliminar archivo físico: {}", filename, e);
            }
        }

        // Eliminar la referencia en el producto
        Producto producto = foto.getProducto();
        if (producto != null && producto.getFotos() != null) {
            producto.getFotos().removeIf(f -> f.getIdFoto().equals(foto.getIdFoto()));
            productRepository.save(producto);
        }

        // Eliminar el registro de la base de datos
        photoRepository.deleteById(id);
        log.info("Foto eliminada de la base de datos: {}", id);
    }

    @Override
    public void deletePhotosByProductId(Long productId) throws IOException {
        List<FotoProducto> fotos = photoRepository.findByProductoIdProducto(productId);

        for (FotoProducto foto : fotos) {
            // Eliminar archivo físico
            String filename = foto.getUrl();
            if (filename != null && !filename.trim().isEmpty()) {
                try {
                    fileStorageService.deleteFile(filename);
                } catch (Exception e) {
                    log.error("Error al eliminar archivo: {}", filename, e);
                }
            }
        }

        // Eliminar todos los registros
        photoRepository.deleteAll(fotos);
        log.info("Todas las fotos del producto {} fueron eliminadas", productId);
    }

    private PhotoResponseDTO convertToResponseDTO(FotoProducto foto) {
        PhotoResponseDTO dto = new PhotoResponseDTO();
        dto.setIdFoto(foto.getIdFoto());
        dto.setUrl(foto.getUrl());
        if (foto.getProducto() != null) {
            dto.setIdProducto(foto.getProducto().getIdProducto());
        }
        return dto;
    }
}
package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PhotoService {

    /**
     * Subir una foto para un producto
     */
    PhotoResponseDTO uploadPhoto(Long productId, MultipartFile file) throws IOException;

    /**
     * Subir múltiples fotos para un producto
     */
    List<PhotoResponseDTO> uploadMultiplePhotos(Long productId, List<MultipartFile> files) throws IOException;

    PhotoResponseDTO getPhotoById(Long id);

    List<PhotoResponseDTO> getAllPhotos();

    List<PhotoResponseDTO> getPhotosByProductId(Long productId);

    void deletePhoto(Long id) throws IOException;

    void deletePhotosByProductId(Long productId) throws IOException;
}
package com.multicompany.sales_system.dto.photo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponseDTO {
    private Long idFoto;
    private String url;
    private Long idProducto;
}
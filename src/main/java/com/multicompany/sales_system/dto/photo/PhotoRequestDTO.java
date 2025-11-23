package com.multicompany.sales_system.dto.photo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhotoRequestDTO {

    @NotBlank(message = "La URL es obligatoria")
    private String url;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long idProducto;
}
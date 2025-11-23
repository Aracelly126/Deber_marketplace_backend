package com.multicompany.sales_system.dto.configuracion;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EliminarPalabraRequestDTO {
    
    @NotBlank(message = "La palabra no puede estar vacía")
    private String palabra;
}

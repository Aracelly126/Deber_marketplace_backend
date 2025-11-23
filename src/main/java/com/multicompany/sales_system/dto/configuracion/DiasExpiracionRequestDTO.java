package com.multicompany.sales_system.dto.configuracion;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiasExpiracionRequestDTO {
    
    @NotNull(message = "Los días de expiración son obligatorios")
    @Min(value = 1, message = "Los días de expiración deben ser al menos 1")
    private Integer dias;
}

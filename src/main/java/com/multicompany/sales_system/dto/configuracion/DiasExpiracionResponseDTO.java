package com.multicompany.sales_system.dto.configuracion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiasExpiracionResponseDTO {
    private Integer dias;
    private String mensaje;
}

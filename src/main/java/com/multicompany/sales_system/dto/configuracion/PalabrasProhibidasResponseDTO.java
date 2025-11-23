package com.multicompany.sales_system.dto.configuracion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PalabrasProhibidasResponseDTO {
    private List<String> palabras;
    private String mensaje;
}

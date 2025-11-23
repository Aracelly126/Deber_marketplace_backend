package com.multicompany.sales_system.dto.user;

import com.multicompany.sales_system.model.enums.UsuarioRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRoleRequest {
    @NotNull(message = "El rol es obligatorio")
    private UsuarioRole rol;
}

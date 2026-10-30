package org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Rol de la aplicación. No conoce a los usuarios: la asignación
 * de roles se gestiona únicamente desde el CRUD de usuarios.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    private Long id;

    @NotEmpty(message = "{msg.role.name.notEmpty}")
    @Size(max = 50, message = "{msg.role.name.size}")
    private String name;

    public Role(String name) {
        this.name = name;
    }
}
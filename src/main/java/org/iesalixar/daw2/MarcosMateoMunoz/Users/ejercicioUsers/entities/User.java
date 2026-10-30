package org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Usuario de la aplicación. Puede tener uno o varios roles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;

    @NotEmpty(message = "{msg.user.firstName.notEmpty}")
    @Size(max = 50, message = "{msg.user.firstName.size}")
    private String firstName;

    @NotEmpty(message = "{msg.user.lastName.notEmpty}")
    @Size(max = 100, message = "{msg.user.lastName.size}")
    private String lastName;

    @NotEmpty(message = "{msg.user.email.notEmpty}")
    @Email(message = "{msg.user.email.invalid}")
    @Size(max = 150, message = "{msg.user.email.size}")
    private String email;

    // Roles completos del usuario. Los rellena el DAO y se usan para mostrarlos en el listado.
    private List<Role> roles = new ArrayList<>();

    // IDs de los roles marcados en el formulario. Es lo que se valida y lo que se guarda.
    @NotEmpty(message = "{msg.user.roles.notEmpty}")
    private List<Long> roleIds = new ArrayList<>();
}
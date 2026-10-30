package org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.daos;

import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities.Role;

import java.util.List;

public interface RoleDAO {

    List<Role> listAllRoles();
    void insertRole(Role role);
    void updateRole(Role role);
    void deleteRole(Long id);
    Role getRoleById(Long id);
    boolean existsRoleByName(String name);
    boolean existsRoleByNameAndNotId(String name, Long id);

}
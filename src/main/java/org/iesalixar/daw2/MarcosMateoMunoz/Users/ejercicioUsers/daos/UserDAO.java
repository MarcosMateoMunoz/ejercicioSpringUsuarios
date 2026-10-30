package org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.daos;

import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities.User;

import java.util.List;

public interface UserDAO {

    List<User> listAllUsers();
    void insertUser(User user);
    void updateUser(User user);
    void deleteUser(Long id);
    User getUserById(Long id);
    boolean existsUserByEmail(String email);
    boolean existsUserByEmailAndNotId(String email, Long id);

}
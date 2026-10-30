package org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.daos;

import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities.Role;
import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public UserDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper de usuario (sin roles: se cargan aparte)
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setEmail(rs.getString("email"));
        return user;
    };

    // RowMapper de rol
    private final RowMapper<Role> roleRowMapper = (rs, rowNum) ->
            new Role(rs.getLong("id"), rs.getString("name"));

    /**
     * Recupera los roles de un usuario a través de la tabla intermedia user_roles.
     */
    private List<Role> findRolesByUserId(Long userId) {
        String sql = "SELECT r.id, r.name " +
                "FROM roles r " +
                "JOIN user_roles ur ON ur.role_id = r.id " +
                "WHERE ur.user_id = ? " +
                "ORDER BY r.name";
        return jdbcTemplate.query(sql, roleRowMapper, userId);
    }

    /**
     * Inserta las filas de la tabla intermedia para un usuario.
     */
    private void insertUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, roleIds, roleIds.size(), (ps, roleId) -> {
            ps.setLong(1, userId);
            ps.setLong(2, roleId);
        });
    }

    @Override
    public List<User> listAllUsers() {
        logger.info("Listing all users with their roles from the database.");
        String sql = "SELECT id, first_name, last_name, email FROM users ORDER BY id";
        List<User> users = jdbcTemplate.query(sql, userRowMapper);
        for (User user : users) {
            user.setRoles(findRolesByUserId(user.getId()));
        }
        logger.info("Retrieved {} users from the database.", users.size());
        return users;
    }

    @Override
    @Transactional
    public void insertUser(User user) {
        logger.info("Inserting user with email: {}", user.getEmail());
        String sql = "INSERT INTO users (first_name, last_name, email) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            return ps;
        }, keyHolder);

        Long userId = keyHolder.getKey().longValue();
        user.setId(userId);
        insertUserRoles(userId, user.getRoleIds());
        logger.info("Inserted user with id: {} and {} roles.", userId, user.getRoleIds().size());
    }

    @Override
    @Transactional
    public void updateUser(User user) {
        logger.info("Updating user with id: {}", user.getId());
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getFirstName(), user.getLastName(), user.getEmail(), user.getId());

        // Se reemplazan las asignaciones: se borran las anteriores y se insertan las nuevas
        jdbcTemplate.update("DELETE FROM user_roles WHERE user_id = ?", user.getId());
        insertUserRoles(user.getId(), user.getRoleIds());
        logger.info("Updated user with id: {}", user.getId());
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        // Las filas de user_roles se borran solas por el ON DELETE CASCADE
        int rowsAffected = jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        logger.info("Deleted user. Rows affected: {}", rowsAffected);
    }

    @Override
    public User getUserById(Long id) {
        logger.info("Retrieving user by id: {}", id);
        String sql = "SELECT id, first_name, last_name, email FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            if (user != null) {
                user.setRoles(findRolesByUserId(id));
            }
            return user;
        } catch (EmptyResultDataAccessException e) {
            logger.warn("No user found with id: {}", id);
            return null;
        }
    }

    @Override
    public boolean existsUserByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email.toLowerCase());
        return count != null && count > 0;
    }

    @Override
    public boolean existsUserByEmailAndNotId(String email, Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(email) = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email.toLowerCase(), id);
        return count != null && count > 0;
    }
}
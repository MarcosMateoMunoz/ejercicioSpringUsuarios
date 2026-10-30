package org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.daos;

import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RoleDAOImpl implements RoleDAO {

    private static final Logger logger = LoggerFactory.getLogger(RoleDAOImpl.class);

    private final JdbcTemplate jdbcTemplate;

    public RoleDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Role> listAllRoles() {
        logger.info("Listing all roles from the database.");
        String sql = "SELECT id, name FROM roles ORDER BY name";
        List<Role> roles = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Role.class));
        logger.info("Retrieved {} roles from the database.", roles.size());
        return roles;
    }

    @Override
    public void insertRole(Role role) {
        logger.info("Inserting role with name: {}", role.getName());
        String sql = "INSERT INTO roles (name) VALUES (?)";
        int rowsAffected = jdbcTemplate.update(sql, role.getName());
        logger.info("Inserted role. Rows affected: {}", rowsAffected);
    }

    @Override
    public void updateRole(Role role) {
        logger.info("Updating role with id: {}", role.getId());
        String sql = "UPDATE roles SET name = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, role.getName(), role.getId());
        logger.info("Updated role. Rows affected: {}", rowsAffected);
    }

    @Override
    public void deleteRole(Long id) {
        logger.info("Deleting role with id: {}", id);
        String sql = "DELETE FROM roles WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        logger.info("Deleted role. Rows affected: {}", rowsAffected);
    }

    @Override
    public Role getRoleById(Long id) {
        logger.info("Retrieving role by id: {}", id);
        String sql = "SELECT id, name FROM roles WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Role.class), id);
        } catch (EmptyResultDataAccessException e) {
            logger.warn("No role found with id: {}", id);
            return null;
        }
    }

    @Override
    public boolean existsRoleByName(String name) {
        String sql = "SELECT COUNT(*) FROM roles WHERE UPPER(name) = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name.toUpperCase());
        return count != null && count > 0;
    }

    @Override
    public boolean existsRoleByNameAndNotId(String name, Long id) {
        String sql = "SELECT COUNT(*) FROM roles WHERE UPPER(name) = ? AND id != ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, name.toUpperCase(), id);
        return count != null && count > 0;
    }
}
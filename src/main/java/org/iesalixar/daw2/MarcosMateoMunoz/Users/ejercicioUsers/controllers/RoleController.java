package org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.daos.RoleDAO;
import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

/**
 * CRUD de roles. Aquí NO se asignan usuarios a roles: eso se hace solo desde el CRUD de usuarios.
 */
@Controller
@RequestMapping("/roles")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listRoles(Model model, Locale locale) {
        logger.info("Solicitando la lista de todos los roles...");
        try {
            model.addAttribute("listRoles", roleDAO.listAllRoles());
        } catch (Exception e) {
            logger.error("Error al listar los roles: {}", e.getMessage());
            model.addAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.list.error", null, locale));
        }
        return "views/role/role-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        logger.info("Mostrando formulario para nuevo rol.");
        model.addAttribute("role", new Role());
        return "views/role/role-form";
    }

    @PostMapping("/insert")
    public String insertRole(@Valid @ModelAttribute("role") Role role,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Insertando nuevo rol {}", role.getName());
        try {
            if (!result.hasFieldErrors("name") && roleDAO.existsRoleByName(role.getName())) {
                result.rejectValue("name", "msg.role.name.exists", "Ya existe un rol con ese nombre.");
            }
            if (result.hasErrors()) {
                return "views/role/role-form";
            }
            roleDAO.insertRole(role);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.role-controller.insert.success", null, locale));
        } catch (Exception e) {
            logger.error("Error al insertar el rol {}: {}", role.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.insert.error", null, locale));
        }
        return "redirect:/roles";
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Mostrando formulario de edición para el rol con ID {}", id);
        try {
            Role role = roleDAO.getRoleById(id);
            if (role == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage("msg.role-controller.edit.notfound", null, locale));
                return "redirect:/roles";
            }
            model.addAttribute("role", role);
            return "views/role/role-form";
        } catch (Exception e) {
            logger.error("Error al obtener el rol con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.edit.error", null, locale));
            return "redirect:/roles";
        }
    }

    @PostMapping("/update")
    public String updateRole(@Valid @ModelAttribute("role") Role role,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Actualizando rol con ID {}", role.getId());
        try {
            if (!result.hasFieldErrors("name") && roleDAO.existsRoleByNameAndNotId(role.getName(), role.getId())) {
                result.rejectValue("name", "msg.role.name.exists", "Ya existe un rol con ese nombre.");
            }
            if (result.hasErrors()) {
                return "views/role/role-form";
            }
            roleDAO.updateRole(role);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.role-controller.update.success", null, locale));
        } catch (Exception e) {
            logger.error("Error al actualizar el rol con ID {}: {}", role.getId(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.update.error", null, locale));
        }
        return "redirect:/roles";
    }

    @PostMapping("/delete")
    public String deleteRole(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Eliminando rol con ID {}", id);
        try {
            roleDAO.deleteRole(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.role-controller.delete.success", null, locale));
        } catch (DataIntegrityViolationException e) {
            // El rol está asignado a algún usuario (clave foránea en user_roles)
            logger.warn("No se puede eliminar el rol {}: está asignado a usuarios.", id);
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.delete.inUse", null, locale));
        } catch (Exception e) {
            logger.error("Error al eliminar el rol con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.role-controller.delete.error", null, locale));
        }
        return "redirect:/roles";
    }
}
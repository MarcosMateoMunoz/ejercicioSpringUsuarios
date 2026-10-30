package org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.controllers;

import jakarta.validation.Valid;
import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.daos.RoleDAO;
import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.daos.UserDAO;
import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities.Role;
import org.iesalixar.daw2.MarcosMateoMunoz.Users.ejercicioUsers.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * CRUD de usuarios. Es el único sitio donde se asignan roles a usuarios.
 */
@Controller
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private RoleDAO roleDAO;

    @Autowired
    private MessageSource messageSource;

    @GetMapping
    public String listUsers(Model model, Locale locale) {
        logger.info("Solicitando la lista de todos los usuarios...");
        try {
            model.addAttribute("listUsers", userDAO.listAllUsers());
        } catch (Exception e) {
            logger.error("Error al listar los usuarios: {}", e.getMessage());
            model.addAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.list.error", null, locale));
        }
        return "views/user/user-list";
    }

    @GetMapping("/new")
    public String showNewForm(Model model, Locale locale) {
        logger.info("Mostrando formulario para nuevo usuario.");
        model.addAttribute("user", new User());
        try {
            model.addAttribute("listRoles", roleDAO.listAllRoles());
        } catch (Exception e) {
            logger.error("Error al cargar los roles para el formulario: {}", e.getMessage());
            model.addAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.error", null, locale));
        }
        return "views/user/user-form";
    }

    @PostMapping("/insert")
    public String insertUser(@Valid @ModelAttribute("user") User user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Locale locale) {
        logger.info("Insertando nuevo usuario con email {}", user.getEmail());
        try {
            if (!result.hasFieldErrors("email") && userDAO.existsUserByEmail(user.getEmail())) {
                result.rejectValue("email", "msg.user.email.exists",
                        "Ya existe un usuario con ese correo electrónico.");
            }
            if (result.hasErrors()) {
                // Hay que volver a cargar los roles para pintar las casillas
                model.addAttribute("listRoles", roleDAO.listAllRoles());
                return "views/user/user-form";
            }
            userDAO.insertUser(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.user-controller.insert.success", null, locale));
        } catch (Exception e) {
            logger.error("Error al insertar el usuario {}: {}", user.getEmail(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.insert.error", null, locale));
        }
        return "redirect:/users";
    }

    @GetMapping("/edit")
    public String showEditForm(@RequestParam("id") Long id,
                               Model model,
                               RedirectAttributes redirectAttributes,
                               Locale locale) {
        logger.info("Mostrando formulario de edición para el usuario con ID {}", id);
        try {
            User user = userDAO.getUserById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        messageSource.getMessage("msg.user-controller.edit.notfound", null, locale));
                return "redirect:/users";
            }
            // Marca en el formulario los roles que ya tiene el usuario
            List<Long> roleIds = user.getRoles().stream()
                    .map(Role::getId)
                    .collect(Collectors.toList());
            user.setRoleIds(roleIds);

            model.addAttribute("user", user);
            model.addAttribute("listRoles", roleDAO.listAllRoles());
            return "views/user/user-form";
        } catch (Exception e) {
            logger.error("Error al obtener el usuario con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.edit.error", null, locale));
            return "redirect:/users";
        }
    }

    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("user") User user,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model,
                             Locale locale) {
        logger.info("Actualizando usuario con ID {}", user.getId());
        try {
            if (!result.hasFieldErrors("email")
                    && userDAO.existsUserByEmailAndNotId(user.getEmail(), user.getId())) {
                result.rejectValue("email", "msg.user.email.exists",
                        "Ya existe un usuario con ese correo electrónico.");
            }
            if (result.hasErrors()) {
                model.addAttribute("listRoles", roleDAO.listAllRoles());
                return "views/user/user-form";
            }
            userDAO.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.user-controller.update.success", null, locale));
        } catch (Exception e) {
            logger.error("Error al actualizar el usuario con ID {}: {}", user.getId(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.update.error", null, locale));
        }
        return "redirect:/users";
    }

    @PostMapping("/delete")
    public String deleteUser(@RequestParam("id") Long id,
                             RedirectAttributes redirectAttributes,
                             Locale locale) {
        logger.info("Eliminando usuario con ID {}", id);
        try {
            userDAO.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage",
                    messageSource.getMessage("msg.user-controller.delete.success", null, locale));
        } catch (Exception e) {
            logger.error("Error al eliminar el usuario con ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    messageSource.getMessage("msg.user-controller.delete.error", null, locale));
        }
        return "redirect:/users";
    }
}
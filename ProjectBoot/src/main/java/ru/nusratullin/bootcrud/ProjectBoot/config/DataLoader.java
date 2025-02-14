package ru.nusratullin.bootcrud.ProjectBoot.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.nusratullin.bootcrud.ProjectBoot.model.User;
import ru.nusratullin.bootcrud.ProjectBoot.service.UserService;

import java.util.HashSet;
import java.util.Set;


@Component
public class DataLoader {

    private UserService userService;

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostConstruct
    @Transactional
    public void loadData() {
        try {
            if (userService.findByEmail("admin@mail.ru").isEmpty()) {
                User admin = new User();
                admin.setName("admin");
                admin.setSurname("admin");
                admin.setAge(27);
                admin.setEmail("admin@mail.ru");
                admin.setPassword("admin");

                Set<String> adminRoleNames = new HashSet<>();
                adminRoleNames.add("ROLE_ADMIN");
                adminRoleNames.add("ROLE_USER");

                userService.saveUser(admin.getName(), admin.getSurname(), admin.getAge(),
                        admin.getEmail(), admin.getPassword(), adminRoleNames);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при создании админа: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        try {
            if (userService.findByEmail("user@mail.ru").isEmpty()) {
                User user = new User();
                user.setName("user");
                user.setSurname("user");
                user.setAge(29);
                user.setEmail("user@mail.ru");
                user.setPassword("user");

                Set<String> userRoleNames = new HashSet<>();
                userRoleNames.add("ROLE_USER");

                userService.saveUser(user.getName(), user.getSurname(), user.getAge(),
                        user.getEmail(), user.getPassword(), userRoleNames);
            }
        } catch (Exception e) {
            System.err.println("Ошибка при создании юзера: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


package ru.nusratullin.bootcrud.ProjectBoot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nusratullin.bootcrud.ProjectBoot.dao.UserDao;
import ru.nusratullin.bootcrud.ProjectBoot.model.Role;
import ru.nusratullin.bootcrud.ProjectBoot.model.User;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    private UserDao userDao;
    private RoleService roleService;
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Autowired
    public void setPasswordEncoder(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void saveUser(String name, String surname, int age, String email, String password, Set<String> roleNames) {
        User user = new User();
        user.setName(name);
        user.setSurname(surname);
        user.setAge(age);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        Set<Role> roles = roleNames.stream()
                .map(roleName -> {
                    Optional<Role> existingRole = roleService.findByName(roleName);
                    if (existingRole.isPresent()) {
                        return existingRole.get();
                    } else {
                        Role newRole = new Role(roleName);
                        return newRole;
                    }
                })
                .collect(Collectors.toSet());
        user.setRoles(roles);
        userDao.save(user);
    }


    @Override
    @Transactional
    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    @Transactional
    public void createUser(String name, String surname, int age, String email, String password, Set<String> roleNames) {
        saveUser(name, surname, age, email, password, roleNames);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> readAllUser() {
        return userDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> readUserById(Long id) {
        return userDao.findById(id);
    }

    @Override
    @Transactional
    public void deleteUserById(Long id) {
        userDao.deleteById(id);
    }

    @Override
    @Transactional
    public void updateUser(Long id, String name, String surname, int age, String email, String password, Set<String> roleNames) {
        Optional<User> optionalUser = readUserById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setName(name);
            user.setSurname(surname);
            user.setAge(age);
            user.setEmail(email);
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            if (roleNames == null || roleNames.isEmpty()) {
                user.setRoles(user.getRoles());
            } else {
                Set<Role> rolesUser = new HashSet<>();
                for (String role : roleNames) {
                    Role roles = roleService.findByName(role).orElseThrow(() ->
                            new RuntimeException("Роль: " + role + "не найдена"));
                    rolesUser.add(roles);
                }
                user.setRoles(rolesUser);
            }
            userDao.save(user);
        } else {
            throw new RuntimeException("Пользователь по id: " + id + "не найден");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserHome(User user) {
        User newUser = new User();
        newUser.setId(user.getId());
        newUser.setName(user.getName());
        newUser.setSurname(user.getSurname());
        newUser.setAge(user.getAge());
        newUser.setEmail(user.getEmail());
        newUser.setRoles(user.getRoles());
        return newUser;
    }

    @Override
    public boolean checkIfAdmin(User user) {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        boolean ifAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return ifAdmin;
    }
}

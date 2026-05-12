package com.suraj.Customer_Portal_29.service;

import com.suraj.Customer_Portal_29.entity.Owner;
import com.suraj.Customer_Portal_29.entity.Permission;
import com.suraj.Customer_Portal_29.entity.SolarRecord;
import com.suraj.Customer_Portal_29.entity.UserRole;
import com.suraj.Customer_Portal_29.repository.OwnerRepository;
import com.suraj.Customer_Portal_29.repository.SolarRecordRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class UserManagementService {

    private final OwnerRepository ownerRepository;
    private final PasswordEncoder passwordEncoder;
    private final SolarRecordRepository solarRecordRepository;

    public UserManagementService(OwnerRepository ownerRepository, PasswordEncoder passwordEncoder, SolarRecordRepository solarRecordRepository) {
        this.ownerRepository = ownerRepository;
        this.passwordEncoder = passwordEncoder;
        this.solarRecordRepository = solarRecordRepository;
    }

    public Owner createUser(String email, String name, String mobile, String password, Set<Permission> permissions) {
        if (ownerRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists");
        }
        Owner user = new Owner();
        user.setEmail(email);
        user.setName(name);
        user.setMobile(mobile);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(UserRole.USER);
        user.setActive(true);

        if (permissions == null || permissions.isEmpty()) {
            user.setPermissions(Set.of(Permission.VIEW_RECORDS));
        } else {
            user.setPermissions(permissions);
        }
        return ownerRepository.save(user);
    }

    public Owner updateUserPermissions(Long userId, Set<Permission> permissions) {
        Owner user = ownerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot modify SUPER_ADMIN permissions");
        }
        user.setPermissions(permissions);
        return ownerRepository.save(user);
    }

    public Owner updateUserStatus(Long userId, boolean isActive) {
        Owner user = ownerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(isActive);
        return ownerRepository.save(user);
    }

    public List<Owner> getAllUsers() {
        return ownerRepository.findAll();
    }

    public List<Owner> getActiveUsers() {
        return ownerRepository.findAll().stream()
                .filter(Owner::isActive)
                .toList();
    }

    public void deleteUser(Long userId) {
        Owner user = ownerRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            throw new RuntimeException("Cannot delete SUPER_ADMIN");
        }

        List<SolarRecord> userRecords = solarRecordRepository.findByCreatedByUserEmail(user.getEmail());
        if (!userRecords.isEmpty()) {
            solarRecordRepository.deleteAll(userRecords);
        }
        ownerRepository.delete(user);
    }

    public boolean hasPermission(Owner user, Permission permission) {
        if (user.getRole() == UserRole.SUPER_ADMIN) {
            return true;  // Super Admin has all permissions
        }
        return user.isActive() && user.getPermissions().contains(permission);
    }
}
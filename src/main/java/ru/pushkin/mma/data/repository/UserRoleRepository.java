package ru.pushkin.mma.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.pushkin.mma.data.model.UserRole;

public interface UserRoleRepository extends MongoRepository<UserRole, String> {
    UserRole findByName(String name);
}

package ru.pushkin.mmb.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.pushkin.mmb.data.model.UserRole;

public interface UserRoleRepository extends MongoRepository<UserRole, String> {
    UserRole findByName(String name);
}

package ru.pushkin.mmb.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import ru.pushkin.mmb.data.model.SecurityRole;

import java.util.Optional;

public interface SecurityRoleRepository extends MongoRepository<SecurityRole, String> {

    SecurityRole findByName(String name);
}

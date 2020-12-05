package ru.pushkin.mmb.data.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import ru.pushkin.mmb.data.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    User findByLogin(String login);
}

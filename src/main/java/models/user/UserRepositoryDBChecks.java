package models.user;

import exceptions.OverlapException;
import models.room.Room;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import records.UserDTO;

import java.sql.Connection;
import java.sql.Time;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// user is named account in db
public class UserRepositoryDBChecks implements UserRepository {
  private final Jdbi jdbi;

  public UserRepositoryDBChecks(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Optional<User> getUser(String name) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM account WHERE name = :name")
              .bind("name", name)
              .mapToMap()
              .stream().findFirst();
      if (result.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(User.parseMap(result.get()));
    });
  }

  @Override
  public Optional<User> getUser(Long id) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM account WHERE account_id = :id")
              .bind("id", id)
              .mapToMap()
              .stream().findFirst();
      if (result.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(User.parseMap(result.get()));
    });
  }

  @Override
  public User addUser(UserDTO userDTO) throws OverlapException {
    return jdbi.inTransaction((Handle handle) -> {
      var result = handle.createUpdate("INSERT INTO account (name) VALUES (:name);")
          .bind("name", userDTO.name())
          .executeAndReturnGeneratedKeys("account_id").mapToMap().findFirst();
      if (result.isEmpty()) {
        throw new OverlapException("User name overlaps", "name", userDTO.name());
      }
      long generatedID = (long) result.get().get("account_id");
      return new User(generatedID, userDTO.name());
    });
  }

  @Override
  public User editUser(User from, UserDTO to) {
    jdbi.useTransaction((Handle handle) -> {
      int updatedRows = handle.createUpdate("UPDATE account SET " +
              "name = :name " +
              "WHERE account_id = :id")
          .bind("name", to.name())
          .bind("id", from.id)
          .execute();
      if (updatedRows == 0) {
        throw new OverlapException("User name overlaps", "name", to.name());
      }
    });
    return getUser(from.id).get();
  }

  @Override
  public List<User> getAllUsers() {
    return jdbi.inTransaction((Handle handle) -> {
      var result = handle.createQuery("SELECT * FROM account")
          .mapToMap().list();
      return result.stream().map(User::parseMap).toList();
    });
  }
}

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Main {
  public static void main(String[] args) throws SQLException, ClassNotFoundException {
    /*
    Class.forName("org.postgresql.Driver");
    Connection connection =
        DriverManager.getConnection("jdbc:postgresql://localhost:5432/testDB", "postgres",
            "otopi24cutie");

    PreparedStatement preparedStatement = connection.prepareStatement(
        "INSERT INTO users (firstName, lastName) VALUES (?, ?)"
    );
    preparedStatement.setString(1, "Иван");
    preparedStatement.setString(2, "Иванов");
    preparedStatement.executeUpdate();
*/
    Config config = ConfigFactory.load();

    Flyway flyway =
        Flyway.configure()
            .outOfOrder(true)
            .locations("classpath:db/migrations")
            .dataSource(config.getString("app.database.url"), config.getString("app.database.user"),
                config.getString("app.database.password"))
            .load();

    Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:5432/postgres", "postgres", "otopi24cutie");
    jdbi.useTransaction((Handle handle) -> {
      handle
          .createUpdate("INSERT INTO users (firstName, lastName) VALUES (:firstName, :lastName)")
          .bind("firstName", "Иван")
          .bind("lastName", "Иванов")
          .execute();
    });
  }
}

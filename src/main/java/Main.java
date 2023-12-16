import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import controller.BookingController;
import controller.Controller;
import models.booking.BookingRepository;
import models.booking.BookingRepositoryDBChecksPremium;
import models.booking.TimeThreshold;
import models.room.RoomRepository;
import models.room.RoomRepositoryDBChecks;
import models.user.UserRepository;
import models.user.UserRepositoryDBChecks;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import spark.Service;
import spark.template.freemarker.FreeMarkerEngine;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class Main {
  public static void main(String[] args) {
    Config config = ConfigFactory.load();

    Flyway flyway =
        Flyway.configure()
            .outOfOrder(true)
            .locations("classpath:db/migrations")
            .dataSource(config.getString("app.database.url"), config.getString("app.database.user"),
                config.getString("app.database.password"))
            .load();
    flyway.migrate();

    Jdbi jdbi = Jdbi.create(
        config.getString("app.database.url"),
        config.getString("app.database.user"),
        config.getString("app.database.password")
    );

    UserRepository userRepository = new UserRepositoryDBChecks(jdbi);
    RoomRepository roomRepository = new RoomRepositoryDBChecks(jdbi);
    BookingRepository bookingRepository = new BookingRepositoryDBChecksPremium(
        roomRepository,
        userRepository,
        jdbi,
        config.getDuration("app.rules.booking.minimumTime"),
        config.getDuration("app.rules.booking.usualTimeLimit"),
        config.getDuration("app.rules.booking.premiumTimeLimit"),
        config.getInt("app.rules.booking.streakRequirementForPremium"),
        config.getDuration("app.rules.booking.timeRequirementForPremium"),
        config.getEnum(TimeThreshold.class, "app.rules.booking.timeThresholdMethod"),
        config.getDuration("app.rules.booking.inFutureAvailability")
    );

    Service service = Service.ignite();
    ObjectMapper objectMapper = new ObjectMapper();
    Controller controller = new BookingController(
        service,
        userRepository,
        roomRepository,
        bookingRepository,
        objectMapper,
        new FreeMarkerEngine()
    );
    controller.initEndpoints();
  }
}

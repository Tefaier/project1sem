CREATE TABLE account
(
    account_id      BIGSERIAL PRIMARY KEY,
    name            VARCHAR(200)             NOT NULL UNIQUE
);

CREATE TABLE room
(
    room_id    BIGSERIAL PRIMARY KEY,
    name       VARCHAR(200)                           NOT NULL UNIQUE,
    restricts  BOOLEAN                                NOT NULL,
    time_from  TIME,
    time_to    TIME
);

CREATE TABLE booking
(
    booking_id     BIGSERIAL PRIMARY KEY,
	account_id     BIGINT REFERENCES account (account_id) NOT NULL,
    room_id        BIGINT REFERENCES room (room_id)       NOT NULL,
	time_from      TIMESTAMP WITH TIME ZONE               NOT NULL,
    time_to        TIMESTAMP WITH TIME ZONE               NOT NULL
);
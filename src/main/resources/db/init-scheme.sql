DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS security_role;
DROP TABLE IF EXISTS session_data;
DROP TABLE IF EXISTS user_data;
DROP TABLE IF EXISTS track_data;

CREATE TABLE security_role
(
    id   SERIAL PRIMARY KEY,
    name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE session_data
(
    id      SERIAL PRIMARY KEY,
    code    varchar(100)  NOT NULL,
    value   varchar(1000) NOT NULL,
    user_id varchar(1000) NOT NULL
);

CREATE TABLE user_data
(
    id       SERIAL PRIMARY KEY,
    login    varchar(1000) NOT NULL UNIQUE,
    password varchar(1000) NOT NULL
);

CREATE TABLE user_role
(
    user_id integer REFERENCES user_data,
    role_id integer REFERENCES security_role
);

CREATE TABLE track_data
(
    id         SERIAL PRIMARY KEY,
    mbid       varchar(100) UNIQUE,
    title      varchar(1000) NOT NULL UNIQUE,
    track_name varchar(500)  NOT NULL,
    artist     varchar(500)  NOT NULL,
    album      varchar(500),
    length     numeric       NOT NULL,
    lastfm_url varchar(1000)
);

INSERT INTO security_role(name)
VALUES ('ROLE_ADMIN'),
       ('ROLE_USER'),
       ('ROLE_UNAUTHORIZED');

INSERT INTO user_data(login, password)
VALUES ('pushkin', '$2a$10$jReSFDX77bTYYBKxr5nzTOING8GriMQRKzCQohT1L31sEsijTC.oG');

DROP TABLE IF EXISTS user_role;
DROP TABLE IF EXISTS security_role;
DROP TABLE IF EXISTS session_data;
DROP TABLE IF EXISTS user_data;

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


DROP TABLE IF EXISTS playlist_track;
DROP TABLE IF EXISTS playlist_data;
DROP TABLE IF EXISTS track_data;

CREATE TABLE track_data
(
    id         SERIAL PRIMARY KEY,
    mbid       varchar(100) UNIQUE,
    title      varchar(1000) NOT NULL UNIQUE,
    track_name varchar(500)  NOT NULL,
    artist     varchar(500)  NOT NULL,
    album      varchar(500),
    length     numeric,
    lastfm_url varchar(1000)
);

CREATE TABLE playlist_data
(
    id            SERIAL PRIMARY KEY,
    title         varchar(1000) NOT NULL,
    description   varchar(10000),
    creation_time timestamp     not null,
    active        boolean       not null default true
);

CREATE TABLE playlist_track
(
    id          SERIAL PRIMARY KEY,
    playlist_id INTEGER NOT NULL REFERENCES playlist_data,
    track_id    INTEGER NOT NULL REFERENCES track_data,
    position    INTEGER NOT NULL
);


INSERT INTO security_role(name)
VALUES ('ROLE_ADMIN'),
       ('ROLE_USER'),
       ('ROLE_UNAUTHORIZED');

INSERT INTO user_data(login, password)
VALUES ('pushkin', '$2a$10$jReSFDX77bTYYBKxr5nzTOING8GriMQRKzCQohT1L31sEsijTC.oG');

INSERT INTO user_role(user_id, role_id)
VALUES (1, 2);

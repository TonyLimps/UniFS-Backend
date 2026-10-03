<div align="center">

# UniFS-Backend

[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://www.java.com//)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-green.svg)](https://spring.io/)
[![LICENSE](https://img.shields.io/badge/LICENSE-AGPL--3.0-crimson.svg)](LICENSE)

</div>

## Create Database
```mysql
DROP TABLE IF EXISTS users_rooms;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS rooms;

CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    server_admin TINYINT NOT NULL CHECK(server_admin in (0,1)) DEFAULT 0,
    public_key VARCHAR(512) NOT NULL UNIQUE,
    register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rooms (
    id BIGINT PRIMARY KEY,
    name VARCHAR(32) NOT NULL,
    password VARCHAR(128) NOT NULL,
    auth_email VARCHAR(64) NOT NULL DEFAULT '',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users_rooms (
    user_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, room_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    room_admin TINYINT NOT NULL CHECK(room_admin in (0,1)) DEFAULT 0,
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
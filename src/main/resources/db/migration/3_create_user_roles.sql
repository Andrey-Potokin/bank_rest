CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(10) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);
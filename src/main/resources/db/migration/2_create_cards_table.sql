CREATE TABLE cards (
    id BIGSERIAL PRIMARY KEY,
    number VARCHAR(255) NOT NULL,
    owner VARCHAR(255) NOT NULL,
    expiration_date DATE NOT NULL,
    status VARCHAR(50) NOT NULL,
    balance DOUBLE PRECISION NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_card_user FOREIGN KEY (user_id) REFERENCES users(id)
);
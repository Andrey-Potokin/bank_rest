WITH new_admin AS (
    INSERT INTO users (username, password)
    VALUES (
        'admin',
        '$2a$12$u8rsK1AD7GUwKOr2AHMgHuKiZDIgWwRIo6sPheohnDe4mOgBy.d4u'
    )
    RETURNING id
)
INSERT INTO user_roles (user_id, role)
SELECT id, 'ADMIN'
FROM new_admin;
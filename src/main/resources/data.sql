-- Use INSERT IGNORE to avoid duplicate key errors on restart
INSERT IGNORE INTO role (role_name) VALUES ('ROLE_USER');
INSERT IGNORE INTO role (role_name) VALUES ('ROLE_ADMIN');

-- BCrypt hash for "123456"
INSERT IGNORE INTO user (username, password, email, enabled)
VALUES ('admin', '$2a$10$joRMgZrWyaPDi41RmssanOCNXYHK.M09ZV1cyoGIaSPFgWiB26Qva', 'admin@petmgt.com', 1);
INSERT IGNORE INTO user (username, password, email, enabled)
VALUES ('user', '$2a$10$joRMgZrWyaPDi41RmssanOCNXYHK.M09ZV1cyoGIaSPFgWiB26Qva', 'user@petmgt.com', 1);

INSERT IGNORE INTO user_role (user_id, role_id) VALUES (1, 2);
INSERT IGNORE INTO user_role (user_id, role_id) VALUES (2, 1);

INSERT IGNORE INTO pet_breed (breed_name, pet_type) VALUES
('British Shorthair', '猫'),
('Persian', '猫'),
('Golden Retriever', '狗'),
('Corgi', '狗'),
('Holland Lop', '兔');

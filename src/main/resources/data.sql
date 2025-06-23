INSERT INTO app_users (username, password, enabled) VALUES
('cip', '{bcrypt}$2a$10$H9zziv7Qk3Gcv7Qj/9PLS.oEeiXYWT07/T3yyzlSVdxjD6GCxU9p6', TRUE);
INSERT INTO authorities (username, authority) VALUES
('cip', 'ROLE_ADMIN');

INSERT INTO participants (name, email, phone) VALUES
('Bon Jovi', 'bon.jovi@email.com', '0723434343');

INSERT INTO locations (name, address, description) VALUES
('Bucharest', 'Romania, Bucharest', 'Capital of Romania');
-- Sample data for the database for testing

BEGIN;

INSERT INTO roles (name)
VALUES
    ('system_administrator'),
    ('library_administrator'),
    ('librarian'),
    ('user'),
    ('driver');

-- Book related tables

INSERT INTO authors (name)
VALUES
    ('Henryk Sienkiewicz'),
    ('Adam Mickiewicz'),
    ('Bolesław Prus'),
    ('Maria Konopnicka');

INSERT INTO publishers (name)
VALUES
    ('Wydawnictwo Literackie'),
    ('Wydawnictwo Naukowe PWN'),
    ('Znak'),
    ('Wydawnictwo Czarne');

INSERT INTO categories (name)
VALUES
    ('Powieść'),
    ('Poezja'),
    ('Historia'),
    ('Dziecięca');

INSERT INTO languages (name)
VALUES
    ('Polski'),
    ('Angielski'),
    ('Niemiecki'),
    ('Francuski');

INSERT INTO books (category_id, language_id, publisher_id, title, isbn, release_year, cover_image_url)
VALUES
    (1, 1, 1, 'Quo Vadis', '9788373271890', 2020, 'imgur.com/1'),
    (2, 1, 2, 'Pan Tadeusz', '9788306014057', 2018, 'imgur.com/2'),
    (3, 1, 3, 'Faraon', '9788370200121', 2021, 'imgur.com/3'),
    (4, 1, 4, 'O krasnoludkach i sierotce Marysi', '9788373271906', 2019, 'imgur.com/4');

INSERT INTO book_authors (book_id, author_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4);

-- System admin insertion
INSERT INTO users (id, role_id, email, username, password)
VALUES
    ('YBWNYIBF1S', 1, 'sysadmin@bookrider.com',  'sysadmin1', 'hashed_password');


-- First library


-- Library admin
INSERT INTO users (id, role_id, email, username, password)
VALUES
    ('LIZ5395XCG', 2, 'libadmin1@library1.pl', 'libadmin1', 'hashed_password');

-- Creating new library
INSERT INTO addresses (street, city, postal_code, latitude, longitude)
VALUES
    ('Aleja Papieża Jana Pawła II 50', 'Szczecin', '70-453', 53.4291, 14.5535);

INSERT INTO library_addition_requests (created_by, address_id, library_name, phone_number, email, status)
VALUES
    ('LIZ5395XCG', 1, 'Miejska Biblioteka Publiczna w Szczecinie - Filia nr 1', '123-422-221', 'filia1@biblioteka.szczecin.pl', 'pending');

INSERT INTO libraries (address_id, name, phone_number, email)
VALUES
    (1, 'Miejska Biblioteka Publiczna w Szczecinie - Filia nr 1', '123-422-221', 'filia1@biblioteka.szczecin.pl');

UPDATE library_addition_requests
SET reviewed_by = 'YBWNYIBF1S', status = 'accepted', reviewed_at = CURRENT_TIMESTAMP
WHERE id = 1;

UPDATE users
SET library_id = 1, is_verified = TRUE
WHERE id = 'LIZ5395XCG';

-- Creating librarian accounts
INSERT INTO users (id, role_id, library_id, username, password, first_name, last_name)
VALUES
    ('EG2XHFX7E0', 3, 1, 'librarian1', 'hashed_password', 'Kasia', 'Wiśniewska'),
    ('NKF86554IJ', 3, 1, 'librarian2', 'hashed_password', 'Mateusz', 'Lewandowski');

-- Adding books to the first library
INSERT INTO library_books (library_id, book_id)
VALUES
    (1, 1),
    (1, 2);


-- Second library


-- Library admin
INSERT INTO users (id, role_id, email, username, password)
VALUES
    ('TST6J7W5NZ', 2, 'libadmin2@library2.pl', 'libadmin2', 'hashed_password');

-- Creating new library
INSERT INTO addresses (street, city, postal_code, latitude, longitude)
VALUES
    ('Plac Lotników 7', 'Szczecin', '70-414', 53.4299, 14.5507);

INSERT INTO library_addition_requests (created_by, address_id, library_name, phone_number, email, status)
VALUES
    ('TST6J7W5NZ', 2, 'Miejska Biblioteka Publiczna w Szczecinie - Filia nr 2', '765-982-221', 'filia2@biblioteka.szczecin.pl', 'pending');

INSERT INTO libraries (address_id, name, phone_number, email)
VALUES
    (2, 'Miejska Biblioteka Publiczna w Szczecinie - Filia nr 2', '765-982-221', 'filia2@biblioteka.szczecin.pl');

UPDATE library_addition_requests
SET reviewed_by = 'YBWNYIBF1S', status = 'accepted', reviewed_at = CURRENT_TIMESTAMP
WHERE id = 2;

UPDATE users
SET library_id = 2, is_verified = TRUE
WHERE id = 'TST6J7W5NZ';

-- Creating librarian accounts
INSERT INTO users (id, role_id, library_id, username, password, first_name, last_name)
VALUES
    ('8GOLU2UJR8', 3, 2, 'librarian1', 'hashed_password', 'Anna', 'Kowalska'),
    ('LKB9EQQL15', 3, 2, 'librarian2', 'hashed_password', 'Jan', 'Nowak');

-- Adding books to the first library
INSERT INTO library_books (library_id, book_id)
VALUES
    (2, 2),
    (2, 3),
    (2, 4);


-- Adding drivers


-- First driver


INSERT INTO users (id, role_id, email, username, password, first_name, last_name)
VALUES
    ('8IP8HAOAB4', 5, 'driver1@gmail.com', 'driver1', 'hashed_password', 'Kacper', 'Wysocki');

INSERT INTO driver_application_requests (user_id, status)
VALUES
    ('8IP8HAOAB4', 'pending');

INSERT INTO driver_documents (driver_application_id, document_type, document_photo_url, expiry_date)
VALUES
    (1, 'ID', 'imgur.com/driver1-id', '2028-06-01'),
    (1, 'driving_licence', 'imgur.com/driver1-driving-licence', '2041-01-01');

UPDATE driver_application_requests
SET reviewed_by = 'YBWNYIBF1S', status = 'accepted', reviewed_at = CURRENT_TIMESTAMP
WHERE id = 1;

UPDATE users
SET is_verified = TRUE
WHERE id = '8GOLU2UJR8';


-- Second driver


INSERT INTO users (id, role_id, email, username, password, first_name, last_name)
VALUES
    ('3XEJRZW5IC', 5, 'driver2@gmail.com', 'driver2', 'hashed_password', 'Dominik', 'Kwiatkowski');

INSERT INTO driver_application_requests (user_id, status)
VALUES
    ('3XEJRZW5IC', 'pending');

INSERT INTO driver_documents (driver_application_id, document_type, document_photo_url, expiry_date)
VALUES
    (2, 'ID', 'imgur.com/driver2-id', '2029-03-01'),
    (2, 'driving_licence', 'imgur.com/driver2-driving_licence', '2035-02-01');

UPDATE driver_application_requests
SET reviewed_by = 'YBWNYIBF1S', status = 'accepted', reviewed_at = CURRENT_TIMESTAMP
WHERE id = 2;

UPDATE users
SET is_verified = TRUE
WHERE id = '3XEJRZW5IC';


-- First user


INSERT INTO users (id, role_id, email, password)
VALUES
    ('GWVF51WKGG', 4, 'user1@gmail.com', 'hashed_password');

-- User verification
INSERT INTO library_cards (user_id, card_id, first_name, last_name, expiration_date)
VALUES
    ('GWVF51WKGG', '123123', 'Michał', 'Bednarek', '2029-09-01');

UPDATE users
SET is_verified = TRUE
WHERE id = 'GWVF51WKGG';

-- User book order
INSERT INTO transactions (user_id, amount, transaction_type)
VALUES
    ('GWVF51WKGG', 10.00, 'deposit');

-- Initial user balance deposit
UPDATE users
SET balance = balance + 10.00
WHERE id = 'GWVF51WKGG';

INSERT INTO addresses (street, city, postal_code, latitude, longitude)
VALUES
    ('ul. Gdańska 12', 'Szczecin', '70-743', 53.4485, 14.5525);

INSERT INTO orders (user_id, library_id, target_address_id, status, payment_status)
VALUES
    ('GWVF51WKGG', 1, 3, 'pending', 'pending');

INSERT INTO order_items (order_id, book_id, quantity, status)
VALUES
    (1, 2, 2, 'pending');

-- Payment for the order from the user balance
INSERT INTO transactions (user_id, order_id, amount, transaction_type)
VALUES
    ('GWVF51WKGG', 1, 10.00, 'order_payment');

UPDATE users
SET balance = balance - 10.00
WHERE id = 'GWVF51WKGG';

UPDATE orders
SET payment_status = 'completed'
WHERE id = 1;

-- Order acceptation by the librarian
UPDATE orders
SET librarian_id = 'EG2XHFX7E0', status = 'accepted', accepted_at = CURRENT_TIMESTAMP
WHERE id = 1;

UPDATE order_items
SET status = 'accepted'
WHERE order_id = 1;

-- Driver accepted the order
UPDATE orders
SET driver_id = '3XEJRZW5IC'
WHERE id = 1;

-- Driver picked up the order from the librarian
UPDATE orders
SET status = 'in_transit', picked_up_at = CURRENT_TIMESTAMP
WHERE id = 1;

UPDATE order_items
SET status = 'in_transit'
WHERE order_id = 1;

-- Driver delivers the order
UPDATE orders
SET delivery_photo_url = 'imgur.com/driver2-delivery1', status = 'delivered', delivered_at = CURRENT_TIMESTAMP
WHERE id = 1;

UPDATE order_items
SET status = 'delivered'
WHERE order_id = 1;

-- Driver payment for the delivery
INSERT INTO transactions (user_id, amount, transaction_type)
VALUES
    ('3XEJRZW5IC', 7.50, 'driver_payment');

UPDATE users
SET balance = balance + 7.50
WHERE id = '3XEJRZW5IC';

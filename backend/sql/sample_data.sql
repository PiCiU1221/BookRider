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
    ('Aleja Papieża Jana Pawła II 50', 'Szczecin', '70-453', 53.429155, 14.551630);

INSERT INTO library_addition_requests (created_by, address_id, library_name, phone_number, email, status)
VALUES
    ('LIZ5395XCG', 1, 'Miejska Biblioteka Publiczna w Szczecinie - Filia nr 1', '123-422-221', 'filia1@biblioteka.szczecin.pl', 'PENDING');

INSERT INTO libraries (address_id, name, phone_number, email)
VALUES
    (1, 'Miejska Biblioteka Publiczna w Szczecinie - Filia nr 1', '123-422-221', 'filia1@biblioteka.szczecin.pl');

UPDATE library_addition_requests
SET reviewed_by = 'YBWNYIBF1S',
    status = 'ACCEPTED',
    reviewed_at = CURRENT_TIMESTAMP
WHERE id = 1;

UPDATE users
SET library_id = 1,
    is_verified = TRUE
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
    ('Plac Lotników 7', 'Szczecin', '70-414', 53.429556, 14.550914);

INSERT INTO library_addition_requests (created_by, address_id, library_name, phone_number, email, status)
VALUES
    ('TST6J7W5NZ', 2, 'Miejska Biblioteka Publiczna w Szczecinie - Filia nr 2', '765-982-221', 'filia2@biblioteka.szczecin.pl', 'PENDING');

INSERT INTO libraries (address_id, name, phone_number, email)
VALUES
    (2, 'Miejska Biblioteka Publiczna w Szczecinie - Filia nr 2', '765-982-221', 'filia2@biblioteka.szczecin.pl');

UPDATE library_addition_requests
SET reviewed_by = 'YBWNYIBF1S',
    status = 'ACCEPTED',
    reviewed_at = CURRENT_TIMESTAMP
WHERE id = 2;

UPDATE users
SET library_id = 2,
    is_verified = TRUE
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
    ('8IP8HAOAB4', 'PENDING');

INSERT INTO driver_documents (driver_application_id, document_type, document_photo_url, expiry_date)
VALUES
    (1, 'ID', 'imgur.com/driver1-id', '2028-06-01'),
    (1, 'DRIVER_LICENSE', 'imgur.com/driver1-driving-licence', '2041-01-01');

UPDATE driver_application_requests
SET reviewed_by = 'YBWNYIBF1S',
    status = 'APPROVED',
    reviewed_at = CURRENT_TIMESTAMP
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
    ('3XEJRZW5IC', 'PENDING');

INSERT INTO driver_documents (driver_application_id, document_type, document_photo_url, expiry_date)
VALUES
    (2, 'ID', 'imgur.com/driver2-id', '2029-03-01'),
    (2, 'DRIVER_LICENSE', 'imgur.com/driver2-driving_licence', '2035-02-01');

UPDATE driver_application_requests
SET reviewed_by = 'YBWNYIBF1S',
    status = 'APPROVED',
    reviewed_at = CURRENT_TIMESTAMP
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


-- Initial user balance deposit
INSERT INTO transactions (user_id, amount, transaction_type)
VALUES
    ('GWVF51WKGG', 100.00, 'WALLET_DEPOSIT');

UPDATE users
SET balance = balance + 100.00
WHERE id = 'GWVF51WKGG';

INSERT INTO addresses (street, city, postal_code, latitude, longitude)
VALUES
    ('ul. Gdańska 12', 'Szczecin', '70-743', 53.4485, 14.5525);

INSERT INTO orders (user_id, library_id, pickup_address_id, destination_address_id, is_return, status, amount, payment_status)
VALUES
    ('GWVF51WKGG', 1, 1, 3, false,'PENDING', 10.00, 'PENDING');

INSERT INTO order_items (order_id, book_id, quantity)
VALUES
    (1, 2, 2);

-- Payment for the order from the user balance
INSERT INTO transactions (user_id, order_id, amount, transaction_type)
VALUES
    ('GWVF51WKGG', 1, 10.00, 'BOOK_ORDER_PAYMENT');

UPDATE users
SET balance = balance - 10.00
WHERE id = 'GWVF51WKGG';

UPDATE orders
SET payment_status = 'COMPLETED'
WHERE id = 1;

-- Order acceptation by the librarian
UPDATE orders
SET librarian_id = 'EG2XHFX7E0',
    status = 'ACCEPTED',
    accepted_at = CURRENT_TIMESTAMP
WHERE id = 1;

-- Driver accepted the order
UPDATE orders
SET driver_id = '3XEJRZW5IC',
    status = 'DRIVER_ACCEPTED'
WHERE id = 1;

-- Driver picked up the order from the librarian
UPDATE orders
SET status = 'IN_TRANSIT',
    picked_up_at = CURRENT_TIMESTAMP
WHERE id = 1;

-- Driver delivers the order
UPDATE orders
SET delivery_photo_url = 'imgur.com/driver2-delivery1',
    status = 'DELIVERED',
    delivered_at = CURRENT_TIMESTAMP
WHERE id = 1;

INSERT INTO rentals (user_id, book_id, library_id, order_id, quantity, rented_at, return_deadline, status)
VALUES
    ('GWVF51WKGG', 2, 1, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP + INTERVAL '30 days', 'RENTED');

-- Driver payment for the delivery
INSERT INTO transactions (user_id, amount, transaction_type)
VALUES
    ('3XEJRZW5IC', 7.50, 'DRIVER_EARNINGS');

UPDATE users
SET balance = balance + 7.50
WHERE id = '3XEJRZW5IC';


-- User book return


-- User creates the return order
INSERT INTO orders (user_id, library_id, pickup_address_id, destination_address_id, is_return, status, amount, payment_status)
VALUES
    ('GWVF51WKGG', 1, 3, 1, true, 'PENDING', 10.00, 'PENDING');

INSERT INTO order_items (order_id, book_id, quantity)
VALUES
    (2, 2, 2);

-- User pays for the return order
INSERT INTO transactions (user_id, order_id, amount, transaction_type)
VALUES
    ('GWVF51WKGG', 2, 10.00, 'BOOK_RETURN_PAYMENT');

UPDATE users
SET balance = balance - 10.00
WHERE id = 'GWVF51WKGG';

UPDATE orders
SET payment_status = 'COMPLETED'
WHERE id = 2;

UPDATE rentals
SET status = 'RETURN_IN_PROGRESS'
WHERE id = 1;

-- Rental table for tracking rentals (because they can be partial)
INSERT INTO rental_returns (return_order_id, returned_at, status)
VALUES
    (1,NULL, 'IN_PROGRESS');

INSERT INTO rental_return_items (rental_return_id, rental_id, book_id, returned_quantity)
VALUES
    (1, 1, 2, 2);

-- Driver accept the return order
UPDATE orders
SET driver_id = '3XEJRZW5IC',
    status = 'DRIVER_ACCEPTED'
WHERE id = 2;

-- Driver picks up the books
UPDATE orders
SET status = 'IN_TRANSIT',
    picked_up_at = CURRENT_TIMESTAMP
WHERE id = 2;

-- Driver delivers the returned books to the library
UPDATE orders
SET delivery_photo_url = 'imgur.com/driver2-return1',
    status = 'DELIVERED',
    delivered_at = CURRENT_TIMESTAMP
WHERE id = 2;

UPDATE orders
SET payment_status = 'COMPLETED'
WHERE id = 2;

UPDATE rentals
SET status = 'RETURNED'
WHERE id = 1;

UPDATE rental_returns
SET returned_at = CURRENT_TIMESTAMP,
    status = 'COMPLETED'
WHERE id = 1;

-- Driver payment for the delivery
INSERT INTO transactions (user_id, amount, transaction_type)
VALUES
    ('3XEJRZW5IC', 7.50, 'DRIVER_EARNINGS');

UPDATE users
SET balance = balance + 7.50
WHERE id = '3XEJRZW5IC';

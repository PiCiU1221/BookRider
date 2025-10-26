-- Database schema with create table statements

BEGIN;

CREATE EXTENSION postgis;

SET TIME ZONE 'Europe/Warsaw';

-- Book related tables

CREATE TABLE authors
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE publishers
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE languages
(
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE books
(
    id SERIAL PRIMARY KEY,
    category_id INT REFERENCES categories(id),
    language_id INT NOT NULL REFERENCES languages(id),
    publisher_id INT REFERENCES publishers(id),
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) UNIQUE NOT NULL,
    release_year INT,
    cover_image_url TEXT
);

CREATE TABLE book_authors
(
    book_id INT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
    author_id INT NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, author_id)
);

-- Address related table

CREATE TABLE addresses
(
    id SERIAL PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL
);

-- Library related tables

CREATE TABLE libraries
(
    id SERIAL PRIMARY KEY,
    address_id INT NOT NULL REFERENCES addresses(id),
    name VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE library_books
(
    library_id INT NOT NULL REFERENCES libraries(id),
    book_id INT NOT NULL REFERENCES books(id),
    PRIMARY KEY (library_id, book_id)
);

-- User related tables

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users
(
    id CHAR(10) PRIMARY KEY,
    role_id INT NOT NULL REFERENCES roles(id),
    library_id INT REFERENCES libraries(id),
    email VARCHAR(100) UNIQUE,
    username VARCHAR(100),
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    balance NUMERIC(10, 2) DEFAULT 0.00,
    is_verified BOOLEAN DEFAULT FALSE,
    last_login TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

/*
Index to make sure that there are no username
duplicates in a certain library
*/
CREATE UNIQUE INDEX unique_library_username
    ON users (library_id, username)
    WHERE library_id IS NOT NULL AND username IS NOT NULL;

CREATE TABLE library_cards
(
    id SERIAL PRIMARY KEY,
    user_id CHAR(10) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    card_id VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    expiration_date DATE NOT NULL
);

CREATE TABLE driver_application_requests
(
    id SERIAL PRIMARY KEY,
    user_id CHAR(10) NOT NULL REFERENCES users(id),
    reviewed_by CHAR(10) REFERENCES users(id),
    status VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP DEFAULT NULL,
    rejection_reason TEXT
);

CREATE TABLE driver_documents
(
    id SERIAL PRIMARY KEY,
    driver_application_id INT NOT NULL REFERENCES driver_application_requests(id) ON DELETE CASCADE,
    document_type VARCHAR(100) NOT NULL,
    document_photo_url TEXT,
    expiry_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shopping cart related tables

CREATE TABLE distance_cache
(
    id SERIAL PRIMARY KEY,
    start_latitude NUMERIC(9, 6) NOT NULL,
    start_longitude NUMERIC(9, 6) NOT NULL,
    end_latitude NUMERIC(9, 6) NOT NULL,
    end_longitude NUMERIC(9, 6) NOT NULL,
    distance NUMERIC(10, 1) NOT NULL
);

CREATE TABLE quotes
(
    id SERIAL PRIMARY KEY,
    valid_until TIMESTAMP NOT NULL,
    book_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

CREATE TABLE quote_options
(
    id SERIAL PRIMARY KEY,
    quote_id INTEGER NOT NULL,
    library_id INTEGER NOT NULL,
    distance_km NUMERIC(10, 1) NOT NULL,
    total_delivery_cost NUMERIC(10, 2) NOT NULL,
    library_name VARCHAR NOT NULL,
    FOREIGN KEY (quote_id) REFERENCES quotes (id) ON DELETE CASCADE,
    FOREIGN KEY (library_id) REFERENCES libraries (id) ON DELETE CASCADE
);

CREATE TABLE shopping_carts
(
    id SERIAL PRIMARY KEY,
    user_id CHAR(10) NOT NULL UNIQUE,
    total_delivery_cost NUMERIC(10, 2) NOT NULL,
    delivery_address_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (delivery_address_id) REFERENCES addresses (id) ON DELETE CASCADE
);

CREATE TABLE shopping_cart_items
(
    id SERIAL PRIMARY KEY,
    shopping_cart_id INTEGER NOT NULL,
    library_id INTEGER NOT NULL,
    delivery_cost NUMERIC(10, 2) NOT NULL,
    FOREIGN KEY (shopping_cart_id) REFERENCES shopping_carts (id) ON DELETE CASCADE,
    FOREIGN KEY (library_id) REFERENCES libraries (id) ON DELETE CASCADE
);

CREATE TABLE shopping_cart_sub_items
(
    id SERIAL PRIMARY KEY,
    shopping_cart_item_id INTEGER NOT NULL,
    book_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    FOREIGN KEY (shopping_cart_item_id) REFERENCES shopping_cart_items (id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);

-- Order related tables

CREATE TABLE orders
(
    id SERIAL PRIMARY KEY,
    user_id CHAR(10) NOT NULL REFERENCES users(id),
    driver_id CHAR(10) REFERENCES users(id),
    librarian_id CHAR(10) REFERENCES users(id),
    library_id INT NOT NULL REFERENCES libraries(id),
    pickup_address_id INT NOT NULL REFERENCES addresses(id),
    destination_address_id INT NOT NULL REFERENCES addresses(id),
    is_return BOOLEAN NOT NULL,
    status VARCHAR(50) NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    delivery_photo_url TEXT,
    note_to_driver TEXT,
    decline_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP DEFAULT NULL,
    driver_assigned_at TIMESTAMP DEFAULT NULL,
    picked_up_at TIMESTAMP DEFAULT NULL,
    delivered_at TIMESTAMP DEFAULT NULL
);

CREATE TABLE order_items
(
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id INT NOT NULL REFERENCES books(id),
    quantity INT NOT NULL CHECK (quantity > 0)
);

CREATE TABLE rentals
(
    id SERIAL PRIMARY KEY,
    user_id CHAR(10) NOT NULL REFERENCES users(id),
    book_id INT NOT NULL REFERENCES books(id),
    library_id INT NOT NULL REFERENCES libraries(id),
    order_id INT NOT NULL REFERENCES orders(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    rented_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    return_deadline TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE rental_returns
(
    id SERIAL PRIMARY KEY,
    return_order_id INT REFERENCES orders(id) ON DELETE CASCADE,
    returned_at TIMESTAMP DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL
);

CREATE TABLE rental_return_items
(
    id SERIAL PRIMARY KEY,
    rental_return_id INT NOT NULL REFERENCES rental_returns(id) ON DELETE CASCADE,
    rental_id INT NOT NULL REFERENCES rentals(id) ON DELETE CASCADE,
    returned_quantity INT NOT NULL CHECK (returned_quantity > 0)
);

CREATE TABLE transactions
(
    id SERIAL PRIMARY KEY,
    user_id CHAR(10) NOT NULL REFERENCES users(id),
    order_id INT REFERENCES orders(id),
    rental_return_id INT REFERENCES rental_returns(id),
    amount NUMERIC(10, 2) NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Library addition request table

CREATE TABLE library_addition_requests
(
    id SERIAL PRIMARY KEY,
    created_by CHAR(10) NOT NULL REFERENCES users(id),
    reviewed_by CHAR(10) REFERENCES users(id),
    address_id INT NOT NULL REFERENCES addresses(id),
    library_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMP DEFAULT NULL,
    rejection_reason TEXT
);

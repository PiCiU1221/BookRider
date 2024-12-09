-- Database schema with create table statements

BEGIN;

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
    name VARCHAR(100) NOT NULL,
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

-- Order related tables

CREATE TABLE orders
(
    id SERIAL PRIMARY KEY,
    user_id CHAR(10) NOT NULL REFERENCES users(id),
    driver_id CHAR(10) REFERENCES users(id),
    librarian_id CHAR(10) REFERENCES users(id),
    library_id INT NOT NULL REFERENCES libraries(id),
    target_address_id INT NOT NULL REFERENCES addresses(id),
    status VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    delivery_photo_url TEXT,
    note_to_driver TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP DEFAULT NULL,
    picked_up_at TIMESTAMP DEFAULT NULL,
    delivered_at TIMESTAMP DEFAULT NULL
);

CREATE TABLE order_items
(
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    book_id INT NOT NULL REFERENCES books(id),
    quantity INT NOT NULL CHECK (quantity > 0),
    return_deadline TIMESTAMP DEFAULT NULL,
    returned_quantity INT DEFAULT 0,
    returned_at TIMESTAMP DEFAULT NULL,
    status VARCHAR(50)
);

CREATE TABLE transactions
(
    id SERIAL PRIMARY KEY,
    user_id CHAR(10) NOT NULL REFERENCES users(id),
    order_id INT REFERENCES orders(id),
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

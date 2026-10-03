-- Car Rental Application - MySQL schema
CREATE DATABASE IF NOT EXISTS car_rental;
USE car_rental;

CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    email          VARCHAR(255) NOT NULL UNIQUE,
    password_hash  VARCHAR(255) NOT NULL,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    phone          VARCHAR(30),
    role           ENUM('CUSTOMER', 'ADMIN') NOT NULL,
    is_active      BOOLEAN NOT NULL DEFAULT TRUE,
    last_login     DATETIME NULL,
    created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customers (
    customer_id    INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL UNIQUE,
    address        VARCHAR(255),
    is_verified    BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE admins (
    admin_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE locations (
    location_id    INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    address        VARCHAR(255),
    city           VARCHAR(100),
    state          VARCHAR(50),
    zip_code       VARCHAR(20),
    phone          VARCHAR(30),
    is_active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE car_types (
    car_type_id    INT AUTO_INCREMENT PRIMARY KEY,
    type_name      VARCHAR(100) NOT NULL,
    description    VARCHAR(255),
    daily_rate     DECIMAL(10,2) NOT NULL
);

CREATE TABLE vehicles (
    vehicle_id     INT AUTO_INCREMENT PRIMARY KEY,
    make           VARCHAR(100) NOT NULL,
    model          VARCHAR(100) NOT NULL,
    year           INT NOT NULL,
    car_type_id    INT NOT NULL,
    license_plate  VARCHAR(30) NOT NULL UNIQUE,
    daily_rate     DECIMAL(10,2) NOT NULL,
    status         ENUM('AVAILABLE', 'RENTED', 'MAINTENANCE') NOT NULL DEFAULT 'AVAILABLE',
    location_id    INT NOT NULL,
    image_url      VARCHAR(500),
    FOREIGN KEY (car_type_id) REFERENCES car_types(car_type_id),
    FOREIGN KEY (location_id) REFERENCES locations(location_id)
);

CREATE TABLE bookings (
    booking_id          INT AUTO_INCREMENT PRIMARY KEY,
    customer_id         INT NOT NULL,
    vehicle_id          INT NOT NULL,
    pickup_location_id  INT NOT NULL,
    dropoff_location_id INT NOT NULL,
    start_date          DATE NOT NULL,
    end_date            DATE NOT NULL,
    status              ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'PENDING',
    total_cost          DECIMAL(10,2) NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    FOREIGN KEY (pickup_location_id) REFERENCES locations(location_id),
    FOREIGN KEY (dropoff_location_id) REFERENCES locations(location_id)
);

INSERT INTO locations (name, address, city, state, zip_code, phone) VALUES
 ('Downtown Branch', '100 Main St', 'Syracuse', 'NY', '13202', '315-555-0100'),
 ('Airport Branch', '1000 Airport Blvd', 'Syracuse', 'NY', '13212', '315-555-0200');

INSERT INTO car_types (type_name, description, daily_rate) VALUES
 ('Economy', 'Compact, fuel-efficient', 39.99),
 ('SUV', 'Spacious, good for families', 69.99),
 ('Luxury', 'Premium comfort and features', 119.99);

INSERT INTO vehicles (make, model, year, car_type_id, license_plate, daily_rate, status, location_id, image_url) VALUES
 ('Toyota', 'Corolla', 2024, 1, 'ABC1234', 39.99, 'AVAILABLE', 1, NULL),
 ('Honda', 'CR-V', 2024, 2, 'DEF5678', 69.99, 'AVAILABLE', 1, NULL),
 ('BMW', '5 Series', 2023, 3, 'GHI9012', 119.99, 'AVAILABLE', 2, NULL);
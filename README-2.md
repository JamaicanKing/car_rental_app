# Wayfare Rentals — Car Rental Prototype (Java + MySQL + HTML/CSS)

Full prototype for the CIS 453 car rental project: registration/login,
viewing available vehicles, and booking a vehicle — built directly off
the class diagram (`User` → `Customer`/`Admin` inheritance, `Vehicle`,
`Location`, `CarType`, `Booking`). One Java process serves both the
REST API and the static frontend, so there's nothing else to run.

## Stack
- **Java 17**, plain OOP (no framework) — one class per UML box
- **MySQL** via JDBC (`mysql-connector-j` — the only external dependency)
- A tiny built-in HTTP server (`com.sun.net.httpserver`, ships with the JDK)
  exposes a JSON REST API **and** serves the frontend — no separate web server
- **HTML/CSS/vanilla JS** frontend (no build step, no npm) — open it by
  visiting `http://localhost:8080` once the server is running

## Project layout
```
src/main/java/com/carrental/
  model/      User (abstract), Customer, Admin, Vehicle, CarType, Location, Booking
  dao/        DatabaseConnection, UserDAO, VehicleDAO, BookingDAO, LocationDAO, CarTypeDAO
  service/    AuthService, VehicleService, BookingService           (business logic)
  exception/  AuthenticationException, BookingException
  api/        Server (HTTP routes), StaticFileHandler, JsonUtil (tiny JSON helper)
  Main.java
frontend/
  index.html       -- sign in / create account
  dashboard.html    -- browse vehicles, book, view/cancel my bookings
  styles.css, app.js, auth.js, dashboard.js
sql/schema.sql     -- run this first to create the database + seed data
```

### Why these classes map to the UML diagram
- `User` is abstract; `Customer` and `Admin` extend it — the same
  inheritance arrow shown in the diagram. `getRole()` is the
  polymorphic method each subclass overrides.
- Every box on the diagram (`Vehicle`, `Booking`, `Location`, `CarType`)
  is its own class with private fields + getters/setters (encapsulation).
- DAOs are the only classes that touch SQL — services never see a
  `Connection` directly, and the HTTP layer never sees SQL at all.

## Setup

1. **Install MySQL** locally (or use one you already have running).
2. **Create the database:**
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
   This creates the `car_rental` database, all tables, and a few seed
   rows (2 locations, 3 car types, 3 vehicles) so `/api/vehicles`
   returns something immediately.
3. **Configure credentials:**
   ```bash
   cp src/main/resources/config.properties.example src/main/resources/config.properties
   ```
   Edit `config.properties` and set `db.user` / `db.password` to match
   your MySQL install.
4. **Build and run** (run from the project root — `frontend/` is loaded
   relative to the working directory):
   ```bash
   mvn clean package
   java -jar target/car-rental-backend.jar
   ```
   You should see `Car Rental app running at http://localhost:8080`.

   No Maven installed? `mvn` needs internet access the first time to
   pull `mysql-connector-j` — run the build on a machine with a normal
   internet connection (your laptop), not an offline sandbox.
5. **Open the app:** visit `http://localhost:8080` in a browser. Create
   an account, browse the seeded vehicles, and book one.

## API quick reference

| Method | Path | Body / Query | Notes |
|---|---|---|---|
| POST | `/api/register` | `{email, password, firstName, lastName, phone, address}` | Creates a Customer |
| POST | `/api/login` | `{email, password}` | Returns `{userId, role, fullName, customerId}` |
| GET | `/api/vehicles?carTypeId=&locationId=` | — | Lists AVAILABLE vehicles, filters optional |
| GET | `/api/vehicles/{id}` | — | Single vehicle |
| POST | `/api/bookings` | `{customerId, vehicleId, pickupLocationId, dropoffLocationId, startDate, endDate}` | Checks availability, computes totalCost, marks vehicle RENTED |
| GET | `/api/bookings?customerId=` | — | A customer's booking history |
| DELETE | `/api/bookings/{id}` | — | Cancels a booking, frees up the vehicle |
| GET | `/api/locations` | — | For the pickup/dropoff dropdowns |
| GET | `/api/car-types` | — | For the vehicle-type filter |

Quick manual test once the server is running:
```bash
curl -X POST localhost:8080/api/register -d '{"email":"a@b.com","password":"pass123","firstName":"Ann","lastName":"Lee","phone":"315-555-1111","address":"1 Main St"}'

curl -X POST localhost:8080/api/login -d '{"email":"a@b.com","password":"pass123"}'

curl localhost:8080/api/vehicles

curl -X POST localhost:8080/api/bookings -d '{"customerId":1,"vehicleId":1,"pickupLocationId":1,"dropoffLocationId":1,"startDate":"2026-10-10","endDate":"2026-10-13"}'
```

## Design notes / what's simplified for a prototype
- Passwords are salted SHA-256 (via `java.security`), not BCrypt — noted
  in `AuthService` as a swap-in for a production build, done this way
  here to keep the dependency list to just the MySQL driver.
- No session/token auth yet — `/api/login` returns the user's IDs and
  the frontend is expected to hold onto `customerId` for subsequent
  calls. Fine for a prototype; add JWT or server sessions before this
  goes further than a class project demo.
- `Payment`, `BillingInfo`, and `Notification` from the full diagram
  aren't implemented yet — the brief asked for registration/login,
  viewing cars, and booking as the minimum, so this covers exactly
  that slice. Those three classes are a natural next phase.

## Frontend notes
- No session tokens — after login the frontend just holds `customerId`
  in `localStorage` and sends it with each booking request. Fine for a
  class prototype; a real build would need server-side sessions/JWT
  before this is public-facing.
- The booking modal shows a client-side cost *estimate* (days × daily
  rate) for instant feedback; the server recalculates and stores the
  authoritative `totalCost` independently.

## Not yet built
- Admin screens (manageVehicles, viewBookings, generateReports) — the
  `Admin` model and role already exist, just no UI/endpoints for it yet
- Payment processing (`Payment`/`BillingInfo` from the full diagram)

package com.carrental.api;

import com.carrental.dao.CarTypeDAO;
import com.carrental.dao.LocationDAO;
import com.carrental.exception.AuthenticationException;
import com.carrental.exception.BookingException;
import com.carrental.model.Booking;
import com.carrental.model.CarType;
import com.carrental.model.Customer;
import com.carrental.model.Location;
import com.carrental.model.User;
import com.carrental.model.Vehicle;
import com.carrental.service.AuthService;
import com.carrental.service.BookingService;
import com.carrental.service.VehicleService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Plain-JDK HTTP API (com.sun.net.httpserver) so the prototype runs with
 * nothing but the MySQL driver on the classpath — no Spring/Javalin needed.
 * The HTML/CSS frontend calls these endpoints with fetch().
 *
 * Routes:
 *   POST /api/register        {email, password, firstName, lastName, phone, address}
 *   POST /api/login           {email, password}
 *   GET  /api/vehicles        ?carTypeId=&locationId=
 *   GET  /api/vehicles/{id}
 *   POST /api/bookings        {customerId, vehicleId, pickupLocationId, dropoffLocationId, startDate, endDate}
 *   GET  /api/bookings?customerId=
 */
public class Server {

    private final AuthService authService = new AuthService();
    private final VehicleService vehicleService = new VehicleService();
    private final BookingService bookingService = new BookingService();
    private final LocationDAO locationDAO = new LocationDAO();
    private final CarTypeDAO carTypeDAO = new CarTypeDAO();

    public void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/register", this::handleRegister);
        server.createContext("/api/login", this::handleLogin);
        server.createContext("/api/vehicles", this::handleVehicles);
        server.createContext("/api/bookings", this::handleBookings);
        server.createContext("/api/locations", this::handleLocations);
        server.createContext("/api/car-types", this::handleCarTypes);

        // Everything else falls through to the static HTML/CSS/JS frontend
        // in ./frontend, so the whole app runs from this one process.
        server.createContext("/", new StaticFileHandler(Path.of("frontend")));

        server.setExecutor(null); // default executor is fine for a prototype
        server.start();
        System.out.println("Car Rental app running at http://localhost:" + port);
    }

    // ---- /api/register -----------------------------------------------

    private void handleRegister(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, JsonUtil.map("error", "Use POST"));
            return;
        }
        Map<String, String> body = JsonUtil.parseFlatObject(readBody(exchange));
        try {
            Customer customer = authService.register(
                    body.get("email"), body.get("password"), body.get("firstName"),
                    body.get("lastName"), body.get("phone"), body.get("address"));
            sendJson(exchange, 201, JsonUtil.map(
                    "customerId", customer.getCustomerId(),
                    "userId", customer.getUserId(),
                    "email", customer.getEmail(),
                    "fullName", customer.getFullName()));
        } catch (AuthenticationException e) {
            sendJson(exchange, 400, JsonUtil.map("error", e.getMessage()));
        }
    }

    // ---- /api/login -----------------------------------------------

    private void handleLogin(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, JsonUtil.map("error", "Use POST"));
            return;
        }
        Map<String, String> body = JsonUtil.parseFlatObject(readBody(exchange));
        try {
            User user = authService.login(body.get("email"), body.get("password"));
            sendJson(exchange, 200, JsonUtil.map(
                    "userId", user.getUserId(),
                    "role", user.getRole(),
                    "fullName", user.getFullName(),
                    "customerId", user instanceof Customer ? ((Customer) user).getCustomerId() : null));
        } catch (AuthenticationException e) {
            sendJson(exchange, 401, JsonUtil.map("error", e.getMessage()));
        }
    }

    // ---- /api/vehicles -----------------------------------------------

    private void handleVehicles(HttpExchange exchange) throws IOException {
        withCors(exchange);
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendJson(exchange, 405, JsonUtil.map("error", "Use GET"));
            return;
        }
        try {
            String path = exchange.getRequestURI().getPath();
            String[] segments = path.split("/");
            if (segments.length == 4) { // /api/vehicles/{id}
                int vehicleId = Integer.parseInt(segments[3]);
                var vehicleOpt = vehicleService.getVehicle(vehicleId);
                if (vehicleOpt.isEmpty()) {
                    sendJson(exchange, 404, JsonUtil.map("error", "Vehicle not found"));
                    return;
                }
                sendJson(exchange, 200, toMap(vehicleOpt.get()));
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI());
            Integer carTypeId = query.containsKey("carTypeId") ? Integer.parseInt(query.get("carTypeId")) : null;
            Integer locationId = query.containsKey("locationId") ? Integer.parseInt(query.get("locationId")) : null;
            List<Vehicle> vehicles = vehicleService.listAvailableVehicles(carTypeId, locationId);
            sendJson(exchange, 200, vehicles.stream().map(this::toMap).collect(Collectors.toList()));
        } catch (SQLException e) {
            sendJson(exchange, 500, JsonUtil.map("error", "Database error: " + e.getMessage()));
        }
    }

    // ---- /api/bookings -----------------------------------------------

    private void handleBookings(HttpExchange exchange) throws IOException {
        withCors(exchange);
        String method = exchange.getRequestMethod();
        try {
            if ("POST".equals(method)) {
                Map<String, String> body = JsonUtil.parseFlatObject(readBody(exchange));
                Booking booking = bookingService.createBooking(
                        Integer.parseInt(body.get("customerId")),
                        Integer.parseInt(body.get("vehicleId")),
                        Integer.parseInt(body.get("pickupLocationId")),
                        Integer.parseInt(body.get("dropoffLocationId")),
                        LocalDate.parse(body.get("startDate")),
                        LocalDate.parse(body.get("endDate")));
                sendJson(exchange, 201, toMap(booking));
            } else if ("GET".equals(method)) {
                Map<String, String> query = parseQuery(exchange.getRequestURI());
                int customerId = Integer.parseInt(query.get("customerId"));
                List<Booking> bookings = bookingService.getBookingsForCustomer(customerId);
                sendJson(exchange, 200, bookings.stream().map(this::toMap).collect(Collectors.toList()));
            } else if ("DELETE".equals(method)) {
                // /api/bookings/{id}  -> cancel
                String[] segments = exchange.getRequestURI().getPath().split("/");
                int bookingId = Integer.parseInt(segments[3]);
                bookingService.cancelBooking(bookingId);
                sendJson(exchange, 200, JsonUtil.map("bookingId", bookingId, "status", "CANCELLED"));
            } else {
                sendJson(exchange, 405, JsonUtil.map("error", "Use GET, POST or DELETE"));
            }
        } catch (BookingException e) {
            sendJson(exchange, 400, JsonUtil.map("error", e.getMessage()));
        } catch (SQLException e) {
            sendJson(exchange, 500, JsonUtil.map("error", "Database error: " + e.getMessage()));
        }
    }

    // ---- /api/locations, /api/car-types -----------------------------------------------

    private void handleLocations(HttpExchange exchange) throws IOException {
        withCors(exchange);
        try {
            List<Location> locations = locationDAO.findAll();
            sendJson(exchange, 200, locations.stream().map(this::toMap).collect(Collectors.toList()));
        } catch (SQLException e) {
            sendJson(exchange, 500, JsonUtil.map("error", "Database error: " + e.getMessage()));
        }
    }

    private void handleCarTypes(HttpExchange exchange) throws IOException {
        withCors(exchange);
        try {
            List<CarType> types = carTypeDAO.findAll();
            sendJson(exchange, 200, types.stream().map(this::toMap).collect(Collectors.toList()));
        } catch (SQLException e) {
            sendJson(exchange, 500, JsonUtil.map("error", "Database error: " + e.getMessage()));
        }
    }

    // ---- mapping helpers -----------------------------------------------

    private Map<String, Object> toMap(Vehicle v) {
        return JsonUtil.map(
                "vehicleId", v.getVehicleId(), "make", v.getMake(), "model", v.getModel(),
                "year", v.getYear(), "carType", v.getCarTypeName(), "licensePlate", v.getLicensePlate(),
                "dailyRate", v.getDailyRate(), "status", v.getStatus().name(),
                "locationId", v.getLocationId(), "imageUrl", v.getImageUrl());
    }

    private Map<String, Object> toMap(Location l) {
        return JsonUtil.map(
                "locationId", l.getLocationId(), "name", l.getName(), "address", l.getAddress(),
                "city", l.getCity(), "state", l.getState());
    }

    private Map<String, Object> toMap(CarType c) {
        return JsonUtil.map(
                "carTypeId", c.getCarTypeId(), "typeName", c.getTypeName(),
                "description", c.getDescription(), "dailyRate", c.getDailyRate());
    }

    private Map<String, Object> toMap(Booking b) {
        return JsonUtil.map(
                "bookingId", b.getBookingId(), "customerId", b.getCustomerId(),
                "vehicleId", b.getVehicleId(), "pickupLocationId", b.getPickupLocationId(),
                "dropoffLocationId", b.getDropoffLocationId(), "startDate", b.getStartDate().toString(),
                "endDate", b.getEndDate().toString(), "status", b.getStatus().name(),
                "totalCost", b.getTotalCost());
    }

    // ---- plumbing -----------------------------------------------

    private void withCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new java.util.LinkedHashMap<>();
        String query = uri.getQuery();
        if (query == null) return result;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) result.put(kv[0], kv[1]);
        }
        return result;
    }

    private void sendJson(HttpExchange exchange, int status, Object payload) throws IOException {
        byte[] bytes = JsonUtil.toJson(payload).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

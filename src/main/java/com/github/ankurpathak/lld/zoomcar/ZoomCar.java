package com.github.ankurpathak.lld.zoomcar;

import lombok.ToString;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class User {
    private static int idCounter = 0;
    int id;
    String name;

    User(String name) {
        id = ++idCounter;
        this.name = name;
    }
}

class UserManager {
    Map<Integer, User> users = new HashMap<>();

    void addUser(User user) {
        users.put(user.id, user);
    }

    void removeUser(User user) {
        users.remove(user.id);
    }
}

abstract class Vehicle {
    private static int idCounter = 0;
    int id;
    String registration;
    VehicleType vehicleType;
    VehicleStatus status;
    Store store;
    int yearOfManufacture;
    double kmDriven;
    BigDecimal securityDeposit = BigDecimal.ZERO;
    BigDecimal hourlyRate = BigDecimal.ZERO;
    int seatingCapacity = 4;
    double average = 10;
    int cc = 800;
    String model = "Swift";
    String brand = "Suzuki";

    Vehicle(String registration, Store store, int yearOfManufacture, double kmDriven, BigDecimal securityDeposit, BigDecimal hourlyRate) {
        id = ++idCounter;
        this.registration = registration;
        this.store = store;
        vehicleType = VehicleType.CAR;
        status = VehicleStatus.AVAILABLE;
        this.yearOfManufacture = yearOfManufacture;
        this.kmDriven = kmDriven;
        this.securityDeposit = securityDeposit;
        this.hourlyRate = hourlyRate;

    }
}

class Car extends Vehicle {
    public Car(String registration, Store store, int yearOfManufacture, double kmDriven, BigDecimal securityDeposit, BigDecimal hourlyRate) {
        super(registration, store, yearOfManufacture, kmDriven, securityDeposit, hourlyRate);
        this.yearOfManufacture = yearOfManufacture;
        this.kmDriven = kmDriven;
        this.hourlyRate = hourlyRate;
    }

    CarType carType = CarType.HATCHBACK;


}

class Location {
    private static int idCounter = 0;
    int id;
    String city;
    String state;
    String country;
    String pinCode;

    Location(String city, String state, String country, String pinCode) {
        id = ++idCounter;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pinCode = pinCode;
    }
}

class Store {
    private static int idCounter = 0;
    int id;
    Location location;
    String name;
    String contact;

    Store(Location location, String name, String contact) {
        id = ++idCounter;
        this.name = name;
        this.contact = contact;
        this.location = location;
    }
}

class StoreManager {
    Map<Integer, Store> stores = new HashMap<>();

    void addStore(Store store) {
        stores.put(store.id, store);
    }

    void removeStore(Store store) {
        stores.remove(store.id);
    }
}

class VehicleManager {
    Map<Integer, Vehicle> vehicles = new HashMap<>();
    Map<Store, List<Vehicle>> storeVehicles = new HashMap<>();

    void addVehicle(Vehicle vehicle) {
        vehicles.put(vehicle.id, vehicle);
        storeVehicles.putIfAbsent(vehicle.store, new ArrayList<>());
        storeVehicles.get(vehicle.store).add(vehicle);
    }

    void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle.id);
        storeVehicles.putIfAbsent(vehicle.store, new ArrayList<>());
        storeVehicles.get(vehicle.store).remove(vehicle);
    }
}

class Booking {
    private static int idCounter = 0;
    int id;
    Vehicle vehicle;
    User user;
    Store store;
    Instant booking;
    Instant bookingTill;
    Instant pickup;
    Instant drop;
    Location pickupLocation;
    Location dropLocation;
    BookingStatus status = BookingStatus.RESERVED;
    List<Payment> payments = new ArrayList<>();
    BigDecimal paidAmount = BigDecimal.ZERO;
    PaymentManager paymentManager;

    Booking(Vehicle vehicle, User user, Store store, int bookingForDays, PaymentManager paymentManager) {
        id = ++idCounter;
        this.vehicle = vehicle;
        this.user = user;
        this.store = store;
        booking = Instant.now();
        bookingTill = booking.plus(bookingForDays, ChronoUnit.DAYS);
        pickupLocation = store.location;
        dropLocation = store.location;
        this.paymentManager = paymentManager;
    }

    BigDecimal computeBookingAmount() {
        long minutes = Duration.between(booking, bookingTill).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(Math.ceil(minutes / 60.00));
        return vehicle.hourlyRate.multiply(hours).add(vehicle.securityDeposit);
    }

    BigDecimal billedAmount() {
        long minutes = Duration.between(pickup, Instant.now()).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(Math.ceil(minutes / 60.00));
        return vehicle.hourlyRate.multiply(hours);
    }

    BigDecimal computeRefund() {
        return paidAmount.subtract(billedAmount());
    }



    void addPayment(Payment payment) {
        paidAmount = paidAmount.add(payment.amount);
        payments.add(payment);
    }

    void removePayment(Payment payment) {
        payments.remove(payment);
    }


    void processPickup() {
        if (status == BookingStatus.RESERVED && paidAmount.compareTo(computeBookingAmount()) >= 0){
            status = BookingStatus.IN_PROGRESS;
            pickup = Instant.now();
        }

    }

    void processDrop() {
        if (status == BookingStatus.IN_PROGRESS &&  paidAmount.compareTo(billedAmount()) >= 0){
            status = BookingStatus.COMPLETED;
            drop = Instant.now();
            Payment payment = paymentManager.createPayment(store, BigDecimal.valueOf(-1).multiply(computeRefund()), PaymentStatus.SUCCESS);
            addPayment(payment);
        }
    }
}





@ToString
class Payment {
    private static int idCounter = 0;
    int id;
    BigDecimal amount = BigDecimal.ZERO;
    PaymentStatus status = PaymentStatus.PENDING;
    Store store;
    Payment(Store store, BigDecimal amount, PaymentStatus status) {
        id = ++idCounter;
        this.store = store;
        this.amount = amount;
        this.status = status;
    }
}

enum PaymentStatus {
    PENDING, SUCCESS, FAILED
}

class BookingManager {
    Map<Integer, Booking> bookings = new HashMap<>();
    Map<Store, List<Booking>> storeBookings = new HashMap<>();
    Map<Integer, Payment> payments = new HashMap<>();
    Map<Store, List<Payment>> storePayments = new HashMap<>();
    PaymentManager paymentManager;

    BookingManager(PaymentManager paymentManager) {
        this.paymentManager = paymentManager;
    }


    void addBooking(Booking booking) {
        bookings.put(booking.id, booking);
        storeBookings.putIfAbsent(booking.store, new ArrayList<>());
        storeBookings.get(booking.store).add(booking);
    }

    void removeBooking(Booking booking) {
        bookings.remove(booking.id);
        storeBookings.putIfAbsent(booking.store, new ArrayList<>());
        storeBookings.get(booking.store).remove(booking);
    }

    Booking createBooking(Vehicle vehicle, User user, Store store, int bookingForDays) {
        Booking booking = new Booking(vehicle, user, store, bookingForDays, paymentManager);
        addBooking(booking);
        return booking;
    }

}

class PaymentManager {
    Map<Integer, Payment> payments = new HashMap<>();
    Map<Store, List<Payment>> storePayments = new HashMap<>();

    void addPayment(Payment payment) {
        payments.put(payment.id, payment);
        storePayments.putIfAbsent(payment.store, new ArrayList<>());
        storePayments.get(payment.store).add(payment);
    }

    void removePayment(Payment payment) {
        payments.remove(payment.id);
        storePayments.putIfAbsent(payment.store, new ArrayList<>());
        storePayments.get(payment.store).remove(payment);
    }

    Payment createPayment(Store store, BigDecimal amount, PaymentStatus status){
        Payment payment =  new Payment(store, amount, status);
        addPayment(payment);
        return payment;
    }
}

enum BookingStatus {
    RESERVED, CANCELLED, COMPLETED, IN_PROGRESS
}

enum VehicleStatus {
    AVAILABLE, BOOKED, UNDER_MAINTENANCE
}

enum CarType {
    SEDAN, SUV, HATCHBACK
}

enum VehicleType {
    CAR
}

class ZoomCar {
    UserManager userManager = new UserManager();
    VehicleManager vehicleManager = new VehicleManager();
    PaymentManager paymentManager = new PaymentManager();
    BookingManager bookingManager = new BookingManager(paymentManager);
    StoreManager storeManager = new StoreManager();

    public static void main(String[] args) throws Exception {
        ZoomCar zoomCar = new ZoomCar();
        User user = new User("Ankur");
        zoomCar.userManager.addUser(user);
        Location pune = new Location("Pune", "Maharashtra", "India", "411047");
        Location delhi = new Location("Pune", "Delhi", "India", "110001");
        Store puneStore = new Store(pune, "ZoomCar Lohegaon Pune", "+912068006800");
        Store delhiStore = new Store(delhi, "ZoomCar Connaught Place  Delhi", "+911168006800");
        zoomCar.storeManager.addStore(puneStore);
        zoomCar.storeManager.addStore(delhiStore);
        Vehicle vehicle = new Car("MH12AB1234", puneStore, 2020, 10000, BigDecimal.valueOf(10000), BigDecimal.valueOf(500));
        zoomCar.vehicleManager.addVehicle(vehicle);
        Booking booking = zoomCar.bookingManager.createBooking(vehicle, user, puneStore, 2);
        Payment payment = zoomCar.paymentManager.createPayment(puneStore, booking.computeBookingAmount(), PaymentStatus.SUCCESS);
        booking.addPayment(payment);
        booking.processPickup();
        TimeUnit.MINUTES.sleep(2);
        booking.processDrop();

    }
}
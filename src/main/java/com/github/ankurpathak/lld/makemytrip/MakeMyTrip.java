package com.github.ankurpathak.lld.makemytrip;

import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

class Airline {
    private static int idCounter = 0;
    int id;
    private String name;
    Map<Integer, Aircraft> aircraft = new HashMap<>();
    Map<Integer, Flight> flights = new HashMap<>();
    Airline(String name) {
        this.id = ++idCounter;
        this.name = name;
    }

    void addAircraft(Aircraft aircraft) {
        this.aircraft.put(aircraft.id, aircraft);
    }

    void removeAircraft(Aircraft aircraft) {
        this.aircraft.remove(aircraft.id);
    }

    void addFlight(Flight flight) {
        this.flights.put(flight.id, flight);
    }

    void removeFlight(Flight flight) {
        this.flights.remove(flight.id);
    }
}

class Aircraft {
    private static int idCounter = 0;
    int id;
    String tailNumber;
    String model;
    String manufacturer;
    List<Seat> seats = new ArrayList<>();
    Aircraft(String tailNumber, String model, String manufacturer) {
        this.id = ++idCounter;
        this.tailNumber = tailNumber;
        this.model = model;
        this.manufacturer = manufacturer;
    }

    public void addSeat(Seat seat) {
        seats.add(seat);
    }

    public void removeSeat(Seat seat) {
        seats.remove(seat);
    }
}

@AllArgsConstructor
class Seat {
    String identifier;
    int row;
    int col;
    SeatType type;
    Set<SeatFeature> features = EnumSet.noneOf(SeatFeature.class);
    void addFeature(SeatFeature feature) {
        features.add(feature);
    }

    void removeFeature(SeatFeature feature) {
        features.remove(feature);
    }
}

enum SeatFeature {
    WINDOW, EXTRA_LEG_ROOM
}

enum SeatType {
    ECONOMY, BUSINESS, PREMIUM;
}


class Airport {
    private static int idCounter = 0;
    int id;
    String code;
    String name;
    Airport(String code, String name) {
        this.id = ++idCounter;
        this.code = code;
        this.name = name;
    }
}

class Flight {
    private static int idCounter = 0;
    int id;
    Airline airline;
    Aircraft aircraft;
    String flightNumber;
    Airport source;
    Airport destination;
    Duration duration;
    int distance;
    Map<Integer, Schedule> schedules = new HashMap<>();

    public Flight(Airline airline, Aircraft aircraft, String flightNumber, Airport source, Airport destination, Duration duration, int distance) {
        id = ++idCounter;
        this.airline = airline;
        this.aircraft = aircraft;
        this.flightNumber = flightNumber;
        this.source = source;
        this.destination = destination;
        this.duration = duration;
        this.distance = distance;
    }

    void addSchedule(Schedule schedule) {
        schedules.put(schedule.id, schedule);
    }

    void removeSchedule(Schedule schedule) {
        schedules.remove(schedule.id);
    }
}

class Schedule {
    private static int idCounter = 0;
    int id;
    LocalDateTime departure;
    LocalDateTime arrival;
    Set<String> seatBookings = new HashSet<>();
    Map<SeatType, BigDecimal> seatPrices = new HashMap<>();
    Map<SeatFeature, BigDecimal> featurePrices = new HashMap<>();

    public Schedule(Flight flight, LocalDateTime departure, LocalDateTime arrival, Set<String> seatBookings) {
        id = ++idCounter;
        this.departure = departure;
        this.arrival = arrival;
        this.seatBookings = seatBookings;
    }
}

class AirlineManager {
    Map<Integer, Airline> airlines = new HashMap<>();


    void addAirline(Airline airline) {
        airlines.put(airline.id, airline);
    }

    void removeAirline(Airline airline) {
        airlines.remove(airline.id);
    }


}

class FlightManager {
    Map<Integer, Flight> flights = new HashMap<>();

    void addFlight(Flight flight) {
        flights.put(flight.id, flight);
    }

    void removeFlight(Flight flight) {
        flights.remove(flight.id);
    }

}

class AirportManager {
    Map<Integer, Airport> airports = new HashMap<>();

    void addAirport(Airport airport) {
        airports.put(airport.id, airport);
    }

    void removeAirport(Airport airport) {
        airports.remove(airport.id);
    }

}

enum FlyerNationalIdType{
    AADHAAR, PASSPORT, VOTER_ID, DRIVING_LICENCE
}

class Flyer {
    String name;
    String email;
    String contact;
    String flyerNationalId;
    FlyerNationalIdType flyerNationalIdType = FlyerNationalIdType.PASSPORT;
    Seat seat;


    public Flyer(String name, String contact, String email) {
        this.name = name;
        this.contact = contact;
        this.email = email;
    }
}

enum BookingStatus {
    PENDING, CONFIRMED, CANCELLED
}

class Booking {
    private static int idCounter = 0;
    int id;
    Flight flight;
    Schedule schedule;
    List<Flyer> flyers;
    List<Payment> payment = new ArrayList<>();
    BookingStatus status = BookingStatus.PENDING;
    boolean selection = false;
    SeatType flyingClass = SeatType.ECONOMY;
    BigDecimal amountPaid = BigDecimal.ZERO;
    MakeMyTrip system;


    public Booking(Flight flight, Schedule schedule, List<Flyer> flyers, SeatType flyingClass, MakeMyTrip system) {
        id = ++idCounter;
        this.flight = flight;
        this.schedule = schedule;
        this.flyers = flyers;
        this.selection = false;
        this.flyingClass = flyingClass;
        this.system = system;
    }

    BigDecimal billedAmount(){
        BigDecimal amount = BigDecimal.ZERO;
        for(Flyer flyer: flyers){
            amount = amount.add(schedule.seatPrices.getOrDefault(flyer.seat != null ? flyer.seat.type :  flyingClass, BigDecimal.ZERO));
            if(flyer.seat != null ){
                for(SeatFeature feature: flyer.seat.features){
                    amount = amount.add(schedule.featurePrices.getOrDefault(feature, BigDecimal.ZERO));
                }
            }
        }
        return amount;
    }
    
    void addPayment(Payment payment) {
        this.payment.add(payment);
    }


    void allocateSeats() {
        for(Flyer flyer: flyers){
            if(flyer.seat == null){
                for(Seat seat: flight.aircraft.seats){
                    if(!schedule.seatBookings.contains(seat.identifier)){
                        flyer.seat = seat;
                        schedule.seatBookings.add(seat.identifier);
                        break;
                    }
                }
            }
        }
    }

    void releaseSeats() {
        for(Flyer flyer: flyers){
            if(flyer.seat != null){
                schedule.seatBookings.remove(flyer.seat.identifier);
                flyer.seat = null;
            }
        }
    }
    


    void makePayment(Payment payment) {
        if(payment.status == PaymentStatus.SUCCESS && billedAmount().equals(payment.amount)){
            addPayment(payment);
            this.status = BookingStatus.CONFIRMED;
            this.amountPaid = payment.amount;
            allocateSeats();
        }
       
    }

    void cancelBooking() {
        if(status == BookingStatus.CONFIRMED && this.amountPaid.compareTo(billedAmount()) == 0) {
            this.status = BookingStatus.CANCELLED;
            this.amountPaid = BigDecimal.ZERO;
            Payment payment = system.paymentManager.createSuccessPayment(this.amountPaid.multiply(BigDecimal.valueOf(-1)));
            addPayment(payment);
            releaseSeats();
        }
    }
}

class BookingManager {
    Map<Integer, Booking> bookings = new HashMap<>();

    void addBooking(Booking booking) {
        bookings.put(booking.id, booking);
    }

    void removeBooking(Booking booking) {
        bookings.remove(booking.id);
    }
}

enum PaymentStatus {
    PENDING, SUCCESS, FAILED
}



class Payment {
    private static int idCounter = 0;
    int id;
    BigDecimal amount = BigDecimal.ZERO;
    PaymentStatus status;

    public Payment(BigDecimal amount, PaymentStatus status) {
        id = ++idCounter;
        this.amount = amount;
        this.status = status;
    }
}

class PaymentManager {
    Map<Integer, Payment> payments = new HashMap<>();


    void addPayment(Payment payment) {
        payments.put(payment.id, payment);

    }

    void removePayment(Payment payment) {
        payments.remove(payment.id);

    }

    Payment createSuccessPayment(BigDecimal amount){
        Payment payment =  new Payment(amount, PaymentStatus.SUCCESS);
        addPayment(payment);
        return payment;
    }

    Payment createFailedPayment(BigDecimal amount){
        Payment payment =  new Payment(amount, PaymentStatus.FAILED);
        addPayment(payment);
        return payment;
    }
}

class MakeMyTrip {
    AirlineManager airlineManager;
    FlightManager flightManager;
    AirportManager airportManager;
    BookingManager bookingManager;
    PaymentManager paymentManager;

    public MakeMyTrip() {
        airlineManager = new AirlineManager();
        flightManager = new FlightManager();
        airportManager = new AirportManager();
        bookingManager = new BookingManager();
        paymentManager = new PaymentManager();
    }



}
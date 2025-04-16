package com.github.ankurpathak.lld.bookmyshow;

import lombok.ToString;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@ToString
class User {
    private static int idCounter = 0;
    int id;
    String name;

    User(String name) {
        this.id = ++idCounter;
        this.name = name;
    }
}

class UserManager {
    Map<Integer, User> users = new HashMap<>();

    public void addUser(User user) {
        users.put(user.id, user);
    }

    public void removeUser(User user) {
        users.remove(user.id);
    }
}


@ToString
class Movie {
    private static int idCounter = 0;
    int id;
    String name;
    Duration duration;

    public Movie(String name, Duration duration) {
        this.id = ++idCounter;
        this.name = name;
        this.duration = duration;
    }
}

class MovieManager {
    Map<Integer, Movie> movies = new HashMap<>();
    Map<City,List<Movie>> cityMovies = new HashMap<>();
    {
        for (City city : City.values()) {
            cityMovies.put(city, new ArrayList<>());
        }
    }

    public void addMovieToCity(Movie movie, City city) {
        movies.put(movie.id, movie);
        cityMovies.get(city).add(movie);
    }

    public void removeMovieFromCity(Movie movie, City city) {
        movies.remove(movie.id);
        cityMovies.get(city).remove(movie);
    }

}

@ToString
class Theater {
    private static int idCounter = 0;
    int id;
    String address;
    City city;
    Map<Integer, Screen> screens = new HashMap<>();
    Map<Integer, Show> shows = new HashMap<>();

    public Theater(String address, City city) {
        this.id = ++idCounter;
        this.city = city;
        this.address = address;
    }

    void addScreen(Screen screen) {
        screens.put(screen.id, screen);
    }

    void removeScreen(Screen screen) {
        screens.remove(screen.id);
    }

    void addShow(Show show) {
        shows.put(show.id, show);
    }

    void removeShow(Show show) {
        shows.remove(show.id);
    }
}


@ToString
class Show {
    private static int idCounter = 0;
    int id;
    Movie movie;
    Screen screen;
    LocalDate date;
    LocalTime time;
    Duration duration;
    Set<String> seatBookings =  new HashSet<>();
    Map<SeatType, BigDecimal> seatCost = new HashMap<>();



    public Show(Movie movie, Screen screen, LocalDate date, LocalTime time, Duration duration) {
        this.id = ++idCounter;
        this.movie = movie;
        this.date = date;
        this.time = time;
        this.duration = duration;
        this.screen = screen;
    }

    void addSeatBooking(String seat) {
        seatBookings.add(seat);
    }

    void removeSeatBooking(String seat) {
        seatBookings.remove(seat);
    }

    void addSeatCost(SeatType type, BigDecimal cost) {
        seatCost.put(type, cost);
    }

    void removeSeatCost(SeatType type) {
        seatCost.remove(type);
    }


}

@ToString
class Screen{
    private static int idCounter = 0;
    int id;
    String name;
    Map<String, Seat> seats = new HashMap<>();

    Screen(String name) {
        this.id = ++idCounter;
        this.name = name;
    }



    void addSeat(Seat seat) {
        seats.put(seat.identifier, seat);
    }

    void removeSeat(Seat seat) {
        seats.remove(seat.identifier);
    }


}

@ToString
class Seat {
    String identifier;
    int row;
    int col;
    SeatType type;

    public Seat(String identifier, int row, int col, SeatType type) {
        this.identifier = identifier;
        this.row = row;
        this.col = col;
        this.type = type;
    }
}

enum SeatType {
    REGULAR, PREMIUM, LUXURY
}

class TheaterManager {
    Map<Integer, Theater> theaters = new HashMap<>();
    Map<City, List<Theater>> cityTheaters = new HashMap<>();
    {
        for (City city : City.values()) {
            cityTheaters.put(city, new ArrayList<>());
        }
    }

    public void addTheater(Theater theater) {
        theaters.put(theater.id, theater);
        cityTheaters.get(theater.city).add(theater);
    }

    public void removeTheater(Theater theater) {
        theaters.remove(theater.id);
        cityTheaters.get(theater.city).remove(theater);
    }
}

@ToString
class Booking {
    private static int idCounter = 0;
    int id;
    Show show;
    List<Seat> seats = new ArrayList<>();
    User user;
    BookingStatus status = BookingStatus.IN_PROGRESS;
    Payment payment;

    public Booking(Show show, List<Seat> seats, User user) {
        this.id = ++idCounter;
        this.show = show;
        this.seats = seats;
        this.user = user;
    }

    BigDecimal computeAmount() {
        BigDecimal amount = BigDecimal.ZERO;
        for (Seat seat : seats) {
            amount = amount.add(show.seatCost.get(seat.type));
        }
        return amount;
    }

    boolean areSeatsAvailable() {
        for (Seat seat : seats) {
            if (show.seatBookings.contains(seat.identifier)) return false;
        }
        return true;
    }


    void assignPayment(Payment payment){
        this.payment = payment;
        if(payment.status == PaymentStatus.SUCCESS && areSeatsAvailable()){
            for (Seat seat : seats) {
                show.addSeatBooking(seat.identifier);
            }
            status = BookingStatus.CONFIRMED;
        } else {
            status = BookingStatus.CANCELLED;
        }
    }

}

enum BookingStatus{
    IN_PROGRESS, CONFIRMED, CANCELLED
}

@ToString
class Payment {
    private static int idCounter = 0;
    int id;
    BigDecimal amount = BigDecimal.ZERO;
    PaymentStatus status = PaymentStatus.PENDING;
    Payment(BigDecimal amount, PaymentStatus status){
        id = ++idCounter;
        this.amount = amount;
        this.status = status;
    }

}

enum PaymentStatus {
    PENDING, SUCCESS, FAILED
}

class BookingManager {
    Map<Integer, Booking> bookings = new HashMap<>();
    Map<Integer, Payment> payments = new HashMap<>();

    public void addBooking(Booking booking) {
        bookings.put(booking.id, booking);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking.id);
    }

    public void addPayment(Payment payment) {
        payments.put(payment.id, payment);
    }

    public void removePayment(Payment payment) {
        payments.remove(payment.id);
    }

}



enum City{
    DELHI, MUMBAI, BANGALORE, PUNE
}

class BookMyShow {
    MovieManager movieManager;
    TheaterManager theaterManager;
    BookingManager bookingManager;
    UserManager userManager;

    public BookMyShow() {
        movieManager = new MovieManager();
        theaterManager = new TheaterManager();
        bookingManager = new BookingManager();
        userManager = new UserManager();
    }

    void initialize(){
        Movie avenger = new Movie("Avengers", Duration.ofHours(2));
        Movie batman = new Movie("Batman", Duration.ofHours(3));

        movieManager.addMovieToCity(avenger, City.PUNE);
        movieManager.addMovieToCity(batman, City.PUNE);

        // ------------------------ PHOENIX MALL ------------------------
        Theater phoenixMall = new Theater("PVR Phoenix Mall", City.PUNE);

        Screen audi = new Screen("Audi");
        Seat audiA1 = new Seat("A1", 1, 1, SeatType.REGULAR);
        Seat audiB2 = new Seat("B2", 2, 1, SeatType.PREMIUM);
        Seat audiC3 = new Seat("C3", 3, 1, SeatType.LUXURY);
        audi.addSeat(audiA1);
        audi.addSeat(audiB2);
        audi.addSeat(audiC3);

        phoenixMall.addScreen(audi);

        // Show on Audi screen
        Show avengersShowEvening = new Show(avenger, audi, LocalDate.now(), LocalTime.now().plusHours(5), avenger.duration.plusMinutes(30));
        avengersShowEvening.addSeatCost(SeatType.LUXURY, BigDecimal.valueOf(1000));
        avengersShowEvening.addSeatCost(SeatType.PREMIUM, BigDecimal.valueOf(500));
        avengersShowEvening.addSeatCost(SeatType.REGULAR, BigDecimal.valueOf(200));

        Show avengersShowMorning = new Show(avenger, audi, LocalDate.now(), LocalTime.now().minusHours(5), avenger.duration.plusMinutes(30));
        avengersShowMorning.addSeatCost(SeatType.LUXURY, BigDecimal.valueOf(800));
        avengersShowMorning.addSeatCost(SeatType.PREMIUM, BigDecimal.valueOf(400));
        avengersShowMorning.addSeatCost(SeatType.REGULAR, BigDecimal.valueOf(150));

        phoenixMall.addShow(avengersShowEvening);
        phoenixMall.addShow(avengersShowMorning);

        // ------------------------ INORBIT MALL ------------------------
        Theater inOrbitMall = new Theater("PVR InOrbit Mall", City.PUNE);

        Screen swift = new Screen("Swift");
        Seat swiftA1 = new Seat("A1", 1, 1, SeatType.LUXURY);
        Seat swiftB2 = new Seat("B2", 2, 1, SeatType.PREMIUM);
        Seat swiftC3 = new Seat("C3", 3, 1, SeatType.REGULAR);
        swift.addSeat(swiftA1);
        swift.addSeat(swiftB2);
        swift.addSeat(swiftC3);

        inOrbitMall.addScreen(swift);

        Show batmanMorningOther = new Show(batman, swift, LocalDate.now(), LocalTime.now().minusHours(6), batman.duration.plusMinutes(30));
        batmanMorningOther.addSeatCost(SeatType.LUXURY, BigDecimal.valueOf(800));
        batmanMorningOther.addSeatCost(SeatType.PREMIUM, BigDecimal.valueOf(400));
        batmanMorningOther.addSeatCost(SeatType.REGULAR, BigDecimal.valueOf(150));

        inOrbitMall.addShow(batmanMorningOther);

        // Add theaters to manager
        theaterManager.addTheater(phoenixMall);
        theaterManager.addTheater(inOrbitMall);

        // ------------------------ USERS ------------------------
        User ankur = new User("Ankur");
        User pradeep = new User("Pradeep");
        userManager.addUser(ankur);
        userManager.addUser(pradeep);

        // ------------------------ BOOKINGS ------------------------
        // Correctly use seats from the screen
        Booking bookingAnkur = new Booking(avengersShowEvening, List.of(audiC3, audiB2), ankur);
        Payment paymentAnkur = new Payment(bookingAnkur.computeAmount(), PaymentStatus.SUCCESS);
        bookingAnkur.assignPayment(paymentAnkur);
        bookingManager.addBooking(bookingAnkur);
        bookingManager.addPayment(paymentAnkur);

        Booking bookingPradeep = new Booking(batmanMorningOther, List.of(swiftA1, swiftC3), pradeep);
        Payment paymentPradeep = new Payment(bookingPradeep.computeAmount(), PaymentStatus.FAILED);
        bookingPradeep.assignPayment(paymentPradeep);
        bookingManager.addBooking(bookingPradeep);
        bookingManager.addPayment(paymentPradeep);
    }

    public static void main(String[] args) {
        BookMyShow bookMyShow = new BookMyShow();
        bookMyShow.initialize();

        for(Integer id: bookMyShow.bookingManager.bookings.keySet()){
            System.out.println(bookMyShow.bookingManager.bookings.get(id));
        }




    }
}

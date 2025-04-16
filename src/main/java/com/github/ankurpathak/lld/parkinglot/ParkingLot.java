package com.github.ankurpathak.lld.parkinglot;

import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

enum VehicleType {
    BIKE, CAR, LIMO;
}
@Getter
abstract class Vehicle {
    private final String registration;
    private final VehicleType type;

    public Vehicle(String registration, VehicleType type) {
        this.registration = registration;
        this.type = type;
    }

    abstract double getHourlyRate();
}

class Bike extends Vehicle {
    public Bike(String registration) {
        super(registration, VehicleType.BIKE);
    }

    @Override
    double getHourlyRate() {
        return 30; // Example rate for bike
    }
}

class Car extends Vehicle {
    public Car(String registration) {
        super(registration, VehicleType.CAR);
    }

    @Override
    double getHourlyRate() {
        return 50; // Example rate for car
    }
}

class Limo extends Vehicle {
    public Limo(String registration) {
        super(registration, VehicleType.LIMO);
    }

    @Override
    double getHourlyRate() {
        return 100; // Example rate for car
    }
}

@Getter
class Ticket {
    private final int ticketId;
    private final ParkingSlot assignedSlot;
    private final Instant entryTime;

    private static int ticketCounter = 0;

    public Ticket(ParkingSlot assignedSlot) {
        this.ticketId = ++ticketCounter; // Unique ticket ID
        this.assignedSlot = assignedSlot;
        this.entryTime = Instant.now(); // Timestamp when ticket is issued
    }

    public Duration getParkingDuration() {
        return Duration.between(entryTime, Instant.now()); // Calculate how long the vehicle has been parked
    }
}

class ParkingSlot implements Comparable<ParkingSlot> {
    int garage;
    int floor;
    int slot;
    VehicleType type;
    Vehicle vehicle;

    public ParkingSlot(int garage, int floor, int slot, VehicleType type) {
        this.floor = floor;
        this.slot = slot;
        this.type = type;
    }

    @Override
    public int compareTo(ParkingSlot other) {
        int result = Integer.compare(this.garage, other.garage);

        if(result == 0) result = Integer.compare(this.floor, other.floor);

        if(result == 0) result = Integer.compare(this.slot, other.slot);

        return result;
    }

    String id(){
        return String.format("%d-%d-%d", garage, floor, slot);
    }

    public void parkVehicle(Vehicle vehicle) {
        if(isAvailable() && vehicle.getType() == this.type) {
            this.vehicle = vehicle;
        } else {
           throw new RuntimeException(String.format("Slot: %s of type: %s occupied or vehicle type: %s mismatch.", id(), type.name(), vehicle.getType().name()));
        }
    }

    public Vehicle unparkVehicle(){
        Vehicle vehicle = this.vehicle;
        this.vehicle = null;
        return vehicle;
    }

    boolean isAvailable(){
        return vehicle == null;
    }


}

class ParkingFloor {
    int floor;
    List<ParkingSlot> slots;

    public ParkingFloor(int floor) {
        this.floor = floor;
        slots = new ArrayList<>();
    }

    public void addSlot(ParkingSlot slot){
        slots.add(slot);
    }

    ParkingSlot getSlot(int idx){
        return slots.get(idx);
    }


}

class ParkingGarage {
    int garage;
    List<ParkingFloor> floors;

    public ParkingGarage(int garage) {
        this.garage = garage;
        floors = new ArrayList<>();
    }


    public void addFloor(ParkingFloor floor){
        floors.add(floor);
    }

    ParkingFloor getFloor(int idx){
        return floors.get(idx);
    }
}

// Parking Lot class to manage multiple garages
class ParkingLot {
    List<ParkingGarage> garages;
    List<List<PriorityQueue<String>>> slotsQes;
    Map<Integer, Ticket> allocateTickets = new HashMap<>(); // To store tickets by ticketId

    public ParkingLot(int noOfGarages, int numberOfFloorsPerGarage, int bikeSlotsPerFloor, int carSlotsPerFloor, int limoSlotsPerFloor) {
        garages = new ArrayList<>();
        slotsQes = new ArrayList<>();
        reset(noOfGarages, numberOfFloorsPerGarage, bikeSlotsPerFloor, carSlotsPerFloor, limoSlotsPerFloor);
    }

    void addGarage(ParkingGarage garage) {
        garages.add(garage);
        slotsQes.add(new ArrayList<>());
    }

    ParkingGarage getGarage(int idx){
        return garages.get(idx);
    }

    void reset(int noOfGarages, int numberOfFloorsPerGarage, int bikeSlotsPerFloor, int carSlotsPerFloor, int limoSlotsPerFloor) {
        for (int i = 0; i < noOfGarages; i++) {
            var garage = new ParkingGarage(i);
            addGarage(garage);
            List<String> bikeSlotIds = new ArrayList<>();
            List<String> carSlotIds = new ArrayList<>();
            List<String> limoSlotIds = new ArrayList<>();
            for (int j = 0; j < numberOfFloorsPerGarage; j++) {
                var floor  = new ParkingFloor(j);
                garage.addFloor(floor);
                for(int k = 0; k < bikeSlotsPerFloor; k++) {
                    var slot = new ParkingSlot(i, j, k, VehicleType.BIKE);
                    floor.addSlot(slot);
                    bikeSlotIds.add(slot.id());
                }
                for(int k = bikeSlotsPerFloor; k < bikeSlotsPerFloor + carSlotsPerFloor; k++) {
                    var slot = new ParkingSlot(i, j, k, VehicleType.CAR);
                    floor.addSlot(slot);
                    carSlotIds.add(slot.id());

                }

                for(int k = bikeSlotsPerFloor; k < bikeSlotsPerFloor + carSlotsPerFloor; k++) {
                    var slot = new ParkingSlot(i, j, k, VehicleType.LIMO);
                    floor.addSlot(slot);
                    limoSlotIds.add(slot.id());
                }
            }
            slotsQes.get(i).add(new PriorityQueue<>(bikeSlotIds));
            slotsQes.get(i).add(new PriorityQueue<>(carSlotIds));
            slotsQes.get(i).add(new PriorityQueue<>(limoSlotIds));
        }
    }



    Ticket parkVehicle(int gateNo, Vehicle vehicle) {
        PriorityQueue<String> q =  slotsQes.get(gateNo).get(vehicle.getType().ordinal());
        if(q == null || q.isEmpty())
            throw new RuntimeException(String.format("No slots available at garage: %d  for %s", gateNo, vehicle.getType().name()));
        String id = q.poll();
        String[] tokens = id.split("-");
        int floor = Integer.parseInt(tokens[1]);
        int slot = Integer.parseInt(tokens[2]);
        ParkingSlot parkingSlot = garages.get(gateNo).getFloor(floor).getSlot(slot);
        parkingSlot.parkVehicle(vehicle);
        Ticket ticket = new Ticket(parkingSlot);
        allocateTickets.put(ticket.getTicketId(), ticket);
        return ticket;
    }

    public double unparkVehicle(int gateNo, int ticketId) {
        if (!allocateTickets.containsKey(ticketId)) {
            throw new RuntimeException("Invalid ticket ID");
        }

        Ticket ticket = allocateTickets.get(ticketId);
        ParkingSlot parkingSlot = ticket.getAssignedSlot();
        Vehicle vehicle = parkingSlot.unparkVehicle();

        slotsQes.get(gateNo).get(vehicle.getType().ordinal()).add(parkingSlot.id());
        allocateTickets.remove(ticketId);

        long minutes = ticket.getParkingDuration().toSeconds();
        double hours = Math.ceil(minutes / 3600.0); // Convert seconds to hours

        return vehicle.getHourlyRate() * hours;
    }


    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(1, 2, 2, 2, 1); // 1 garage, 2 floors, various slots

        Vehicle car = new Car("DL-01-AB-1234");
        Ticket ticket = lot.parkVehicle(0, car);

        Thread.sleep(2000); // Simulate 2 seconds of parking

        double fee = lot.unparkVehicle(0, ticket.getTicketId());
        System.out.println("Fee charged: Rs " + fee);
    }


}







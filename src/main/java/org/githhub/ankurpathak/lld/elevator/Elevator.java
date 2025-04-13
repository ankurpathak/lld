package org.githhub.ankurpathak.lld.elevator;

import lombok.Getter;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

enum Direction {
    UP, DOWN, IDLE
}

enum ElevatorType {
    ODD, EVEN, ALL
}

class Elevator {

    @Getter
    private final int id;
    @Getter
    private int currentFloor;
    @Getter
    private Direction direction;
    private final PriorityQueue<Integer> requestQueueUp;   // Floors in the UP direction
    private final PriorityQueue<Integer> requestQueueDown; // Floors in the DOWN direction
    @Getter
    private final ElevatorType type;
    private boolean oppositeDirectionRequestSupported;

    private static int idCounter = 0;

    String elevator(){
        return String.format("Elevator(ID:%d Type:%s CurrentFloor:%d Direction:%s)",
                id, type, currentFloor, direction);
    }

    public Elevator(ElevatorType type) {
        this.id = ++idCounter;
        this.currentFloor = 0;
        this.direction = Direction.IDLE;
        this.requestQueueUp = new PriorityQueue<>();
        this.requestQueueDown = new PriorityQueue<>(Comparator.reverseOrder());
        this.type = type;
    }

    // Add internal request (from inside the elevator)
    public void addInternalRequest(int... floors) {
        for(int floor : floors) {
            if(supports(floor)) {
                System.out.println(elevator() + " received request to floor: " + floor);
                addRequestToCorrectQueue(floor);
            } else {
                System.out.println(elevator() + " doesn't support request to floor: " + floor);
            }
        }
        processRequests();
    }

    private void addRequestToCorrectQueue(int floor) {
        if(!oppositeDirectionRequestSupported){
            if (direction == Direction.IDLE ) {
                if (floor > currentFloor) {
                    requestQueueUp.add(floor);
                    direction = Direction.UP;
                } else if (floor < currentFloor) {
                    requestQueueDown.add(floor);
                    direction = Direction.DOWN;
                }
            } else if (direction == Direction.UP && floor > currentFloor) {
                requestQueueUp.add(floor);
            } else if (direction == Direction.DOWN && floor < currentFloor) {
                requestQueueDown.add(floor);
            } else {
                System.out.println("Invalid request: Elevator is moving in the opposite direction.");
                return;
            }
        }else {
            if (floor > currentFloor) {
                requestQueueUp.add(floor);
            } else if (floor < currentFloor) {
                requestQueueDown.add(floor);
            }
        }
    }







    // Process requests using the LOOK algorithm
    private void processRequests() {
        if (requestQueueUp.isEmpty() && requestQueueDown.isEmpty()) {
            this.direction = Direction.IDLE;
            System.out.println(elevator() + " is idle.");
            return;
        }

        if (direction == Direction.IDLE) {
            // Decide direction based on which queue has requests
            if (!requestQueueUp.isEmpty()) {
                direction = Direction.UP;
            } else {
                direction = Direction.DOWN;
            }
        }

        // Handle requests based on the direction
        if (direction == Direction.UP) {
            processUpwardRequests();
        } else if (direction == Direction.DOWN) {
            processDownwardRequests();
        }
    }

    // Process upward requests (moving UP)
    private void processUpwardRequests() {
        // Handle requests in the UP direction
        while (!requestQueueUp.isEmpty()) {
            int nextFloor = requestQueueUp.poll();
            moveToFloor(nextFloor);
        }

        direction = Direction.DOWN;
        processRequests();

    }


    private void processDownwardRequests() {

        while (!requestQueueDown.isEmpty()) {
            int nextFloor = requestQueueDown.poll();
            moveToFloor(nextFloor);
        }

        direction = Direction.UP;
        processRequests(); // Recurse with the new direction

    }

    // Move the elevator to a specific floor
    private void moveToFloor(int floor) {
        direction = (floor > currentFloor) ? Direction.UP : Direction.DOWN;
        while (currentFloor != floor) {
            System.out.println(elevator() +" is skipping " + currentFloor +" while moving to floor " + floor);
            currentFloor += (direction == Direction.UP) ? 1 : -1;
        }
        System.out.println(elevator() + " arrived at floor " + currentFloor);
    }


    public boolean supports(int floor) {
        return type == ElevatorType.ALL ||
                (type == ElevatorType.ODD && floor % 2 == 1) ||
                (type == ElevatorType.EVEN && floor % 2 == 0);
    }

}



@Getter
class ElevatorScheduler {
    private final List<Elevator> elevators;
    private final int floors;

    public ElevatorScheduler(List<Elevator> elevators, int floors) {
        this.elevators = elevators;
        this.floors = floors;
    }




    public Elevator handleExternalRequest(int fromFloor, Direction direction) {
        System.out.println("Received external request: fromFloor:" + fromFloor + " direction:" + direction);
        try {
            validateLimits(fromFloor, "fromFloor");
        }catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            return null;
        }

        Elevator bestElevator = findBestElevator(fromFloor);
        if(bestElevator == null){
            System.out.println("No suitable elevator found for request.");
            return null;
        }

        bestElevator.addInternalRequest(fromFloor);

        return bestElevator;

    }

    // Handle external requests (from floor panels)
    public void handleStimulateRequest(int fromFloor, Direction direction, int toFloor) {
        System.out.println("Received stimulate request: fromFloor:" + fromFloor + " direction:" + direction + " toFloor:" + toFloor);
        Elevator bestElevator = handleExternalRequest(fromFloor, direction);
        try {
            validateLimits(toFloor, "toFloor");
            validateDirectionAndFloor(fromFloor, direction, toFloor);
        }catch (IllegalArgumentException ex){
            System.out.println(ex.getMessage());
            return;
        }

        bestElevator.addInternalRequest(toFloor);
    }




    void validateLimits(int floor, String tag) {
        if(tag == null) tag = "";
        if (floor < 0 || floor > floors) {
            throw new IllegalArgumentException(String.format("Invalid request %s: %d should be within bounds.", tag, floor));
        }
    }

    void validateDirectionAndFloor(int fromFloor, Direction direction, int toFloor) {
        if (direction == Direction.UP) {
            if (toFloor <= fromFloor) {
                throw new IllegalArgumentException(String.format("Invalid request: toFloor:%d should be greater than fromFloor:%d for UP direction.", toFloor, fromFloor));
            }
        } else if (direction == Direction.DOWN) {
            if (toFloor >= fromFloor) {
                throw new IllegalArgumentException(String.format("Invalid request: toFloor:%d should be less than fromFloor:%d for DOWN direction.", toFloor, fromFloor));
            }
        } else if (direction == Direction.IDLE) {
            throw new IllegalArgumentException(String.format("Invalid request: direction:%s should be up or down", direction.name()));
        }
    }




    private Elevator findBestElevator(int floor) {
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;

        for (Elevator elevator : elevators) {
            if (isElevatorSuitable(elevator, floor)) {
                int distance = Math.abs(elevator.getCurrentFloor() - floor);
                if (distance < minDistance) {
                    minDistance = distance;
                    bestElevator = elevator;
                }
            }
        }
        return bestElevator;
    }


    private boolean isElevatorSuitable(Elevator elevator, int floor) {
        boolean support = elevator.supports(floor);
        if (!support) {
            return false;
        }

        return switch (elevator.getDirection()) {
            case UP -> floor > elevator.getCurrentFloor();
            case DOWN -> floor < elevator.getCurrentFloor();
            case IDLE -> true;
        };
    }




}

record Request(int fromFloor, Direction direction, int toFloor) {}


@Getter
class ElevatorSystem {

    private final ElevatorScheduler elevatorScheduler;

    public ElevatorSystem(List<Elevator> elevators, int floors) {
        elevatorScheduler = new ElevatorScheduler(elevators, floors);
    }


    public static void main(String[] args) {
        List<Elevator> elevators = new ArrayList<>();
        elevators.add(new Elevator(ElevatorType.ALL));
        elevators.add(new Elevator(ElevatorType.EVEN));
        elevators.add(new Elevator(ElevatorType.ODD));



        ElevatorSystem system = new ElevatorSystem(elevators, 10);
        ElevatorScheduler elevatorScheduler = system.getElevatorScheduler();

        Elevator elevator = elevatorScheduler.handleExternalRequest(3, Direction.UP);

        elevator.addInternalRequest(9, 6, 1, 8, 2, 5);



        int requestCount = 10;
        do {
            int direction = ThreadLocalRandom.current().nextInt(0, 2);
            int from = ThreadLocalRandom.current().nextInt(2, elevatorScheduler.getFloors() - 2 + 1);

            int to;

            if (direction == 0) {
                to = ThreadLocalRandom.current().nextInt(from + 1, elevatorScheduler.getFloors()  + 1);
            } else {
                to = ThreadLocalRandom.current().nextInt(0, from);
            }

            elevatorScheduler.handleStimulateRequest(from, direction == 0 ? Direction.UP : Direction.DOWN, to);
            requestCount--;

        } while (requestCount > 0);



    }

}


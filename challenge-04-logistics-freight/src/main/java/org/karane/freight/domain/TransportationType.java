package org.karane.freight.domain;

public sealed interface TransportationType
        permits TransportationType.Boat,
                TransportationType.Truck,
                TransportationType.Rail {

    String label();

    record Boat(String portOfOrigin, String portOfDestination) implements TransportationType {
        @Override 
        public String label() { return "Boat"; }
    }

    record Truck(int maxPayloadKg) implements TransportationType {
        // compact constructor
        public Truck { 
            if (maxPayloadKg <= 0) throw new IllegalArgumentException("maxPayloadKg must be positive");
        }
        
        @Override 
        public String label() { return "Truck"; }
    }

    record Rail(String railNetwork) implements TransportationType {
        @Override 
        public String label() { return "Rail"; }
    }
}

-- composite index bc used together in a lot of queries
CREATE INDEX idx_flightinstance_flightdate ON FlightInstance (FlightNumber, FlightDate);
-- reservation look-up, the customer lookup was the only one that took over 0ms(tho sometimes it was 0ms)
CREATE INDEX idx_reservation_resid ON Reservation (ReservationID);
-- to join reservations to flights
CREATE INDEX idx_reservation_flightinstance ON Reservation (FlightInstanceID);
-- for the repairs lookup
CREATE INDEX idx_repair_technician ON Repair (TechnicianID);
-- range queries
CREATE INDEX idx_repair_plane_date ON Repair (PlaneID, RepairDate);
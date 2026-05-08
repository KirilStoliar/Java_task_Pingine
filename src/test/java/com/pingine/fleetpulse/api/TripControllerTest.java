package com.pingine.fleetpulse.api;

import com.pingine.fleetpulse.api.dto.TripResponse;
import com.pingine.fleetpulse.api.dto.VehicleResponse;
import com.pingine.fleetpulse.service.TripService;
import com.pingine.fleetpulse.service.VehicleNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TripController.class)
class TripControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TripService tripService;

    private static final String VEHICLE_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private static final String NON_EXISTENT_VEHICLE_ID = "non-existent-id";

    @Test
    void getLastTrip_ShouldReturnTrip_WhenVehicleExistsAndHasTrips() throws Exception {
        // given
        Instant startedAt = Instant.parse("2026-04-27T08:00:00Z");
        Instant endedAt = Instant.parse("2026-04-27T08:30:00Z");

        VehicleResponse vehicle = VehicleResponse.builder()
                .id(VEHICLE_ID)
                .licensePlate("B-PG-1001")
                .model("Mercedes Actros")
                .vin("TESTVIN0000000001")
                .driverName("Driver One")
                .build();

        TripResponse.PointDto point1 = TripResponse.PointDto.builder()
                .ts(Instant.parse("2026-04-27T08:00:00Z"))
                .lat(52.5200)
                .lon(13.4050)
                .speedKph(0.0)
                .build();

        TripResponse.PointDto point2 = TripResponse.PointDto.builder()
                .ts(Instant.parse("2026-04-27T08:30:00Z"))
                .lat(52.5700)
                .lon(13.5000)
                .speedKph(0.0)
                .build();

        TripResponse expectedResponse = TripResponse.builder()
                .vehicle(vehicle)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .distanceKm(15.5)
                .avgSpeedKph(45.2)
                .pointCount(2)
                .points(List.of(point1, point2))
                .build();

        when(tripService.getLastTrip(VEHICLE_ID)).thenReturn(expectedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/last-trip", VEHICLE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicle.id").value(VEHICLE_ID))
                .andExpect(jsonPath("$.vehicle.licensePlate").value("B-PG-1001"))
                .andExpect(jsonPath("$.vehicle.model").value("Mercedes Actros"))
                .andExpect(jsonPath("$.vehicle.vin").value("TESTVIN0000000001"))
                .andExpect(jsonPath("$.vehicle.driverName").value("Driver One"))
                .andExpect(jsonPath("$.startedAt").value("2026-04-27T08:00:00Z"))
                .andExpect(jsonPath("$.endedAt").value("2026-04-27T08:30:00Z"))
                .andExpect(jsonPath("$.distanceKm").value(15.5))
                .andExpect(jsonPath("$.avgSpeedKph").value(45.2))
                .andExpect(jsonPath("$.pointCount").value(2))
                .andExpect(jsonPath("$.points").isArray())
                .andExpect(jsonPath("$.points.length()").value(2))
                .andExpect(jsonPath("$.points[0].ts").value("2026-04-27T08:00:00Z"))
                .andExpect(jsonPath("$.points[0].lat").value(52.5200))
                .andExpect(jsonPath("$.points[0].lon").value(13.4050))
                .andExpect(jsonPath("$.points[0].speedKph").value(0.0))
                .andExpect(jsonPath("$.points[1].lat").value(52.5700))
                .andExpect(jsonPath("$.points[1].lon").value(13.5000));
    }

    @Test
    void getLastTrip_ShouldReturn404_WhenVehicleNotFound() throws Exception {
        // given
        when(tripService.getLastTrip(NON_EXISTENT_VEHICLE_ID))
                .thenThrow(new VehicleNotFoundException(NON_EXISTENT_VEHICLE_ID));

        // when & then
        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/last-trip", NON_EXISTENT_VEHICLE_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void getLastTrip_ShouldReturn404_WhenVehicleHasNoTrips() throws Exception {
        // given
        // Предполагаем, что сервис выбрасывает VehicleNotFoundException, когда поездок нет
        when(tripService.getLastTrip(VEHICLE_ID))
                .thenThrow(new VehicleNotFoundException(VEHICLE_ID));

        // when & then
        mockMvc.perform(get("/api/v1/vehicles/{vehicleId}/last-trip", VEHICLE_ID))
                .andExpect(status().isNotFound());
    }
}
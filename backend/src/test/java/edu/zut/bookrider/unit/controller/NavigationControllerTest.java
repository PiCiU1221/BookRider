package edu.zut.bookrider.unit.controller;

import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.NavigationResponseDTO;
import edu.zut.bookrider.service.NavigationService;
import edu.zut.bookrider.service.enums.TransportProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
public class NavigationControllerTest {
    @MockBean
    private NavigationService navigationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenCorrectData_thenReturnCorrectResponse() throws Exception {
        CoordinateDTO startCoordinates = new CoordinateDTO(53.434444, 14.504721);
        CoordinateDTO endCoordinates = new CoordinateDTO(53.433332, 14.506454);
        TransportProfile transportProfile = TransportProfile.CAR;

        NavigationResponseDTO navigationResponseDTO = new NavigationResponseDTO();
        navigationResponseDTO.setTotalDistance(1);

        when(navigationService.getDirectionsFromCoordinates(startCoordinates, endCoordinates, transportProfile))
                .thenReturn(navigationResponseDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("start", startCoordinates.getLatitude() + "," + startCoordinates.getLongitude())
                        .param("end", endCoordinates.getLatitude() + "," + endCoordinates.getLongitude())
                        .param("transportProfile", "car"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalDistance").value(1));
    }

    @Test
    public void whenStartParamIsMissing_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("end", "53.433332,14.506454")
                        .param("transportProfile", "car"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Required request parameter 'start' is missing."));
    }

    @Test
    public void whenEndParamIsMissing_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("start", "53.434444,14.504721")
                        .param("transportProfile", "car"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Required request parameter 'end' is missing."));
    }

    @Test
    public void whenTransportProfileParamIsMissing_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("start", "53.434444,14.504721")
                        .param("end", "53.433332,14.506454"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Required request parameter 'transportProfile' is missing."));
    }

    @Test
    public void whenMultipleParamsAreMissing_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("transportProfile", "car"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Required request parameter 'start' is missing."));
    }

    @Test
    public void whenStartParamIsInWrongFormat_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("start", "53.42,test")
                        .param("end", "53.433332,14.506454")
                        .param("transportProfile", "car"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Start coordinates must be in 'double,double' format"));
    }

    @Test
    public void whenTransportProfileParamIsWrong_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("start", "53.434444,14.504721")
                        .param("end", "53.433332,14.506454")
                        .param("transportProfile", "something"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid transport profile: something"));
    }
}

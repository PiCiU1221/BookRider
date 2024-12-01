package edu.zut.bookrider.unit;

import edu.zut.bookrider.controller.NavigationController;
import edu.zut.bookrider.dto.CoordinateDTO;
import edu.zut.bookrider.dto.NavigationResponseDTO;
import edu.zut.bookrider.service.NavigationService;
import edu.zut.bookrider.service.enums.TransportProfile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NavigationController.class)
public class NavigationControllerTest {
    @MockBean
    private NavigationService navigationService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void whenCorrectData_thenReturnCorrectResponse() throws Exception {
        CoordinateDTO startCoordinates = new CoordinateDTO(14.504721, 53.434444);
        CoordinateDTO endCoordinates = new CoordinateDTO(14.506454, 53.433332);
        TransportProfile transportProfile = TransportProfile.CAR;

        NavigationResponseDTO navigationResponseDTO = new NavigationResponseDTO();
        navigationResponseDTO.setTotalDistance(1);

        when(navigationService.getDirectionsFromCoordinates(startCoordinates, endCoordinates, transportProfile))
                .thenReturn(navigationResponseDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("start", "14.504721,53.434444")
                        .param("end", "14.506454,53.433332")
                        .param("transportProfile", "car"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalDistance").value(1));
    }

    @Test
    public void whenStartParamIsMissing_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("end", "14.506454,53.433332")
                        .param("transportProfile", "car"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Required request parameter 'start' is missing."));
    }

    @Test
    public void whenEndParamIsMissing_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("start", "14.504721,53.434444")
                        .param("transportProfile", "car"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Required request parameter 'end' is missing."));
    }

    @Test
    public void whenTransportProfileParamIsMissing_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("start", "14.504721,53.434444")
                        .param("end", "14.506454,53.433332"))
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
                        .param("start", "14.504721,test")
                        .param("end", "14.506454,53.433332")
                        .param("transportProfile", "car"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Start coordinates must be in 'double,double' format"));
    }

    @Test
    public void whenTransportProfileParamIsWrong_thenReturnBadRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/navigation/directions")
                        .param("start", "14.504721,53.434444")
                        .param("end", "14.506454,53.433332")
                        .param("transportProfile", "something"))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Invalid transport profile: something"));
    }
}

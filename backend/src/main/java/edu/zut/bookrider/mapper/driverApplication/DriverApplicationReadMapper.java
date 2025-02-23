package edu.zut.bookrider.mapper.driverApplication;

import edu.zut.bookrider.dto.DriverApplicationSummaryDTO;
import edu.zut.bookrider.mapper.Mapper;
import edu.zut.bookrider.model.DriverApplicationRequest;
import org.springframework.stereotype.Component;

@Component
public class DriverApplicationReadMapper implements Mapper<DriverApplicationRequest, DriverApplicationSummaryDTO> {
    @Override
    public DriverApplicationSummaryDTO map(DriverApplicationRequest object) {
        return new DriverApplicationSummaryDTO(
                object.getId(),
                object.getUser().getEmail(),
                object.getStatus(),
                object.getSubmittedAt()
        );
    }
}

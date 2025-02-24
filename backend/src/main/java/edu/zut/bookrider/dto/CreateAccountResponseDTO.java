package edu.zut.bookrider.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import edu.zut.bookrider.model.User;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateAccountResponseDTO {
    private String id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;

    public CreateAccountResponseDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
    }
}

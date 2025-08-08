package dev.Pedro.movies_api.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Document(collection = "roles")
@Data
public class Role {

    @Id
    private ObjectId id;

    private ClientRoles roleName;

    public Role(ClientRoles roleName) {
        this.roleName = roleName;
    }

}

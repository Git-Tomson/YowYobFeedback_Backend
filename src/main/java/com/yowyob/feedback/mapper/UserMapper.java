package com.yowyob.feedback.mapper;

import com.yowyob.feedback.dto.response.UserResponseDTO;
import com.yowyob.feedback.entity.AppUser;
import com.yowyob.feedback.dto.request.RegisterRequestDTO;
import com.yowyob.feedback.entity.Organization;
import com.yowyob.feedback.entity.Person;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.UUID;


/**
 * Manual mapper class for user entity conversions.
 * Handles conversion between entities and DTOs for the user hierarchy.
 *
 * @author Thomas Djotio Ndié
 *  * @since 2024-12-12
 *  * @version 1.0
 */
@Component
public class UserMapper {

    /**
     * Map a DTO request to an entity
     *
     * @param register_request
     * @return The equivalent AppUser object
     */
    public AppUser toUserEntity(RegisterRequestDTO register_request){
        return AppUser.builder().user_type(register_request.user_type())
                .user_firstname(register_request.user_firstname())
                .user_lastname(register_request.user_lastname())
                .email(register_request.email())
                .password(register_request.password())
                .contact(register_request.contact())
                .user_logo(register_request.user_logo())
                .domain(register_request.domain())
                .description(register_request.description())
                .registration_date_time(OffsetDateTime.now())
                .certified(false)
                .build();
    }


    /**
     * Map a DTO request to a Person entity
     *
     * @param register_request
     * @return the Equivalent Person Entity
     */
    public Person toPersonEntity(RegisterRequestDTO register_request){
        return Person.builder().occupation(register_request.occupation())
                .build();
    }

    public Organization toOrganizationEntity(RegisterRequestDTO register_request){
        return Organization.builder().location(register_request.location())
                .build();
    }

    /**
     * Map an entity to a DTO response
     *
     * @param app_user
     * @return a UserResponseDTO
     */
    public UserResponseDTO toUserResponseDTO (AppUser app_user){
        return UserResponseDTO.builder().user_type(app_user.getUser_type())
                .user_id(app_user.getUser_id())
                .user_firstname(app_user.getUser_firstname())
                .user_lastname(app_user.getUser_lastname())
                .email(app_user.getEmail())
                .contact(app_user.getContact())
                .user_logo(app_user.getUser_logo())
                .domain(app_user.getDomain())
                .description(app_user.getDescription())
                .registration_date_time(app_user.getRegistration_date_time())
                .certified(app_user.getCertified())
                .build();
    }

}

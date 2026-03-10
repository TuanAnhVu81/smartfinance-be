package com.smartfinance.mapper;

import com.smartfinance.dto.response.UserResponse;
import com.smartfinance.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Entity → Response DTO (exclude password and roles for safety)
    UserResponse toResponse(User user);

    // Partial update: map non-null fields from DTO onto existing entity (ignore nulls)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUserFromRequest(String fullName, String avatarUrl, @MappingTarget User user);
}

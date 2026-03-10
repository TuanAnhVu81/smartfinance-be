package com.smartfinance.mapper;

import com.smartfinance.dto.request.TransactionRequest;
import com.smartfinance.dto.response.TransactionResponse;
import com.smartfinance.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface TransactionMapper {

    // Entity → Response DTO
    // MapStruct auto-maps category field via CategoryMapper (declared in 'uses')
    @Mapping(target = "createdAt", expression = "java(transaction.getCreatedAt().toLocalDate())")
    TransactionResponse toResponse(Transaction transaction);

    // Request DTO → new Entity (system fields are ignored, set in service)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)   // resolved manually in service via categoryId
    @Mapping(target = "isDeleted", ignore = true)
    Transaction toEntity(TransactionRequest request);

    // Update only mutable fields on an existing managed entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "category", ignore = true)   // resolved manually in service via categoryId
    @Mapping(target = "isDeleted", ignore = true)
    void updateTransactionFromRequest(TransactionRequest request, @MappingTarget Transaction transaction);
}

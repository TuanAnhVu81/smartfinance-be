package com.smartfinance.mapper;

import com.smartfinance.dto.response.BudgetResponse;
import com.smartfinance.entity.Budget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class})
public interface BudgetMapper {

    // Entity → Response DTO (percentage is set manually in service after calling toResponse)
    // CategoryMapper is declared in 'uses' to handle nested category mapping automatically
    @Mapping(target = "percentage", ignore = true)
    BudgetResponse toResponse(Budget budget);
}

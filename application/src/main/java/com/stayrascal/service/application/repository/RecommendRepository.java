package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.Recommendation;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendRepository {
  List<Recommendation> getRecommendation(@Param("userId") long userId, @Param("itemId") long itemId, @Param("num") int num, @Param("measureType") String measureType);
}

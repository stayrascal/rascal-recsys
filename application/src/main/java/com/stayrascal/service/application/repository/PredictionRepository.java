package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.Prediction;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionRepository {
    List<Prediction> getPrediction(@Param("userName") String userName, @Param("compName") String compName, @Param("num") int num);
}

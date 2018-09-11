package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.Component;

import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentRepository {
    Component getComponentByName(@Param("name") String compName) throws DataAccessException;

    List<Component> getComponentByNameLike(@Param("nameLike") String nameLike) throws DataAccessException;

    Component getComponentById(@Param("id") int id) throws DataAccessException;

    void addComponent(@Param("component") Component component) throws DataAccessException;

    void updateComponent(@Param("component") Component component) throws DataAccessException;

    void deleteComponentByName(@Param("name") String compName) throws DataAccessException;

    List<Component> getPagedComponents(@Param("offset") int offset, @Param("limit") int limit);
}

package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.Item;

import org.apache.ibatis.annotations.Param;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository {
    Item getItemByUUID(@Param("uuid") String uuid) throws DataAccessException;

    Item getItemById(@Param("id") String uuid) throws DataAccessException;

    void addItem(@Param("item") Item item) throws DataAccessException;

    void updateItem(@Param("item") Item item) throws DataAccessException;

    void deleteItemByUUID(@Param("uuid") String uuid) throws DataAccessException;

    void deleteItemById(@Param("id") String id) throws DataAccessException;

    List<Item> getPagedItems(@Param("offset") int offset, @Param("limit") int limit);
}

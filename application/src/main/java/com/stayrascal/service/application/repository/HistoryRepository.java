package com.stayrascal.service.application.repository;

import com.stayrascal.service.application.domain.HistoryRecord;
import com.stayrascal.service.application.domain.NumOfUsers;
import com.stayrascal.service.application.domain.TotalFreq;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository {
    /**
     * 获得有多少用户在使用完构件1之后又使用构件2
     *
     * @param compName 构件1的名字
     * @return 用户数
     */
    List<NumOfUsers> getNumOfUsers(@Param("compName") String compName);

    /**
     * 获得用户使用构件1之后又使用了哪些构件
     *
     * @param uuid     用户uuid
     * @param compName 构件1的名字
     * @return 使用历史
     */
    List<HistoryRecord> getHistoryForUser(@Param("uuid") String uuid, @Param("compName") String compName);

    /**
     * 获得使用完构件1又使用构件2的总次数
     *
     * @param compName 构件1的名字
     * @return 总次数
     */
    List<TotalFreq> getTotalFreq(@Param("compName") String compName);

    /**
     * 获得受欢迎的构件对的使用人数
     *
     * @param limit 指定构件对的数量
     * @return
     */
    List<NumOfUsers> getPopularUsagesPopulation(@Param("limit") int limit);

    /**
     * 获得受欢迎的构件对的使用次数
     *
     * @param limit 指定构件对的数量
     * @return
     */
    List<TotalFreq> getPopulatedUsagesCount(@Param("limit") int limit);
}

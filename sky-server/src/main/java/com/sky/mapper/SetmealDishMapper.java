package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    void insertBatch(List<SetmealDish> list);

    SetmealDish queryByDishId(Long id);

    List<SetmealDish> queryBySetmealId(Long id);

    Integer countByDishId(Long id);


    void deleteBySetmealId(Long id);
}

package com.sky.mapper;

import com.sky.dto.DishDTO;
import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    void insert(DishFlavor flavor);

    List<DishFlavor> queryByDishId(Long id);

    void deleteByDishId(Long dishId);
}

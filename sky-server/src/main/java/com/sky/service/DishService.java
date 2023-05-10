package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.PageResult;
import com.sky.entity.Dish;

import java.util.List;


public interface DishService {

    void add(DishDTO dishDTO);

    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    DishDTO queryById(Long id);

    void update(DishDTO dishDTO);

    void delete(List<Long> ids);

    void updateStatus(Integer status, Long id);

    List<Dish> list(Long categoryId);
}

package com.sky.service;

import com.sky.dto.PageResult;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    void add(SetmealDTO setmealDTO);

    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO queryById(Long id);

    void updateWithDish(SetmealDTO setmealDTO);

    void updateWithStatus(Integer status, Long id);

    void delete(List<Long> ids);
}

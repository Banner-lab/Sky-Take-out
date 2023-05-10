package com.sky.service;

import com.sky.dto.PageResult;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.vo.SetmealVO;

public interface SetmealService {
    void add(SetmealDTO setmealDTO);

    PageResult page(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO queryById(Long id);
}

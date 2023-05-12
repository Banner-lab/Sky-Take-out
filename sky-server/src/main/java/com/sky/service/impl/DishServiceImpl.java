package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.PageResult;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @ClassName DishServiceImpl
 * @Description TODO
 * @Author XMING
 * @Date 2023/5/10 14:09
 * @Version 1.0
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    public void add(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish,"flavors");

        dishMapper.insert(dish);
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null){
            flavors.forEach(df->{
                df.setDishId(dish.getId());
                dishFlavorMapper.insert(df);
            });
        }
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> dishes = (Page<DishVO>)dishMapper.findAll(dishPageQueryDTO);

        PageResult pageResult = new PageResult();
        pageResult.setTotal(dishes.getTotal());
        pageResult.setRecords(dishes.getResult());
        return pageResult;
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishDTO queryById(Long id) {
        DishDTO dishDTO = new DishDTO();
        Dish dish = dishMapper.queryById(id);
        BeanUtils.copyProperties(dish,dishDTO);
        // 查询菜品包含口味信息
        List<DishFlavor> flavors = dishFlavorMapper.queryByDishId(id);
        dishDTO.setFlavors(flavors);
        return dishDTO;
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Override
    public void update(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish,"flavors");
        dishMapper.update(dish);
        //先删除原有的菜品口味信息
        dishFlavorMapper.deleteByDishId(dish.getId());

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors!= null){
            flavors.forEach(df->{
                df.setDishId(dish.getId());
                dishFlavorMapper.insert(df);
            });
        }
    }

    /**
     * 根据id删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
        // 查看要删除的菜品中是否有菜品处于启售状态
        Integer cnt = dishMapper.countByStatus(ids,StatusConstant.ENABLE);
        if(cnt > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }

        //查询要删除的菜品是否关联了套餐
        ids.forEach(id->{
            SetmealDish setmealDish = setmealDishMapper.queryByDishId(id);
            if(setmealDish!= null){
                throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
            }
        });

        //删除菜品的同时需要删除口味信息
        ids.forEach(id->{
            dishMapper.deleteById(id);
            dishFlavorMapper.deleteByDishId(id);
        });
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        Dish dish = new Dish();

        dish.setStatus(status);
        dish.setId(id);
        dishMapper.update(dish);
    }

    @Override
    public List<Dish> list(Long cateoryId) {
        List<Dish> dishes = dishMapper.queryByCateoryId(cateoryId);
        return dishes;
    }
}

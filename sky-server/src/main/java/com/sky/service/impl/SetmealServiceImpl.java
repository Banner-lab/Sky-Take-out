package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.PageResult;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @ClassName SetmealServiceImpl
 * @Description TODO
 * @Author XMING
 * @Date 2023/5/10 16:19
 * @Version 1.0
 */
@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    @Transactional
    @CacheEvict(cacheNames = "setmealCache",key = "#setmealDTO.categoryId")
    public void add(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(s->s.setSetmealId(setmeal.getId()));


        setmealMapper.insert(setmeal);
        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> pr = setmealMapper.query(setmealPageQueryDTO);
        PageResult pageResult = new PageResult();
        pageResult.setRecords(pr.getResult());
        pageResult.setTotal(pr.getTotal());
        return pageResult;
    }

    @Override
    public SetmealVO queryById(Long id) {
        SetmealVO setmealVO = setmealMapper.queryById(id);
        List<SetmealDish> setmealDishes = setmealDishMapper.queryBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public void updateWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmeal.setId(setmeal.getId());
        setmealMapper.updateById(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        // 删除原来与套餐关联的菜品信息
        setmealDishMapper.deleteBySetmealId(setmeal.getId());

        setmealDishes.forEach(s->s.setSetmealId(setmeal.getId()));

        setmealDishMapper.insertBatch(setmealDishes);
    }

    @Override
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public void updateWithStatus(Integer status, Long id) {
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        // 启售会先检查套餐中包含菜品是否包含处于停售状态的菜品
        if(status.equals(StatusConstant.ENABLE)){
            List<SetmealDish> setmealDishes = setmealDishMapper.queryBySetmealId(id);
            List<Long> ids = new ArrayList<Long>();
            setmealDishes.forEach(s->ids.add(s.getDishId()));
            Integer count = dishMapper.countByStatus(ids,StatusConstant.DISABLE);
            if (count>0) {
                throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }
        setmealMapper.updateById(setmeal);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "setmealCache",allEntries = true)
    public void delete(List<Long> ids) {
        // 套餐处于启售状态无法删除
        Integer cnt = setmealMapper.countByStatus(ids, StatusConstant.ENABLE);
        if (cnt > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        // 删除套餐
        setmealMapper.deleteBatch(ids);
        // 删除套餐菜品关系表中的记录
        ids.forEach(id->{
            setmealDishMapper.deleteBySetmealId(id);
        });
    }

    @Override
    public List<Setmeal> list(Setmeal setmeal) {
        return setmealMapper.list(setmeal);
    }

    /**
     * 根据套餐id查询套餐中包含的菜品
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id) {
      return setmealMapper.getDishItemById(id);
    }
}

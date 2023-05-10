package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.PageResult;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName DishController
 * @Description 菜品控制器
 * @Author XMING
 * @Date 2023/5/10 11:20
 * @Version 1.0
 */
@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api("菜品管理")
public class DishController {
    @Autowired
    private DishService dishService;

    /**
     * 添加菜品
     * @param dishDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result add(@RequestBody DishDTO dishDTO){
        dishService.add(dishDTO);
        return Result.success();
    }

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        PageResult pg = dishService.page(dishPageQueryDTO);
        return Result.success(pg);
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishDTO> getById(@PathVariable Long id){
        DishDTO dishDTO = dishService.queryById(id);
        return Result.success(dishDTO);
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dishDTO){
        dishService.update(dishDTO);
        return Result.success();
    }

    /**
     * 根据id删除菜品，菜品处于停售状态才可以删除
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam("ids") List<Long> ids){
        dishService.delete(ids);
        return Result.success();
    }

    /**
     * 修改菜品售卖状态
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启售或停售")
    public Result updateStatus(@PathVariable Integer status,@RequestParam Long id){
        dishService.updateStatus(status,id);
        return Result.success();
    }

    /**
     * 根据分类查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类查询菜品")
    public Result<List<Dish>> list(@RequestParam  Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

}

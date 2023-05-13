package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName ShoppingCartController
 * @Description TODO
 * @Author XMING
 * @Date 2023/5/13 11:11
 * @Version 1.0
 */
@RestController
@RequestMapping("/user/shoppingCart")
@Api(tags="购物车接口")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result addCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.addCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看用户购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查看购物车")
    public Result<List<ShoppingCart>> list(){
        List<ShoppingCart> cartList =  shoppingCartService.list();
        return Result.success(cartList);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车")
    public Result clean(){
        shoppingCartService.clean();
        return Result.success();
    }

    /**
     * 购物车数目减少
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation("购物车数目消减")
    public Result subCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        shoppingCartService.subCart(shoppingCartDTO);
        return Result.success();
    }
}

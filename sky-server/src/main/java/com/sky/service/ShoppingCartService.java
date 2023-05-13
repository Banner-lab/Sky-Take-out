package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;

import java.util.List;

public interface ShoppingCartService {
    void addCart(ShoppingCartDTO shoppingCartDTO);
    List<ShoppingCart> list();

    void clean();

    void subCart(ShoppingCartDTO shoppingCartDTO);
}

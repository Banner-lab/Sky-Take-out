package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PageResult;
import com.sky.entity.Employee;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     * @param employeeDTO
     */
    void add(EmployeeDTO employeeDTO);

    /**
     * 员工分页查询
     * @param
     * @return
     */
    PageResult page(EmployeePageQueryDTO employeePageQueryDTO);

    void status(Integer status,Long id);

    Employee queryById(Long id);

    void update(EmployeeDTO employeeDTO);
}

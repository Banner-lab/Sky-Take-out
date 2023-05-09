package com.sky.dto;

import com.sky.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName EmployeePageDto
 * @Description TODO
 * @Author XMING
 * @Date 2023/5/9 10:52
 * @Version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult implements Serializable {
    private List records;
    private Long total;
}

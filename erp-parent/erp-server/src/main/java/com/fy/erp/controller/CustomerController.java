package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.Customer;
import com.fy.erp.result.Result;
import com.fy.erp.service.CustomerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    public Result<Page<Customer>> page(@RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Customer::getName, keyword)
                    .or().like(Customer::getContact, keyword)
                    .or().like(Customer::getPhone, keyword));
        }
        if (level != null) {
            wrapper.eq(Customer::getLevel, level);
        }
        if (status != null) {
            wrapper.eq(Customer::getStatus, status);
        }

        java.util.List<String> userRoles = com.fy.erp.security.UserContext.get().getRoles();
        if (userRoles.contains("SALES") && !userRoles.contains("ADMIN") && !userRoles.contains("FIN")) {
            wrapper.eq(Customer::getOwnerUserId, com.fy.erp.security.UserContext.get().getUserId());
        }

        return Result.success(customerService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<Customer> detail(@PathVariable Long id) {
        return Result.success(customerService.getById(id));
    }

    @PostMapping
    public Result<Customer> create(@RequestBody Customer customer) {
        customerService.save(customer);
        return Result.success(customer);
    }

    @PutMapping("/{id}")
    public Result<Customer> update(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        customerService.updateById(customer);
        return Result.success(customer);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.removeById(id);
        return Result.success();
    }
}

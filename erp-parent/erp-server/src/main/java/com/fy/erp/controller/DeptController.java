package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.SysDept;
import com.fy.erp.result.Result;
import com.fy.erp.service.SysDeptService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/depts")
public class DeptController {
    private final SysDeptService deptService;

    public DeptController(SysDeptService deptService) {
        this.deptService = deptService;
    }

    @GetMapping
    public Result<Page<SysDept>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysDept::getDeptName, keyword);
        }
        if (status != null) {
            wrapper.eq(SysDept::getStatus, status);
        }
        return Result.success(deptService.page(new Page<>(page, size), wrapper));
    }

    @PostMapping
    public Result<SysDept> create(@RequestBody SysDept dept) {
        deptService.save(dept);
        return Result.success(dept);
    }

    @PutMapping("/{id}")
    public Result<SysDept> update(@PathVariable Long id, @RequestBody SysDept dept) {
        dept.setId(id);
        deptService.updateById(dept);
        return Result.success(dept);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        deptService.removeById(id);
        return Result.success();
    }
}

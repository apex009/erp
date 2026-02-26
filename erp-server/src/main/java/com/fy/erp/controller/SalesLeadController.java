package com.fy.erp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fy.erp.entities.SalesLead;
import com.fy.erp.result.Result;
import com.fy.erp.service.SalesLeadService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leads")
public class SalesLeadController {
    private final SalesLeadService leadService;

    public SalesLeadController(SalesLeadService leadService) {
        this.leadService = leadService;
    }

    @GetMapping
    public Result<Page<SalesLead>> page(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String stage,
                                        @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<SalesLead> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(SalesLead::getName, keyword).or().like(SalesLead::getRemark, keyword));
        }
        if (stage != null && !stage.isBlank()) {
            wrapper.eq(SalesLead::getStage, stage);
        }
        if (status != null) {
            wrapper.eq(SalesLead::getStatus, status);
        }
        return Result.success(leadService.page(new Page<>(page, size), wrapper));
    }

    @GetMapping("/{id}")
    public Result<SalesLead> detail(@PathVariable Long id) {
        return Result.success(leadService.getById(id));
    }

    @PostMapping
    public Result<SalesLead> create(@RequestBody SalesLead lead) {
        if (lead.getStatus() == null) {
            lead.setStatus(1);
        }
        leadService.save(lead);
        return Result.success(lead);
    }

    @PutMapping("/{id}")
    public Result<SalesLead> update(@PathVariable Long id, @RequestBody SalesLead lead) {
        lead.setId(id);
        leadService.updateById(lead);
        return Result.success(lead);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        leadService.removeById(id);
        return Result.success();
    }
}

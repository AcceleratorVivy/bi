package com.mika.bi.service.impl;

import com.baomidou.mybatisplus.core.metadata.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mika.bi.common.ErrorCode;
import com.mika.bi.constant.CommonConstant;
import com.mika.bi.exception.ThrowUtils;
import com.mika.bi.model.dto.chart.ChartQueryRequest;
import com.mika.bi.model.entity.Chart;
import com.mika.bi.model.entity.User;
import com.mika.bi.model.vo.ChartVO;
import com.mika.bi.service.ChartService;
import com.mika.bi.mapper.ChartMapper;
import com.mika.bi.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author weiyishen
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2024-03-08 17:36:46
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {

    @Resource
    private UserService userService;

    @Override
    public void validChart(Chart chart, boolean b) {
        if (b) {
            String goal = chart.getGoal();
            String chartData = chart.getChartData();
            String chartType = chart.getChartType();
            ThrowUtils.throwIf(StringUtils.isAnyBlank(goal, chartData, chartType), ErrorCode.PARAMS_ERROR);
        }
        ThrowUtils.throwIf(chart == null, ErrorCode.PARAMS_ERROR);
    }

    @Override
    public ChartVO getChartVO(Chart chart, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        ChartVO chartVO = new ChartVO();
        BeanUtils.copyProperties(chart, chartVO);
        return chartVO;
    }

    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        String status = chartQueryRequest.getStatus();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        Long userId = chartQueryRequest.getUserId();
        QueryWrapper<Chart> chartQueryWrapper = new QueryWrapper<>();
        chartQueryWrapper.orderBy(StringUtils.isNotBlank(sortField), CommonConstant.SORT_ORDER_ASC.equals(sortOrder), sortField)
                .eq(StringUtils.isNotBlank(status), "status", status)
                .eq(StringUtils.isNotBlank(chartType), "chartType", chartType)
                .like(StringUtils.isNotBlank(goal), "goal", goal)
                .like(StringUtils.isNotBlank(name), "name", name)
                .eq(ObjectUtils.isNotNull(id), "id", id)
                .eq(ObjectUtils.isNotNull(userId),"userId",userId);
        return chartQueryWrapper;
    }

    @Override
    public Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request) {
        List<Chart> records = chartPage.getRecords();
        long total = chartPage.getTotal();
        long size = chartPage.getSize();
        long current = chartPage.getCurrent();
        Page<ChartVO> chartVOPage = new Page<>(current, size, total);
        List<ChartVO> collect = records.stream().map(o -> getChartVO(o, request)).collect(Collectors.toList());
        chartVOPage.setRecords(collect);
        return chartVOPage;
    }
}





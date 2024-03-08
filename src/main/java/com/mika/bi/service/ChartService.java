package com.mika.bi.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mika.bi.model.dto.chart.ChartQueryRequest;
import com.mika.bi.model.entity.Chart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mika.bi.model.vo.ChartVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author weiyishen
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2024-03-08 17:36:46
*/
public interface ChartService extends IService<Chart> {

    void validChart(Chart chart, boolean b);

    ChartVO getChartVO(Chart chart, HttpServletRequest request);

    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request);
}

package com.mika.bi.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 生成图表视图（脱敏）
 *
 */
@Data
public class ChartVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 图标名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表数据
     */
    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     * 图表生成状态,wait、succeed,running,failed
     */
    private String status;

    /**
     * 图表生成信息描述
     */
    private String execMessage;

    /**
     * 创建用户 id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
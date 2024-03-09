package com.mika.bi.model.dto.chart;

import com.mika.bi.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *

 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MyChartQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;
}
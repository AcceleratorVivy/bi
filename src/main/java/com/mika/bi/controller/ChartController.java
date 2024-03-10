package com.mika.bi.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mika.bi.annotation.AuthCheck;
import com.mika.bi.common.BaseResponse;
import com.mika.bi.common.DeleteRequest;
import com.mika.bi.common.ErrorCode;
import com.mika.bi.common.ResultUtils;
import com.mika.bi.constant.UserConstant;
import com.mika.bi.exception.BusinessException;
import com.mika.bi.exception.ThrowUtils;
import com.mika.bi.manager.MyMessageConsumer;
import com.mika.bi.manager.MyMessageProducer;
import com.mika.bi.manager.RedissonRateLimitManager;
import com.mika.bi.manager.ThreadPoolExecutorManager;
import com.mika.bi.model.dto.chart.ChartAddRequest;
import com.mika.bi.model.dto.chart.ChartQueryRequest;
import com.mika.bi.model.dto.chart.ChartUpdateRequest;
import com.mika.bi.model.dto.chart.MyChartQueryRequest;
import com.mika.bi.model.entity.Chart;
import com.mika.bi.model.entity.User;
import com.mika.bi.model.vo.ChartVO;
import com.mika.bi.service.ChartService;
import com.mika.bi.service.UserService;
import com.mika.bi.utils.ExcelUtils;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private YuCongMingClient yuCongMingClient;
    // region 增删改查
    @Resource
    private RedissonRateLimitManager redissonRateLimitManager;

    @Resource
    private MyMessageProducer myMessageProducer;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;




    @PostMapping("/gen")
    public BaseResponse<ChartVO> genChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                           ChartAddRequest chartAddRequest, HttpServletRequest request) {
         String name = chartAddRequest.getName();
         String goal = chartAddRequest.getGoal();
         ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.NOT_FOUND_ERROR,"请填写目标需求");
         ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>100,ErrorCode.PARAMS_ERROR,"图标名称过长");
        long size = multipartFile.getSize();
        final int ONE_M = 1024 * 1024;
        final int NUM = 1;
        ThrowUtils.throwIf(size > NUM*ONE_M,ErrorCode.OPERATION_ERROR,"文件大小大于"+NUM+"M");

        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> vaildFileSuffix = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!vaildFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件格式错误");

        String chartType = chartAddRequest.getChartType();
        chartType = StringUtils.isBlank(chartType)? "折线图" : chartType ;

        User loginUser = userService.getLoginUser(request);

        redissonRateLimitManager.doRateLimit("rateLimit.genChartByAi."+loginUser.getId());

        String excelData = ExcelUtils.readExcel(multipartFile);
        /**
         * 给数据、 要求图表类型、 分析目的
         * prompt
         *final String prompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
         *        "分析需求：\n" +
         *        "{数据分析的需求或者目标}\n" +
         *        "原始数据：\n" +
         *        "{csv格式的原始数据，用,作为分隔符}\n" +
         *        "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
         *        "【【【【【\n" +
         *        "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
         *        "【【【【【\n" +
         *        "{明确的数据分析结论、越详细越好，不要生成多余的注释}";
         */
        StringBuffer requestBuffer = new StringBuffer();
        requestBuffer.append("分析需求:\n");
        requestBuffer.append(goal).append("请使用:").append(chartType).append("\n");
        requestBuffer.append("原始数据:\n").append(excelData);
        ThrowUtils.throwIf(requestBuffer.length()>1024, ErrorCode.OPERATION_ERROR,"数据太多");
        Long modelId = 1659171950288818178L;
        DevChatRequest devChatRequest = new DevChatRequest(modelId,String.valueOf(requestBuffer));
        com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        String[] data = response.getData().getContent().split("【【【【【");

        ThrowUtils.throwIf(data.length != 3, ErrorCode.SYSTEM_ERROR,"AI 故障");

        String chartCode = data[1];
        String result = data[2];
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(excelData);//todo 分表 将数据另创建一个新的表存储 data_{数据id}
        chart.setChartType(chartType);
        chart.setGenChart(chartCode);
        chart.setGenResult(result);
        chart.setStatus("succeed");
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"保存图表失败");
        // todo  chart 转 chartVO 方法
        ChartVO chartVO = new ChartVO();
        chartVO.setId(chart.getId());
        chartVO.setName(chart.getName());
        chartVO.setGoal(goal);
        chartVO.setChartType(chartType);
        chartVO.setGenChart(chartCode);
        chartVO.setGenResult(result);
        chartVO.setStatus("succeed");
        chartVO.setExecMessage("");
        return ResultUtils.success(chartVO);
    }

    /**
     * 异步处理生成图表
     * @param multipartFile
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<String> AsyncGenChartByAi(@RequestPart("file") MultipartFile multipartFile,
                                              ChartAddRequest chartAddRequest, HttpServletRequest request) {
        String name = chartAddRequest.getName();
        String goal = chartAddRequest.getGoal();
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.NOT_FOUND_ERROR,"请填写目标需求");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>100,ErrorCode.PARAMS_ERROR,"图标名称过长");
        long size = multipartFile.getSize();
        final int ONE_M = 1024 * 1024;
        final int NUM = 1;
        ThrowUtils.throwIf(size > NUM*ONE_M,ErrorCode.OPERATION_ERROR,"文件大小大于"+NUM+"M");

        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> vaildFileSuffix = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!vaildFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件格式错误");

        String chartType = chartAddRequest.getChartType();
        chartType = StringUtils.isBlank(chartType)? "折线图" : chartType ;

        User loginUser = userService.getLoginUser(request);

        redissonRateLimitManager.doRateLimit("rateLimit.genChartByAi."+loginUser.getId());

        /**
         * 给数据、 要求图表类型、 分析目的
         * prompt
         *final String prompt = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
         *        "分析需求：\n" +
         *        "{数据分析的需求或者目标}\n" +
         *        "原始数据：\n" +
         *        "{csv格式的原始数据，用,作为分隔符}\n" +
         *        "请根据这两部分内容，按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
         *        "【【【【【\n" +
         *        "{前端 Echarts V5 的 option 配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，比如注释}\n" +
         *        "【【【【【\n" +
         *        "{明确的数据分析结论、越详细越好，不要生成多余的注释}";
         */
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
//        chart.setChartData(excelData);//todo 分表 将数据另创建一个新的表存储 data_{数据id}
        chart.setChartType(chartType);
//        chart.setGenChart(chartCode);
//        chart.setGenResult(result);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean save = chartService.save(chart);
        if(!save){
            chartService.genError(chart,ErrorCode.SYSTEM_ERROR,"保存图表失败");
        }

        CompletableFuture.runAsync(()->{
            String excelData = ExcelUtils.readExcel(multipartFile);
            chart.setChartData(excelData);
            chart.setStatus("running");
            boolean b = chartService.updateById(chart);
            if(!b){
                chartService.genError(chart,ErrorCode.OPERATION_ERROR,"执行失败");
            }

            StringBuffer requestBuffer = new StringBuffer();
            requestBuffer.append("分析需求:\n");
            requestBuffer.append(goal).append("请使用:").append(chart.getChartType()).append("\n");
            requestBuffer.append("原始数据:\n").append(excelData);
            if(requestBuffer.length()>1024){
                chartService.genError(chart,ErrorCode.OPERATION_ERROR,"数据过大生成图表失败");
            }

            Long modelId = 1659171950288818178L;
            DevChatRequest devChatRequest = new DevChatRequest(modelId,String.valueOf(requestBuffer));
            com.yupi.yucongming.dev.common.BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);

            String[] data = response.getData().getContent().split("【【【【【");
            if(data.length != 3){
                chartService.genError(chart,ErrorCode.SYSTEM_ERROR,"AI 故障");
            }
            String chartCode = data[1];
            String result = data[2];
            chart.setGenChart(chartCode);
            chart.setGenResult(result);
            chart.setStatus("succeed");
            b = chartService.updateById(chart);
        },threadPoolExecutor);

        return ResultUtils.success("等待图表生成");

    }

    /**
     *   通过消息队列处理生成图表
     * @param multipartFile
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async/mq")
    public BaseResponse<String> AsyncGenChartByAiMq(@RequestPart("file") MultipartFile multipartFile,
                                                  ChartAddRequest chartAddRequest, HttpServletRequest request) {
        String name = chartAddRequest.getName();
        String goal = chartAddRequest.getGoal();
        ThrowUtils.throwIf(StringUtils.isBlank(goal),ErrorCode.NOT_FOUND_ERROR,"请填写目标需求");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name)&&name.length()>100,ErrorCode.PARAMS_ERROR,"图标名称过长");
        long size = multipartFile.getSize();
        final int ONE_M = 1024 * 1024;
        final int NUM = 1;
        ThrowUtils.throwIf(size > NUM*ONE_M,ErrorCode.OPERATION_ERROR,"文件大小大于"+NUM+"M");

        String originalFilename = multipartFile.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> vaildFileSuffix = Arrays.asList("xlsx","xls");
        ThrowUtils.throwIf(!vaildFileSuffix.contains(suffix),ErrorCode.PARAMS_ERROR,"文件格式错误");

        String chartType = chartAddRequest.getChartType();
        chartType = StringUtils.isBlank(chartType)? "折线图" : chartType ;

        User loginUser = userService.getLoginUser(request);

        redissonRateLimitManager.doRateLimit("rateLimit.genChartByAi."+loginUser.getId());
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
//        chart.setChartData(excelData);//todo 分表 将数据另创建一个新的表存储 data_{数据id}
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        String excelData = ExcelUtils.readExcel(multipartFile);
        chart.setChartData(excelData);
        boolean save = chartService.save(chart);
        if(!save){
            chartService.genError(chart,ErrorCode.SYSTEM_ERROR,"保存图表失败");
        }

        myMessageProducer.sendMessage(String.valueOf(chart.getId()));

        return ResultUtils.success("等待图表生成");

    }











    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody MyChartQueryRequest myChartQueryRequest,HttpServletRequest request) {

        ChartQueryRequest chartQueryRequest = new ChartQueryRequest();
        BeanUtils.copyProperties(myChartQueryRequest,chartQueryRequest);
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = myChartQueryRequest.getCurrent();
        long size = myChartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }










    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        chartService.validChart(chart, true);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        // 参数校验
        chartService.validChart(chart, false);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ChartVO> getChartVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chartService.getChartVO(chart, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param chartQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<ChartVO>> listChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartService.getChartVOPage(chartPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<ChartVO>> listMyChartVOByPage(@RequestBody ChartQueryRequest chartQueryRequest,
            HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                chartService.getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartService.getChartVOPage(chartPage, request));
    }

    // endregion





}

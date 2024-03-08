package com.mika.bi.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ExcelUtils {

    public static String readExcel(MultipartFile multipartFile){
//        File file = null;
//        try {
//            file = ResourceUtils.getFile("classpath:网站数据.xlsx");
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        List<Map<Integer, String>> list = null;
        try {
            list = EasyExcel.read(multipartFile.getInputStream())
                    .excelType(ExcelTypeEnum.XLSX)
                    .sheet()
                    .headRowNumber(0)
                    .doReadSync();
        } catch (IOException e) {
            e.printStackTrace();
        }
        LinkedHashMap<Integer, String> header = (LinkedHashMap<Integer, String>) list.get(0);
        StringBuilder builder = new StringBuilder();
        List<String> headers = header.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
        String headerStr = StringUtils.join(headers, ",");
        builder.append(headerStr).append("\n");
        for(int i = 1 ; i< header.size(); i++){
            LinkedHashMap<Integer, String> row = (LinkedHashMap<Integer, String>) (list.get(i));
            List<String> rowData = row.values().stream().filter(ObjectUtils::isNotEmpty).collect(Collectors.toList());
            builder.append(StringUtils.join(rowData,",")).append("\n");
        }
        log.info("\n{}",builder);
        return String.valueOf(builder);
    }

    public static void main(String[] args) {
        System.out.println(readExcel(null));;
    }

}

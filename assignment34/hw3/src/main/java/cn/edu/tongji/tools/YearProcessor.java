package cn.edu.tongji.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class YearProcessor {
    public static int getTotalPublication(String data, int startYear, int endYear) throws JsonProcessingException {
        int result = 0;
        if (startYear == -1) {
            startYear = 0;
        }
        if (endYear == -1) {
            endYear = 3000;
        }
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Map<String, Integer>> mapData = mapper.readValue(data, new TypeReference<>() {});

        for (Map.Entry<String, Map<String, Integer>> entry: mapData.entrySet()) {
            Map<String, Integer> innerMap = entry.getValue();
            for (Map.Entry<String, Integer> innerEntry: innerMap.entrySet()) {
                int year = Integer.parseInt(innerEntry.getKey());
                if (year >= startYear && year <= endYear) {
                    result += innerEntry.getValue();
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws JsonProcessingException {
        String jsonData = "{\"Weixiong Rao\": {\"2020\": 6, \"2015\": 8, \"2021\": 13, \"2022\": 2, \"2023\": 9, \"2011\": 3, \"2014\": 5, \"2010\": 1, \"2005\": 1, \"2013\": 4, \"2019\": 8, \"2012\": 5, \"2016\": 6, \"2007\": 1, \"2018\": 7, \"2009\": 2, \"2017\": 9, \"2003\": 2, \"2004\": 2}}";
        int res = getTotalPublication(jsonData, 0, 3000);
        System.out.println(res);
    }
}

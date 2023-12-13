package cn.edu.tongji.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;

public class GetPublications {
    private String author;
    private Integer startYear;
    private Integer endYear;
    private HashMap<String, HashMap<Integer, Integer>> data = new HashMap<>();
    // 如果有多个 ser 文件 考虑多线程读取
    private String filePath = "data.ser";
    public GetPublications(String author, Integer startYear, Integer endYear) {
        this.author = author;
        this.startYear = startYear;
        this.endYear = endYear;
    }
    public int getAll() {
        getHashtable();
        return getPublications();
    }
    private void getHashtable() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            data = (HashMap<String, HashMap<Integer, Integer>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private int getPublications() {
        // 不用时间查询
        if (startYear == null && endYear == null) {
            return data.get(author).values().stream().mapToInt(Integer::intValue).sum();
        }

        if (startYear == null) {
            startYear = 1234;
        }
        if (endYear == null) {
            endYear = 2100;
        }
        int res = 0;
        // 时间相同
        if (startYear == endYear) {
            return data.get(author).get(startYear);
        } else {
            var yearToPublications = data.get(author);
            for (Map.Entry<Integer, Integer> entry: yearToPublications.entrySet()) {
                Integer year = entry.getKey();
                Integer publications = entry.getValue();
                if (startYear <= year && year <= endYear) {
                    res += publications;
                }
            }
        }
        return res;
    }

    public static void main(String[] args) {
        GetPublications getPublications = new GetPublications("sjm1", null, null);
        System.out.println(getPublications.getAll());
    }
}

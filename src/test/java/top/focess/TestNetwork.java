package top.focess;

import com.focess.api.util.Pair;
import com.focess.api.util.json.JSON;
import com.focess.api.util.network.HttpResponse;
import com.focess.api.util.network.NetworkHandler;
import com.focess.core.util.MethodCaller;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.math3.random.ISAACRandom;
import org.apache.commons.math3.random.RandomGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.Map;

public class TestNetwork {

    public static final NetworkHandler NETWORK_HANDLER = new NetworkHandler();

    public static final String SESSION_ID = "94ad9fca-6188-4772-a1ed-5b6a03ac568b";

    private static Map<String,Object> getStudentData(int id){
        Map<String,String> header = Maps.newHashMap();
        header.put("Cookie","SESSION=" + SESSION_ID);
        header.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        HttpResponse httpResponse = NETWORK_HANDLER.get("https://jzsz.uestc.edu.cn/instructor-develop/api/instrctor/work/studentDimension/detail/" + id + "?_t=" + System.currentTimeMillis(),Maps.newHashMap(),header);
        JSON json = httpResponse.getAsJSON();
        return json.get("data");
    }

    private static List<Integer> getStudents() {
        Map<String,String> header = Maps.newHashMap();
        header.put("Cookie","SESSION=" + SESSION_ID);
        header.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        Map<String,Object> d = Maps.newHashMap();
        d.put("page",1);
        d.put("size",1000);
        d.put("startTime","");
        d.put("endTime","");
        d.put("key","");
        d.put("workCode",0);
        HttpResponse httpResponse = NETWORK_HANDLER.post("https://jzsz.uestc.edu.cn/instructor-develop/api/instrctor/work/listStudentsDimensionWorkInfo",d,header,NetworkHandler.JSON);
        JSON json = httpResponse.getAsJSON();
        Map<String,Object> data = json.get("data");
        List<Map<String,Object>> list = (List<Map<String,Object>>) data.get("list");
        List<Integer> students = Lists.newArrayList();
        for (Map<String,Object> map : list)
            students.add((int) map.get("id"));
        return students;
    }

    private static List<Integer> getStudentsFamily() {
        Map<String,String> header = Maps.newHashMap();
        header.put("Cookie","SESSION=" + SESSION_ID);
        header.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        Map<String,Object> d = Maps.newHashMap();
        d.put("page",1);
        d.put("size",1000);
        d.put("startTime","");
        d.put("endTime","");
        d.put("key","");
        d.put("workCode",2);
        HttpResponse httpResponse = NETWORK_HANDLER.post("https://jzsz.uestc.edu.cn/instructor-develop/api/instrctor/work/listStudentsDimensionWorkInfo",d,header,NetworkHandler.JSON);
        JSON json = httpResponse.getAsJSON();
        Map<String,Object> data = json.get("data");
        List<Map<String,Object>> list = (List<Map<String,Object>>) data.get("list");
        List<Integer> students = Lists.newArrayList();
        for (Map<String,Object> map : list)
            students.add((int) map.get("id"));
        return students;
    }

    private static void postStudentTalk(int id,String talk,long timeInSeconds){
        Map<String,String> header = Maps.newHashMap();
        header.put("Cookie","SESSION=" + SESSION_ID);
        header.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        Map<String,Object> data = getStudentData(id);
        Map<String,Object> t = Maps.newHashMap();
        t.put("id",null);
        t.put("talkId",id);
        t.put("talkTime",timeInSeconds * 1000);
        t.put("content",talk);
        t.put("fileIds",null);
        t.put("fileList",null);
        t.put("isSome",true);
        List<Map<String,Object>> talks = data.get("talkDetailAddVos") == null ? Lists.newArrayList() : (List<Map<String,Object>>) data.get("talkDetailAddVos");
        talks.add(t);
        data.put("talkDetailAddVos",talks);
        HttpResponse httpResponse = NETWORK_HANDLER.post("https://jzsz.uestc.edu.cn/instructor-develop/api/instrctor/work/studentDimension/addOrUpdate",data,header,NetworkHandler.JSON);
        System.out.println(httpResponse.getResponse());
    }

    private static void postFamilyTalk(int id,String talk,long timeInSeconds){
        Map<String,String> header = Maps.newHashMap();
        header.put("Cookie","SESSION=" + SESSION_ID);
        header.put("User-Agent","Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        Map<String,Object> data = getStudentData(id);
        Map<String,Object> t = Maps.newHashMap();
        t.put("id",null);
        t.put("talkId",id);
        t.put("talkTime",timeInSeconds * 1000);
        t.put("content",talk);
        t.put("fileIds",null);
        t.put("fileList",Lists.newArrayList());
        t.put("isSome",true);
        List<Map<String,Object>> talks = data.get("talkDetailAddVos") == null ? Lists.newArrayList() : (List<Map<String,Object>>) data.get("talkDetailAddVos");
        talks.add(t);
        data.put("talkDetailAddVos",talks);
        HttpResponse httpResponse = NETWORK_HANDLER.post("https://jzsz.uestc.edu.cn/instructor-develop/api/instrctor/work/studentDimension/addOrUpdate",data,header,NetworkHandler.JSON);
        System.out.println(httpResponse.getResponse());
    }

    //get timestamp for input day using LocalDate
    private static long getTimestamp(int year,int month,int day){
        return LocalDate.of(year,month,day).getLong(ChronoField.EPOCH_DAY);
    }

    private static long getRandomTimeInSecond(int startMouth,int endMouth,List<Pair<Integer,Integer>> timeRange){
        RandomGenerator randomGenerator = new ISAACRandom();
        Pair<Integer,Integer> pair = timeRange.get(randomGenerator.nextInt(timeRange.size()));
        int startHour = pair.getKey();
        int endHour = pair.getValue();
        int startTime = startHour * 6;
        int endTime = endHour * 6;
        int mouth = randomGenerator.nextInt(endMouth - startMouth + 1) + startMouth;
        int day = randomGenerator.nextInt(30) + 1; // not ok for 2
        long dayTime = getTimestamp(2021,mouth,day) * 24 * 3600 - 8 * 3600;
        long time = (randomGenerator.nextInt(endTime - startTime + 1) + startTime) * 600L;
        if (LocalDateTime.ofEpochSecond(dayTime + time,0, ZoneOffset.of("+8")).isAfter(LocalDateTime.now()))
            return getRandomTimeInSecond(startMouth,endMouth,timeRange);
        return dayTime + time;
    }

    public static void main(String[] args) throws IOException {
//        File file = new File("talk.txt");
//        List<String> talks = Files.readAllLines(file.toPath());
//        RandomGenerator randomGenerator = new ISAACRandom();
//        List<Integer> students = getStudents();
//        for (int id : students) {
//            Map<String,Object> data = getStudentData(id);
//            if (data.get("name").equals("张赛涛") || data.get("name").equals("康恒锐"))
//                continue;
//            postStudentTalk(id,talks.get(randomGenerator.nextInt(talks.size())),getRandomTimeInSecond(3,6,Lists.newArrayList(Pair.of(9,12),Pair.of(14,18))));
//            postStudentTalk(id,talks.get(randomGenerator.nextInt(talks.size())),getRandomTimeInSecond(3,6,Lists.newArrayList(Pair.of(9,12),Pair.of(14,18))));
//            postStudentTalk(id,talks.get(randomGenerator.nextInt(talks.size())),getRandomTimeInSecond(9,12,Lists.newArrayList(Pair.of(9,12),Pair.of(14,18))));
//            postStudentTalk(id,talks.get(randomGenerator.nextInt(talks.size())),getRandomTimeInSecond(9,12,Lists.newArrayList(Pair.of(9,12),Pair.of(14,18))));
//        }
        System.out.println(MethodCaller.getCallerClass());
//        File file = new File("parent.txt");
//        List<String> talks = Files.readAllLines(file.toPath());
//        RandomGenerator randomGenerator = new ISAACRandom();
//        List<Integer> studentsFamily = getStudentsFamily();
//        for (int id : studentsFamily) {
//            Map<String,Object> data = getStudentData(id);
//            if (data.get("name").equals("张赛涛") || data.get("name").equals("康恒锐"))
//                continue;
//            postFamilyTalk(id,talks.get(randomGenerator.nextInt(talks.size())),getRandomTimeInSecond(3,6,Lists.newArrayList(Pair.of(9,12),Pair.of(14,18))));
//            postFamilyTalk(id,talks.get(randomGenerator.nextInt(talks.size())),getRandomTimeInSecond(3,6,Lists.newArrayList(Pair.of(9,12),Pair.of(14,18))));
//            postFamilyTalk(id,talks.get(randomGenerator.nextInt(talks.size())),getRandomTimeInSecond(9,12,Lists.newArrayList(Pair.of(9,12),Pair.of(14,18))));
//            postFamilyTalk(id,talks.get(randomGenerator.nextInt(talks.size())),getRandomTimeInSecond(9,12,Lists.newArrayList(Pair.of(9,12),Pair.of(14,18))));
//        }
    }
}

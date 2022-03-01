package top.focess;

import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class TestMain {

    private static int[] people = new int[1000];
    private static Map<Integer,Integer> map = Maps.newHashMap();

    public static void main(String[] args) {
        Arrays.fill(people,100);
        Random random = new Random();
        int count = 20000000;
        for (int i = 0;i<count;i++) {
            int a = random.nextInt(1000);
            int b = random.nextInt(1000);
            if (a == b)
                count++;
            people[a]++;
            people[b]--;
        }
        Arrays.sort(people);
        System.out.println(count);
        System.out.println(Arrays.stream(people).sum());
        System.out.println(Arrays.toString(people));
        int lastpos = 0;
        for (int i = 1;i<people.length;i++) {
            if (people[i-1] != people[i]) {
                map.put(people[i-1],i - lastpos);
                lastpos = i;
            }
        }
        System.out.println(map);
    }
}

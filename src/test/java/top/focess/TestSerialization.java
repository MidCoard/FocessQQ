package top.focess;

import com.google.common.collect.Lists;
import top.focess.qq.api.serialize.FocessSerializable;
import top.focess.qq.api.util.yaml.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TestSerialization {

    public static class A implements FocessSerializable {
        private final int age;

        private final List<Integer>[] arrayListList = new List[]{Lists.newArrayList(),Lists.newArrayList()};
        private final Class<?>[] I = {int.class,double.class,Double.class,String.class};
        private final Integer[] data;

        public A(int age) {
            this.age = age;
            this.arrayListList[0].add(age);
            this.arrayListList[0].add(null);
            this.arrayListList[1].add(1);
            this.data = new Integer[10];
            Random random = new Random();
            for (int i = 0; i < data.length; i++) {
                data[i] = random.nextInt(100);
            }
        }


        @Override
        public String toString() {
            return "A{" +
                    "age=" + age +
                    ", arrayListList=" + Arrays.toString(arrayListList) +
                    ", I=" + Arrays.toString(I) +
                    ", data=" + Arrays.toString(data) +
                    '}';
        }
    }

    public static void main(String[] args) {
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadFile(new File("temp.yml"));
        System.out.println(yamlConfiguration.get("a").toString());
//        A a = new A(102);
//        YamlConfiguration yamlConfiguration = new YamlConfiguration(null);
//        yamlConfiguration.set("a",a);
//        yamlConfiguration.save(new File("temp.yml"));
    }
}

package top.focess;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TestMain {

    public static void main(String[] args) throws IOException {
        Yaml yaml = new Yaml();
        Map<String,Object> maps = new HashMap<>();
        TypeDescription description = new TypeDescription(TestClass.class);
        description.substituteProperty("key",TestClass.class,"","");
        description.substituteProperty("key",TestClass.class,"","");
        yaml.addTypeDescription(description);
        maps.put("fff",new TestClass("",1,1,"fuckaa"));
        System.out.println(yaml.dump(new TestClass("",1,1,"fuckaa")));
        yaml.dump(maps,new FileWriter(new File("a.yml")));
    }

    public static class TestClass implements Serializable {
        private final String key;
        private final int a;
        private final int b;
        private final String value;

        public TestClass(String key, int a, int b, String value) {
            this.key = key;
            this.a = a;
            this.b = b;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }

        public String getValue() {
            return value;
        }
    }
}

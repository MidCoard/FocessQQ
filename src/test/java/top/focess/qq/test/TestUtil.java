package top.focess.qq.test;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import top.focess.qq.core.util.MethodCaller;
import top.focess.util.Base64;
import top.focess.util.json.JSONObject;
import top.focess.util.network.HttpResponse;
import top.focess.util.network.NetworkHandler;
import top.focess.util.option.Option;
import top.focess.util.option.OptionParserClassifier;
import top.focess.util.option.Options;
import top.focess.util.option.type.IntegerOptionType;
import top.focess.util.option.type.OptionType;
import top.focess.util.serialize.*;
import top.focess.util.version.Version;
import top.focess.util.version.VersionFormatException;
import top.focess.util.yaml.YamlConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Util Test")
public class TestUtil {

    @Test
    void testMethodCaller() {
        Runnable runnable = () -> assertEquals(TestUtil.class, MethodCaller.getCallerClass());
        runnable.run();
        Runnable runnable1 = () -> {
            Runnable runnable2 = () -> {
                assertNotNull(MethodCaller.getCallerClass());
                // because lambda expression is realized by method reference
                assertEquals(TestUtil.class, MethodCaller.getCallerClass());
            };
            runnable2.run();
        };
        runnable1.run();
        Runnable runnable2 = new Runnable() {
            @Override
            public void run() {
                Runnable runnable3 = new Runnable() {
                    @Override
                    public void run() {
                        assertNotNull(MethodCaller.getCallerClass());
                        assertTrue(Runnable.class.isAssignableFrom(MethodCaller.getCallerClass()));
                    }
                };
                runnable3.run();
            }
        };
        runnable2.run();
    }

    @Test
    void testOptions() {
        String[] args = new String[]{"--a", "1", "--b", "--c", "--d", "hello", "world"};
        Options options = Options.parse(args,
                new OptionParserClassifier("a", IntegerOptionType.INTEGER_OPTION_TYPE),
                new OptionParserClassifier("b"),
                new OptionParserClassifier("c"),
                new OptionParserClassifier("d", OptionType.DEFAULT_OPTION_TYPE, OptionType.DEFAULT_OPTION_TYPE)
        );
        Option option = options.get("a");
        assertNotNull(option);
        assertEquals(1, option.get(IntegerOptionType.INTEGER_OPTION_TYPE));
        option = options.get("b");
        assertNotNull(option);
        option = options.get("c");
        assertNotNull(option);
        option = options.get("d");
        assertNotNull(option);
        assertEquals("hello", option.get(OptionType.DEFAULT_OPTION_TYPE));
        assertEquals("world", option.get(OptionType.DEFAULT_OPTION_TYPE));
    }

    static class ErrorSerialize {

        private final int value;

        public ErrorSerialize(int value) {
            this.value = value;
        }
    }

    static class ErrorSerialize2 implements FocessSerializable {

        private final int value;
        private final ErrorSerialize errorSerialize;

        public ErrorSerialize2(int value) {
            this.value = value;
            this.errorSerialize = new ErrorSerialize(value);
        }
    }

    static class CustomSerialize implements FocessSerializable {
        private final int value;

        public CustomSerialize(int value) {
            this.value = value;
        }
    }

    @Test
    void testYamlConfiguration() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration(null);
        yamlConfiguration.set("test", 1);
        assertThrows(NotFocessSerializableException.class, () -> yamlConfiguration.set("a", new ErrorSerialize(1)));
        assertDoesNotThrow(() -> yamlConfiguration.set("b", new CustomSerialize(1)));
        CustomSerialize customSerialize = yamlConfiguration.get("b");
        assertNotNull(customSerialize);
        assertEquals(1, customSerialize.value);
        assertThrows(NotFocessSerializableException.class, () -> yamlConfiguration.set("c", new ErrorSerialize2(1)));
        List<CustomSerialize> customSerializes = Lists.newArrayList();
        customSerializes.add(new CustomSerialize(1));
        customSerializes.add(new CustomSerialize(2));
        customSerializes.add(new CustomSerialize(3));
        assertDoesNotThrow(() -> yamlConfiguration.set("list", customSerializes));
        List<CustomSerialize> customSerializes1 = yamlConfiguration.get("list");
        assertNotNull(customSerializes1);
        assertEquals(3, customSerializes1.size());
        assertEquals(1, customSerializes1.get(0).value);
        assertEquals(2, customSerializes1.get(1).value);
        assertEquals(3, customSerializes1.get(2).value);
    }

    @Test
    void testSerialization() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        FocessWriter focessWriter = FocessWriter.newFocessWriter(byteArrayOutputStream);
        focessWriter.write(new CustomSerialize(1));
        FocessReader focessReader = FocessReader.newFocessReader(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        CustomSerialize customSerialize = (CustomSerialize) focessReader.read();
        assertNotNull(customSerialize);
        assertEquals(1, customSerialize.value);
        assertThrows(NotFocessSerializableException.class, () -> focessWriter.write(new ErrorSerialize(1)));
        assertThrows(SerializationParseException.class, focessReader::read);
    }

    @Test
    void testNetworkHandler() {
        NetworkHandler networkHandler = new NetworkHandler();
        HttpResponse httpResponse = networkHandler.request("https://www.baidu.com", NetworkHandler.RequestType.GET);
        assertFalse(httpResponse.isError());
        assertEquals(200, httpResponse.getCode());
    }



    @Test
    void testVersion() {
        Version version = new Version("1.0.0");
        assertEquals(1, version.getMajor());
        assertEquals(0, version.getMinor());
        assertEquals(0, version.getRevision());
        assertEquals("1.0.0", version.toString());
        assertEquals(0, version.compareTo(new Version("1.0.0")));
        Version version1 = new Version("2.0.0");
        assertEquals(1, version1.compareTo(version));
        assertEquals(-1, version.compareTo(version1));
        assertTrue(version1.higher(version));
        assertFalse(version1.lower(version) || version1.equals(version));
        assertDoesNotThrow(() -> new Version("1.0.0"));
        assertThrows(VersionFormatException.class, () -> new Version("1.0.0.0.0"));
    }

    @Test
    void testJson() {
        JSONObject jsonObject = JSONObject.parse("{\"name\":\"focess\",\"age\":18}");
        assertEquals("focess", jsonObject.get("name"));
        assertEquals(18, (Integer) jsonObject.get("age"));
        assertEquals("{\"name\":\"focess\",\"age\":18}", jsonObject.toJson());
        JSONObject jsonObject1 = JSONObject.parse("[{\"name\":\"focess\",\"age\":18},{\"name\":\"focess2\",\"age\":19}]");
        assertEquals("focess", jsonObject1.getJSON(0).get("name"));
        assertEquals(18, (Integer) jsonObject1.getJSON(0).get("age"));
        assertEquals("focess2", jsonObject1.getJSON(1).get("name"));
        assertEquals(19, (Integer) jsonObject1.getJSON(1).get("age"));
    }

    @Test
    void testBase64() {
        String s = "focess";
        byte[] bytes = s.getBytes();
        byte[] encode = Base64.encodeBase64(bytes);
        byte[] decode = Base64.decodeBase64(encode);
        assertEquals(s, new String(decode));
    }


}

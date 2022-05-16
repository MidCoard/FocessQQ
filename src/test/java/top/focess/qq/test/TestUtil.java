package top.focess.qq.test;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import top.focess.qq.core.debug.Section;
import top.focess.qq.core.util.MethodCaller;
import top.focess.scheduler.*;
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
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
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

    @Test
    void testScheduler() {
        Scheduler scheduler = new FocessScheduler("Test");
        Task task = scheduler.run(() -> {
            System.out.println(1);
        });
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            fail();
        }
        assertFalse(task.cancel());
        assertFalse(task.isCancelled());
        assertTrue(task.isFinished());
        assertFalse(task.isRunning());
        Task task1 = scheduler.run(() -> {
            System.out.println(2);
        }, Duration.ofSeconds(1));
        assertTimeoutPreemptively(Duration.ofSeconds(2), ()->task1.join());
        assertTrue(task1.isFinished());
        Task task2 = scheduler.run(() -> {
            try {
                Thread.sleep(2000);
                System.out.println(3);
            } catch (InterruptedException e) {
                fail();
            }
        });
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(task2.isRunning());
        try {
            task2.join();
        } catch (Exception e) {
            fail();
        }
        assertTrue(task2.isFinished());
        Task task3 = scheduler.run(() -> {
            System.out.println(4);
        }, Duration.ofSeconds(1));
        assertTrue(task3.cancel());
        assertTrue(task3.isCancelled());
        scheduler.close();
    }

    @RepeatedTest(5)
    void testScheduler2() {
        Scheduler scheduler = new ThreadPoolScheduler( 10,false,"test-2");
        List<Task> taskList = Lists.newArrayList();
        for (int i = 0; i < 20; i++) {
            int finalI = i;
            taskList.add(scheduler.run(() -> {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    fail();
                }
                System.out.println(finalI);
            }));
        }
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(10, taskList.stream().filter(Task::isRunning).count());
        assertTrue(taskList.get(0).cancel(true));
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            fail();
        }
        assertEquals(10, taskList.stream().filter(Task::isRunning).count());
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            fail();
        }
        scheduler.close();
    }

    @RepeatedTest(5)
    void testScheduler3() {
        Scheduler scheduler = new ThreadPoolScheduler( 5,false,"test-3");

        Task task = scheduler.run(()->{
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                fail();
            }
            throw new NullPointerException();
        });

        assertThrows(ExecutionException.class, task::join);
        assertTrue(task.isFinished());

        Task task1 = scheduler.run(()->{
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                fail();
            }
            throw new InternalError();
        });
        assertThrows(ExecutionException.class, task::join);
        assertTrue(task.isFinished());
        AtomicInteger count = new AtomicInteger(0);
        List<Task> tasks = Lists.newArrayList();
        for (int i = 0; i<5;i++) {
            int finalI = i;
            tasks.add(scheduler.run(()->{
                try {
                    sleep(1000 * (finalI));
                } catch (InterruptedException e) {
                    fail();
                }
                throw new InternalError();
            }, "test-" + i, (executionException)->{
                assertInstanceOf(InternalError.class, executionException.getCause());
                count.incrementAndGet();
            }));
        }
        for (Task task2 : tasks)
            assertDoesNotThrow(()->task2.join());
        assertEquals(5, count.get());
        scheduler.close();
    }

    @Test
    void testScheduler4() throws Exception {
        Scheduler scheduler = new ThreadPoolScheduler(10, true, "test-4");
        Field field = AScheduler.class.getDeclaredField("tasks");
        field.setAccessible(true);
        Field field1 = ComparableTask.class.getDeclaredField("time");
        field1.setAccessible(true);
        Field field2 = ComparableTask.class.getDeclaredField("task");
        field2.setAccessible(true);
        PriorityBlockingQueue<ComparableTask> tasks2 = (PriorityBlockingQueue<ComparableTask>) field.get(scheduler);
        List<Task> tasks = Lists.newArrayList();
        for (int i = 0;i<5;i++) {
            int finalI = i;
            tasks.add(scheduler.run(()-> System.out.println(finalI),Duration.ofSeconds(1),finalI + ""));
            System.out.println(tasks2.stream().map(t -> {
                try {
                    return field1.get(t).toString() + " " + field2.get(t).toString();
                } catch (IllegalAccessException e) {
                    return "";
                }
            }).collect(Collectors.toList()));
        }
        for (Task task : tasks)
            assertDoesNotThrow(()->task.join());
        scheduler.close();
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
    void testSection() {
        Scheduler scheduler = new ThreadPoolScheduler(1, false,"test-section-1");
        Task task = scheduler.run(() -> {
            try {
                sleep(10000);
            } catch (InterruptedException e) {
                fail();
            }
        });
        Section section = Section.startSection("test", task, Duration.ofSeconds(2));
        assertTimeoutPreemptively(Duration.ofSeconds(3), () -> assertThrows(CancellationException.class, task::join));
        assertTrue(task.isCancelled());
        scheduler.close();
    }

    @Test
    void testSection2() {
        Scheduler scheduler = new ThreadPoolScheduler( 1,false,"test-2");
        Task task = scheduler.run(() -> {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                fail();
            }
        });
        Section section = Section.startSection("test", task, Duration.ofSeconds(2));
        try {
            task.join();
        } catch (Exception e) {
            fail();
        }
        section.stop();
        assertFalse(task.isCancelled());
        assertTrue(task.isFinished());
        scheduler.close();
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
        assertEquals("Zm9j", Base64.base64Encode("foc".getBytes(StandardCharsets.UTF_8)));
        assertEquals("foc", new String(Base64.base64Decode("Zm9j"), StandardCharsets.UTF_8));
    }

    @RepeatedTest(5)
    void testScheduler5() {
        Scheduler scheduler = new FocessScheduler("test-5");
        AtomicInteger atomicInteger = new AtomicInteger(0);
        Task task = scheduler.runTimer(()->{
            atomicInteger.incrementAndGet();
            throw new NullPointerException();
        }, Duration.ofSeconds(0), Duration.ofSeconds(1),"test");
        try {
            sleep(3000);
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(task.isPeriod());
        task.cancel();
        try {
            sleep(500);
        } catch (InterruptedException e) {
            fail();
        }
        assertTrue(task.isCancelled());
        assertNotEquals(1, atomicInteger.get());
        scheduler.close();
    }


}

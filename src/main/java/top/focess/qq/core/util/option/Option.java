package top.focess.qq.core.util.option;

import top.focess.qq.core.util.option.type.OptionType;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import java.util.Map;
import java.util.Queue;

public class Option {

    private final OptionParserClassifier classifier;

    private final Map<OptionType<?>, Queue<String>> optionTypes = Maps.newHashMap();

    public Option(OptionParserClassifier classifier) {
        this.classifier = classifier;
    }

    public String getName() {
        return classifier.getName();
    }

    public void put(OptionType<?> optionType, String value) {
        optionTypes.compute(optionType,(k,v)->{
            if (v == null)
                v = Queues.newConcurrentLinkedQueue();
            v.offer(value);
            return v;
        });
    }

    public <T> T get(OptionType<T> optionType) {
        Queue<String> options = optionTypes.getOrDefault(optionType,Queues.newConcurrentLinkedQueue());
        T t = optionType.parse(options.poll());
        optionTypes.put(optionType,options);
        return t;
    }

    @Override
    public String toString() {
        return "Option{" +
                "optionTypes=" + optionTypes +
                '}';
    }
}

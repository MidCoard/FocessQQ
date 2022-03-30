package top.focess.qq.core.util.option;

import top.focess.qq.core.util.option.type.OptionType;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import java.util.Map;
import java.util.Queue;

public class Option {

    private final OptionParserClassifier classifier;

    private final Map<OptionType<?>, Queue<String>> optionTypes = Maps.newHashMap();

    public Option(final OptionParserClassifier classifier) {
        this.classifier = classifier;
    }

    public String getName() {
        return this.classifier.getName();
    }

    public void put(final OptionType<?> optionType, final String value) {
        this.optionTypes.compute(optionType,(k, v)->{
            if (v == null)
                v = Queues.newConcurrentLinkedQueue();
            v.offer(value);
            return v;
        });
    }

    public <T> T get(final OptionType<T> optionType) {
        final Queue<String> options = this.optionTypes.getOrDefault(optionType,Queues.newConcurrentLinkedQueue());
        final String v = options.poll();
        final T t = optionType.parse(v == null ? "" : v);
        this.optionTypes.put(optionType,options);
        return t;
    }

    @Override
    public String toString() {
        return "Option{" +
                "optionTypes=" + this.optionTypes +
                '}';
    }
}

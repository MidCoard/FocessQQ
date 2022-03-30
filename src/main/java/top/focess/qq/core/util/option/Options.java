package top.focess.qq.core.util.option;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Options {

    private final Map<String,Option> options = Maps.newHashMap();

    public static Options parse(final String[] args, final OptionParserClassifier... classifiers) {
        final List<String> temp = Lists.newArrayList();
        final List<OptionParserClassifier> defaultClassifier = Lists.newArrayList();
        final Options options = new Options();
        for (final String arg : args) {
            if (arg.startsWith("--")) {
                for (final OptionParserClassifier classifier : defaultClassifier)
                    options.add(classifier.createOption(temp.toArray(new String[0])));
                defaultClassifier.clear();
                temp.clear();
                for (final OptionParserClassifier classifier : classifiers)
                    if (arg.equals("--" + classifier.getName()))
                        defaultClassifier.add(classifier);
            } else
                temp.add(arg);
        }
        for (final OptionParserClassifier classifier : defaultClassifier)
            options.add(classifier.createOption(temp.toArray(new String[0])));
        return options;
    }

    private void add(@Nullable final Option option) {
        if (option == null)
            return;
        this.options.put(option.getName(),option);
    }

    @Nullable
    public Option get(final String name) {
        return this.options.get(name);
    }

    @Override
    public String toString() {
        return "Options{" +
                "options=" + this.options +
                '}';
    }
}

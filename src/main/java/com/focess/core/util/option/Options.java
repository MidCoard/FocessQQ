package com.focess.core.util.option;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class Options {

    private final Map<String,Option> options = Maps.newHashMap();

    public static Options parse(String[] args, OptionParserClassifier... classifiers) {
        List<String> temp = Lists.newArrayList();
        List<OptionParserClassifier> defaultClassifier = Lists.newArrayList();
        Options options = new Options();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                for (OptionParserClassifier classifier : defaultClassifier)
                    options.add(classifier.createOption(temp.toArray(new String[0])));
                defaultClassifier.clear();
                for (OptionParserClassifier classifier : classifiers)
                    if (arg.equals("--" + classifier.getName()))
                        defaultClassifier.add(classifier);
            } else
                temp.add(arg);
        }
        for (OptionParserClassifier classifier : defaultClassifier)
            options.add(classifier.createOption(temp.toArray(new String[0])));
        return options;
    }

    private void add(Option option) {
        if (option == null)
            return;
        this.options.put(option.getName(),option);
    }

    @Nullable
    public Option get(String name) {
        return this.options.get(name);
    }


}

package top.focess.qq.core.util.option;

import org.jetbrains.annotations.Nullable;
import top.focess.qq.core.util.option.type.OptionType;

public class OptionParserClassifier {

    private final String name;
    private final OptionType<?>[] optionTypes;

    public OptionParserClassifier(String name,OptionType<?>... optionTypes) {
        this.name = name;
        this.optionTypes = optionTypes;
    }

    public String getName() {
        return name;
    }

    public OptionType<?>[] getOptionTypes() {
        return optionTypes;
    }

    @Nullable
    public Option createOption(String[] args) {
        if (args.length != optionTypes.length)
            return null;
        Option option = new Option(this);
        for (int i = 0;i<args.length;i++)
            if (optionTypes[i].accept(args[i]))
                option.put(optionTypes[i],args[i]);
            else return null;
        return option;
    }
}
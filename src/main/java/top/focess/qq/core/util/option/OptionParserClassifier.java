package top.focess.qq.core.util.option;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.focess.qq.core.util.option.type.OptionType;

public class OptionParserClassifier {

    private final String name;
    private final OptionType<?>[] optionTypes;

    public OptionParserClassifier(final String name, final OptionType<?>... optionTypes) {
        this.name = name;
        this.optionTypes = optionTypes;
    }

    public String getName() {
        return this.name;
    }

    public OptionType<?>[] getOptionTypes() {
        return this.optionTypes;
    }

    @Nullable
    public Option createOption(@NotNull final String[] args) {
        if (args.length != this.optionTypes.length)
            return null;
        final Option option = new Option(this);
        for (int i = 0; i < args.length; i++)
            if (this.optionTypes[i].accept(args[i]))
                option.put(this.optionTypes[i], args[i]);
            else return null;
        return option;
    }
}
package top.focess;

import com.focess.core.util.option.Option;
import com.focess.core.util.option.OptionParserClassifier;
import com.focess.core.util.option.Options;
import com.focess.core.util.option.optiontype.IntegerOptionType;
import com.focess.core.util.option.optiontype.LongOptionType;
import com.focess.core.util.option.optiontype.OptionType;

import java.util.Arrays;

public class Test {

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        Options options = Options.parse(args,
                new OptionParserClassifier("user", LongOptionType.LONG_OPTION_TYPE, OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("server", IntegerOptionType.INTEGER_OPTION_TYPE),
                new OptionParserClassifier("client",OptionType.DEFAULT_OPTION_TYPE,IntegerOptionType.INTEGER_OPTION_TYPE,OptionType.DEFAULT_OPTION_TYPE,IntegerOptionType.INTEGER_OPTION_TYPE,OptionType.DEFAULT_OPTION_TYPE),
                new OptionParserClassifier("sided"),
                new OptionParserClassifier("client",OptionType.DEFAULT_OPTION_TYPE,IntegerOptionType.INTEGER_OPTION_TYPE));
        Option option = options.get("user");
        System.out.println(options);
    }
}

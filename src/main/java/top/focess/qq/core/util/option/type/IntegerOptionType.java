package top.focess.qq.core.util.option.type;

public class IntegerOptionType extends ExceptionOptionType<Integer> {

    public static final IntegerOptionType INTEGER_OPTION_TYPE = new IntegerOptionType();

    @Override
    public Integer parse(final String v) {
        return Integer.parseInt(v);
    }

    @Override
    public String toString() {
        return "INTEGER_OPTION_TYPE";
    }
}

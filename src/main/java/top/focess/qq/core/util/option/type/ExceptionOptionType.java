package top.focess.qq.core.util.option.type;


public abstract class ExceptionOptionType<T> extends OptionType<T> {

    @Override
    public boolean accept(final String v) {
        try {
            this.parse(v);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }
}

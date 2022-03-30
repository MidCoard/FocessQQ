package top.focess.qq.api.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommandArgument<V> {

    private final DataConverter<V> dataConverter;
    private final V value;
    private final boolean isNullable;

    private CommandArgument(@NotNull final DataConverter<V> dataConverter, @Nullable final V value) {
        this.dataConverter = dataConverter;
        this.value = value;
        this.isNullable = false;
    }

    private CommandArgument(@NotNull final DataConverter<V> dataConverter, final boolean isNullable) {
        this.dataConverter = dataConverter;
        this.value = null;
        this.isNullable = isNullable;
    }

    public static CommandArgument<String> ofString() {
        return new CommandArgument<>(DataConverter.DEFAULT_DATA_CONVERTER, null);
    }

    public static CommandArgument<Long> ofLong() {
        return new CommandArgument<>(DataConverter.LONG_DATA_CONVERTER, null);
    }

    public static CommandArgument<Integer> ofInt() {
        return new CommandArgument<>(DataConverter.INTEGER_DATA_CONVERTER, null);
    }

    public static <V> CommandArgument<V> of(final DataConverter<V> defaultDataConverter) {
        return new CommandArgument<>(defaultDataConverter, null);
    }

    @NotNull
    public static CommandArgument<String> of(@NotNull final String value) {
        return new CommandArgument<>(DataConverter.DEFAULT_DATA_CONVERTER, value);
    }

    @NotNull
    public static <V> CommandArgument<V> of(@NotNull final DataConverter<V> dataConverter, @NotNull final V value) {
        return new CommandArgument<>(dataConverter, value);
    }

    @NotNull
    public static <V> CommandArgument<V> ofNullable(@NotNull final DataConverter<V> dataConverter) {
        return new CommandArgument<>(dataConverter, true);
    }

    public boolean isNullable() {
        return this.isNullable;
    }

    public boolean isDefault() {
        return this.value != null;
    }

    public V getValue() {
        return this.value;
    }

    public DataConverter<V> getDataConverter() {
        return this.dataConverter;
    }

    public boolean accept(final String arg) {
        if (this.isDefault())
            return this.getDataConverter().accept(arg) && this.getValue().equals(this.getDataConverter().convert(arg));
        else return this.getDataConverter().accept(arg);
    }

    public void put(final DataCollection dataCollection, final String arg) {
        if (!this.isDefault())
            this.getDataConverter().put(dataCollection, arg);
    }

    @Override
    public String toString() {
        return "CommandArgument{" +
                "dataConverter=" + this.dataConverter +
                ", value=" + this.value +
                ", isNullable=" + this.isNullable +
                '}';
    }
}

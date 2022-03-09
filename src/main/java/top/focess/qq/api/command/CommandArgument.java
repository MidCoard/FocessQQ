package top.focess.qq.api.command;

import org.jetbrains.annotations.NotNull;

public class CommandArgument<V> {

    private final DataConverter<V> dataConverter;
    private final V value;
    private final boolean isNullable;

    private CommandArgument(@NotNull DataConverter<V> dataConverter,V value) {
        this.dataConverter = dataConverter;
        this.value = value;
        this.isNullable = false;
    }

    private CommandArgument(@NotNull DataConverter<V> dataConverter,boolean isNullable) {
        this.dataConverter = dataConverter;
        this.value = null;
        this.isNullable = isNullable;
    }

    public static CommandArgument<String> ofString() {
        return new CommandArgument<>(DataConverter.DEFAULT_DATA_CONVERTER,null);
    }

    public static CommandArgument<Long> ofLong() {
        return new CommandArgument<>(DataConverter.LONG_DATA_CONVERTER,null);
    }

    public static CommandArgument<Integer> ofInt() {
        return new CommandArgument<>(DataConverter.INTEGER_DATA_CONVERTER,null);
    }

    public boolean isNullable() {
        return isNullable;
    }

    public boolean isDefault() {
        return value != null;
    }

    public V getValue() {
        return value;
    }

    public DataConverter<V> getDataConverter() {
        return dataConverter;
    }

    public static <V> CommandArgument<V> of(DataConverter<V> defaultDataConverter) {
        return new CommandArgument<>(defaultDataConverter,null);
    }

    @NotNull
    public static CommandArgument<String> of(@NotNull String value) {
        return new CommandArgument<>(DataConverter.DEFAULT_DATA_CONVERTER, value);
    }

    @NotNull
    public static <V> CommandArgument<V> of(@NotNull DataConverter<V> dataConverter,@NotNull V value) {
        return new CommandArgument<>(dataConverter,value);
    }

    @NotNull
    public static <V> CommandArgument<V> ofNullable(@NotNull DataConverter<V> dataConverter) {
        return new CommandArgument<>(dataConverter,true);
    }

    public boolean accept(String arg) {
        if (this.isDefault())
            return this.getDataConverter().accept(arg) && this.getValue().equals(this.getDataConverter().convert(arg));
        else return this.getDataConverter().accept(arg);
    }

    public void put(DataCollection dataCollection, String arg) {
        if (!this.isDefault())
            this.getDataConverter().put(dataCollection, arg);
    }

    @Override
    public String toString() {
        return "CommandArgument{" +
                "dataConverter=" + dataConverter +
                ", value=" + value +
                ", isNullable=" + isNullable +
                '}';
    }
}

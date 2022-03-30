package top.focess.qq.api.command;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Represents a command argument.
 * @param <V> the type of the argument.
 */
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

    /**
     * Represent an unknown String CommandArgument
     *
     * Note: this argument indicates this position need a String value.
     *
     * @return the CommandArgument representing an unknown String
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public static CommandArgument<String> ofString() {
        return new CommandArgument<>(DataConverter.DEFAULT_DATA_CONVERTER, null);
    }

    /**
     * Represent an unknown Long CommandArgument
     *
     * Note: this argument indicates this position need a Long value.
     *
     * @return the CommandArgument representing an unknown Long
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public static CommandArgument<Long> ofLong() {
        return new CommandArgument<>(DataConverter.LONG_DATA_CONVERTER, null);
    }

    /**
     * Represents an unknown Int CommandArgument
     *
     * Note: this argument indicates this position need an Int value.
     *
     * @return the CommandArgument representing an unknown Int
     */
    @NotNull
    @Contract(value = " -> new", pure = true)
    public static CommandArgument<Integer> ofInt() {
        return new CommandArgument<>(DataConverter.INTEGER_DATA_CONVERTER, null);
    }

    /**
     * Represents an unknown CommandArgument with a specific DataConverter
     *
     * Note: this argument indicates this position need a V type value
     *
     * @param defaultDataConverter the DataConverter
     * @param <V> the type of the argument.
     * @return the CommandArgument representing an unknown CommandArgument with a specific DataConverter
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static <V> CommandArgument<V> of(final DataConverter<V> defaultDataConverter) {
        return new CommandArgument<>(defaultDataConverter, null);
    }

    /**
     * Represents a CommandArgument with a specific String value
     *
     * Note: this argument indicates this position is a known String value.
     *
     * @param value the String value of the CommandArgument
     * @return the CommandArgument with a specific String value
     */
    @NotNull
    public static CommandArgument<String> of(@NotNull final String value) {
        return new CommandArgument<>(DataConverter.DEFAULT_DATA_CONVERTER, value);
    }

    /**
     * Represents a CommandArgument with a specific value
     *
     * Note: this argument indicates this position is a known V value.
     *
     * @param dataConverter the DataConverter
     * @param value the value of the CommandArgument
     * @param <V> the type of the argument.
     * @return the CommandArgument with a specific value
     */
    @NotNull
    public static <V> CommandArgument<V> of(@NotNull final DataConverter<V> dataConverter, @NotNull final V value) {
        return new CommandArgument<>(dataConverter, value);
    }

    /**
     * Represents a nullable CommandArgument with a specific DataConverter
     *
     * Note: this argument indicates this position is a nullable value.
     *
     * @param dataConverter the DataConverter
     * @param <V> the type of the argument.
     * @return the nullable CommandArgument with a specific DataConverter
     */
    @NotNull
    public static <V> CommandArgument<V> ofNullable(@NotNull final DataConverter<V> dataConverter) {
        return new CommandArgument<>(dataConverter, true);
    }

    boolean isNullable() {
        return this.isNullable;
    }

    boolean isDefault() {
        return this.value != null;
    }

    @Nullable
    V getValue() {
        return this.value;
    }

    DataConverter<V> getDataConverter() {
        return this.dataConverter;
    }

    boolean accept(final String arg) {
        if (this.isDefault())
            return this.getDataConverter().accept(arg) && Objects.equals(this.getValue(), this.getDataConverter().convert(arg));
        else return this.getDataConverter().accept(arg);
    }

    void put(final DataCollection dataCollection, final String arg) {
        if (!this.isDefault())
            this.getDataConverter().put(dataCollection, arg);
    }

}

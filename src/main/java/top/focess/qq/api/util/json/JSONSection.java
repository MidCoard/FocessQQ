package top.focess.qq.api.util.json;

import java.util.Map;

/**
 * Section of JSON.
 */
public class JSONSection extends JSON {
    private final JSON parent;

    public JSONSection(final JSON parent, final Map<String,Object> values) {
        super(values);
        this.parent = parent;
    }

    /**
     * Get the parent section
     *
     * @return the parent section
     */
    public JSON getParent() {
        return this.parent;
    }
}

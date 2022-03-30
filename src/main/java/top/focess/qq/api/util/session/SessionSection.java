package top.focess.qq.api.util.session;

import java.util.Map;

/**
 * Section of Session.
 */
public class SessionSection extends Session{
    private final Session parent;

    public SessionSection(final Session session, final Map<String, Object> values) {
        super(values);
        this.parent = session;
    }

    /**
     * Get the parent section
     *
     * @return the parent section
     */
    public Session getParent() {
        return this.parent;
    }
}

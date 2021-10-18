package com.focess.api.util.session;

import java.util.Map;

/**
 * Section of Session.
 */
public class SessionSection extends Session{
    private final Session parent;

    public SessionSection(Session session, Map<String, Object> values) {
        super(values);
        this.parent = session;
    }

    /**
     * Get the parent section
     *
     * @return the parent section
     */
    public Session getParent() {
        return parent;
    }
}

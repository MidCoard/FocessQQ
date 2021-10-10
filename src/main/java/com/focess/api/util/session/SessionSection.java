package com.focess.api.util.session;

import java.util.Map;

public class SessionSection extends Session{
    private final Session parent;

    public SessionSection(Session session, Map<String, Object> values) {
        super(values);
        this.parent = session;
    }

    public Session getParent() {
        return parent;
    }
}

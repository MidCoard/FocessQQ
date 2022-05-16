package top.focess.qq.api.command.data;

import org.checkerframework.checker.nullness.qual.NonNull;
import top.focess.command.data.DataBuffer;
import top.focess.command.data.StringBuffer;
import top.focess.qq.core.permission.Permission;

public class PermissionBuffer extends DataBuffer<Permission> {

    private final StringBuffer stringBuffer;
    public PermissionBuffer(int size) {
        this.stringBuffer = StringBuffer.allocate(size);
    }

    @Override
    public void flip() {
        this.stringBuffer.flip();
    }

    @Override
    public void put(Permission permission) {
        this.stringBuffer.put(permission.getName());
    }

    @Override
    public @NonNull Permission get() {
        return Permission.valueOf(this.stringBuffer.get());
    }

    @Override
    public @NonNull Permission get(int i) {
        return Permission.valueOf(this.stringBuffer.get(i));
    }

    public static PermissionBuffer allocate(int size) {
        return new PermissionBuffer(size);
    }
}

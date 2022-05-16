package top.focess.qq.api.command.converter;

import top.focess.command.converter.ExceptionDataConverter;
import top.focess.qq.core.permission.Permission;

import java.util.Locale;

public class PermissionDataConverter extends ExceptionDataConverter<Permission> {

    public static final PermissionDataConverter PERMISSION_DATA_CONVERTER = new PermissionDataConverter();

    @Override
    public Permission convert(String s) {
        return Permission.valueOf(s.toUpperCase(Locale.ROOT).trim());
    }

    @Override
    protected Class<Permission> getTargetClass() {
        return Permission.class;
    }
}

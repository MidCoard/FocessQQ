package top.focess.qq.test;

import org.junit.jupiter.api.MethodDescriptor;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrdererContext;

public class FocessMethodOrder extends MethodOrderer.MethodName implements MethodOrderer {

    @Override
    public void orderMethods(MethodOrdererContext context) {
        super.orderMethods(context);
        int id = -1;
        for (int i = 0; i < context.getMethodDescriptors().size(); i++)
            if (context.getMethodDescriptors().get(i).getMethod().getName().equals("testExit"))
                id = i;
        if (id != -1) {
            MethodDescriptor methodDescriptor = context.getMethodDescriptors().get(context.getMethodDescriptors().size() - 1);
            context.getMethodDescriptors().set(context.getMethodDescriptors().size() - 1, cast(context.getMethodDescriptors().get(id)));
            context.getMethodDescriptors().set(id, cast(methodDescriptor));
        }
    }

    private static <T extends MethodDescriptor> T cast(MethodDescriptor methodDescriptor) {
        return (T) methodDescriptor;
    }
}

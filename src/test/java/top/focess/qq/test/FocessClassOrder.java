package top.focess.qq.test;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

public class FocessClassOrder extends ClassOrderer.ClassName implements ClassOrderer {
    @Override
    public void orderClasses(ClassOrdererContext context) {
        super.orderClasses(context);
        int id = -1;
        for (int i = 0; i< context.getClassDescriptors().size(); i++)
            if (context.getClassDescriptors().get(i).getTestClass().getSimpleName().equals("TestUtil"))
                id = i;
        if (id != -1) {
            ClassDescriptor classDescriptor = context.getClassDescriptors().get(0);
            context.getClassDescriptors().set(0, cast(context.getClassDescriptors().get(id)));
            context.getClassDescriptors().set(id, cast(classDescriptor));
        }
    }

    private static <T extends ClassDescriptor> T cast(ClassDescriptor classDescriptor) {
        return (T) classDescriptor;
    }
}

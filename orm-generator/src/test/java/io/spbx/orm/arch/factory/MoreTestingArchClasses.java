package io.spbx.orm.arch.factory;

import io.spbx.orm.arch.testing.TestingArchClasses;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldLocalGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldPrivateGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldProtectedGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.PrivateFieldPublicGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldLocalGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldPrivateGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldProtectedGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.ProtectedFieldPublicGetterHolder;
import io.spbx.orm.arch.testing.TestingArchClasses.PublicFieldHolder;

/**
 * Must be in a different package than {@link TestingArchClasses}.
 */
public class MoreTestingArchClasses {
    public static class AlsoPrivateFieldPublicGetterDerived extends PrivateFieldPublicGetterHolder {
        AlsoPrivateFieldPublicGetterDerived(int foo) { super(foo); }
    }

    public static class AlsoPrivateFieldPrivateGetterDerived extends PrivateFieldPrivateGetterHolder {
        AlsoPrivateFieldPrivateGetterDerived(int foo) { super(foo); }
    }

    public static class AlsoPrivateFieldLocalGetterDerived extends PrivateFieldLocalGetterHolder {
        AlsoPrivateFieldLocalGetterDerived(int foo) { super(foo); }
    }

    public static class AlsoPrivateFieldProtectedGetterDerived extends PrivateFieldProtectedGetterHolder {
        AlsoPrivateFieldProtectedGetterDerived(int foo) { super(foo); }
    }

    public static class AlsoPublicFieldDerived extends PublicFieldHolder {
        AlsoPublicFieldDerived(int foo) { super(foo); }
    }

    public static class AlsoProtectedFieldPublicGetterDerived extends ProtectedFieldPublicGetterHolder {
        AlsoProtectedFieldPublicGetterDerived(int foo) { super(foo); }
    }

    public static class AlsoProtectedFieldPrivateGetterDerived extends ProtectedFieldPrivateGetterHolder {
        AlsoProtectedFieldPrivateGetterDerived(int foo) { super(foo); }
    }

    public static class AlsoProtectedFieldLocalGetterDerived extends ProtectedFieldLocalGetterHolder {
        AlsoProtectedFieldLocalGetterDerived(int foo) { super(foo); }
    }

    public static class AlsoProtectedFieldProtectedGetterDerived extends ProtectedFieldProtectedGetterHolder {
        AlsoProtectedFieldProtectedGetterDerived(int foo) { super(foo); }
    }
}

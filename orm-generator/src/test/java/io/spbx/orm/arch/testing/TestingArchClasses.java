package io.spbx.orm.arch.testing;

import java.io.Serializable;
import java.util.Objects;

@SuppressWarnings({ "FieldMayBeFinal", "FieldCanBeLocal", "unused" })
public class TestingArchClasses {
    public static class PrivateFieldPublicGetterHolder {
        private final int foo;
        protected PrivateFieldPublicGetterHolder(int foo) { this.foo = foo; }
        public int foo() { return foo; }
    }

    public static class PrivateFieldPrivateGetterHolder {
        private final int foo;
        protected PrivateFieldPrivateGetterHolder(int foo) { this.foo = foo; }
        private int foo() { return foo; }
    }

    public static class PrivateFieldLocalGetterHolder {
        private final int foo;
        protected PrivateFieldLocalGetterHolder(int foo) { this.foo = foo; }
        int foo() { return foo; }
    }

    public static class PrivateFieldProtectedGetterHolder {
        private final int foo;
        protected PrivateFieldProtectedGetterHolder(int foo) { this.foo = foo; }
        protected int foo() { return foo; }
    }

    public static class PublicFieldHolder {
        public final int foo;
        protected PublicFieldHolder(int foo) { this.foo = foo; }
    }

    public static class ProtectedFieldPublicGetterHolder {
        protected final int foo;
        protected ProtectedFieldPublicGetterHolder(int foo) { this.foo = foo; }
        public int foo() { return foo; }
    }

    public static class ProtectedFieldPrivateGetterHolder {
        protected final int foo;
        protected ProtectedFieldPrivateGetterHolder(int foo) { this.foo = foo; }
        private int foo() { return foo; }
    }

    public static class ProtectedFieldLocalGetterHolder {
        protected final int foo;
        protected ProtectedFieldLocalGetterHolder(int foo) { this.foo = foo; }
        int foo() { return foo; }
    }

    public static class ProtectedFieldProtectedGetterHolder {
        protected final int foo;
        protected ProtectedFieldProtectedGetterHolder(int foo) { this.foo = foo; }
        protected int foo() { return foo; }
    }

    /** Derived **/

    public static class PrivateFieldPublicGetterDerived extends PrivateFieldPublicGetterHolder {
        PrivateFieldPublicGetterDerived(int foo) { super(foo); }
    }

    public static class PrivateFieldPrivateGetterDerived extends PrivateFieldPrivateGetterHolder {
        PrivateFieldPrivateGetterDerived(int foo) { super(foo); }
    }

    public static class PrivateFieldLocalGetterDerived extends PrivateFieldLocalGetterHolder {
        PrivateFieldLocalGetterDerived(int foo) { super(foo); }
    }

    public static class PrivateFieldProtectedGetterDerived extends PrivateFieldProtectedGetterHolder {
        PrivateFieldProtectedGetterDerived(int foo) { super(foo); }
    }

    public static class PublicFieldDerived extends PublicFieldHolder {
        PublicFieldDerived(int foo) { super(foo); }
    }

    public static class ProtectedFieldPublicGetterDerived extends ProtectedFieldPublicGetterHolder {
        ProtectedFieldPublicGetterDerived(int foo) { super(foo); }
    }

    public static class ProtectedFieldPrivateGetterDerived extends ProtectedFieldPrivateGetterHolder {
        ProtectedFieldPrivateGetterDerived(int foo) { super(foo); }
    }

    public static class ProtectedFieldLocalGetterDerived extends ProtectedFieldLocalGetterHolder {
        ProtectedFieldLocalGetterDerived(int foo) { super(foo); }
    }

    public static class ProtectedFieldProtectedGetterDerived extends ProtectedFieldProtectedGetterHolder {
        ProtectedFieldProtectedGetterDerived(int foo) { super(foo); }
    }

    /** Other **/

    public static class FooClassInterface implements Serializable {
        private int i;
        public FooClassInterface(int i) { this.i = i; }
    }

    public static class DerivedExtended extends PrivateFieldPublicGetterDerived {
        private final int bar;
        private final String string;
        DerivedExtended(int foo, int bar, String string) {
            super(foo);
            this.bar = bar;
            this.string = string;
        }
        public int bar() { return bar; }
        public String string() { return string; }
        @Override public boolean equals(Object obj) {
            return obj instanceof DerivedExtended that && this.bar == that.bar && Objects.equals(this.string, that.string);
        }
        @Override public int hashCode() { return Objects.hash(foo(), bar, string); }
        @Override public String toString() { return "%s:%s:%s".formatted(foo(), bar, string); }
    }
}

import lombok.Setter;
class SetterPlain {
  @lombok.Setter int i;
  @Setter int foo;
  @Setter java.sql.Timestamp ts;
  @Setter int[] arr;
  SetterPlain() {
    super();
  }
  public @java.lang.SuppressWarnings("all") void setI(final int i) {
    this.i = i;
  }
  public @java.lang.SuppressWarnings("all") void setFoo(final int foo) {
    this.foo = foo;
  }
  public @java.lang.SuppressWarnings("all") void setTs(final java.sql.Timestamp ts) {
    this.ts = ((ts == null) ? null : (java.sql.Timestamp) ts.clone());
  }
  public @java.lang.SuppressWarnings("all") void setArr(final int[] arr) {
    this.arr = ((arr == null) ? null : arr.clone());
  }
}

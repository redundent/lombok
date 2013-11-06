import lombok.Getter;
class GetterPlain {
  @lombok.Getter int i;
  @Getter int foo;
  @Getter java.sql.Timestamp ts;
  @Getter int[] arr;
  GetterPlain() {
    super();
  }
  public @java.lang.SuppressWarnings("all") int getI() {
    return this.i;
  }
  public @java.lang.SuppressWarnings("all") int getFoo() {
    return this.foo;
  }
  public @java.lang.SuppressWarnings("all") java.sql.Timestamp getTs() {
    return ((this.ts == null) ? null : (java.sql.Timestamp) this.ts.clone());
  }
  public @java.lang.SuppressWarnings("all") int[] getArr() {
    return ((this.arr == null) ? null : this.arr.clone());
  }
}

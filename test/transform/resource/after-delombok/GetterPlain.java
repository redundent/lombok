class GetterPlain {
	int i;
	int foo;
	java.sql.Timestamp ts;
	int[] arr;
	@java.lang.SuppressWarnings("all")
	public int getI() {
		return this.i;
	}
	@java.lang.SuppressWarnings("all")
	public int getFoo() {
		return this.foo;
	}
	@java.lang.SuppressWarnings("all")
	public java.sql.Timestamp getTs() {
		return this.ts == null ? null : (java.sql.Timestamp)this.ts.clone();
	}
	@java.lang.SuppressWarnings("all")
	public int[] getArr() {
		return this.arr == null ? null : this.arr.clone();
	}
}
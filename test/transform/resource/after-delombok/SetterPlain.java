class SetterPlain {
	int i;
	int foo;
	java.sql.Timestamp ts;
	int[] arr;
	
	@java.lang.SuppressWarnings("all")
	public void setI(final int i) {
		this.i = i;
	}
	@java.lang.SuppressWarnings("all")
	public void setFoo(final int foo) {
		this.foo = foo;
	}
	
	@java.lang.SuppressWarnings("all")
	public void setTs(final java.sql.Timestamp ts) {
		this.ts = ts == null ? null : (java.sql.Timestamp)ts.clone();
	}
	
	@java.lang.SuppressWarnings("all")
	public void setArr(final int[] arr) {
		this.arr = arr == null ? null : arr.clone();
	}
}
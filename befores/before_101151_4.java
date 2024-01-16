package befores;commit 7 bcd7773ab9ea8b2f808211aac437d3843b4f93d Author:Pedro Ulisses<pedro.ulisses @aluno.uece.br>Date:Sat Apr 29 12:04:50 2023-0300

First commited as after_101151_rev4

diff--git a/org.eclipse.jgit.test/tst/org/eclipse/jgit/internal/storage/reftable/ReftableTest.java b/org.eclipse.jgit.test/tst/org/eclipse/jgit/internal/storage/reftable/ReftableTest.java index 7 aa49d9b31..332b20156d 100644---a/org.eclipse.jgit.test/tst/org/eclipse/jgit/internal/storage/reftable/ReftableTest.java+++b/org.eclipse.jgit.test/tst/org/eclipse/jgit/internal/storage/reftable/ReftableTest.java @@-57,10+57,13 @@

import java.io.ByteArrayOutputStream;import java.io.IOException;+import java.util.ArrayList;import java.util.Arrays;import java.util.Collection;+import java.util.List;

import org.eclipse.jgit.internal.JGitText;+import org.eclipse.jgit.internal.storage.reftable.ReftableWriter.Stats;import org.eclipse.jgit.lib.ObjectId;import org.eclipse.jgit.lib.ObjectIdRef;import org.eclipse.jgit.lib.Ref;@@-72,6+75,8 @@

public class ReftableTest {
	private static final String MASTER = "refs/heads/master";
	private static final String V1_0 = "refs/tags/v1.0";

	+
	private Stats stats;+

	@Test
 	public void emptyTable() throws IOException {
 		byte[] table = write();
@@ -194,6 +199,98 @@

	public void seekNotFound() throws IOException {
		assertFalse(r.next());
	}

	+

	@SuppressWarnings("boxing")
+	@Test
+	public void indexScan() throws IOException {
+		List<Ref> refs = new ArrayList<>();
+		for (int i = 1; i <= 5670; i++) {
+			refs.add(ref(String.format("refs/heads/%04d", i), i));
+		}
+
+		byte[] table = write(refs);
+		assertTrue(stats.refIndexKeys() > 0);
+		assertTrue(stats.refIndexSize() > 0);
+
+		ReftableReader r = read(table);
+		r.seekToFirstRef();
+		for (Ref exp : refs) {
+			assertTrue("has " + exp.getName(), r.next());
+			Ref act = r.getRef();
+			assertEquals(exp.getName(), act.getName());
+			assertEquals(exp.getObjectId(), act.getObjectId());
+		}
+		assertFalse(r.next());
+	}+ +

	@SuppressWarnings("boxing")
+	@Test
+	public void indexSeek() throws IOException {
+		List<Ref> refs = new ArrayList<>();
+		for (int i = 1; i <= 5670; i++) {
+			refs.add(ref(String.format("refs/heads/%04d", i), i));
+		}
+
+		byte[] table = write(refs);
+		assertTrue(stats.refIndexKeys() > 0);
+		assertTrue(stats.refIndexSize() > 0);
+
+		for (Ref exp : refs) {
+			ReftableReader r = seek(table, exp.getName());
+			assertTrue("has " + exp.getName(), r.next());
+			Ref act = r.getRef();
+			assertEquals(exp.getName(), act.getName());
+			assertEquals(exp.getObjectId(), act.getObjectId());
+			assertFalse(r.next());
+		}
+	}+ +

	@SuppressWarnings("boxing")
+	@Test
+	public void noIndexScan() throws IOException {
+		List<Ref> refs = new ArrayList<>();
+		for (int i = 1; i <= 567; i++) {
+			refs.add(ref(String.format("refs/heads/%03d", i), i));
+		}
+
+		byte[] table = write(refs);
+		assertEquals(0, stats.refIndexKeys());
+		assertEquals(0, stats.refIndexSize());
+		assertEquals(4, stats.refBlockCount());
+		assertEquals(table.length, stats.totalBytes());
+
+		ReftableReader r = read(table);
+		r.seekToFirstRef();
+		for (Ref exp : refs) {
+			assertTrue("has " + exp.getName(), r.next());
+			Ref act = r.getRef();
+			assertEquals(exp.getName(), act.getName());
+			assertEquals(exp.getObjectId(), act.getObjectId());
+		}
+		assertFalse(r.next());
+	}+ +

	@SuppressWarnings("boxing")
+	@Test
+	public void noIndexSeek() throws IOException {
+		List<Ref> refs = new ArrayList<>();
+		for (int i = 1; i <= 567; i++) {
+			refs.add(ref(String.format("refs/heads/%03d", i), i));
+		}
+
+		byte[] table = write(refs);
+		assertEquals(0, stats.refIndexKeys());
+		assertEquals(4, stats.refBlockCount());
+
+		for (Ref exp : refs) {
+			ReftableReader r = seek(table, exp.getName());
+			assertTrue("has " + exp.getName(), r.next());
+			Ref act = r.getRef();
+			assertEquals(exp.getName(), act.getName());
+			assertEquals(exp.getObjectId(), act.getObjectId());
+			assertFalse(r.next());
+		}
+	}+

	public void unpeeledDoesNotWrite() {
 		try {
 			write(new ObjectIdRef.Unpeeled(PACKED, MASTER, id(1)));
@@ -267,17 +364,18 @@

	private static ReftableReader read(byte[] table) {
		return new ReftableReader(BlockSource.from(table));
	}

	-

	private static byte[] write(Ref... refs) throws IOException {
+

	private byte[] write(Ref... refs) throws IOException {
		return write(Arrays.asList(refs));
	}

	-

	private static byte[] write(Collection<Ref> refs) throws IOException {
+

	private byte[] write(Collection<Ref> refs) throws IOException {
 		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 		ReftableWriter writer = new ReftableWriter().begin(buffer);
 		for (Ref r : RefComparator.sort(refs)) {
 			writer.writeRef(r);
 		}
 		writer.finish();
+		stats = writer.getStats();
 		return buffer.toByteArray();
 	}
}

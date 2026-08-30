# 100 Concept-Based Java Multithreading & Concurrency Interview Questions

**Target profile:** Backend developer with ~6 years of experience
**Goal:** After working through all 100 concepts, no multithreading interview question should feel unfamiliar — from `Thread` basics through the Java Memory Model, `java.util.concurrent`, `CompletableFuture`, and virtual threads.

**How to use this document:**

1. Answer each question out loud *before* reading the answer — concept questions are scored on precision of language.
2. Understand the mechanism, not the definition — interviewers always ask "how does that actually work underneath?"
3. The **Follow-up trap** after most answers is the interviewer's next question. Prepare it.

---

## Table of Contents

| Section | Questions | Topic |
|---|---|---|
| 1 | 1–10 | Thread Fundamentals & Lifecycle |
| 2 | 11–20 | The Java Memory Model, volatile & Safe Publication |
| 3 | 21–30 | synchronized, wait/notify & Liveness Failures |
| 4 | 31–40 | java.util.concurrent Locks, CAS & Atomics |
| 5 | 41–50 | Thread Pools & the Executor Framework |
| 6 | 51–58 | Coordination Utilities & Blocking Queues |
| 7 | 59–66 | Concurrent Collections |
| 8 | 67–76 | Future, CompletableFuture & Asynchronous Pipelines |
| 9 | 77–88 | ThreadLocal, Fork/Join, Parallel Streams & Virtual Threads |
| 10 | 89–100 | Concurrent Design Patterns, Debugging & Capstone |

---

# Section 1 — Thread Fundamentals & Lifecycle (Q1–Q10)

---

### Q1. What is the difference between a process and a thread, and what exactly does a Java thread map to at the OS level? Why is context switching between threads cheaper than between processes — and when is it still expensive?

**Answer:**

A **process** is an OS-level unit of resource ownership: its own virtual address space, file descriptors, security context. A **thread** is a unit of *execution scheduling* inside a process: its own program counter, register state, and stack — but sharing the process's heap, static fields, and open resources. That sharing is both the point (cheap communication through shared memory) and the entire source of concurrency bugs (shared mutable state).

Classic Java threads (`new Thread()`) are **platform threads** — each maps 1:1 to a native OS thread (`pthread` on Linux); the OS kernel schedules them preemptively across cores. Consequences: creating one costs a native stack reservation (default ~1 MB virtual, `-Xss` tunable), and the practical ceiling is thousands, not millions — which is precisely the problem virtual threads later solve (Q84).

**Context switching:** switching threads of the *same* process saves/restores registers and stack pointer but keeps the address space — no page-table switch, so TLB and much cache content stay warm. Process switches swap page tables → TLB flushes and cold caches. But thread switches are still far from free: 1–10+ µs each including the scheduler work, plus the *indirect* cost — cache pollution when the new thread evicts the old thread's working set. A system doing hundreds of thousands of switches/second (too many runnable threads, lock convoys, tiny tasks on many threads) burns a large fraction of CPU on switching — the reason "more threads" so often makes throughput *worse*, and the conceptual root of thread-pool sizing (Q45).

**Follow-up trap:** *"Who schedules Java threads — the JVM or the OS?"* — The OS, for platform threads; the JVM has no scheduler for them (priorities are mere hints passed down, Q7). Virtual threads flip this: the JVM schedules them onto carrier threads (Q84).

---

### Q2. Compare the ways to define a task/thread in Java: extending `Thread`, implementing `Runnable`, implementing `Callable`. Why is "implements Runnable" the standard answer, and where does `Callable` fit?

**Answer:**

```java
// 1. Extend Thread — couples the task to the thread mechanism
class Worker extends Thread { public void run() { … } }

// 2. Runnable — the task as data; no result, no checked exceptions
Runnable task = () -> process();
new Thread(task).start();

// 3. Callable — returns a value, may throw checked exceptions; runs via executors
Callable<Report> job = () -> buildReport();
Future<Report> future = executor.submit(job);
```

Why `Runnable`/`Callable` over extending `Thread`:

- **Separation of concerns:** the task (what to do) is decoupled from the execution mechanism (who runs it, when, on which pool). A `Runnable` can run on a raw thread, a pool, a scheduler, or inline in a test — an extended `Thread` cannot.
- **Composition over inheritance:** Java is single-inheritance; extending `Thread` spends it. Also `Thread` carries state/methods you don't want on a domain task.
- In modern code you almost never create raw `Thread`s at all — you submit tasks to an **executor** (Q41), which only accepts `Runnable`/`Callable`. Extending `Thread` is a red flag outside frameworks/demos.

**`Callable<V>` vs `Runnable`:** `call()` returns `V` and can throw checked exceptions; the executor wraps the outcome in a `Future<V>` — the exception is captured and re-thrown wrapped in `ExecutionException` at `future.get()` (Q67). `Runnable.run()` returns nothing and any exception escapes to the thread's uncaught-exception handler (or is silently swallowed into the `Future` if submitted — Q50's famous trap).

**Follow-up trap:** *"What happens if you both extend Thread with a run() body AND pass a Runnable to its constructor?"* — the overridden `run()` wins (it replaces the default implementation that would have delegated to the target Runnable). A neat check of whether you know `Thread.run()`'s default behavior: `if (target != null) target.run()`.

---

### Q3. Explain `start()` vs `run()` — everyone's first trick question — but go deeper: what exactly does `start()` do, why can't you call it twice, and what state transitions are involved?

**Answer:**

- **`run()` called directly** is a plain method call: the code executes synchronously **on the current thread**. No concurrency happens. The program still "works" in tests, which is why this bug survives review.
- **`start()`** asks the JVM to: (1) check the thread is in state NEW (else throw `IllegalThreadStateException`); (2) create the native OS thread (memory for its stack, kernel bookkeeping); (3) move it to RUNNABLE; (4) arrange for the new thread to invoke `run()` itself. `start()` returns immediately on the calling thread — the two threads then race, with **no ordering guarantee** about who does what first (except: everything the parent did *before* `start()` happens-before the child's first action — an actual JMM rule worth citing, Q13).

**Why not twice:** a `Thread` object represents one lifecycle. Once started, the state is no longer NEW — a second `start()` throws `IllegalThreadStateException`, even after the thread has died (TERMINATED is final; threads are not restartable). Wanting to "restart a thread" is the signal you should be using a pool: threads are infrastructure, tasks are the unit of reuse.

State transitions here: `NEW → (start) → RUNNABLE → … → TERMINATED` (full lifecycle in Q4). The `Thread` *object* is still a normal Java object after termination — you can read its name/state — it just can't run again.

**Follow-up trap:** *"Is the thread guaranteed to be running when start() returns?"* — No. RUNNABLE means *eligible*; the OS may not schedule it for an arbitrarily long time. Code that assumes the child has begun (e.g., reading a flag it sets) without synchronization is a race — this is how interviewers pivot from trivia into the JMM.

---

### Q4. Draw the Java thread state machine: all six `Thread.State` values, what moves a thread between them, and — the part that trips people — the difference between BLOCKED and WAITING, and where "running" is.

**Answer:**

Six states from `Thread.State`:

- **NEW** — created, not started.
- **RUNNABLE** — eligible to run *or actually running*: Java folds "ready" and "running" into one state (there is no RUNNING enum — the classic gotcha; the OS knows which threads are on-core, the JVM state doesn't distinguish). Also covers threads blocked in *native* I/O — a thread stuck in a socket read reports RUNNABLE, which routinely confuses thread-dump readers.
- **BLOCKED** — waiting to **acquire a monitor lock**: either to enter a `synchronized` block/method, or to *re-enter* one after `wait()` wakes. Nothing else puts you in BLOCKED. Not interruptible-out-of (interrupt sets the flag but the thread stays blocked).
- **WAITING** — parked indefinitely *by choice*, waiting for another thread's action: `Object.wait()` (no timeout), `Thread.join()` (no timeout), `LockSupport.park()`. Leaves via `notify/notifyAll`, the joined thread dying, `unpark`, or **interrupt** (throws `InterruptedException` for wait/join).
- **TIMED_WAITING** — same but with a deadline: `sleep(ms)`, `wait(ms)`, `join(ms)`, `parkNanos`, `tryLock(timeout)` etc.
- **TERMINATED** — `run()` returned or threw.

**BLOCKED vs WAITING — the interview core:** BLOCKED = involuntary, contending for a *monitor*, will proceed the instant the lock frees; WAITING = voluntary, waiting for a *condition/signal*, needs an explicit wake. In thread dumps: many BLOCKED on one monitor = lock contention/convoy; many WAITING = idle pool threads (normal) or a lost-signal/stuck-dependency bug. Also note: `ReentrantLock.lock()` contention shows as **WAITING** (it parks via `LockSupport`), *not* BLOCKED — monitor-specific semantics, and a genuinely senior detail for reading dumps (Q94).

**Follow-up trap:** *"Which states can an interrupt get a thread out of?"* — WAITING/TIMED_WAITING via `InterruptedException` (for wait/sleep/join and interruptible lock waits); NOT out of BLOCKED-on-monitor, and not out of RUNNABLE-in-native-I/O (except channels that are interruptible — NIO `InterruptibleChannel` closes on interrupt). Full interruption model in Q9.

---

### Q5. `sleep()` vs `wait()` — the perennial question, answered at depth: locks held, required context, who wakes them, and what class each belongs to and why that placement makes sense.

**Answer:**

| | `Thread.sleep(ms)` | `obj.wait(ms)` |
|---|---|---|
| Belongs to | `Thread` (static — always current thread) | `Object` (every monitor) |
| Requires | nothing | must **hold `obj`'s monitor** (else `IllegalMonitorStateException`) |
| Lock behavior | **keeps** any locks held | **releases `obj`'s monitor** while waiting (only that one!) |
| Wakes via | timeout, interrupt | `notify`/`notifyAll` on `obj`, timeout, interrupt, *spurious wakeup* |
| Purpose | pause execution (rate-limit, retry backoff) | **condition waiting**: "pause until some state changes" |

The two deepest points to articulate:

1. **Why `wait()` releases the lock:** the whole protocol is "wait until another thread changes shared state" — but the state is guarded by the same monitor; if the waiter kept it, the changer could never acquire it to make the change → guaranteed deadlock. Hence wait atomically (release monitor + park); on wake it must **re-acquire** the monitor (passing through BLOCKED) before returning. `sleep` has no associated condition, so it has no business releasing anything — sleeping while holding a lock is *legal and often a bug* (you stall every contender for the duration).
2. **Why `wait()` lives on `Object`:** any object can be a monitor (every Java object has a header with monitor support), and condition waiting is intrinsically tied to *a monitor*, not to a thread. You wait *on a lock's condition*; you sleep *as a thread*. That's the design rationale interviewers fish for.

Also: `wait()` releases only the monitor you call it on — if you hold two nested locks and wait on the inner, the outer stays held (a classic nested-monitor deadlock, Q28). And `sleep(0)`/`Thread.yield()` are scheduler hints with no guarantees — never correctness tools.

**Follow-up trap:** *"Why must `wait()` be called in a loop?"* — spurious wakeups + the condition may be consumed by another woken thread between notify and re-acquisition: `while (!condition) obj.wait();` — never `if`. Full treatment in Q25.

---

### Q6. What is a daemon thread? Explain the inheritance rule, what happens to daemons at JVM exit, and why "make it a daemon so the app can shut down" is a dangerous fix. Where do daemon threads legitimately appear in real services?

**Answer:**

The JVM exits when the last **non-daemon** (user) thread terminates. Daemon threads don't count toward keeping the JVM alive — when only daemons remain, the JVM **halts them abruptly**: no `InterruptedException`, no stack unwinding, **`finally` blocks do not run**, resources aren't closed. Mid-write file corruption and half-sent messages are the canonical casualties.

Rules: `setDaemon(true)` must be called **before `start()`** (after → `IllegalThreadStateException`); daemon status is **inherited** — a thread created by a daemon is a daemon by default (relevant for pools: `ThreadFactory` should set it explicitly rather than inherit whatever the creator was — Q44); `main` is a user thread.

**Why the "fix" is dangerous:** an app that won't exit has non-daemon threads still running — usually an executor nobody shut down. Marking them daemon makes exit *possible* by making termination *unsafe*: in-flight tasks are killed wherever they are. The correct fix is lifecycle management — `ExecutorService.shutdown()` + `awaitTermination` in a shutdown hook / Spring `@PreDestroy` (Q47). Daemon-ness is appropriate only for tasks that are safe to kill at any instruction.

**Legitimate daemons:** JVM housekeeping (GC threads, JIT compiler, reference handler/finalizer), watchdog/metrics/heartbeat reporters, cache-expiry sweepers — pure background upkeep where lost work is meaningless. Netty/Tomcat worker threads are notably *non*-daemon by design: they hold live work.

**Follow-up trap:** *"Do shutdown hooks run when daemons are killed?"* — Ordering: `System.exit`/last-user-thread-exit triggers shutdown **hooks** (which are threads themselves) → after hooks complete, JVM halts, killing daemons. So hooks run, daemons' finallys don't. `Runtime.halt()` skips even the hooks. Precise sequencing here reads as real operational knowledge.

---

### Q7. Thread priority, `yield()`, and `join()`: what does each actually guarantee? Include the `join()` memory-visibility guarantee and why priority-based designs are broken on mainstream OSes.

**Answer:**

- **Priority** (`setPriority(1..10)`): a *hint* mapped onto the OS scheduler's priority model — Linux largely flattens the 10 levels (niceness mapping is coarse and often disabled for unprivileged processes), Windows maps them differently. **No JLS guarantee of any scheduling behavior.** Designs encoding correctness in priorities ("high-priority thread will preempt and handle X first") are nonportable and invite **priority inversion** — a low-priority thread holding a lock needed by a high-priority one, while medium-priority threads starve the low one so it can never release (the Mars Pathfinder story is the interview-friendly citation; OSes mitigate with priority inheritance — Java monitors don't). Professional stance: leave priorities alone; encode urgency in *queue design* (separate pools, priority queues) not thread priority.
- **`yield()`**: "scheduler, I'm willing to give up my slice" — may do absolutely nothing; the spec permits it to be a no-op. Legitimate uses are almost none in application code (micro-optimization inside spin-wait loops, benchmarking); its appearance usually flags someone trying to "fix" a race with timing (Q92).
- **`join()`**: current thread waits until the target TERMINATES (WAITING/TIMED_WAITING meanwhile; interruptible; `join(ms)` returns silently on timeout — check `isAlive()` after, a subtle bug source). The part that separates candidates: **join is a happens-before edge** — *all* writes performed by the joined thread are visible to the joiner after `join()` returns, with no additional synchronization. Same family: `start()` publishes the parent's prior writes to the child. These two rules are why simple fork/join code with no volatiles is still correct (Q13).

```java
Thread t = new Thread(() -> result = compute());   // plain field, no volatile
t.start();
t.join();
use(result);            // guaranteed visible & correct — HB via start()+join()
```

**Follow-up trap:** *"Implement join yourself?"* — it's essentially `synchronized(t) { while (t.isAlive()) t.wait(); }` — the runtime calls `notifyAll()` on the Thread object at death. Which is also why you should **never** `wait()`/`notify()` on a Thread object yourself — you'd collide with the runtime's protocol.

---

### Q8. Exceptions in threads: what happens to an uncaught exception thrown inside a thread's `run()`? Explain `UncaughtExceptionHandler` at the three levels, and the notorious difference between `execute()` and `submit()` on a pool.

**Answer:**

An exception escaping `run()` terminates **that thread only** — it does not propagate to the parent, the thread that called `start()`, or main. The JVM then consults, in order: (1) the thread's own `setUncaughtExceptionHandler`, (2) its `ThreadGroup` (whose default implementation delegates upward and finally prints), (3) the static `Thread.setDefaultUncaughtExceptionHandler`. Default outcome: stack trace to stderr, thread dies, program continues — often silently degraded (a worker died and nobody noticed).

```java
Thread.setDefaultUncaughtExceptionHandler((t, e) ->
    log.error("Thread {} died", t.getName(), e));   // baseline hygiene for any service
```

**The pool trap** — recite this precisely:

- `executor.execute(runnable)`: an escaping exception reaches the **worker thread's uncaught handler** (visible in logs, if configured). The pool replaces the dead worker with a fresh thread (worker replacement is automatic — small but real churn cost).
- `executor.submit(anything)`: the task is wrapped in a **`FutureTask`**, which **catches everything** and stores it as the Future's outcome. Nothing is logged, no handler fires. If nobody calls `future.get()`, the exception **vanishes without a trace** — the single most common "our job has been failing for three weeks" story in Java backends.

Mitigations: always consume futures; or override `ThreadPoolExecutor.afterExecute` (must unwrap the `FutureTask` case); or wrap task bodies in try/catch-log; `CompletableFuture` pipelines need `exceptionally`/`whenComplete` terminals for the same reason (Q72). For `ScheduledExecutorService`, worse: an exception **cancels all future runs** of that periodic task, silently (Q49).

**Follow-up trap:** *"Where does an exception in a `CompletableFuture.supplyAsync` go?"* — into the future's completion (completed exceptionally), wrapped in `CompletionException` for downstream stages — same swallow-if-unobserved hazard, chained handling in Q72.

---

### Q9. The Java interruption model: what does `interrupt()` actually do, `isInterrupted()` vs `Thread.interrupted()`, the correct pattern for handling `InterruptedException`, and why `Thread.stop()` was deprecated.

**Answer:**

`interrupt()` is **cooperative cancellation**: it sets a per-thread boolean flag and, if the target is parked in an interruptible wait (`sleep/wait/join`, interruptible lock/queue ops, `InterruptibleChannel` I/O), wakes it by throwing **`InterruptedException`** — which also **clears the flag** (crucial!). It does not stop the thread; a thread that never checks never notices (a CPU-bound loop is uninterruptible unless it polls `isInterrupted()` — that's *your* responsibility in long computations).

- `isInterrupted()` — instance method, reads without clearing.
- `Thread.interrupted()` — **static**, reads current thread's flag **and clears it**; misusing it as a check (e.g., in a condition twice) loses the interrupt. Interviewers love this pair.

**Handling rules** — the only two legitimate responses to `InterruptedException`:

```java
// A. You own the thread's policy (top-level loop): honor it — exit cleanly
try { queue.take(); } catch (InterruptedException e) { running = false; }

// B. You're library/intermediate code: RESTORE the flag and get out
catch (InterruptedException e) {
    Thread.currentThread().interrupt();   // re-set — callers up-stack must see it
    throw new UncheckedIOException(...);  // or return/cleanup
}
```

The crime is **swallowing** it (`catch (InterruptedException e) {}` or just logging): the cancellation signal is destroyed; shutdowns hang; `shutdownNow()` (which works *by interrupting workers*, Q47) stops working. "Restore the interrupt status" is the phrase to say.

**Why `stop()` (and `suspend/resume`) died:** `stop()` threw `ThreadDeath` asynchronously at **any** instruction, releasing all monitors *mid-critical-section* — other threads then observe objects in half-mutated, invariant-broken states, with no way to write safe code against it. `suspend()` parked a thread *while keeping its locks* → trivial deadlocks. Cooperative interruption is the replacement: cancellation only at points the code declares safe. (These methods now throw `UnsupportedOperationException` in current JDKs — fully removed behavior, a nice currency note.)

**Follow-up trap:** *"How do you cancel a task blocked in a socket read?"* — plain `InputStream` reads ignore interrupts: close the socket from another thread (forces an IOException), use NIO interruptible channels, or timeouts. "Interruption doesn't cover all blocking" is the mature caveat.

---

### Q10. What is a race condition, precisely? Distinguish *data race* from *race condition*, give the two canonical shapes (check-then-act, read-modify-write), and explain why `count++` is broken even on a single-core machine.

**Answer:**

- A **data race** (JMM term): two threads access the same memory location concurrently, at least one write, with **no happens-before ordering** — the program is incorrectly synchronized and reads may see stale/inconsistent values (Q11).
- A **race condition** (design term): correctness depends on the *relative timing/interleaving* of operations. You can have a race condition with zero data races (every access synchronized, yet the composite logic is wrong) — that distinction is a senior marker:

```java
// No data race (ConcurrentHashMap is thread-safe) — still a race condition:
if (!map.containsKey(k))       // check
    map.put(k, expensive());    // act — two threads both pass the check → double compute
// Fix: the ATOMIC composite — map.computeIfAbsent(k, key -> expensive())
```

**Canonical shapes:** **check-then-act** (above; also lazy init, "if file exists then read", singleton without synchronization) and **read-modify-write**:

```java
count++;    // three steps: read count → add 1 → write count
```

Two threads both read 10, both write 11 → one increment lost. **Single core doesn't save you:** the thread can be preempted *between* the read and the write — interleaving, not parallelism, is the enemy. (At bytecode level it's getfield/iadd/putfield; JIT compilation doesn't make it atomic either.) Additionally, without synchronization there's a *visibility* problem stacked on the atomicity problem — another thread may not see the write at all (Q12). Fixes by intent: `AtomicInteger.incrementAndGet()` (CAS, Q38), `synchronized`, or `LongAdder` under heavy contention (Q39).

The general law to state: **thread-safe components do not compose into thread-safe logic** — every "if X then do Y on shared state" needs its atomicity designed (atomic compound operations à la `computeIfAbsent`/`putIfAbsent`, or a lock spanning the whole invariant).

**Follow-up trap:** *"Is `long`/`double` assignment atomic?"* — Historically non-volatile 64-bit reads/writes could tear into two 32-bit halves (JLS permitted it); `volatile long` was always atomic. Java 17+ (JEP memory model updates hardened on 64-bit JVMs) makes tearing practically extinct, but the JLS-level answer plus "volatile fixes it" is what's being tested — and word tearing is a great segue into the JMM section.

---

# Section 2 — The Java Memory Model, volatile & Safe Publication (Q11–Q20)

---

### Q11. What problem does the Java Memory Model (JMM) exist to solve? Explain the two hardware/compiler realities it abstracts — caching/visibility and reordering — and what "happens-before" means as the contract.

**Answer:**

Without a memory model, "what does a read see?" has no portable answer, because two realities intervene between your source code and RAM:

1. **Caching/visibility:** each core works against its own cache hierarchy and store buffers. A write by core A sits in a store buffer/L1 before reaching coherence visibility; a read by core B may be served from B's stale cache line or a register the JIT hoisted the value into. Without synchronization, there is **no guarantee a write is ever seen** by another thread (the JIT may legally compile `while (!flag) {}` into `if (!flag) while(true) {}` — hoisting the read — the infinite-loop demo every JMM talk uses).
2. **Reordering:** compilers (javac barely, **JIT heavily**) and CPUs reorder independent operations for speed — as long as *single-threaded* semantics are preserved ("as-if-serial"). Another thread, watching from outside, can observe effects in an order that never appears in your source.

The JMM (JLS §17, the JSR-133 model) defines the contract: a read is guaranteed to see a write **only if** the write *happens-before* the read. **Happens-before (HB)** is a partial order built from: program order within a thread, plus **synchronization edges** — monitor unlock → subsequent lock of the same monitor; volatile write → subsequent volatile read of the same field; `start()` → child's actions; child's actions → `join()` return; interrupt → detection; and transitivity. Correctly synchronized programs (no data races) get **sequential consistency** — you can reason as if operations interleave in some global order. Racy programs get weak, surprising-but-bounded semantics.

The mental shift to articulate: synchronization is not (only) about mutual exclusion — it's about **ordering and visibility**. A lock's release "publishes" everything before it; the acquire "subscribes." Volatile does the same without exclusion.

**Follow-up trap:** *"Does happens-before mean 'executes before'?"* — No: it means *visibility/ordering guarantee*, not wall-clock order; and its absence doesn't mean reordering will happen — it means you may not assume it won't. Races are allowed to look correct in every test you run (Q92).

---

### Q12. Explain `volatile` completely: the two guarantees, what it does NOT guarantee, the canonical flag example, and what actually happens at the hardware level (fences, MESI — sketch level).

**Answer:**

`volatile` gives exactly two guarantees:

1. **Visibility:** a write to a volatile field is visible to every subsequent read of that field (volatile write HB volatile read of the same variable).
2. **Ordering:** volatile accesses can't be reordered with each other, and — the JSR-133 strengthening — a volatile **write acts as a release** (no prior operation moves below it) and a volatile **read acts as an acquire** (no later operation moves above it). Consequence: when the reader sees the volatile write, it also sees **everything the writer did before it** — even writes to *non*-volatile fields. Volatile is a publication mechanism, not just a per-variable modifier — most candidates miss this.

```java
private volatile boolean shutdown;              // canonical use: state flag
public void stop() { shutdown = true; }
public void run() { while (!shutdown) { work(); } }   // guaranteed to terminate
```

**Not guaranteed: atomicity of compound actions.** `volatile int c; c++` is still read-modify-write — lost updates remain (Q10). Volatile is right for: status flags, publish-once references (config snapshots — write a fully-built immutable object into a volatile field; readers get all of it), the DCL singleton's field (Q16). Wrong for counters, check-then-act, or any multi-variable invariant.

**Hardware sketch (enough for interviews):** cores keep caches coherent via **MESI**-style protocols — a write invalidates other cores' copies of that cache line; readers re-fetch — so *coherence exists anyway*; what volatile really adds is **ordering discipline**: the JIT emits the field access with acquire/release semantics and inserts **memory fences** where the target CPU needs them (x86 is strong — mainly a StoreLoad barrier, e.g. a `lock`-prefixed instruction after volatile stores; ARM is weak — explicit load-acquire/store-release instructions), and it forbids *compiler* optimizations like hoisting the read out of the loop. "Volatile mainly constrains the compiler and orders the store buffer; MESI already handles raw propagation" is a much stronger answer than "it writes to main memory directly" — which is folklore.

**Follow-up trap:** *"volatile vs synchronized in one line?"* — volatile = visibility + ordering, no atomicity, no blocking, cheap; synchronized = all of that plus mutual exclusion, at blocking cost. Then: *"Can volatile replace locking for a 64-bit value?"* — for single reads/writes yes (no tearing), for increments no.

---

### Q13. List the practical happens-before rules every Java developer should know, and for each give the one-line reason it lets real code omit explicit synchronization.

**Answer:**

The working set (beyond program order + transitivity):

1. **Monitor unlock → later lock (same monitor):** everything done inside/before a `synchronized` block is visible to the next thread entering it — why guarded state needs no extra volatiles.
2. **Volatile write → later read (same field):** publication flags/references (Q12).
3. **`Thread.start()` → everything in the child:** you can build objects, then start the worker that uses them — no sync needed for the handoff (constructor-fill-then-start pattern).
4. **Everything in a thread → `join()` return on it:** fork, compute into plain fields, join, read — correct with zero volatiles (Q7).
5. **`executor.submit()`-style handoffs:** actions before submitting HB the task's execution; task's completion HB `Future.get()` return — the `j.u.c.` contract ("memory consistency effects" paragraphs in the javadoc) — why you can pass mutable task inputs/outputs through executors safely without locks. Same for putting into a `BlockingQueue` HB taking that element.
6. **Interrupt → detection of interrupt; thread termination → `isAlive()==false`/join.**
7. **Final-field semantics (special, not exactly HB):** if the object is *safely constructed* (no `this` escape), any thread that sees the reference sees correctly-initialized final fields — even without synchronization (Q15).
8. **Class initialization:** static initializers HB any use of the class — the enum/holder singleton correctness basis (Q16).

The exam skill is applying transitivity: writer thread sets `data = …` (plain), then `flag = true` (volatile); reader sees `flag == true` (volatile read) → HB chains program order + volatile edge + program order → reader sees `data`. One volatile publishes a cart-load of plain state — and, inversely, checking a *plain* flag establishes nothing.

**Follow-up trap:** *"Does `System.out.println` / sleeping 'fix' visibility?"* — Any observed fix is accidental (println's internal lock creates incidental edges only if both threads pass through it; sleep just changes timing). "It works with a print statement" is the fingerprint of a data race, not of correctness.

---

### Q14. What is safe publication? Show the four safe idioms, one broken publication with its possible outcomes, and connect it to why immutable objects are the easiest things to share.

**Answer:**

**Publication** = making an object reference visible to other threads. **Safe** publication ensures the object is seen *fully constructed* — reference visibility and state visibility arrive together. Broken publication:

```java
public Holder holder;                    // plain field
public void init() { holder = new Holder(42); }   // thread A
// thread B: if (holder != null) holder.assertSanity();
```

Because `new` compiles to allocate → construct → assign-reference, and the JIT/CPU may **reorder the reference-store before the field-stores**, thread B can observe a non-null `holder` whose fields are still default values (0/null) — "partially constructed object." B might also see the reference and *never* see it, or see stale field values arbitrarily later. All are legal outcomes of the race.

**The four safe idioms** (Java Concurrency in Practice's list — citing it lands well):

1. Publish via **static initializer** (class-init HB rule): `static Holder h = new Holder(42);`
2. Store into a **volatile field** (or `AtomicReference`): release/acquire ordering carries construction.
3. Store into a field **guarded by a lock** — including into thread-safe collections: `ConcurrentHashMap.put`, `BlockingQueue.put` are publication points by contract (Q13.5).
4. **Final fields + safe construction**: an object whose state is in `final` fields, with no `this`-escape during construction, may be published *any* way — the JMM's final-field freeze guarantees initialized values (Q15).

Idiom 4 generalizes into the master rule: **immutable objects (final fields, no post-construction mutation) are automatically thread-safe and can be shared through data races unharmed** — which is why the modern style (records, immutable DTOs/config snapshots swapped via one volatile reference) sidesteps most of this section in practice. Effectively-immutable objects (never mutated after publication) need safe publication once, then are free.

**Follow-up trap:** *"Is `holder = new Holder(42)` fixed by making `Holder`'s field final?"* — Yes (idiom 4) — and that's exactly the DCL-without-volatile subtlety in Q16. Interviewers chain these two questions deliberately.

---

### Q15. Final fields in the JMM: what exactly does the "freeze" guarantee promise, what is `this`-escape, and why can a `String` be safely shared across threads through a data race?

**Answer:**

**The guarantee:** if an object is constructed without letting `this` escape, then when any other thread obtains a reference to it — *even via an unsynchronized, racy read* — it will see the **final fields' constructor-assigned values**, and everything reachable *through* those final fields as of construction (the transitive closure: a `final int[]` guarantees the array contents written in the constructor are visible). Mechanically: a "freeze" at constructor end; JIT emits a store-store barrier before publishing the reference (free on x86; real instruction on ARM). Non-final fields of the same object enjoy no such promise — through a race they may appear as defaults.

**`this`-escape voids the warranty:** if the constructor publishes `this` before completing — registering itself as a listener, starting a thread from the constructor, storing `this` into a static/shared field, calling an overridable method that a subclass uses to leak it — another thread can see the object *pre-freeze*, and even final fields may look uninitialized:

```java
public Foo() {
    EventBus.register(this);      // ESCAPE — bus thread may use half-built Foo
    this.rate = computeRate();    // assigned after escape
}
// Fix: private constructor + static factory that registers AFTER construction
```

("Don't start threads or register listeners in constructors" is the reviewable rule; factory methods are the pattern.)

**Why `String` is share-safe:** its fields (`value` array, hash cache aside) are final and it's deeply immutable — so even a maliciously racy publication of a String can never show torn contents. This underpins security-sensitive code: you can't TOCTOU-mutate a String someone validated. (The `hash` field is a benign race by design: non-final, may be computed twice by racing threads, same deterministic value — "benign race" is a term worth dropping, with the caveat that *almost* nothing you write qualifies.)

**Follow-up trap:** *"Do final fields make setters impossible via reflection?"* — Reflection/`Unsafe` can mutate finals (frameworks do), and doing so forfeits JMM guarantees (JIT may have constant-folded the value). Records + strong encapsulation are tightening this; conceptually: the *model* guarantee assumes finals are truly final.

---

### Q16. Dissect the double-checked locking singleton: why the naive version is broken, why `volatile` fixes it, and rank the alternatives (eager, holder idiom, enum) as a modern engineer would.

**Answer:**

```java
class Singleton {
    private static volatile Singleton instance;      // volatile is load-bearing
    static Singleton getInstance() {
        Singleton local = instance;                  // 1st check (no lock)
        if (local == null) {
            synchronized (Singleton.class) {
                local = instance;                    // 2nd check (with lock)
                if (local == null) instance = local = new Singleton();
            }
        }
        return local;
    }
}
```

**Why the non-volatile version is broken:** `instance = new Singleton()` can publish the reference **before** the constructor's field-stores (Q14's reordering). A second thread hits the *first* check, sees non-null, skips the lock entirely, and uses a half-initialized singleton. The synchronized block protects writers from each other but the fast-path read has no HB edge — that asymmetry is the whole bug. **`volatile` fixes it** by making the assignment a release and the first check an acquire: see the reference ⇒ see the construction. (The local-variable copy is a micro-optimization: one volatile read instead of two.)

**Modern ranking:**

1. **Eager init** (`static final Singleton I = new Singleton();`) — correct by class-init HB, trivially simple. Default choice; "lazy" is usually a premature optimization since the class loads on first use anyway.
2. **Initialization-on-demand holder** — lazy *and* lock-free, using the classloader's guarantees (class init is synchronized and happens-once):

```java
private static class Holder { static final Singleton I = new Singleton(); }
static Singleton getInstance() { return Holder.I; }
```

3. **Enum singleton** — correctness plus serialization/reflection resistance; slightly odd stylistically for stateful services.
4. **DCL** — correct with volatile, but it's the most error-prone spelling of the same result; justify it only for expensive init with non-static context. And in a Spring shop, the honest top answer: the container manages singletons; hand-rolled lazy singletons are mostly interview artifacts.

**Follow-up trap:** *"Is DCL fixed if Singleton's fields are all final?"* — Sneaky: final-field semantics protect the *fields*, and modern JMM analysis says the object's finals would be seen initialized — but the `instance` reference itself is still read racily (could be seen null again later? no — but stale-null forever is possible in theory: the read may miss the write). Answer: it removes the half-constructed hazard but not the visibility guarantee for the reference; volatile remains the stated requirement. Showing you can reason at that grain is the point.

---

### Q17. What does "immutable" require in Java, strictly? Effective immutability, defensive copies, records, and why immutability is called the most powerful concurrency strategy.

**Answer:**

Strict immutability (the JCiP definition): (1) state cannot be modified after construction; (2) **all fields are `final`** (for the JMM freeze — Q15 — not just style); (3) the object is **safely constructed** (no `this`-escape). Plus the practical clauses people forget:

- **Deep immutability:** a `final List<Item>` field to a mutable list is a mutable object wearing a coat — you need `List.copyOf(...)` at the boundary (**defensive copy in**) and no accessor leaking internals (**copy or unmodifiable view out**). Same for `Date`/arrays (arrays can never be immutable — copy or switch types).
- The class should be non-subclassable for these guarantees to hold at the type level (`final` class or private constructors).

```java
public record Money(BigDecimal amount, Currency currency) { }   // final fields, final class
```

**Records** give you the shape (all-final, no setters) — but not deep immutability (`record Basket(List<Item> items)` still needs `items = List.copyOf(items)` in a compact constructor — a favorite nitpick).

**Effectively immutable:** technically mutable but never mutated after publication (e.g., a `HashMap` built once at startup). Safe **if safely published once** (Q14) and the never-mutate discipline holds — a discipline, not a compiler guarantee.

**Why "most powerful":** immutable objects need **no synchronization ever** — share freely, cache without invalidation protocols, iterate without `ConcurrentModificationException`, hand to any thread. Whole bug classes vanish. The architecture that follows: mutable *identity* holding immutable *values* — one `volatile Config current;` swapped atomically to a freshly-built immutable snapshot; readers grab the reference once and see a consistent world (copy-on-write at the design level, Q60's collection is the same idea). Functional-style transformation (old snapshot → new snapshot → CAS/volatile swap) replaces field-by-field locking.

**Follow-up trap:** *"Immutability's cost?"* — allocation/GC pressure from copies (usually cheap with modern GCs; persistent data structures amortize), and awkwardness for genuinely high-rate mutation (counters → LongAdder, not snapshots). Knowing where it stops is part of the answer.

---

### Q18. `ThreadLocal` as a memory-model concept: what problem does it solve, how is it implemented (which object owns the map?), the initialValue/withInitial idiom — and the famous leak mechanics in thread pools and webapps.

**Answer:**

`ThreadLocal<T>` gives each thread its **own independent copy** — sharing avoided rather than synchronized. Legit uses: per-thread non-thread-safe tools (`SimpleDateFormat` historically — today just use immutable `DateTimeFormatter`, say so), per-request context (security principal, tenant, trace/MDC ids), per-thread buffers/RNG (`ThreadLocalRandom`), transaction contexts (Spring's transaction synchronization — connecting to your JPA knowledge: `TransactionSynchronizationManager` is ThreadLocals all the way down).

**Implementation — the direction matters:** each **`Thread` owns a `ThreadLocalMap`**; the `ThreadLocal` object is the *key* (not a map from threads inside the ThreadLocal — inverted from most people's guess). Entries are `Entry extends WeakReference<ThreadLocal<?>>` with a **strong** reference to the value. `get()` = `currentThread().threadLocalMap.entry(this)` — no contention, no locking, by construction.

```java
static final ThreadLocal<StringBuilder> BUF =
    ThreadLocal.withInitial(() -> new StringBuilder(1024));
```

**Leak mechanics** (the interview centerpiece): the key is weak, the **value is strong**. If the `ThreadLocal` object becomes unreachable, the key is collected → "stale entry" with key `null` but value still strongly held **by the thread**. In a **thread pool**, threads live ~forever → values accumulate; per-request data (a user's whole object graph) survives into unrelated requests — both a leak and a **data-bleed/security bug** (request B sees request A's principal). The map purges stale entries only opportunistically on other accesses — not reliably. In **webapp redeploys**, values referencing classes from the old classloader pin the entire classloader → PermGen/Metaspace leaks (Tomcat's famous warnings).

**The rule:** whoever sets must **`remove()` in `finally`** — typically a servlet filter/interceptor:

```java
try { CTX.set(requestContext); chain.doFilter(req, res); }
finally { CTX.remove(); }
```

Declare ThreadLocals `static final` (instance-field ThreadLocals multiply entries and worsen leaks).

**Follow-up trap:** *"Why weak keys at all?"* — so an abandoned ThreadLocal *can* eventually be purged rather than pinned forever by every thread's map; it's mitigation, not absolution — `remove()` remains mandatory. `InheritableThreadLocal` and context loss on pools → Q77.

---

### Q19. Atomic publication tools beyond volatile: `AtomicReference` snapshots and the copy-then-CAS update loop; when do you need `VarHandle`/explicit fences, and what are acquire/release/opaque/plain access modes (survey level)?

**Answer:**

**`AtomicReference` = volatile reference + CAS.** The lock-free *update* pattern for shared immutable state:

```java
private final AtomicReference<Config> config = new AtomicReference<>(initial);

public void updateRateLimit(int newLimit) {
    config.updateAndGet(cur -> cur.withRateLimit(newLimit));   // retry loop inside
}
```

`updateAndGet` runs read → pure function → `compareAndSet`, retrying on interference. Rules: the function must be **side-effect-free** (it may run multiple times!) and cheap; contention pathology → backoff or a lock. This "immutable snapshot + CAS swap" is the idiomatic lock-free state machine and pairs with Q17. CAS semantics themselves and ABA → Q38.

**`VarHandle`** (Java 9+, replacing `Unsafe` for civilians): performs atomic/ordered operations on *plain fields and array elements* with an explicit **access-mode ladder** — the modern JMM vocabulary interviewers increasingly probe at survey level:

- **plain** — no ordering (normal field access);
- **opaque** — coherent per-variable access (no tearing, eventually visible; no cross-variable ordering) — progress-flag reads in loops;
- **release/acquire** — one-way publication ordering: release-store pairs with acquire-load; weaker & cheaper than volatile (no total order across variables — no StoreLoad fence);
- **volatile** — full sequentially-consistent semantics, plus `compareAndSet`, `getAndAdd` etc. on the field directly (this is how `Atomic*` classes are built; `AtomicXFieldUpdater` is the older reflective variant for saving per-object wrapper memory).

`VarHandle.fullFence()/acquireFence()/releaseFence()` exist for fence-style reasoning; needing them in application code is a smell — they belong in data-structure libraries. The honest senior positioning: "I use volatile/Atomic for app code; I can *read* release-acquire code and know why a library chose `setRelease` over volatile — to skip the expensive StoreLoad barrier on the hot path."

**Follow-up trap:** *"Why is volatile more expensive than release/acquire?"* — volatile's total-order guarantee needs the StoreLoad fence after stores (draining the store buffer — the priciest barrier on x86); release/acquire only constrain one direction and are nearly free on x86/ARM64 loads. Being able to name *which* fence costs is deep-cut credibility.

---

### Q20. A concept panel: explain (a) why "synchronized fixes visibility even for reads of variables written elsewhere" is subtly wrong, (b) lock coarsening/elision and biased-locking history as JIT realities, and (c) what a memory-consistency bug looks like in production and why it evades tests.

**Answer:**

**(a) The same-monitor rule:** the HB edge is unlock → lock **of the same monitor**. A writer synchronizing on `lockA` and a reader synchronizing on `lockB` (or reading unsynchronized) have **no edge** — the reader can see stale state despite "using synchronized." Guard each piece of state by exactly **one** designated monitor, document it (`@GuardedBy("lock")`), and audit every access path — read paths included: an unsynchronized getter beside a synchronized setter is the everyday spelling of this bug ("synchronized on writes only" ≈ broken).

**(b) JIT lock realities:** the JIT may **coarsen** adjacent lock regions (merge repeated lock/unlock on the same monitor in a loop into one), and **elide** locks entirely via escape analysis when an object provably never escapes a thread (locking a local `StringBuffer` costs ~nothing). **Biased locking** — historically, an uncontended monitor was "biased" to its first thread making reentry nearly free; it was **disabled by default in JDK 15 and removed in JDK 18+** (maintenance cost, revocation storms, modern uncontended CAS being cheap) — knowing that timeline is a strong currency signal. Net message: uncontended `synchronized` is *cheap* (thin-lock CAS on the object header/monitor word — full mechanics Q21); the expensive thing is **contention**, not the keyword.

**(c) Memory-consistency bugs in production:** symptoms — a config value "half-applied"; a rare NPE on a field "that can't be null" (partially published object, Q14); a worker that never notices a stop flag (hoisted read, Q12); a stat counter drifting a little low (lost updates). Character: **non-deterministic, load-dependent, architecture-dependent** (shows on ARM, invisible on strong-ordered x86 — turns up when you move to Graviton — a very now war story), and *unprovoked by tests* because JIT tiers, timing, and single-machine runs rarely produce the bad interleavings; adding logging perturbs timing and "fixes" it (Heisenbug). Defenses are structural, not test-based: minimize shared mutable state, immutability + safe publication idioms, `@GuardedBy` discipline and reviews, jcstress for suspect data structures, and treating every "it only fails in prod at peak" visibility anecdote as a race until proven otherwise (debugging playbook: Q92–94).

**Follow-up trap:** *"Can you fix visibility by making everything volatile?"* — It fixes per-field visibility, not compound atomicity or multi-field invariants, adds cost on hot paths, and signals unstructured sharing. The cure is architecture (confinement/immutability/queues — Q89), not keyword sprinkling.

---

# Section 3 — synchronized, wait/notify & Liveness Failures (Q21–Q30)

---

### Q21. How does `synchronized` actually work? Object headers, monitorenter/monitorexit, thin vs inflated locks, and what "every Java object is a monitor" means in memory.

**Answer:**

Every Java object carries a **header** containing the *mark word*, which multiplexes hash code, GC age, and **lock state**. `synchronized(obj)` compiles to `monitorenter`/`monitorexit` bytecodes (synchronized *methods* instead set the `ACC_SYNCHRONIZED` flag — same semantics, locking `this` or the `Class` object).

Lock state ladder (post-biased-locking JDKs — Q20b):

1. **Unlocked** — mark word holds hash/age bits.
2. **Thin/lightweight lock** — on uncontended acquisition, the thread CASes a pointer to a lock record in its own stack frame into the mark word. Reentry and uncontended exit are cheap CAS-level operations, no OS involvement. This is why "synchronized is slow" is 2005 folklore — uncontended it's nanoseconds.
3. **Inflated/heavyweight monitor** — first *contention* (or `wait()` use, or hash-code conflict with the thin encoding) inflates the lock into a full `ObjectMonitor` structure: owner field, **entry list** (threads BLOCKED trying to enter), **wait set** (threads that called `wait()`), recursion count for reentrancy. Contended acquisition typically spins adaptively a little (hoping the owner exits fast), then parks the thread via the OS (futex on Linux) — the expensive path: context switches, convoys.

**Reentrancy:** the monitor records owner + recursion count — the same thread entering nested synchronized blocks on the same object just increments the count. Without reentrancy, a synchronized method calling another synchronized method on `this` (or a subclass calling `super`'s synchronized method) would self-deadlock — reentrancy is per-*thread*, not per-call.

Memory semantics ride along: monitorexit is a release, monitorenter an acquire (Q13.1). And *criticality of the chosen object*: `synchronized` on `this`/public objects lets outside code lock your monitor and interfere (lock on a `private final Object lock = new Object();` — the private lock idiom, Q23).

**Follow-up trap:** *"What happens to the hash code when a lock inflates?"* — the identity hash and lock bits share the mark word; computing identity hash on a thin-locked object forces inflation (the monitor stores the displaced header). A genuinely deep-cut detail — even partial knowledge ("they share the mark word, so there's a conflict to resolve") scores.

---

### Q22. Instance-level vs class-level locking: `synchronized` method vs `static synchronized` vs synchronized blocks. Two threads call different methods — which combinations block each other? Why is "synchronized keyword makes the class thread-safe" wrong?

**Answer:**

- Instance `synchronized` method → locks **`this`** (that instance's monitor).
- `static synchronized` method → locks the **`Class` object** (`Foo.class`) — a completely different monitor.
- Block form → locks whatever you name: `synchronized(this)`, `synchronized(Foo.class)`, `synchronized(lockObj)`.

Interference matrix (must be instant): two threads on the **same instance**'s synchronized methods → mutually exclusive. **Different instances** → no interaction whatsoever (different monitors — "synchronized" protects an object, not code). Instance-synchronized vs static-synchronized → **never** block each other (different monitors!) — so an instance method mutating static state under `this`'s lock and a static method under the class lock is a data race dressed in synchronized keywords. Static state needs the class-level (or one shared private) lock, always.

Why "the class is now thread-safe" is wrong, layer by layer:

1. Only *marked* methods participate — one unsynchronized getter reintroduces the visibility/exclusivity hole (Q20a).
2. **Compound operations across calls** remain racy: `if (map.containsKey(k)) map.get(k)` on a fully synchronized map is still check-then-act (Q10); clients need their own lock spanning the sequence — this is why `Collections.synchronizedMap` requires manual locking around iteration (Q63).
3. Invariants spanning **multiple objects** need one lock covering all of them, not each object's own.
4. Lock granularity: `this`-locking couples your internal protection to a publicly reachable monitor — any client can `synchronized(yourObject)` and stall you (accidentally or maliciously).

```java
private final Object lock = new Object();       // private lock idiom
public void transfer(...) { synchronized (lock) { … } }
```

**Follow-up trap:** *"Constructor synchronized?"* — illegal (and meaningless: no other thread can see the object during proper construction — unless you leak `this`, which synchronization wouldn't fix — Q15). Also asked: locking on `String` literals or boxed integers — interned/cached shared instances make unrelated code share monitors; never lock on strings/boxed primitives.

---

### Q23. Choosing what to lock on and how long: lock scope, granularity, striping, and the rules for what makes a good lock object. Include "don't hold locks while calling alien methods."

**Answer:**

**Lock object rules:** a dedicated `private final Object` (or `ReentrantLock`) per guarded state-cluster; never `this`/public objects (external interference), never Strings/boxed primitives (shared instances — Q22), never a mutable field that can be reassigned mid-flight (`synchronized(config)` where `config = newConfig` elsewhere → two threads lock *different* objects and both proceed — the reassigned-lock bug is a beautiful interview trap).

**Scope (hold time):** hold locks for the shortest span covering the invariant — compute inputs before, do I/O after, keep the critical section to the mutation:

```java
Data d = expensivePrep(input);           // outside
synchronized (lock) { state.apply(d); }  // tiny critical section
notifyListeners();                       // outside — ALIEN CODE never under lock
```

**"Alien methods"** (JCiP term): callbacks, listeners, logging appenders, anything overridable or injected — calling them while holding your lock invites deadlock (the callee may lock something whose holder wants your lock — cross-lock cycle you can't see from your code) and unbounded hold times. Snapshot state, release, then call out — the open-call pattern.

**Granularity:** one coarse lock is simple but serializes everything (contention, convoying); per-field/per-entity locks scale but multiply deadlock ordering obligations (Q28). **Lock striping** is the middle path: N locks, key-hashed —

```java
private final Object[] stripes = new Object[16];
Object stripeFor(K key) { return stripes[(key.hashCode() & 0x7fffffff) % 16]; }
```

— contention drops ~16×; the cost is that whole-structure operations (size, clear, multi-key invariants) now need all stripes (in fixed order!). This is historically how `ConcurrentHashMap` scaled (segments pre-Java-8 → per-bin now, Q59), and describing striping as "the idea CHM industrialized" ties the bow.

Also name **lock convoys** (threads queueing behind a hot lock, throughput collapsing to sequential + switching overhead) as the symptom that triggers granularity work — and the alternatives ladder: narrower sections → striping → read/write locks (Q35) → lock-free/immutable designs (Q17/38).

**Follow-up trap:** *"Is logging under a lock really a problem?"* — synchronous appenders do I/O and have internal locks (historic log4j deadlocks are real); at minimum it stretches hold times at exactly the moments (errors, load) contention peaks. Async logging or log-outside is the practice.

---

### Q24. The wait/notify protocol end-to-end: write a correct bounded buffer with `wait()`/`notifyAll()`, and annotate every line that interviews test — the loop, the monitor requirement, which object you notify, and notify's "no handoff" semantics.

**Answer:**

```java
class BoundedBuffer<T> {
    private final Queue<T> items = new ArrayDeque<>();
    private final int capacity;
    private final Object lock = new Object();

    BoundedBuffer(int capacity) { this.capacity = capacity; }

    public void put(T item) throws InterruptedException {
        synchronized (lock) {
            while (items.size() == capacity)      // (1) WHILE, never if
                lock.wait();                      // (2) releases lock; reacquires before returning
            items.add(item);
            lock.notifyAll();                     // (3) wake waiters — state changed
        }
    }

    public T take() throws InterruptedException {
        synchronized (lock) {
            while (items.isEmpty())
                lock.wait();
            T item = items.remove();
            lock.notifyAll();
            return item;
        }
    }
}
```

Annotations:

**(1) `while`, not `if`** — three independent reasons: **spurious wakeups** (the JVM/OS may wake waiters with no notify — spec-sanctioned); **notifyAll wakes everyone** but maybe only one can proceed — the rest must re-check and re-wait; and between the notify and the waiter's re-acquisition of the monitor, a *third* thread can barge in and consume the state. The condition must be re-verified **while holding the lock, after every wake**.

**(2) Must hold the monitor to call wait/notify** — else `IllegalMonitorStateException`. Wait atomically releases *this* monitor and parks (wait set of the inflated monitor, Q21); on wake it moves to the entry list and competes (BLOCKED) to re-acquire before `wait()` returns — waking is not returning.

**(3) Notify carries no message and no lock handoff** — it just moves waiter(s) from wait set toward the entry list. The notifier still holds the lock until its block exits. There's no "the notified thread gets the lock next" — hence barging, hence the loop.

**notify vs notifyAll:** single `notify` wakes an *arbitrary* waiter; with producers AND consumers waiting on **one** monitor, a producer's notify may wake another producer → it re-checks, re-waits → **signal lost, system stuck** (the classic lost-wakeup deadlock). `notifyAll` is the safe default; single-notify is a valid optimization only when waiters are uniform (all wait for the same condition) and one wake enables exactly one. The structural fix for two conditions is two `Condition` objects on a `ReentrantLock` (`notFull`/`notEmpty`) — Q34 — or, in real code, just use `ArrayBlockingQueue` (Q52) and say hand-rolled buffers are for interviews.

**Follow-up trap:** *"Why does `wait()` throw InterruptedException and what do you do mid-buffer?"* — waiting is cancellable (Q9); since we haven't mutated yet when it throws, propagating is clean — and note the subtlety that on interrupt the thread must still re-acquire the monitor before the exception is thrown.

---

### Q25. Spurious wakeups and lost signals: define both failure modes precisely, show a minimal broken example of each, and explain the guarded-wait discipline that makes both impossible.

**Answer:**

**Spurious wakeup:** `wait()` returns with **no notify, no timeout, no interrupt** — permitted by the spec because underlying primitives (futex/condition variables) can wake spuriously and because forbidding it would cost more than the loop discipline does. Broken code:

```java
synchronized (lock) {
    if (queue.isEmpty()) lock.wait();     // spurious wake →
    return queue.remove();                // NoSuchElementException
}
```

**Lost signal / lost wakeup:** the notify happens **before** the wait begins — the signal isn't queued; a notify with nobody in the wait set is a no-op. Broken shape — checking the condition *outside* the lock, or signaling without holding it:

```java
// waiter                                   // signaler
if (!ready)               // reads ready    ready = true;      // (unsynchronized)
    synchronized (l) {                       synchronized (l) { l.notify(); }
        l.wait();         // waits FOREVER — notify already fired between check & wait
    }
```

The window between the unsynchronized check and entering the wait is where the signal slips through. (A cousin: waiting and notifying on **different objects** — no protocol at all.)

**The guarded-wait discipline** (state it as three invariants):

1. **Shared condition state is guarded by the monitor** — only read/written while holding it.
2. **Wait in a `while(condition-not-met)` loop** inside the monitor — handles spurious wakes, barging, and multi-waiter over-wake in one stroke.
3. **Every state change that could satisfy a wait notifies under the same monitor** — because the check-and-park in `wait()` is atomic *with respect to that monitor*, a signaler holding the monitor cannot fire in the waiter's check-park window. Atomicity of check+park versus change+signal is the entire theorem; say it in those words.

Then generalize: the identical discipline applies to `Condition.await()/signal()` (Q34) — and every higher-level tool (BlockingQueue, latches) is this protocol implemented correctly once so you stop reimplementing it.

**Follow-up trap:** *"Are spurious wakeups actually observed or just theoretical?"* — Rare-but-real (signal handling, futex-level artifacts) — but the winning answer: it doesn't matter, because the loop is *already required* for barging and multi-waiter reasons; spurious wakeups just make the requirement unconditional.

---

### Q26. Deadlock: the four Coffman conditions mapped to Java code, the canonical two-lock example, and the prevention toolkit — lock ordering, tryLock with backoff, open calls — plus how you'd *detect* one in a running JVM.

**Answer:**

**Coffman conditions** (all four must hold; break any one to prevent):

1. **Mutual exclusion** — locks are exclusive (given).
2. **Hold and wait** — thread holds lock A while waiting for B.
3. **No preemption** — locks can't be forcibly taken (Java monitors can't).
4. **Circular wait** — T1 holds A wants B; T2 holds B wants A.

```java
// T1: transfer(acc1, acc2)             // T2: transfer(acc2, acc1)
synchronized (from) {                   // T1 locks acc1; T2 locks acc2
    synchronized (to) { … }             // each waits for the other — cycle, forever
}
```

**Prevention toolkit, in order of preference:**

1. **Don't take two locks** — redesign: copy under one lock then work; queues/confinement (Q89); one coarser lock if contention allows. The best deadlock is a structurally impossible one.
2. **Global lock ordering** — break *circular wait*: acquire in a canonical order regardless of call semantics (order by `System.identityHashCode`, or a domain key like account id — with a tiebreaker lock for equal hashes):

```java
Account first  = a.id() < b.id() ? a : b;
Account second = a.id() < b.id() ? b : a;
synchronized (first) { synchronized (second) { … } }
```

3. **tryLock with timeout + backoff** — break *hold-and-wait/no-preemption* (needs `ReentrantLock`, Q31): acquire both or release everything, randomize backoff, retry — livelock-aware (Q27).
4. **Open calls** — never hold your lock into alien code (Q23), removing invisible edges from the lock graph.
5. **Lock-free/immutable designs** where feasible.

**Detection in a live JVM:** `jstack <pid>` (or `jcmd Thread.print`) — the JVM's deadlock detector prints "Found one Java-level deadlock" with the cycle, for *monitors and owned `ReentrantLock`s* (ownable synchronizers). Programmatically: `ThreadMXBean.findDeadlockedThreads()` — wire it to a health check/alert (a self-monitoring service that pages on deadlock is a great thing to have said you built). Limits: it can't see deadlocks involving non-ownable waits (semaphores, latch cycles), DB row locks, or cross-process cycles — those you infer from stacks + external state (Q94). Java offers **no recovery** — no victim-killing like a database (Q77 of JPA doc parallels); processes get restarted, which is why prevention-by-ordering is the production posture.

**Follow-up trap:** *"Can a single thread deadlock itself?"* — with reentrant monitors no; with **non-reentrant** primitives yes (a thread `acquire()`ing a `Semaphore(1)` twice, or `lock.lock()` on a *different* lock instance it assumed was the same); and the nested-monitor deadlock (wait on inner lock holding outer — Q5) is single-thread-initiated. Precision here is the differentiator.

---

### Q27. Livelock and starvation: define each, contrast with deadlock, give a realistic Java example of each, and the standard remedies (randomized backoff, fairness).

**Answer:**

- **Deadlock:** threads permanently *blocked* — no CPU burned, no progress; the cycle is static and visible in a thread dump.
- **Livelock:** threads permanently *active* — running, changing state, responding to each other — yet making **no forward progress**. Nothing is blocked; dumps look "healthy" with busy RUNNABLE threads. Classic sources: (a) over-polite retry — two threads using `tryLock`-both-else-release-and-retry *in lockstep*: each acquires its first lock, finds the second taken, releases, retries simultaneously, forever (the corridor dance); (b) mutual retry storms — two services timing out on each other and retrying in sync; (c) message-requeue loops — consumer fails a poison message, requeues, re-consumes, repeats at full CPU (the ops-world livelock everyone has actually seen — say this one).
- **Starvation:** *some* threads make no progress while others thrive — an unfairness pathology: a hot lock repeatedly barged by fresh threads (nonfair locks favor throughput at the cost of the queue — Q32), read-heavy `ReadWriteLock` starving writers (or vice versa — Q35), low-priority threads under priority abuse (Q7), a thread pool whose long tasks starve short ones behind them in the queue (head-of-line blocking).

**Remedies:**

- Livelock → **break the symmetry with randomness:** jittered exponential backoff before retry (each thread waits `random(0, backoff)`); this is exactly Ethernet CSMA/CD's fix and the reason "add jitter" appears in every retry-policy review. For poison messages: bounded redelivery + dead-letter queue.
- Starvation → **fairness mechanisms:** `new ReentrantLock(true)` / fair `Semaphore` (FIFO handoff — at real throughput cost, Q32); `ReentrantReadWriteLock`'s writer-preference behaviors; separate pools/queues so long work can't shadow short work (bulkheading); avoid priority games.

The design remark that elevates the answer: deadlock, livelock, starvation are all **liveness failures** — the second family of concurrency bugs alongside safety failures (races). Safety = "nothing bad happens"; liveness = "something good eventually happens" — and most tools trade one against the other (fair locks: more liveness, less throughput; nonfair: opposite). Framing trade-offs in those terms is senior vocabulary.

**Follow-up trap:** *"How do you tell livelock from a busy system in production?"* — progress metrics, not thread states: work-completed counters flat while CPU is high; repeated identical retry logs; queue depth static with high dequeue attempts. Livelock is diagnosed by throughput telemetry — dumps alone can't see it.

---

### Q28. Nested locks and lock-ordering discipline at scale: the nested-monitor deadlock with `wait()`, why lock ordering is hard to maintain in a real codebase, and what structural patterns keep systems deadlock-free without global reasoning.

**Answer:**

**Nested-monitor deadlock** — the subtle spelling: `wait()` releases only the monitor it's called on (Q5). Hold lock A, then `synchronized(B){ while(!cond) B.wait(); }` — B is released but **A stays held**; if the thread that would set `cond` needs A first → permanent deadlock, *with no cycle visible among monitor owners* (one thread is in WAITING, not BLOCKED — dump readers miss it). Rule: never wait while holding an outer lock; conditions and their state should live under a *single* monitor.

**Why global lock ordering decays:** it requires every developer to know the total order across modules that don't know each other; callbacks/virtual methods hide acquisitions (Q23's alien calls); layering inverts naturally (storage layer calls a listener that calls the service layer); refactors move code between lock scopes silently. Ordering works within one subsystem (accounts by id — Q26); across a large codebase it's unenforceable by convention alone. Partial enforcement exists — a `Striped`/ordered-lock utility, lock hierarchies with runtime assertion (each thread tracks held-lock levels and asserts monotonicity in dev builds) — worth naming as "how you'd actually police it."

**Structural patterns that remove the problem:**

1. **One lock per aggregate/subsystem, no cross-holding:** coarse-grain the design so no code path needs two subsystem locks; cross-subsystem work goes through **asynchronous handoff** (queue a task; don't call while holding).
2. **Confinement + message passing:** each stateful component single-threaded (its own executor/actor); interactions are messages — no shared locks at all (Q89). This is the pattern that scales organizationally, not just technically.
3. **Immutable snapshots** for read paths (Q17) — readers take no locks, halving the lock graph.
4. **Open calls everywhere** as a lint-able rule: no I/O, no callbacks, no foreign objects under a lock.
5. **Timeout-everything at boundaries:** `tryLock(t)`/bounded waits so residual cycles degrade into recoverable errors + alert, not frozen JVMs — deadlock becomes an SLO event rather than an outage.

Close with the honest hierarchy: in application code you should almost never *hold two locks knowingly*; if a design requires it routinely, the model (not the locking) is wrong.

**Follow-up trap:** *"Thread dump shows no deadlock but the system is frozen — name suspects."* — nested-monitor wait (WAITING not BLOCKED), lost signal (Q25), everyone waiting on an external resource (connection pool empty — the JPA Q89 crossover), a full bounded queue with its consumer dead (Q8's silent worker death), livelock (Q27). Listing five non-cycle freezes is exactly the seniority test.

---

### Q29. String-lock, integer-cache, and class-lock hazards: why `synchronized("id")`, `synchronized(userId)` (Integer), and locking on `getClass()` in a hierarchy are all bugs. What's the correct per-key locking design (weak canonical map / striping)?

**Answer:**

- **`synchronized("orders")`:** string literals are **interned** — every class in the JVM using the literal `"orders"` shares one object, hence one monitor: unrelated code contends or deadlocks with yours invisibly (and a malicious/naive library can hold your "private" lock forever). Same for any shared canonical instance.
- **`synchronized(userId)` where `userId` is `Integer`:** autoboxing uses the **Integer cache** for −128..127 — ids 1..127 share cached instances JVM-wide (colliding with anyone locking small integers), while id 1000 boxes a fresh object per call — so different call sites lock *different* monitors for the same logical key: no exclusion at all. Two failure modes in one line of code. (Also `Boolean.TRUE`, `Long` caches, `Character` — all shared.)
- **`synchronized(getClass())` in an inheritable class:** subclass instances lock `Sub.class`, parent's code locks `Base.class` — the "same" static guard splits into per-class monitors and static state is unguarded across the hierarchy. Lock a named class literal (`Base.class`) or a private static lock object.

**Correct per-key locking** — the real requirement behind these bugs ("one lock per order id"):

1. **Striping** (bounded memory, simplest): `Striped<Lock>`-style — hash key → one of N locks (Q23). Collisions serialize unrelated keys occasionally; usually fine.
2. **Canonical lock map** for exact per-key exclusion:

```java
ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<>();
Object lockFor(String key) { return locks.computeIfAbsent(key, k -> new Object()); }
```

— add eviction thought: unbounded growth with high-cardinality keys → cleanup via `locks.remove(key)` when quiescent (racy to get right), weak-value maps, or Caffeine with weak values; or accept striping instead. Mentioning the eviction problem unprompted is the senior beat.
3. **`ConcurrentHashMap.compute(key, …)` as the lock itself:** CHM guarantees per-key mutual exclusion *inside* compute — for short critical sections, "do the work in compute" is per-key locking with zero extra machinery (with the caveat: no blocking/long work inside compute — it holds the bin lock, Q59).

**Follow-up trap:** *"`String.intern()` to canonicalize keys then lock?"* — works mechanically but shares the global intern namespace (same collision problem you started with) and pressures the string table; the explicit map/striping designs dominate. Recognizing "intern-as-lock" as an anti-pattern is the checkmark.

---

### Q30. Design question: make this class thread-safe and defend every choice — a `TemperatureMonitor` with `update(sensorId, value)`, `getLatest(sensorId)`, `getMinMax()` (global), and a listener callback on new extremes. Walk your reasoning: what's the invariant, what's the lock, what leaves the lock.

**Answer:**

A model answer in four moves:

**1. Identify state & invariants.** Per-sensor latest value (independent per key); global min/max (a *pair* that must be mutually consistent — one invariant spanning two values); listener list (read-mostly registry).

**2. Choose per state, not one hammer.**

```java
class TemperatureMonitor {
    private final ConcurrentHashMap<String, Double> latest = new ConcurrentHashMap<>();
    private final Object extremesLock = new Object();
    @GuardedBy("extremesLock") private double min = NaN, max = NaN;
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
```

- `latest`: independent per-key writes → CHM, no external locking (Q59).
- min/max: **pair invariant** → cannot be two Atomics (a reader could see new min with old max — torn invariant); one small dedicated lock guarding both, private so nothing else couples to it (Q23). Alternative defended: an immutable `Extremes(min,max)` record in an `AtomicReference` with a CAS loop (Q19) — lock-free reads; either is right *if you name the invariant*.
- listeners: COW list — iteration-heavy, mutation-rare (Q60).

**3. Compose without compound races.**

```java
    public void update(String id, double v) {
        latest.put(id, v);
        Extremes toNotify = null;
        synchronized (extremesLock) {
            if (Double.isNaN(min) || v < min || v > max) {
                min = Double.isNaN(min) ? v : Math.min(min, v);
                max = Double.isNaN(max) ? v : Math.max(max, v);
                toNotify = new Extremes(min, max);     // snapshot under lock
            }
        }
        if (toNotify != null)                           // ALIEN CALL outside the lock
            for (Listener l : listeners) l.onExtreme(toNotify);
    }
    public double[] getMinMax() { synchronized (extremesLock) { return new double[]{min, max}; } }
```

Points being scored: the check-and-update of extremes is one atomic section (no check-then-act split); `getMinMax` returns a **consistent snapshot** taken under the same lock (returning fields read separately would be the subtle bug); callbacks fire **after** release with a snapshot (open call — deadlock and hold-time hygiene, Q23); no lock covers `latest` — unrelated state, unrelated mechanism.

**4. Say the meta-principles:** guard *invariants*, not fields; smallest lock that covers the invariant; document with `@GuardedBy`; prefer existing concurrent components per access pattern; never let listeners/IO under a lock; and state the visibility story for each piece (CHM and COW have their own HB contracts; the lock covers the pair). Interviewers grade the *narrative* — invariant → mechanism → boundary — more than the code.

**Follow-up trap:** *"A reader wants latest-for-sensor AND current extremes consistently with each other."* — cross-structure consistency needs either one lock over both (coarser design) or versioned snapshots (immutable world-state swapped atomically — Q17) — admitting the current design doesn't give it, and pricing both fixes, is precisely the mature close.

---

# Section 4 — java.util.concurrent Locks, CAS & Atomics (Q31–Q40)

---

### Q31. `ReentrantLock` vs `synchronized`: enumerate every capability the explicit lock adds, the costs it brings, and the decision rule you actually use. Why is the `try/finally` shape non-negotiable?

**Answer:**

Capabilities `ReentrantLock` adds over `synchronized`:

1. **`tryLock()`** — acquire-or-fail-now (deadlock avoidance, "skip if busy" fast paths);
2. **`tryLock(timeout, unit)`** — bounded waiting (degrade to error instead of hanging — boundary hygiene, Q28);
3. **`lockInterruptibly()`** — cancellable lock waits (monitor BLOCKED ignores interrupts, Q4);
4. **Fairness option** (`new ReentrantLock(true)` — Q32);
5. **Multiple `Condition` objects per lock** (notFull/notEmpty separation — Q34);
6. **Non-block-structured locking** — acquire in one method, release in another (hand-over-hand/lock-coupling traversals — impossible with `synchronized`'s lexical scoping);
7. Introspection: `isHeldByCurrentThread()`, queue-length probes for monitoring.

**Costs:** manual release — the entire class of forgot-to-unlock bugs; verbosity; the lock is an object you can leak/reassign; no automatic mark-word optimization story (both are fast uncontended — performance parity is the modern truth; the old "ReentrantLock is faster" was a Java-5/6 artifact, and saying so is a currency signal).

```java
lock.lock();                  // outside try: if lock() itself fails, nothing to unlock
try {
    // critical section
} finally {
    lock.unlock();            // ALWAYS runs — exception paths included
}
```

Non-negotiable because any escaping exception otherwise leaves the lock held forever — every later contender parks permanently (a leaked monitor is released by stack unwinding; a leaked ReentrantLock never is). Also `lock()` goes *before* `try` (the convention prevents unlocking a lock you failed to acquire).

**Decision rule:** default `synchronized` — less to get wrong, JIT-friendly, dump-friendly (monitors show richer info); reach for `ReentrantLock` when you *name* the needed feature: tryLock/timeout/interruptible/fairness/multiple conditions/non-lexical scope. "I use the feature list as the justification checklist" is the interview-ready formulation.

**Follow-up trap:** *"Is `ReentrantLock` reentrant like monitors?"* — yes (hold count; `unlock()` per `lock()`); and the AQS mechanics behind it (Q33) is the natural next question.

---

### Q32. Fair vs nonfair locking: what does fairness mean mechanically, why is nonfair the default, what is barging, and when is fairness worth its price?

**Answer:**

- **Nonfair (default):** an arriving thread first **tries to CAS-grab the lock immediately** — even if others have been queued for milliseconds ("**barging**"). Only on failure does it enqueue. Why this wins on throughput: the moment a lock frees, an *already-running* thread can take it in nanoseconds, while waking a parked queue-head takes microseconds (unpark → schedule → resume); handing the lock strictly to the queue head leaves the lock idle across that wake-up latency on every handoff — and invites **lock convoys**. Nonfair fills those gaps; queued threads still eventually win (the barger only beats them at the exact release instant).
- **Fair (`new ReentrantLock(true)`):** strict FIFO by arrival in the AQS queue — an arriving thread enqueues if anyone is waiting; no barging. Price: every handoff pays the park/unpark round-trip; throughput commonly drops by a large factor (JCiP's classic benchmarks: order-of-magnitude differences under contention); context-switch load rises.

**When fairness is worth it:** starvation is *observed* and matters — long hold times with continuous arrival where an unlucky thread can wait unboundedly (latency-SLO-bound work, per-tenant fairness); or ordering itself is a requirement. Even then, prefer redesigning the contention away (shorter sections, striping, queues) before buying fair locks; fairness treats the symptom.

Nuances that score: `tryLock()` (no-arg) **barges even on a fair lock** (documented!) — use `tryLock(0, TimeUnit.SECONDS)` for fairness-honoring try; monitors (`synchronized`) are always nonfair with no option; fair mode still doesn't guarantee *condition-queue* FIFO wake ordering ties into scheduling; and semaphores/RW locks offer the same fair/nonfair choice with the same trade-off (Q35/Q53).

**Follow-up trap:** *"Does nonfair mean starvation is likely?"* — In practice rare (bargers only win a tiny race window; queue drains constantly); pathological starvation needs sustained arrival + long holds. "Nonfair is statistically fair enough for almost all workloads" is the calibrated claim.

---

### Q33. What is AQS (`AbstractQueuedSynchronizer`)? Explain the state+queue model, how ReentrantLock/Semaphore/CountDownLatch map onto it, and why one framework underlies most of `java.util.concurrent`.

**Answer:**

AQS is the **synchronizer construction kit** Doug Lea built so every blocking primitive wouldn't reimplement queuing/parking. Its model:

1. A single **`volatile int state`** — meaning assigned by the subclass;
2. A **CLH-derived FIFO queue** of waiting threads (each node spins briefly then parks via `LockSupport.park`, watching its predecessor);
3. Subclasses override tiny hooks — `tryAcquire`/`tryRelease` (exclusive) or `tryAcquireShared`/`tryReleaseShared` (shared) — expressing *only* the atomic state transition; AQS supplies enqueueing, parking/unparking, cancellation on interrupt/timeout, and the condition-queue machinery.

Mappings (the recitable table):

- **ReentrantLock:** state = hold count (0 free; reentrancy increments); exclusive; owner recorded for reentrancy checks; fair vs nonfair = whether `tryAcquire` barges or defers to queued predecessors (Q32).
- **Semaphore:** state = available permits; **shared** mode (multiple acquirers can succeed); `acquire` = CAS state−n, `release` = state+n and wake sharers.
- **CountDownLatch:** state = count; `await` = acquire-shared succeeds only when state==0; `countDown` = release-shared decrement; at zero, the whole queue is released (broadcast).
- **ReentrantReadWriteLock:** one int split — high 16 bits reader count, low 16 writer hold count (hence the 65,535 limits — a lovely detail); readers acquire shared, writer exclusive.
- Also on AQS: `ThreadPoolExecutor`'s Worker (aborts idle interrupts), `FutureTask` historically, `SynchronousQueue` pieces.

Why one framework matters conceptually: locks and latches and semaphores differ only in the **state-transition rule** — waiting/waking is common infrastructure. Under it all sits **`LockSupport.park()/unpark(thread)`** — the primitive that (unlike wait/notify) needs no monitor and *doesn't lose signals*: `unpark` before `park` makes the next `park` return immediately (a one-permit model) — which is exactly how AQS avoids lost-wakeup races (Q25) without holding a lock during signaling.

**Follow-up trap:** *"Why does contended `ReentrantLock` show WAITING, not BLOCKED, in dumps?"* — because AQS parks via LockSupport (WAITING on the sync node), while BLOCKED is monitor-specific (Q4) — one framework question closing the loop to observable dumps is a favorite chain.

---

### Q34. `Condition` objects: how do they improve on wait/notify, rewrite the bounded buffer with two conditions, and state the rules (await loop, signal vs signalAll, which lock must be held).

**Answer:**

`Condition` (from `lock.newCondition()`) is wait/notify **per-lock, multiplied**: a lock can have *several* wait-sets, so differently-purposed waiters aren't mixed in one pool. That kills the core notify/notifyAll dilemma (Q24): with **separate `notFull` and `notEmpty` queues**, a producer signals only consumers and vice versa — `signal()` (single wake) becomes *safe and efficient*, because everyone in a given queue waits for the same predicate.

```java
class BoundedBuffer<T> {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull  = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Object[] items;  private int head, tail, count;

    public void put(T x) throws InterruptedException {
        lock.lock();
        try {
            while (count == items.length) notFull.await();   // loop — same discipline
            items[tail] = x; tail = (tail + 1) % items.length; count++;
            notEmpty.signal();                               // exactly one consumer wakes
        } finally { lock.unlock(); }
    }
    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (count == 0) notEmpty.await();
            @SuppressWarnings("unchecked") T x = (T) items[head];
            items[head] = null; head = (head + 1) % items.length; count--;
            notFull.signal();
            return x;
        } finally { lock.unlock(); }
    }
}
```

(This is essentially `ArrayBlockingQueue`'s actual implementation — saying so shows you've read it.)

**Rules:** must hold *the* lock to `await/signal` (else `IllegalMonitorStateException`); `await()` atomically releases the lock and parks, re-acquires before returning — identical protocol to `wait` (Q24), same **while-loop requirement** (spurious wakeups + barging exist here too); `signalAll()` when waiters are heterogeneous or state changes might satisfy several; extra APIs worth naming — `awaitNanos` (returns remaining time — timed waits without oversleep math), `awaitUntil(deadline)`, `awaitUninterruptibly()` (rare; shutdown-resistant waits).

**Follow-up trap:** *"Why can't Conditions exist for `synchronized`?"* — a monitor has exactly **one** wait set baked into the object header/ObjectMonitor (Q21); multiple wait-sets require the lock to be an object that can mint them — which is exactly what `Lock.newCondition()` is. Design-level cause-and-effect, not trivia.

---

### Q35. `ReentrantReadWriteLock`: semantics, the write→read downgrade (legal) vs read→write upgrade (deadlock), starvation behaviors, and why RW locks often *don't* beat a plain lock — leading into `StampedLock`.

**Answer:**

Semantics: **read lock** — shared, any number of concurrent readers, granted when no writer holds/waits (mode-dependent); **write lock** — exclusive against everyone. Payoff exists only for **read-mostly workloads with non-trivial read sections**; state = one AQS int split 16/16 (Q33).

- **Downgrade (legal & useful):** hold write → acquire read → release write — you exit the exclusive section while *keeping* a consistent-view guarantee, letting other readers in but no writers:

```java
w.lock();
try { update(); r.lock(); }      // acquire read BEFORE releasing write
finally { w.unlock(); }
try { readConsistently(); } finally { r.unlock(); }
```

- **Upgrade (read → write): impossible — by design.** Two readers both attempting upgrade each wait for the *other's* read lock to clear → mutual deadlock; the API simply blocks forever instead of special-casing. Correct pattern: release read, acquire write, **re-validate** state (the world changed in the gap) — or use StampedLock's `tryConvertToWriteLock` which makes the attempt explicit and failable.

**Starvation & fairness:** nonfair mode historically lets continuous readers **starve writers** (readers keep piling onto the shared hold); the implementation mitigates (a queued writer blocks *new* reader acquisitions in many paths), and fair mode enforces arrival order at the usual throughput cost (Q32).

**Why RW often disappoints** — the honest performance story: the read lock is still a **shared CAS on one contended cache line** — under many short reads, that coherence traffic can cost as much as a plain exclusive lock; bookkeeping is heavier than ReentrantLock; and if reads are short or writes frequent, you've bought complexity for nothing. Benchmarks regularly show plain `synchronized` beating RWL for small read sections. Alternatives ladder: **immutable snapshot + volatile swap** (readers pay one volatile read — Q17), COW collections (Q60), or **StampedLock optimistic reads** — `tryOptimisticRead()` returns a stamp, you read fields, `validate(stamp)` checks no writer intervened, retry/fallback to a real read lock if not: readers touch **no shared write state at all** on the happy path (just a validation load) — dramatically more scalable. StampedLock caveats to recite: **not reentrant**, no conditions, and misuse (using data from a failed validation) is a correctness bug — it's an expert tool for hot read paths.

**Follow-up trap:** *"When exactly would you pick RWL over all alternatives?"* — mutable structure too big/awkward to snapshot, reads long enough to amortize the shared-CAS (traversals, aggregate computations), writes rare, reentrancy or Condition needed (StampedLock lacks both). Naming the *niche* rather than defending the tool is the senior move.

---

### Q36. `LockSupport.park()/unpark()`: the primitive under everything — its permit model, how it avoids lost wakeups without a lock, spurious returns, and why application code should almost never touch it.

**Answer:**

`LockSupport` parks/unparks *threads* directly, no monitor required. The model: each thread has a **single binary permit**. `park()` — if the permit is available, consume it and return immediately; else block. `unpark(t)` — make t's permit available (capped at one; repeated unparks don't accumulate) and wake t if parked.

**Why no lost wakeups** (the conceptual win over wait/notify): with wait/notify, a notify with no one waiting is a no-op → the check-then-wait window loses signals unless a lock makes check+park atomic (Q25). With the permit model, **`unpark` *before* `park` is remembered** — the next `park` consumes the permit and sails through. Signal-before-wait is safe, so AQS can wake successors without holding any lock during the handoff (Q33).

**Spurious returns are official:** `park()` may return for no reason at all (also returns on interrupt — *without throwing*; it leaves the interrupt flag set for you to check). Therefore the same discipline as ever, now enforced by convention: **always park in a loop re-checking the real condition**:

```java
while (!flagIsSet())
    LockSupport.park(this);      // 'this' = blocker object, shows in thread dumps
```

(The `park(Object blocker)` overload sets the blocker shown by jstack — always use it; anonymous parks make dumps unreadable — a practitioner's detail.)

**Why not in app code:** it's a *mechanism*, not a protocol — no condition management, no fairness, no queueing; you'd be re-deriving AQS with fewer guarantees. Its legitimate users: AQS itself, `FutureTask`/`CompletableFuture` waiters, `SynchronousQueue`/exchanger internals, Loom's continuation mounting (virtual-thread park is the *cheap* one — Q84). Interview-wise, know it as the answer to "how do j.u.c. classes block without synchronized?" and "what's under Condition.await?" — a mechanism-level answer that distinguishes read-the-source candidates.

**Follow-up trap:** *"Difference between `Thread.sleep(t)` and `parkNanos(t)`?"* — park is cancellable by unpark (early wake by design) and doesn't throw on interrupt (flag-based); sleep can only end by timeout/interrupt-exception. Follow-through: timed waits in AQS are parkNanos loops with deadline re-computation — oversleep handling included.

---

### Q37. Compare-and-swap: the primitive, its hardware basis, the retry-loop pattern, and the trade against locks — including what happens under high contention and what "lock-free" formally means.

**Answer:**

**CAS(location, expected, new)**: atomically — if the location holds `expected`, write `new` and report success; else fail (returning the witnessed value in the `compareAndExchange` variant). Hardware: a single instruction/pair (`lock cmpxchg` on x86; load-linked/store-conditional — LL/SC — on ARM), atomic at the cache-coherence level: the core owns the line exclusively for the operation. In Java: `Atomic*` classes, `VarHandle.compareAndSet`, all bottoming out in the same intrinsics; every CAS also carries full-fence memory semantics (it's a volatile-grade access — Q19).

**The optimistic retry loop** — concurrency without exclusion:

```java
public int incrementAndGet(AtomicInteger a) {
    for (;;) {
        int cur = a.get();
        int next = cur + 1;
        if (a.compareAndSet(cur, next)) return next;   // interference → loop again
    }
}
```

Read → compute → attempt; a failure means *someone else made progress* — recompute from their result and retry. Requirements: the computed transition must be derivable from the single witnessed value (or packaged into one object — Q19's snapshot pattern), side-effect-free, cheap.

**Locks vs CAS:** locks are *pessimistic* (assume conflict; exclude) — blocked threads park (context switches, convoys, priority inversion risk); CAS is *optimistic* (assume rare conflict; detect and retry) — no parking, no scheduler, immune to "lock-holder got descheduled" stalls. Under **low/moderate contention CAS wins decisively**; under **high contention** the retry loop degrades: every failure re-runs the loop while the contended cache line **ping-pongs between cores** (coherence traffic is the real cost), burning CPU on failed attempts — a lock that parks losers can beat it. The escape for counters: **LongAdder-style striping** (Q39); generally: reduce sharing, not sharpen the primitive.

**Formal progress terms** (drop them precisely): **blocking** — a paused holder can stall everyone; **lock-free** — *some* thread completes in a bounded number of steps system-wide (CAS loops qualify: a failure implies another's success); **wait-free** — *every* thread completes in bounded steps (e.g., `getAndAdd` on x86 via `lock xadd`). Lock-free ≠ starvation-free for an individual thread — precision here is the differentiator.

**Follow-up trap:** *"Can CAS operate on two locations at once?"* — hardware CAS is single-word (double-width CMPXCHG16B exists but Java doesn't expose general DCAS); multi-location invariants need packaging into one reference (immutable pair + AtomicReference), a lock, or STM-style designs — exactly why Q30's min/max pair couldn't be two Atomics.

---

### Q38. The ABA problem: construct a concrete failure, explain why counters and stamps fix it, `AtomicStampedReference`/`AtomicMarkableReference`, and why GC makes ABA rarer in Java than in C++.

**Answer:**

**ABA:** a CAS validates *value equality*, not *history*. Thread 1 reads A, stalls; others change A→B→**back to A**; Thread 1's CAS(A→C) **succeeds** — though the world it validated is gone. Where value-sameness ≠ state-sameness, that's corruption.

**Concrete classic — lock-free stack pop:**

```java
// top -> A -> B -> C
Node oldTop = top.get();          // T1 reads A, next = B … then stalls
// Meanwhile T2: pops A, pops B, then pushes A back  → stack: A -> C
top.compareAndSet(oldTop, oldTop.next);   // T1: CAS succeeds (top is A again!)
// but oldTop.next was captured as B — B is spliced back in though it was popped:
// stack now points into freed/stale structure → lost/duplicated nodes
```

The CAS "proved" nothing changed; actually two pops and a push happened. Same shape appears in free-lists, ring-buffer slots, and any "reference reused for a new life" design.

**Fixes:**

1. **Version/stamp the reference** — CAS on (reference, version) so any intervening mutation bumps the version: `AtomicStampedReference<Node>` (`compareAndSet(expectedRef, newRef, expectedStamp, newStamp)`) — packs the pair into one CAS'd holder object. `AtomicMarkableReference` = boolean flavor (mark "logically deleted" — used in Harris-style lock-free lists). Hardware analogue: tagged pointers/double-width CAS.
2. **Never reuse identity:** fresh node objects per push — then A-again literally cannot occur…

…which is the **GC point**: in C++ a freed node's memory is recycled into a "new" node at the same address → ABA is endemic (hence hazard pointers/epoch reclamation — name-drops that land). In Java, as long as anyone holds `oldTop`, that object can't be reused; ABA needs the *application* to re-insert the same object. Rarer — **not** impossible (object pools! wrapper caches! any recycling reintroduces it) — the calibrated claim interviewers want.

Also connect: `AtomicReference.updateAndGet` with pure functions on immutable snapshots is ABA-immune *if* semantics depend only on the current value — most app-level CAS uses are; ABA bites *structural* algorithms where captured internals (like `.next`) must match the witnessed reference's era.

**Follow-up trap:** *"Does the stamp fully solve it?"* — a bounded int stamp can wrap (2³² operations during a stall — theoretical but say it); and stamping adds an allocation per update in ASR's pair-holder design. Robust designs prefer immutability/no-reuse over stamps where possible.

---

### Q39. `AtomicLong` vs `LongAdder`: why does a single hot counter stop scaling, how does LongAdder's striping work, what do you give up (`sum()` semantics), and where does each belong? Include false sharing and `@Contended`.

**Answer:**

**Why AtomicLong saturates:** every `incrementAndGet` is a CAS on **one memory word → one cache line** that must be owned exclusively by the incrementing core; under N threads the line ping-pongs (MESI invalidations) and CAS failure/retry rates climb — throughput *falls* as you add cores. The bottleneck is coherence traffic, not the instruction.

**LongAdder:** decompose the counter into a base + a lazily-grown array of **`Cell`s** (striped sub-counters). A thread hashes (its probe value) to a cell and CASes *that cell*; on contention it re-probes/expands the cell array (up to ~#cores). Different threads mostly hit **different cache lines** → near-linear write scaling. `sum()` adds base + all cells — **not a linearizable snapshot**: it's a moving sum, accurate only when quiescent; there's no atomic read-and-reset that's also exact under concurrent updates (`sumThenReset` is best-effort). You've traded read precision for write throughput.

**Placement rule:** **statistics/metrics counters → LongAdder** (writes hot, reads rare and tolerant — exactly its contract; Micrometer/Dropwizard use it internally); **sequence/id generation, or logic depending on each returned value → AtomicLong** (you need `incrementAndGet`'s exact per-call result — LongAdder can't give you "your" value at all). `LongAccumulator` generalizes to max/min/any commutative-associative op.

**False sharing** — the sibling concept: two *unrelated* hot variables residing on **one 64-byte cache line** contend as if shared — each core's write invalidates the other's line ("sharing" that's an accident of layout). LongAdder's `Cell` is annotated **`@jdk.internal.vm.annotation.Contended`** — the JVM pads/isolates it to its own line (application code needs `-XX:-RestrictContended`; more practically: pad manually or restructure). Symptoms: perfect-looking code scaling badly; diagnosis: perf counters (cache-line contention events), or just knowing the pattern — hot fields written by different threads should not be adjacent in one object.

**Follow-up trap:** *"Why not stripe AtomicLongs yourself?"* — you'd rebuild LongAdder minus its careful probe rehashing, growth heuristics, and padding; also asked: *"LongAdder for a rate limiter?"* — no: limiters need read-check-act on a precise current value → AtomicLong CAS loops or token buckets. Matching tool to read-semantics is the actual skill being tested.

---

### Q40. Concept synthesis: build a lock-free bounded ID-range allocator — `next()` returns sequential ids and must fail cleanly when exhausted — and use it to demonstrate CAS loops, invariant packaging, and when you would *stop* being lock-free.

**Answer:**

Requirements force the interesting bits: sequential (state transition), bounded (check-then-act must be atomic with the increment), concurrent (no locks requested).

**Naive broken:** `if (counter.get() < max) return counter.incrementAndGet();` — check-then-act race: threads over-allocate past `max` (Q10). The check and the claim must be **one atomic transition**:

```java
final class IdRange {
    private final long max;                       // exclusive bound — immutable
    private final AtomicLong next = new AtomicLong();

    IdRange(long start, long max) { this.next.set(start); this.max = max; }

    OptionalLong next() {
        for (;;) {
            long cur = next.get();
            if (cur >= max) return OptionalLong.empty();     // exhausted — clean failure
            if (next.compareAndSet(cur, cur + 1))
                return OptionalLong.of(cur);                 // claimed exactly one id
        }
    }
}
```

Teaching points to narrate: the **witnessed value `cur` carries the whole invariant** (bound check + claimed id derive from one read — no multi-variable problem, so plain CAS suffices, Q37); failure of CAS = someone else claimed `cur` → re-read and retry — progress is system-wide guaranteed (**lock-free**: my failure implies their success); return the *pre-increment* value — each id handed out exactly once; exhaustion is stable (once `cur >= max`, all threads observe it — monotonic state makes the empty branch race-free); no ABA possible (monotonically increasing value never returns to A, Q38 — monotonicity as an ABA defense is a point worth making explicitly).

**When to stop being lock-free:** the moment requirements grow past one word's worth of invariant — e.g., *"also record who took each id"* (two locations → package into an immutable record + AtomicReference, or lock), *"block when exhausted until ids are returned"* (blocking protocol → Semaphore of permits is literally this, Q53 — recognizing "this is a semaphore" wins the round), *"batched allocation for throughput"* (CAS `cur → cur+batch`, hand out locally — the JPA sequence-allocationSize idea, connecting your two documents), or contention so high the loop thrashes (→ striping per Q39, or accept a lock). The senior close: lock-freedom is a *means* — chosen when the invariant fits in one CAS and contention is moderate — not an identity.

**Follow-up trap:** *"Make it wait-free."* — `getAndIncrement()` (fetch-add) is wait-free hardware-side, but the bound check breaks pure fetch-add (over-shoot then compensate: `long v = next.getAndIncrement(); return v < max ? of(v) : empty();` — ids beyond max are burned but never *issued* — correct if the range can afford waste). Trading waste for wait-freedom, and saying which contract changed, is exactly the depth this question exists to find.

---

# Section 5 — Thread Pools & the Executor Framework (Q41–Q50)

---

### Q41. Why do thread pools exist — enumerate the actual problems with thread-per-task — and map the Executor framework's interfaces (`Executor`, `ExecutorService`, `ScheduledExecutorService`) to their responsibilities.

**Answer:**

**Thread-per-task's failure modes:** (1) creation/teardown cost paid per task (native thread + ~1MB stack reservation, kernel calls — Q1); (2) **unbounded resource consumption** — load spikes create threads until memory/scheduler collapse (`OutOfMemoryError: unable to create native thread` is a *thread* problem, not a heap problem — worth saying); (3) more runnable threads than cores past a point only adds context-switch and cache-pollution overhead — throughput *degrades* under the load it's meant to absorb; (4) no management surface — no queueing policy, no backpressure, no graceful shutdown, no metrics.

**Pools invert it:** a fixed set of long-lived workers consuming tasks from a queue — creation amortized, **concurrency bounded** (the pool is a resource governor as much as a performance tool), the queue is an explicit buffer with an explicit overflow policy (Q43), and lifecycle/observability come built in.

**Interface map:**

- **`Executor`** — one method, `execute(Runnable)`: the pure *decoupling* abstraction — "task submission separated from execution mechanics." Depend on this when that's all you need.
- **`ExecutorService`** — adds lifecycle (`shutdown`, `shutdownNow`, `awaitTermination`, `isTerminated`) and richer submission: `submit` → `Future`, `invokeAll` (all tasks, wait for all), `invokeAny` (first success wins, rest cancelled — nice for redundant/hedged calls), `close()` (Java 19+: AutoCloseable = shutdown+await, enabling try-with-resources pools).
- **`ScheduledExecutorService`** — delayed and periodic execution (`schedule`, `scheduleAtFixedRate`, `scheduleWithFixedDelay` — semantics and the exception trap in Q49). The modern replacement for `Timer` (Timer: single thread, one task's exception kills the whole timer, wall-clock sensitivity — the three reasons it's legacy).
- **`ThreadPoolExecutor` / `ScheduledThreadPoolExecutor` / `ForkJoinPool`** — the implementations; `Executors` — factory shortcuts (with famous caveats — Q46).

**Follow-up trap:** *"Is `execute` vs `submit` just Future-vs-not?"* — also exception routing (uncaught-handler vs swallowed-into-Future — Q8's trap), a distinction that decides whether your errors are visible.

---

### Q42. `ThreadPoolExecutor`'s seven constructor parameters: explain each, then the counterintuitive interaction rule between corePoolSize, the queue, and maximumPoolSize that most engineers get wrong.

**Answer:**

```java
new ThreadPoolExecutor(
    corePoolSize,        // workers kept alive (target size)
    maximumPoolSize,     // hard ceiling on workers
    keepAliveTime, unit, // idle timeout for workers ABOVE core
    workQueue,           // BlockingQueue<Runnable> — the buffer & the policy
    threadFactory,       // naming, daemon flag, priority, UEH (Q44)
    rejectedHandler)     // what happens when saturated (Q43)
```

**The decision algorithm on every `execute()`** — this ordering is the whole question:

1. Fewer than `corePoolSize` workers → **create a new worker** (even if others are idle).
2. Core full → **offer to the queue**. If it accepts — done. *No new thread is created while the queue accepts.*
3. Queue full → create a worker **up to `maximumPoolSize`**.
4. At max and queue full → **reject** (handler).

**The counterintuitive consequence:** with an **unbounded queue** (`LinkedBlockingQueue` default capacity — what `newFixedThreadPool` uses), step 2 *always* succeeds → **maximumPoolSize is dead configuration; the pool never grows past core**, and the queue grows without limit instead (latency + memory creep under sustained overload — the failure is deferred, not prevented). Conversely `SynchronousQueue` (capacity 0 — `newCachedThreadPool`) always fails step 2 when no worker is waiting → every burst goes straight to thread creation up to max. "Scale threads first, then queue" requires either a bounded queue sized deliberately — or overriding `offer` (the trick used by Tomcat's executor: prefer creating threads below max before queueing — knowing this exists is a strong signal).

Details that round it out: `keepAlive` applies to threads above core (and to core too with `allowCoreThreadTimeOut(true)` — pools that shrink to zero); `prestartAllCoreThreads()` for latency-critical warmup; core threads are created lazily per-task by default.

**Follow-up trap:** *"Pool of core=2, max=4, queue capacity=10 — 8 tasks arrive; how many threads?"* — 2 threads, 6 queued (queue isn't full — no growth). The 3rd thread appears only at task 13. Walking this cold is the test.

---

### Q43. Saturation policy: the four built-in `RejectedExecutionHandler`s, why `CallerRuns` is the interesting one (backpressure), when you'd write a custom handler, and how rejection relates to load shedding at the system level.

**Answer:**

When queue and pool are both full (or the executor is shutdown):

- **`AbortPolicy` (default):** throw `RejectedExecutionException`. Loud, immediate; the caller must handle it — correct default because *silence is worse* .
- **`CallerRunsPolicy`:** the **submitting thread executes the task itself**. Elegant twofold effect: the task isn't lost, and — because the submitter is now busy working — the *producer is slowed down to the consumer's pace*: **backpressure by conscription**. Caveats worth stating: the caller might be a thread you must not block (an event loop, another pool's worker → cascade stalls); ordering degrades; and under pathological load *every* caller becomes a worker (throttled chaos, but throttled).
- **`DiscardPolicy`:** drop the new task silently. Only for genuinely optional work (sampled metrics) — silent loss is a production incident generator otherwise.
- **`DiscardOldestPolicy`:** evict the queue head, retry the offer — "newest wins." Rarely right (invisibly drops the oldest committed work; breaks fairness/FIFO expectations; with Futures, someone waits forever on the discarded task).

**Custom handlers** — the real-world set: block-with-timeout (`queue.offer(task, 5s)` → then abort) for "apply backpressure but bounded"; reject-with-metric + fallback (enqueue to overflow storage / return 429); shed-by-priority (drop low-priority classes first). A rejection handler is where **load shedding policy** lives at pool granularity.

**System framing** (this earns the senior points): a bounded pool + bounded queue is your service's *admission control*; rejection is the pool telling you demand exceeds capacity. The healthy chain is: bounded resources → rejection → mapped to backpressure upstream (429/`Retry-After`, slow consumers, dropped low-value work) → autoscaling on the metrics rejections emit. Unbounded queues don't remove the overload — they convert it into latency and OOM later (Q42), i.e., they *hide the signal*. "Fail fast at the edge beats dying slowly in the middle" is the doctrine sentence.

**Follow-up trap:** *"Which policy for a web server's request pool?"* — bounded queue + Abort mapped to HTTP 503/429 with Retry-After (shed at the boundary, protect latency of admitted requests); CallerRuns would conscript the acceptor/IO threads — precisely the threads you must never block. Tying policy to *which thread would pay* is the expected reasoning.

---

### Q44. `ThreadFactory`: why a custom one is baseline hygiene, what you set on created threads, and how pool threads' names/daemon-status/exception-handlers affect production debugging.

**Answer:**

The default factory gives `pool-7-thread-3`: non-daemon, normal priority, no uncaught-exception handler. Every serious codebase overrides it:

```java
ThreadFactory tf = r -> {
    Thread t = new Thread(r, "order-worker-" + COUNTER.incrementAndGet());
    t.setDaemon(false);                          // explicit lifecycle decision (Q6)
    t.setUncaughtExceptionHandler((th, e) ->     // execute()-path errors get logged (Q8)
        log.error("Worker {} died", th.getName(), e));
    return t;
};
```

(Guava's `ThreadFactoryBuilder` / your framework's equivalent in practice.)

**Why each field matters operationally:**

- **Names** are the single highest-value line: thread dumps, profiler flamegraphs, and `top -H` become legible — "40 threads named `payment-io-*` blocked on socketRead" is a diagnosis; "pool-12-thread-40" is archaeology. Name by *role*, include an index.
- **Daemon flag:** an explicit choice per pool (Q6) — worker pools holding real work: non-daemon + proper shutdown; background sweepers: daemon and kill-safe.
- **UEH:** covers the `execute()` path (submit-path exceptions still hide in Futures — Q8); without it, worker deaths are stderr-or-nothing.
- **Priority:** leave alone (Q7).
- Advanced but worth naming: the factory is also where you attach **context propagation** (MDC/trace-id copying into the thread — though per-task wrapping is more correct, Q77) and where security/classloader context gets pinned (`Thread.contextClassLoader` — appserver leak lore).

Also: the factory participates in **worker replacement** — when a worker dies from an escaped Throwable, the pool creates a replacement via the factory; a factory that throws or returns null can wedge the pool (rare, but it's why factories must be trivial and non-failing).

**Follow-up trap:** *"How would you find which pool is leaking threads?"* — `jstack` histogram by name prefix over time; unnamed pools make this guesswork — circling back to why naming is answer #1. Interviewers like when the "boring" question closes into the debugging story.

---

### Q45. Pool sizing: derive the classic formulas for CPU-bound vs I/O-bound workloads, explain Little's Law's role, why "one big pool" fails mixed workloads (bulkheading), and what changes when downstream is the bottleneck.

**Answer:**

**CPU-bound:** more runnable threads than cores buys only context switching (Q1) → `N_threads ≈ N_cores` (+1 traditionally, to cover page-fault stalls). `Runtime.getRuntime().availableProcessors()` — and note it respects **container CPU quotas** in modern JVMs (a cgroup-limited pod sees its quota, not the host's 64 cores — the Kubernetes gotcha worth volunteering).

**I/O-bound:** threads spend most time *waiting*; you need enough to keep cores busy during waits. The Brian-Goetz form: `N = cores × (1 + wait/compute)` — 10ms compute + 90ms wait → ×10 multiplier. Equivalent lens, **Little's Law**: concurrency needed `L = λ × W` (target throughput × per-request latency) — 100 req/s at 200ms in-flight time needs ~20 concurrent executions; size pool ≈ L with headroom. Both formulas are *estimates to be load-tested*, and both silently assume the waits are on something that can absorb the concurrency — which they often can't:

**Downstream-bounded reality:** if each task holds a DB connection and the pool has 30 connections, a 200-thread executor just moves the queue to the connection pool (adding blocked threads and lock/timeouts) — size to the **narrowest resource in the chain** (DB pool, rate-limited API, disk) and let *your* queue hold the excess where you control policy (Q43). This links straight to your JPA doc's Hikari-sizing question — same law, one layer up.

**Bulkheading (one big pool fails mixed work):** latency-critical short tasks stuck behind slow I/O tasks in one queue = head-of-line blocking; a downstream outage consuming all workers takes *unrelated* features down with it. Partition: separate, individually-sized pools per dependency/workload-class (the ship-compartment metaphor; Hystrix/Resilience4j formalized it) — failure isolation + per-class sizing + per-class metrics. Cost: more total threads, some idle capacity — buy the isolation for anything user-facing.

And the modern footnote that reframes the whole question: for I/O-bound-with-huge-fanout, **virtual threads** dissolve the sizing problem (thread-per-task returns, cheap — Q84) — the *bounding* job then migrates to semaphores/rate limiters around scarce downstreams (Q53).

**Follow-up trap:** *"Why is oversizing bad if threads are 'just waiting'?"* — each parked thread still costs stack memory + scheduler load; when the wait breaks (downstream recovers), *all* of them stampede the downstream simultaneously (thundering herd); and oversized pools mask backpressure signals until OOM. "Idle threads are latent load" is the crisp phrasing.

---

### Q46. The `Executors` factory methods — `newFixedThreadPool`, `newCachedThreadPool`, `newSingleThreadExecutor`, `newWorkStealingPool` — what each builds, the known hazards of the first two, and why code review standards often say "construct ThreadPoolExecutor explicitly."

**Answer:**

- **`newFixedThreadPool(n)`** = TPE(core=max=n, **unbounded LinkedBlockingQueue**). Hazard: unbounded queue — sustained overload becomes unbounded memory + unbounded latency, rejection never fires, max is meaningless (Q42). Fine for bounded/batch workloads; risky as a service's ingest pool.
- **`newCachedThreadPool()`** = TPE(core=0, **max=Integer.MAX_VALUE**, keepAlive 60s, **SynchronousQueue**). Direct handoff: every task gets a waiting or fresh thread. Hazard: unbounded *threads* — a burst or a stalled downstream creates thousands (native-thread OOM, scheduler thrash). Fine for many short-lived tasks with naturally bounded arrival; dangerous as a general utility.
- **`newSingleThreadExecutor()`** — one worker, unbounded queue, **guaranteed serial order** — the poor man's actor: confine state to it and skip locks entirely (Q89). (Wrapped so it can't be reconfigured — unlike a fixed(1) pool.) Same unbounded-queue caveat.
- **`newWorkStealingPool()`** = a **ForkJoinPool** (parallelism = cores): work-stealing deques, unordered, daemon-like usage patterns; for many small independent CPU tasks — *not* a drop-in TPE (no queue-bounding, different semantics — Q79).
- (`newVirtualThreadPerTaskExecutor()` — Java 21+: not a pool at all, one virtual thread per task — Q84.)

**Why "construct explicitly":** the factories hide exactly the decisions that cause outages — queue type/bound and max — so the famous guidance (Alibaba's coding standard, most style guides) requires spelling out all seven parameters: it forces the conversation about capacity, rejection, and naming (Q44) at review time. The factories aren't broken; they encode defaults appropriate to their era's use cases — the discipline is *knowing you've chosen* an unbounded structure rather than inheriting one silently.

**Follow-up trap:** *"When is an unbounded queue actually correct?"* — when the producer is intrinsically bounded (a fixed cron fan-out, N partitions each submitting ≤1 task) or the tasks are tiny and finite (batch chunking) — boundedness must exist *somewhere*; if the caller guarantees it, the queue may relax. Naming where the bound lives is the answer's shape.

---

### Q47. Pool lifecycle: `shutdown()` vs `shutdownNow()` — exact semantics, the canonical graceful-shutdown sequence, how shutdownNow interacts with interruption (Q9), and typical Spring/webapp integration.

**Answer:**

- **`shutdown()`** — stop *accepting* (new submissions rejected), but **run everything already accepted**: active tasks finish, queued tasks still execute. Non-blocking — it flips state and returns.
- **`shutdownNow()`** — stop accepting, **drain the queue** (returns the never-started tasks as `List<Runnable>` — you can log/persist them), and **interrupt** every active worker. "Best-effort stop": tasks that honor interruption (Q9) exit; tasks that swallow it or run uninterruptible loops keep running — shutdownNow *cannot kill* anything (no `stop()` — Q9's deprecation is why). This is the question's hinge: **cancellation quality is a property of the tasks, not the pool.**
- **`awaitTermination(t, u)`** — block until all workers exit or timeout; neither shutdown method waits by itself.

**Canonical sequence** (straight from the javadoc, worth reproducing verbatim in interviews):

```java
pool.shutdown();                                       // no new work; drain queue
try {
    if (!pool.awaitTermination(30, TimeUnit.SECONDS)) { // grace period
        List<Runnable> dropped = pool.shutdownNow();    // interrupt stragglers
        log.warn("Forcing shutdown, {} tasks never ran", dropped.size());
        if (!pool.awaitTermination(10, TimeUnit.SECONDS))
            log.error("Pool did not terminate");        // uninterruptible tasks remain
    }
} catch (InterruptedException e) {
    pool.shutdownNow();
    Thread.currentThread().interrupt();                 // restore (Q9)
}
```

Java 19+: `ExecutorService implements AutoCloseable` — `close()` ≈ shutdown + indefinite await (try-with-resources for scoped pools).

**Integration:** register pools as Spring beans with `@Bean(destroyMethod=...)`/`@PreDestroy` running the sequence (Spring's `ThreadPoolTaskExecutor` has `setWaitForTasksToCompleteOnShutdown`/`setAwaitTerminationSeconds` — the same knobs); tie into container `SIGTERM` → graceful window → `SIGKILL` (your grace period must fit inside Kubernetes' `terminationGracePeriodSeconds` — the ops detail that shows production scars). Forgotten pools = the app that won't exit (non-daemon workers — Q6) or dropped in-flight work on deploys.

**Follow-up trap:** *"What happens to tasks submitted after shutdown?"* — routed to the `RejectedExecutionHandler` (default: `RejectedExecutionException`) — rejection isn't only saturation (Q43); shutdown is the other trigger, and CallerRuns-after-shutdown quietly runs tasks on the caller — a spicy edge case to know.

---

### Q48. Task queues in pools: compare `LinkedBlockingQueue`, `ArrayBlockingQueue`, `SynchronousQueue`, and `PriorityBlockingQueue` as *executor work queues* specifically — ordering, bounding, handoff, fairness — and the trap with priority queues and Futures.

**Answer:**

As pool queues (general BlockingQueue theory in Q52):

- **`LinkedBlockingQueue`** (optionally bounded; unbounded by default): FIFO; two-lock design (put-lock/take-lock) → producers and consumers don't contend with each other — highest pool throughput; node allocation per element (GC churn at extreme rates). Unbounded default = Q42's dead-max hazard; **bounded LBQ is the standard service choice.**
- **`ArrayBlockingQueue`** (always bounded): FIFO ring buffer, single lock (both ends contend), zero per-element allocation, optional **fairness** on the lock. Slightly lower throughput than LBQ under mixed load; predictable memory; good when capacity is the point.
- **`SynchronousQueue`**: capacity **zero** — pure **handoff**: an offer succeeds only if a consumer is actively waiting. In a TPE this means "never buffer — demand a thread now" → the cachedThreadPool recipe: instant latency while threads are available, thread-explosion risk without a sane max (Q46). Use with a *bounded* max + Abort/CallerRuns to build "no queueing, fail fast" pools — a deliberate low-latency design.
- **`PriorityBlockingQueue`**: unbounded heap ordered by comparator — urgent-first scheduling. **The Future trap:** `submit()` wraps your task in a `FutureTask`, which does **not** implement your `Comparable`/comparator → `ClassCastException` at runtime. Fixes: use `execute()` with comparable Runnables, or override `newTaskFor()` to return a comparable FutureTask carrying the priority. Second trap: unbounded (no saturation signal), and **starvation of low-priority work** is the design's own feature-bug (add aging if unacceptable). Also: equal priorities are *not* FIFO — ordering among ties is unspecified.
- (`DelayQueue` — elements become takeable at their delay expiry: the retry/scheduler building block; `LinkedTransferQueue.transfer()` — block until consumed, a stronger handoff.)

Decision summary as a sentence each: bounded LBQ = default; ABQ = strict bounded/fair/no-GC; SQ = no buffering, direct handoff semantics; PBQ = priority with the newTaskFor homework done.

**Follow-up trap:** *"Why does TPE require a BlockingQueue at all — could it poll a ConcurrentLinkedQueue?"* — workers must *block* efficiently when idle (park until work arrives — Q36) and `keepAlive` uses timed `poll`; a non-blocking queue would force spin/sleep polling — worse latency and CPU. The queue's blocking-ness *is* the worker lifecycle mechanism.

---

### Q49. `ScheduledExecutorService`: `scheduleAtFixedRate` vs `scheduleWithFixedDelay`, what happens when a run overruns the period, the silent-death exception rule, and building resilient periodic jobs (including why distributed schedulers exist).

**Answer:**

- **`scheduleAtFixedRate(task, init, period)`** — target *cadence*: runs aim at t₀, t₀+p, t₀+2p… If a run **overruns the period**, the next run starts **immediately after** (late runs queue conceptually — but executions never overlap: a periodic task is scheduled one-at-a-time; there's no concurrent pileup, "catch-up" happens by back-to-back runs, and long-term the schedule *skips* rather than storing infinite debt). Use for rate-meaningful work (metrics emission each 10s).
- **`scheduleWithFixedDelay(task, init, delay)`** — target *gap*: next run starts `delay` after the previous **finishes**. Duration-elastic; cadence drifts. Use for polling loops, cleanup sweeps — anything where "breathing room" matters more than clock alignment. Default choice when unsure (self-protecting under slowness).

**The silent-death rule** (the trap this question exists for): if a periodic task **throws**, the exception is captured into the task's internal Future — which nobody reads — and **all subsequent executions are cancelled, silently**. The job just stops; you learn weeks later. Defense is non-optional:

```java
Runnable safe = () -> {
    try { pollUpstream(); }
    catch (Throwable t) { log.error("poll failed", t); }   // survive to run again
};
ses.scheduleWithFixedDelay(safe, 0, 30, SECONDS);
```

(Catching `Throwable` deliberately here — a periodic must outlive even Errors it can survive; re-throw truly fatal ones after logging if policy demands.)

**Resilient periodic jobs — the checklist:** wrap-and-log (above); timeout the work inside (a stuck poll blocks *its own* future runs — and with a single-threaded SES, every *other* scheduled task too: size the scheduler or isolate critical timers); make runs **idempotent and overlap-tolerant** anyway (redeploys create overlap across instances); monitor "last successful run" as a metric (the only reliable detector of silent death); jitter start times across instances (thundering herd — Q27). And the scaling truth: SES is **per-JVM** — N replicas run N copies; wanting "runs once across the cluster" is a *distributed* scheduling problem → leader election, DB-lock-guarded jobs (ShedLock), or a scheduler service (Quartz clustered/temporal systems) — naming that boundary is the senior close.

**Follow-up trap:** *"`ScheduledThreadPoolExecutor` with corePoolSize=0?"* — historically pathological (spins); it wants ≥1 core threads. Also asked: does fixedRate run concurrent executions when overrunning? — No (single-execution guarantee) — most candidates guess wrong; knowing it cold is a differentiator.

---

### Q50. Concept integration: a service submits tasks to a pool and sometimes "loses" work and "hangs on shutdown." Enumerate the classic executor anti-patterns — swallowed Future exceptions, missing shutdown, ThreadLocal leakage, nested-pool deadlock, unbounded everything — each with its mechanism and fix.

**Answer:**

The greatest-hits audit list (each: mechanism → fix):

1. **Swallowed exceptions via `submit` + ignored Future** — errors live unobserved in the Future (Q8) → "the job silently failed for a month." Fix: always `get()`/consume, or `execute` + UEH, or afterExecute hook, or CompletableFuture with exception stages (Q72).
2. **No shutdown** — non-daemon workers keep the JVM alive (the "hangs on exit" half); or shutdown without awaitTermination → deploy kills in-flight work. Fix: Q47's sequence wired to lifecycle.
3. **ThreadLocal residue in pooled threads** — context bleeds between tasks / leaks memory (Q18) → wrong tenant/user attribution. Fix: set/remove in finally per task; wrapping decorators (Q77).
4. **Nested-pool (thread-starvation) deadlock** — task in pool P blocks waiting on a *sub-task also queued to P*: with all workers doing the waiting, sub-tasks never run → permanent stall at exactly full load (the deadliest, because it needs saturation to trigger). Fix: never block a worker on work scheduled to the *same* pool — separate pools per dependency layer, compose async instead of blocking (Q74), or FJP's managed blockers/join-with-help (Q79). This one deserves its name said aloud: **thread starvation deadlock**.
5. **Unbounded queue/threads** — hidden overload → OOM/latency (Q42/46). Fix: bounded + explicit rejection policy (Q43).
6. **Blocking calls without timeouts in tasks** — a hung downstream permanently consumes workers (pool "shrinks" to zero effective size). Fix: timeouts on everything; bulkheads (Q45).
7. **`Future.get()` without timeout** on the caller side — the submitting thread joins the hang. Fix: `get(t, u)` + handling; deadlines end-to-end.
8. **Shared mutable state across tasks without synchronization** — the pool reintroduces every Section-2/3 bug at scale; tasks should own or receive immutable inputs (Q89).
9. **Fire-and-forget scheduling deaths** (Q49) and **priority-queue ClassCastException** (Q48) as the specialty items.

Frame it as your review method: for any executor in a codebase ask *five questions* — who bounds it, who shuts it down, where do exceptions go, what blocks inside it, what context crosses into it. That rubric answers the scenario ("lost work" = 1/5/6, "hangs" = 2/4/7) and doubles as your closing statement.

**Follow-up trap:** *"How would you detect #4 in production before it bites?"* — load tests at saturation (it only manifests full), thread dumps showing all workers in `get()`/join on the same pool's futures, and static rule "no same-pool blocking joins" in review. Knowing it hides below saturation is the proof of understanding.

---

# Section 6 — Coordination Utilities & Blocking Queues (Q51–Q58)

---

### Q51. `CountDownLatch`: semantics, the two canonical patterns (start-gate and completion-wait), memory visibility guarantees, one-shot nature, and the timeout discipline.

**Answer:**

A latch is a **one-shot gate** over a count (AQS shared mode — Q33): `await()` blocks until the count reaches zero; `countDown()` decrements (never blocks, never goes below zero); once open, **open forever** — later `await()`s sail through, the count can't be reset (that's `CyclicBarrier`/`Phaser` territory — Q54).

**Pattern 1 — completion-wait (N workers, 1 waiter):**

```java
CountDownLatch done = new CountDownLatch(tasks.size());
for (Task t : tasks)
    pool.execute(() -> { try { t.run(); } finally { done.countDown(); } });  // finally!
if (!done.await(30, TimeUnit.SECONDS))                                       // ALWAYS timed
    throw new TimeoutException("only " + done.getCount() + " remaining");
```

`countDown()` in **finally** — a task that throws without counting down leaves the latch short and the waiter hung; this line placement is what the interviewer checks. Timed `await` always: an untimed await inherits every worker's failure modes.

**Pattern 2 — start gate (1 signal, N waiters):** latch(1); N threads `await()`; coordinator does setup then `countDown()` — releases all simultaneously (max-contention test harnesses; "don't serve traffic until warmup done" readiness gates).

**Memory visibility:** actions before `countDown()` happen-before actions after the corresponding `await()` returns — workers' results written to plain fields are safely readable by the waiter, no extra synchronization (the j.u.c. HB contract — Q13.5). Latch as *publication mechanism*, not just a gate.

Design notes: count is fixed at construction (choose it to match real completion events — completions, not attempts); no way to "count up"; a dying-without-countdown worker is unrecoverable except via timeout → for revisable participation you want Phaser (Q54).

**Follow-up trap:** *"Latch vs `join()`?"* — join needs owned Thread references and thread-per-task (useless with pools); latch decouples completion signaling from thread identity. And *"latch vs Future.get in a loop?"* — invokeAll/CompletableFuture.allOf are the modern spellings; the latch remains the primitive underneath and the interview lingua franca.

---

### Q52. The `BlockingQueue` contract: the four-method families (throw/special-value/block/timeout), producer-consumer as *the* decoupling pattern, poison pills for shutdown, and the memory-visibility contract at handoff.

**Answer:**

The interface's method matrix — reproduce it cold:

| | Throws | Special value | Blocks | Times out |
|---|---|---|---|---|
| Insert | `add(e)` (IllegalState) | `offer(e)` → false | `put(e)` | `offer(e,t,u)` |
| Remove | `remove()` (NoSuchElement) | `poll()` → null | `take()` | `poll(t,u)` |
| Examine | `element()` | `peek()` | — | — |

Choosing the column *is* choosing your overload policy at that boundary (block = backpressure; special value = shed/fallback; timeout = bounded patience — Q43's philosophy in miniature).

**Producer–consumer:** producers `put`, consumers `take`; the queue absorbs rate mismatch (burst smoothing), **decouples lifetimes and speeds** (either side can be scaled/replaced independently), converts shared-state concurrency into **transfer-of-ownership**: the producer must not touch an object after handing it off — the object is *confined* to the consumer thereafter (Q89) — which is why this pattern needs no further locking. Bounded capacity is what turns it from a buffer into a *flow-control* mechanism: full queue = producers blocked = upstream slows (say "bounded queues are how backpressure propagates").

**Visibility contract:** actions pre-`put` happen-before post-`take` of that element (Q13.5) — the transferred object's state, though written without locks, is fully visible to the consumer. This single guarantee is why queues are the safest concurrency tool in the box.

**Poison pill shutdown:** a sentinel object enqueued to tell consumers "drain and die":

```java
static final Task POISON = new Task();
// producer at end: for (int i = 0; i < consumerCount; i++) queue.put(POISON);
// consumer loop: Task t = queue.take(); if (t == POISON) break; process(t);
```

Details that score: one pill **per consumer** (each consumer swallows exactly one); pills go in only after real work (FIFO ⇒ drain-then-stop semantics); with multiple producers you need producer-completion coordination before pilling (a latch — Q51). Alternative: interrupt-based shutdown (`take()` throws InterruptedException — Q9) — pills give *graceful drain*, interrupts give *prompt stop*; choose per requirement, name both.

**Follow-up trap:** *"add vs offer on a bounded queue in production code?"* — `add` throws on full (crash-your-producer semantics — rarely intended); `offer` unchecked-dropped-return is a silent-loss bug; the honest options are `put`, timed `offer` with handling, or explicit shed logic. Flagging bare-`offer()`-ignored as a review catch is exactly the practical depth wanted.

---

### Q53. `Semaphore`: permits model, fairness, the resource-pool and bounded-concurrency patterns, `tryAcquire` for shed-vs-wait, why release-without-acquire is legal (and dangerous), and semaphore-vs-lock.

**Answer:**

A semaphore holds N **permits**: `acquire()` blocks until one is free and takes it; `release()` returns one (AQS shared state — Q33). It bounds *how many threads may be inside something at once* — mutual exclusion generalized from 1 to N.

**Pattern 1 — bounded concurrency around a scarce downstream** (the modern headline use, especially with virtual threads where pools no longer bound work — Q84):

```java
Semaphore dbSlots = new Semaphore(30);            // match the connection pool
void query() throws InterruptedException {
    dbSlots.acquire();
    try { runQuery(); } finally { dbSlots.release(); }   // finally — always
}
```

**Pattern 2 — shed instead of wait:** `if (!dbSlots.tryAcquire(200, MILLISECONDS)) return fallback();` — a semaphore + tryAcquire is a poor-man's bulkhead/rate-limiter (Resilience4j's bulkhead is exactly this; token-bucket limiters are "semaphore + scheduled refill" — the connective tissue interviewers reward).

**Pattern 3 — resource pool:** permits track free objects; acquire-then-borrow, return-then-release (object pools, license slots).

**Semantics that distinguish it from a lock:** a semaphore has **no owner** — any thread may `release()`, including one that never acquired (that's how permits can be *created*: `new Semaphore(0)` + release-as-signal is a latch-like handoff). Corollaries: it's **not reentrant** (re-acquiring by the same thread consumes another permit — self-deadlock with 1 permit, Q26's follow-up); nothing stops a buggy double-release **inflating the permit count** — the invariant "release exactly once per acquire, in finally" is on you; there's no condition-wait facility. A binary `Semaphore(1)` is thus *not* a ReentrantLock — no reentrancy, no owner check, but *transferable* release (occasionally exactly what you want: acquire on thread A, release on thread B in async pipelines — locks forbid it).

**Fairness:** constructor flag; nonfair barging vs FIFO handoff, same trade as Q32; fair matters more here because long waits behind bulk acquisitions are common (`acquire(n)` waits for n permits — and in fair mode a big waiter blocks smaller ones behind it: convoy-by-design to prevent starvation).

**Follow-up trap:** *"Implement a bounded buffer with semaphores."* — the textbook trio: `items(0)`, `spaces(cap)`, plus a lock/CHM for the actual structure: put = spaces.acquire → insert → items.release; take = items.acquire → remove → spaces.release. Being able to sketch it shows the primitive is understood, not memorized.

---

### Q54. `CyclicBarrier` and `Phaser`: barrier semantics, the barrier action, `BrokenBarrierException` and failure propagation, reuse across generations — and what Phaser adds (dynamic parties, tiering) that justifies its complexity.

**Answer:**

**CyclicBarrier(n, action):** n parties each call `await()`; all block until the **n-th arrives**, then (a) the optional **barrier action** runs *once*, executed by the last-arriving thread — the safe place for merge/reduce steps between phases (runs in mutual exclusion, before anyone proceeds); (b) all n release together; (c) the barrier **resets automatically** for the next cycle — hence *cyclic*, built for **iterative phased algorithms**: N workers each compute a slice of generation k, barrier, then generation k+1 reads k's results (visibility: pre-await actions HB post-await actions across parties — the barrier is also the memory fence between phases).

**Breakage — the failure model interviewers probe:** if any waiter is interrupted, times out (`await(t,u)`), or the action throws, the barrier **breaks**: every current and future `await` throws `BrokenBarrierException` until `reset()`. Rationale: a phased computation missing one participant is *corrupt* — better to fail all parties loudly than have n−1 threads wait forever for a dead colleague. Handling: treat BBE as "the cohort failed," tear down or rebuild the cohort; `reset()` mid-flight is racy (it breaks current waiters) — mostly for test harnesses.

**Latch vs barrier in one line:** latch = one-shot, count events, waiters ≠ counters (asymmetric); barrier = reusable, count *threads*, the waiters are the participants (symmetric).

**Phaser** — both generalized, with two additions that justify it:

1. **Dynamic parties:** `register()`/`arriveAndDeregister()` — participants may join/leave between phases (latch count and barrier n are fixed forever). The idiomatic replacement for "I don't know how many tasks yet": register before spawn, `arriveAndAwaitAdvance()` per phase, deregister on completion; a phaser with parties hitting 0 terminates.
2. **Per-phase control & non-blocking arrival:** `arrive()` (signal without waiting), `awaitAdvance(phase)` (wait without being a party — observer pattern), phase numbers returned for staleness checks, and overridable `onAdvance(phase, parties)` as the between-phases hook / termination condition.
3. **Tiering:** phasers form trees (child phasers roll up to parents) to cut contention with thousands of parties — a scalability answer barriers don't have.

Cost: a genuinely harder API — use latch/barrier when they fit; Phaser when parties are dynamic or phases need control. Interrupt behavior differs too (arriveAndAwaitAdvance is not interruptible by default — `awaitAdvanceInterruptibly` exists): a detail worth one sentence.

**Follow-up trap:** *"Barrier action throws — who sees what?"* — the barrier breaks; the action-runner gets the exception, all others get BrokenBarrierException — asymmetric outcomes from one failure, so the action must be as close to infallible as the domain allows (or do the risky work after release, per-thread).

---

### Q55. `SynchronousQueue`, `Exchanger`, and `TransferQueue`: the rendezvous family. Semantics of each, where handoff (vs buffering) is the right architecture, and the cachedThreadPool connection.

**Answer:**

The family's common idea: **no capacity — synchronization *is* the transfer**; producer and consumer must meet in time.

- **`SynchronousQueue`:** every `put` waits for a `take` and vice versa (dual-stack/dual-queue lock-free internals; optional fairness). Zero buffering means: transfer latency is minimal (no queue residency), *and* the producer gets perfect knowledge — "my item is being handled *now*." As the cachedThreadPool's queue (Q46): `offer` succeeds **only if a worker is already waiting** — that's the mechanism that converts "no idle worker" into "create a thread," i.e., the pool's elasticity is literally the queue's handoff semantics surfacing through TPE's algorithm (Q42 step 2). Use directly when buffering would *lie* — e.g., work must not be accepted unless someone is actually free (admission control with zero queue debt).
- **`Exchanger<V>`:** a **bidirectional** rendezvous for exactly two threads: each calls `exchange(x)` and receives the other's object. Canonical use: **double-buffering** — a filler thread swaps its full buffer for the emptier thread's empty one, endlessly recycling two buffers with no allocation and no shared-buffer locking (genomics/log pipelines). Niche but memorable; the visibility contract rides along as always.
- **`LinkedTransferQueue`:** superset — behaves as an unbounded queue (`put`/`poll`) *plus* transfer semantics: **`transfer(e)`** blocks until a consumer receives the element (not merely enqueued — *consumed*); `tryTransfer(e)` hands off only to an already-waiting consumer (else immediate false); `hasWaitingConsumer()` lets producers adapt (transfer when someone's ready, else enqueue/spill). Use when producers need **delivery confirmation** as a first-class signal — message-passing designs where "queued" ≠ "accepted."

**Architecture framing:** buffering optimizes throughput and decouples rates (Q52); handoff optimizes **latency and truthfulness of backpressure** — the producer blocks precisely when the system is actually not consuming (no queue to hide it). Choose handoff at admission boundaries and for elasticity triggers; choose buffers inside pipelines. That one-sentence contrast is the answer's spine.

**Follow-up trap:** *"SynchronousQueue vs TransferQueue.transfer — same thing?"* — for the blocking path nearly; TQ adds the mixed mode (can also buffer) and probing — SQ is the pure primitive, TQ the pragmatic hybrid. And: *"fair SynchronousQueue — when?"* — FIFO matching matters when handoff order is user-visible (request admission), at the usual throughput cost.

---

### Q56. Design a rate limiter three ways — synchronized token bucket, semaphore + refiller, and lock-free CAS on packed state — and compare correctness, contention behavior, and burst semantics. (The classic "build it live" concept question.)

**Answer:**

**Token bucket semantics first** (say this before coding): capacity C tokens, refill r/sec; a request consumes one; empty bucket → reject (or wait). Bursts up to C absorbed; sustained rate bounded to r.

**V1 — synchronized, lazy refill (correct, simple — start here live):**

```java
class TokenBucket {
    private final long capacity; private final double refillPerNano;
    private double tokens; private long lastRefill;         // @GuardedBy(this)

    synchronized boolean tryAcquire() {
        long now = System.nanoTime();
        tokens = Math.min(capacity, tokens + (now - lastRefill) * refillPerNano);
        lastRefill = now;
        if (tokens >= 1) { tokens -= 1; return true; }
        return false;
    }
}
```

Lazy refill (compute on demand from elapsed time) beats a scheduler thread: no background work, no drift, exact. One lock, tiny critical section — correct under all interleavings; contention only matters at very high call rates.

**V2 — Semaphore + scheduled refiller:** permits = tokens; `tryAcquire()` per request; a `ScheduledExecutorService` releases `min(r·Δt, capacity − available)` periodically. Pros: trivially readable; timed-wait ("acquire or wait up to t") for free. Cons: refill *granularity* = scheduler period (bursty edge behavior), needs the release-cap logic to not overfill (release is unguarded — Q53's inflation hazard), a live thread, and Q49's silent-death risk on the refiller — enumerate these, they're the actual evaluation.

**V3 — lock-free:** pack state into one `AtomicLong` (e.g., 32 bits token-count-in-millitokens + 32 bits truncated-timestamp) or an `AtomicReference<State>` record; CAS loop: read → compute refill+consume → CAS, retry on conflict (Q37/40). Pros: no blocking, scales under heavy readers. Cons: packing/precision fiddliness, retry storms at extreme contention (Q37's caveat), and materially harder to review — say explicitly that V1 wins until profiling proves the lock hot ("correct and simple, then fast").

**Comparisons to volunteer:** burst semantics identical (bucket property, not implementation); waiting versions — V2 natural, V1 needs wait/notify or sleep-retry, V3 needs park+queue (you're rebuilding a semaphore — recognize it, Q40's close); distributed limiting is a different problem entirely (shared state in Redis + Lua atomicity — name the boundary). Guava `RateLimiter` (smooth-warmup variants) as the "in production I'd reach for" citation.

**Follow-up trap:** *"Why nanoTime, not currentTimeMillis?"* — monotonicity: wall clock jumps (NTP) would mint or destroy tokens; `nanoTime` is monotonic-per-JVM, exactly for elapsed-time math. A two-line answer that quietly certifies production experience.

---

### Q57. Producer–consumer at system scale: how the in-JVM pattern (queues, backpressure, poison pills) maps onto message brokers — and what new concerns (delivery guarantees, idempotency, ordering, DLQs) appear when the queue leaves the process.

**Answer:**

The mapping is direct and interviewers love it drawn explicitly: `BlockingQueue` → broker topic/queue (Kafka, RabbitMQ, SQS); bounded capacity → retention/queue limits + broker flow control; `put` blocking → producer flow control/acks; consumer `take` loop → consumer group poll loop; poison pill → consumer-group shutdown/rebalance protocols; the pool of consumers → competing consumers/partitions.

**What changes when the queue crosses the process boundary:**

1. **The handoff visibility guarantee is gone** — replaced by *serialization*: the object graph must be encoded (schema versioning, compatibility — a whole discipline the in-JVM queue never needed).
2. **Delivery guarantees become explicit:** in-JVM transfer is exactly-once trivially; brokers offer at-most-once / at-least-once / (transactional) effectively-once — and the practical stance: **build for at-least-once + idempotent consumers** (dedupe keys), "exactly-once is a contract between your idempotency and the broker's retries" (echoing the outbox discussion in your JPA set — cross-reference it in interviews, it lands).
3. **Ordering shrinks to a partition/key:** the global FIFO of one queue becomes per-key ordering at best; design keys around the invariant needing order (per-aggregate), accept cross-key reordering.
4. **Failure handling gets a new organ:** the **dead-letter queue** — bounded retries then quarantine (the distributed fix for Q27's requeue livelock); plus visibility timeouts/redelivery (crash between receive and ack ⇒ redelivery ⇒ idempotency again).
5. **Backpressure becomes lag:** a full in-JVM queue blocks producers *now*; a broker absorbs (that's its job) and the signal moves to **consumer lag metrics** — monitoring replaces blocking as the feedback line; sustained lag = scale consumers (up to partition count — the parallelism bound) or shed upstream.
6. **The buffer is durable:** restarts don't lose work (the in-JVM queue's fatal flaw for critical tasks) — which is *why* the pattern migrates out of process once work must survive a deploy.

The synthesis sentence: the concurrency concepts don't change — bounded buffers, ownership transfer, backpressure, poison-message policy — they just acquire operational names; strong candidates demonstrate the concept once in-JVM and then translate fluently.

**Follow-up trap:** *"When is the in-JVM queue still the right choice over a broker?"* — sub-millisecond latency, work not worth durability, no cross-service fan-out, simplicity (no infra) — a boundary answer, not a technology preference.

---

### Q58. Build a bounded, restartable pipeline: stage A (parse) → stage B (enrich, calls remote API) → stage C (write). Choose queue types and sizes, pool shapes, shutdown order, and failure policy — and defend the design under the interviewer's "what if B's API hangs?" probe.

**Answer:**

**Topology:** three stages, two bounded queues between them — SEDA-style (staged event-driven architecture — name it):

```
[A: parse, CPU-ish, pool=cores] → Q1 (ArrayBlockingQueue 512)
→ [B: enrich, I/O-bound, pool sized by Little's Law vs API limits (Q45)] → Q2 (ABQ 512)
→ [C: write, pool matched to sink (DB batch width)]
```

**Why per-stage pools + bounded queues:** independent sizing per bottleneck (B needs 50 threads, A needs 8 — one shared pool couldn't express that, Q45's bulkhead logic); bounded queues make backpressure *propagate stage-by-stage*: C slows → Q2 fills → B's `put` blocks → Q1 fills → A blocks → ingestion slows. The system's intake rate automatically becomes its slowest stage's rate — no OOM, no silent loss. Queue size = burst absorption vs memory vs staleness (512 ≈ seconds of buffer at expected rates; state that sizing is a latency-budget decision, not a magic number).

**"What if B's API hangs?"** — the probe, answered in layers: (1) every remote call has a **timeout** (no exceptions to this rule — Q50.6); (2) B's threads bounded → at worst B's pool is fully stuck for the timeout duration, upstream blocks (bounded damage — the bulkhead holds); (3) circuit breaker on the API: open → B fails fast → per-item failure policy (retry with backoff+jitter n times → then dead-letter the item to an error store and continue — never wedge the pipeline on one item, Q27/57); (4) lag/queue-depth metrics alert long before users do.

**Shutdown order (the elegance test):** stop intake → A drains and poison-pills Q1 (one per B-worker — Q52) → B drains, pills Q2 → C drains → pools shut down in stage order with Q47's sequence. Draining forward in topological order loses nothing; interrupt-based fast-stop as the abort variant. **Restartability:** stages idempotent + a progress marker (offset/checkpoint) at C — after crash, replay from checkpoint; queues are volatile so anything critical is re-derivable from the source (or you promote Q1/Q2 to a broker — Q57's migration criterion, stated as a conscious trade).

Close with the alternatives acknowledgment: this is hand-rolled SEDA; today you'd weigh reactive streams (built-in backpressure protocol), Kafka-between-stages (durability), or virtual-threads-per-item with semaphore bulkheads (Q84) — the *concepts* (bounds, ownership handoff, per-stage isolation, drain-order shutdown) are identical in every dressing, which is the point of the question.

**Follow-up trap:** *"Why ABQ not LBQ here?"* — predictable fixed memory, no per-element allocation at steady high rate, fairness option if needed (Q48) — but concede LBQ's two-lock throughput edge is real; either defended beats either asserted.

---

# Section 7 — Concurrent Collections (Q59–Q66)

---

### Q59. `ConcurrentHashMap` internals and contract: how it achieves concurrency post-Java-8 (bins, CAS, synchronized on head nodes, treeification), what operations are atomic, the rules inside `compute`, and what `size()`/iterators actually promise.

**Answer:**

**Java 8+ design** (the segment/16-locks answer is a decade stale — say so): an array of bins; operations touch only their bin. **Empty-bin insert = pure CAS** on the array slot (no lock at all); non-empty bin → **`synchronized` on the first node** of that bin — the lock granularity is *one bucket*, so contention requires two threads hashing to the same bin. Long collision chains (≥8, table ≥64) **treeify** into red-black trees — O(log n) worst case, which is also the defense against hash-collision DoS. Resize is **cooperative**: multiple threads help transfer bins (forwarding nodes mark moved bins). Reads are **lock-free** — `get` traverses via volatile reads, never blocks, and can proceed *during* writes and resizes.

**Atomicity contract** — atomic: `put`, `putIfAbsent`, `remove(k,v)`, `replace(k,old,new)`, and the **compute family** (`computeIfAbsent`, `computeIfPresent`, `compute`, `merge`) — each runs its function **atomically per key** (under the bin lock): the fix for every check-then-act (Q10):

```java
map.computeIfAbsent(key, k -> new Counter()).increment();   // atomic get-or-create
map.merge(word, 1L, Long::sum);                              // atomic upsert-accumulate
```

**Rules inside compute functions** (each is an interview checkpoint): keep them **short and non-blocking** — the bin lock is held (a slow remote call inside compute stalls every key in that bin); **no re-entrant mutation of the same map** inside the function (undefined/deadlock — computeIfAbsent-inside-computeIfAbsent famously breaks); functions **may not run** if mapping state short-circuits, and `computeIfAbsent` returns the existing value without calling the function; null return = remove/absent (CHM forbids null keys/values *precisely* so null can mean "absent" unambiguously — the "why no nulls" answer).

**Weak guarantees:** `size()`/`isEmpty()` are estimates under concurrency (striped `CounterCell` sums — moving targets, Q39's LongAdder inside!); iterators are **weakly consistent** — never throw `ConcurrentModificationException`, reflect some-state-since-creation, may or may not see concurrent updates (vs fail-fast — Q63). Bulk ops (`forEach`, `search`, `reduce` with parallelismThreshold) run over a moving world — fine for stats, wrong for invariant checks.

**Follow-up trap:** *"Is `if (!map.containsKey(k)) map.put(k,v)` safe on CHM?"* — No — thread-safe map, racy composite (Q10); `putIfAbsent`. The oldest trap in the book, still catching people.

---

### Q60. `CopyOnWriteArrayList`/`Set`: the copy-on-write mechanism, why iteration is snapshot-consistent and lock-free, the write-cost math, the listener-list sweet spot, and when COW becomes an anti-pattern.

**Answer:**

**Mechanism:** the backing array is **immutable**; every mutation locks, copies the entire array with the change applied, and swaps the (volatile) reference. Readers and iterators grab the current array reference once and traverse it **with no synchronization ever** — a live application of immutable-snapshot publishing (Q17): reads are wait-free, iteration never throws `ConcurrentModificationException`, and an iterator sees a perfectly consistent **frozen snapshot** (mutations during iteration are simply invisible to it; iterator `remove()` is unsupported — the snapshot isn't the live list).

**Cost math:** every write is O(n) copy + allocation → sane only when **reads massively outnumber writes and the list is small**. The canonical fit: **listener/observer registries** — mutated at startup/rarely, iterated on every event by many threads, and (crucially) iteration-during-mutation must not break — exactly COW's guarantees; this is why the JDK and every event bus use it internally.

**Semantics worth stating:** readers may act on slightly **stale** membership (a listener removed mid-broadcast may still receive the in-flight event — document that contract; it's usually acceptable and *unavoidable* in any snapshot design); memory: transient 2× array during copy, and old snapshots live while iterators hold them; `CopyOnWriteArraySet` = COW list + linear-scan contains (small sets only).

**Anti-pattern territory:** frequent writes (copy storm → GC pressure, quadratic bulk-add behavior — `addAll` once beats add-in-loop), large lists, or write-latency-sensitive paths; needing per-element updates (copy per set()); using it as a general "thread-safe List" because it's the only one in the box — the honest alternatives: `Collections.synchronizedList` + locked iteration (Q63), `ConcurrentLinkedQueue`/deque if order+queue semantics fit, or snapshotting an ordinary list yourself under a lock at a coarser grain (batch mutations, publish once — hand-rolled COW at the right granularity).

**Follow-up trap:** *"Why is there no ConcurrentArrayList?"* — random-access mutable semantics (positional set/add with shifting) don't decompose into per-element concurrency the way hash bins do; every design either locks globally (synchronizedList), snapshots (COW), or changes the abstraction (queues/deques). "The data structure's contract, not the JDK's laziness" is the answer's core.

---

### Q61. `ConcurrentLinkedQueue`/`ConcurrentLinkedDeque` vs blocking queues: Michael–Scott lock-free queues at concept level, when non-blocking wins, the `size()` trap, and why "CLQ + spin loop" is usually the wrong consumer design.

**Answer:**

**`ConcurrentLinkedQueue`** = the Michael–Scott lock-free queue: unbounded linked nodes; `offer` CASes the tail's next pointer, `poll` CASes the head forward; helping (threads finish each other's half-completed operations — a lagging tail is advanced by whoever notices) gives **lock-free progress** (Q37's definition: someone always completes). No capacity, no blocking — operations always return immediately.

**When it wins:** many threads exchanging short-lived items where *nobody should ever park* — event accumulation drained periodically, free-lists, hand-off buffers inside frameworks; latency profiles where a park/unpark (µs) dwarfs the CAS (ns). Also: usable at places where blocking is forbidden (inside locks — carefully; signal handlers-ish contexts).

**The traps:**

1. **`size()` is O(n)** — walks the whole list, and it's stale-by-construction anyway (moving target). Using `size()` in a loop condition is an accidental O(n²); use `isEmpty()` or count externally (LongAdder — Q39).
2. **No backpressure:** unbounded — producers never slow; overload becomes memory (the Q42/Q46 hazard reborn); if you need a bound you'll rebuild blocking semantics — just use `LinkedBlockingQueue`.
3. **The consumer problem:** with no blocking `take`, a consumer must poll:

```java
while (running) {
    Task t = queue.poll();
    if (t == null) { /* now what? */ }    // spin = 100% CPU; sleep = added latency
    else process(t);
}
```

Spin burns a core; `sleep(1)` adds up to 1ms latency and still wastes wakeups; `Thread.onSpinWait()` helps micro-waits but not the design. The correct fixes are *structural*: use a BlockingQueue (park-on-empty is the feature — Q48's follow-up), or pair CLQ with an explicit signal (semaphore released per offer — at which point you've rebuilt LBQ). "CLQ is for when consumers *naturally* revisit (periodic drain), not for dedicated consumer threads" is the design sentence.

`ConcurrentLinkedDeque` adds double-ended ops (work-stealing-style patterns — steal from one end, own from the other — Q79's deque concept, generalized).

**Follow-up trap:** *"Is CLQ faster than LBQ?"* — uncontended and non-blocking paths, yes; but LBQ's two-lock design is excellent, and the moment you need waiting or bounding, LBQ (or ABQ) is *correct* where CLQ needs scaffolding. Benchmark-with-your-pattern, not folklore — the calibrated close.

---

### Q62. `ConcurrentSkipListMap`/`Set`: why the concurrent sorted map is a skip list and not a red-black tree, the probabilistic structure at concept level, its navigation API, and where sorted concurrency actually appears in systems.

**Answer:**

**Why not a tree:** balanced trees (RB/AVL) fix invariant violations with **rotations** that restructure multiple nodes atomically — under concurrency that means locking whole subtrees (contention, complexity) or heroic lock-free rotation algorithms (impractical). A **skip list** needs only *local* pointer updates: each node has a random tower of forward pointers (level i present with p≈1/2 per level); insertion links one node into a few singly-linked lists — each link a CAS; deletion marks then unlinks (logical-then-physical, the Harris technique — `AtomicMarkableReference` conceptually, Q38). Expected O(log n) search/insert/delete via probabilistic balance — **no global rebalancing ever**, which is the entire reason it's the concurrent choice (say exactly this sentence).

**API surface** (`ConcurrentNavigableMap`): sorted iteration, `firstEntry/lastEntry`, `ceilingKey/floorKey/higher/lower`, range views (`subMap/headMap/tailMap` — live, weakly-consistent views), `pollFirstEntry/pollLastEntry` (atomic remove-min/max — the concurrent priority-queue-with-lookup move). Iterators weakly consistent (Q59's semantics).

**Where it appears in real systems** (the part that separates read-the-javadoc from built-things): time-indexed data — a map keyed by timestamp with range scans ("events in the last 5 minutes" = `tailMap(now − 5m)`) under concurrent appends; schedulers/timeout wheels' simpler cousins (poll earliest deadline — `pollFirstEntry`); order books (price-sorted levels, concurrent inserts/cancels); leaderboards/rankings; LSM-tree **memtables** (skip lists in RocksDB/Cassandra — same data structure, same reasons — a name-drop that lands hard). Versus alternatives: `TreeMap` + lock serializes; `PriorityBlockingQueue` gives only min-access without lookup/range (and its iterator is unordered! — a classic gotcha worth attaching here); CSLM is what you use when you need *sorted + concurrent + navigable* simultaneously.

Costs: higher memory per entry (towers), constants worse than CHM (use CHM whenever order is unneeded), `size()` O(n)-ish weak as usual.

**Follow-up trap:** *"Atomic 'process and remove the earliest entry'?"* — `pollFirstEntry()` is the atomic take; `firstEntry()` then `remove()` is check-then-act racy under competitors (Q10, applied to a sorted map). The question is a re-skin of the oldest trap — recognizing that pattern across structures is the actual skill.

---

### Q63. `Collections.synchronizedX` wrappers vs concurrent collections: what the wrapper actually does, the mandatory manual-lock-on-iteration rule, fail-fast iterators and what `ConcurrentModificationException` really detects, and the migration decision table.

**Answer:**

**The wrapper:** decorates every method with `synchronized(mutex)` (one mutex for the whole structure). Individual operations become thread-safe; **compound operations do not** (check-then-act across two calls — Q10, Q22.2) — and **iteration is a compound operation**: the iterator makes many `next()` calls with the lock released between them. The documented, mandatory idiom:

```java
List<String> list = Collections.synchronizedList(new ArrayList<>());
synchronized (list) {                    // lock the WRAPPER (it is the mutex)
    for (String s : list) process(s);    // else: CME or corruption under writes
}
```

(Locking the wrapper object specifically — the wrapper's own methods use it as the mutex; the underlying list must never be touched directly.)

**Fail-fast iterators:** ArrayList/HashMap iterators carry an expected `modCount`; structural modification outside the iterator bumps it → next operation throws **`ConcurrentModificationException`**. Precision points: it's **best-effort debugging**, not a guarantee (races on modCount itself can miss); it fires for *single-threaded* violation too (modify-while-iterating in one thread — the most common CME!); and its absence proves nothing. Contrast: **weakly consistent** iterators (CHM, CLQ, CSLM — Q59) never throw, tolerate concurrent mutation, show a some-state view; **snapshot** iterators (COW — Q60) show frozen state. Three iterator consistency models — naming all three is the complete answer.

**Wrapper vs concurrent — decision table:** wrappers = full-structure exclusion (single global lock): simple, supports any List/Map/Set semantics, gives you a lock to *extend* for compound ops (that's their one real advantage: `synchronized(list){ check; act; }` composes) — but serializes everything (no read concurrency) and iteration locks the world (long iterations starve writers). Concurrent collections = per-bin/lock-free machinery: scale, weakly-consistent iteration without blocking, atomic compounds built-in (compute family) — but you can't bolt *arbitrary* compound atomicity onto them (no global lock exists to take!). So: hot shared map → CHM, always; a structure needing custom multi-step invariants under one lock → synchronized wrapper (or explicit lock + plain collection, clearer); legacy `Hashtable`/`Vector` = the same global-lock design with the same flaws — mention only to bury them.

**Follow-up trap:** *"Can you get CME from a synchronizedList?"* — yes: unlocked iteration while another thread mutates — the wrapper doesn't protect iteration (the whole point of the idiom); and even single-threaded remove-in-for-each throws. Both variants get asked; both are the same lesson.

---

### Q64. `BlockingDeque` and work patterns; `NavigableMap` recap aside — the real focus: choosing collections by *access pattern*. Build the decision tree an interviewer wants: keyed lookup, FIFO transfer, ordered traversal, mostly-read registry, priority, delayed.

**Answer:**

The meta-skill this question tests: collections are chosen by **access pattern + consistency need**, not by "what's thread-safe." The decision tree, verbalized:

1. **Keyed lookup/update, no order** → `ConcurrentHashMap` (default answer for shared state, with compute-family atomics — Q59).
2. **Keyed + sorted/range/nearest** → `ConcurrentSkipListMap` (Q62).
3. **Transfer between threads (FIFO), with flow control** → bounded `BlockingQueue` (ABQ/LBQ — Q48/52); zero-buffer handoff → `SynchronousQueue`/`transfer` (Q55); both ends needed → **`LinkedBlockingDeque`** (bounded, blocking at either end — the underrated one: bounded work-stealing, undo stacks, "process newest first but steal oldest").
4. **Non-blocking multi-producer accumulation, periodic drain** → `ConcurrentLinkedQueue`/`Deque` (Q61 — with its consumer caveat).
5. **Read-mostly registry/iteration-heavy small list** → `CopyOnWriteArrayList` (Q60).
6. **Priority dispatch** → `PriorityBlockingQueue` (unbounded! comparator! unordered iterator! — Q48) ; **time-triggered** → `DelayQueue` (elements takeable at expiry — retry queues, session expiry, token refresh — worth one concrete sentence: element implements `Delayed`, `take()` parks until the earliest expires).
7. **Set semantics** → backed views: `ConcurrentHashMap.newKeySet()`, `ConcurrentSkipListSet`, `CopyOnWriteArraySet` — each inherits its backer's profile.
8. **Nothing shared at all** — the best row of the table: confine a plain `ArrayList`/`HashMap` to one thread (Q89), or share an immutable `List.of(...)` snapshot (Q17). Reaching for a concurrent collection when confinement/immutability suffices is itself an anti-pattern — close the tree with this.

Cross-cutting checks before finalizing any row: bounded or not (who provides backpressure — Q43)? iterator consistency needed (snapshot/weak/locked — Q63)? compound invariants (does the structure's atomic vocabulary cover them, or do you need an external lock — Q63)? size/emptiness semantics (estimates on lock-free structures — Q59/61)? memory/GC profile (COW copies, skip-list towers, node allocation)?

**Follow-up trap:** *"Shared LRU cache — which collection?"* — trick: none directly; `LinkedHashMap` (access-order) isn't thread-safe and synchronizing it serializes; real answer = Caffeine (striped, window-TinyLFU) or design your own segmented LRU — recognizing "the JDK doesn't ship this; here's the industrial solution" is the expected escape from the menu.

---

### Q65. Maps under concurrency — the pathology collection: `HashMap` corrupted by concurrent puts (what actually breaks), why `get` can loop forever pre-Java-8, `HashMap` vs `Hashtable` vs `CHM` in one table, and null-handling rationale.

**Answer:**

**Concurrent `HashMap` mutation — what actually breaks:** unsynchronized puts race on bucket links and on **resize**. Pre-Java-8's transfer reversed node order per bucket; two threads resizing concurrently could create a **cycle in a bucket's linked list** → a later `get` for an absent key on that bucket **spins forever** traversing the loop — the legendary 100%-CPU HashMap bug (Xing/old-war-story status; describing the mechanism, not just "infinite loop," is the flex). Java 8 changed transfer (order-preserving split into lo/hi lists) so *that* cycle is gone, but concurrent use remains broken: **lost updates** (two puts to one bucket, one link overwritten), **lost entries after racing resizes**, size drift, and reads seeing half-linked structure — "data race = all bets" (Q11). `HashMap` is corrupted *silently*; nothing fail-fasts writes.

**The table** (deliver as prose): `Hashtable` — every method synchronized on `this` (global lock): thread-safe per-op, serialized throughput, compound ops still racy, iteration lock-the-world, legacy (superseded, like `Vector`). `Collections.synchronizedMap` — same model, composable mutex (Q63). `ConcurrentHashMap` — per-bin machinery, lock-free reads, atomic compute vocabulary, weakly-consistent iteration (Q59): the default. `HashMap` — fastest, single-threaded/confined/immutable-published only.

**Null rationale** (asked constantly): CHM and Hashtable forbid null keys/values because in a concurrent map, `get(k) == null` **must** unambiguously mean "absent" — with nulls allowed, disambiguating requires `containsKey` + `get`, a check-then-act that concurrency invalidates between the calls (Q10). `HashMap` allows nulls because single-threaded code *can* do the two-step check consistently. Doug Lea's stated reason — recounting it as a design principle ("APIs for concurrency remove ambiguous states") upgrades the answer. Bonus adjacency: `computeIfAbsent` returning null = "don't map" (Q59) — the null-means-absent convention doing load-bearing work.

**Follow-up trap:** *"You inherit code doing `synchronized(hashMap){...}` everywhere — migrate to CHM?"* — audit first: if any block guards a *multi-op invariant*, naive CHM migration **removes** that atomicity (CHM has no global lock to extend — Q63); translate each block to compute-family equivalents or keep an external lock for the compound paths. "Migration is an atomicity audit, not a find-replace" — the senior sentence.

---

### Q66. Concept capstone for collections: design an in-memory metrics registry — `counter(name).inc()` hot path from hundreds of threads, periodic scrape iterating everything, occasional dynamic registration — choosing every structure and justifying via the access-pattern tree (Q64).

**Answer:**

**Decompose by access pattern first** (the method is the answer): (a) name → counter lookup: read-ultra-hot, write-rare (registration) → **CHM**; (b) the counter's own increment: write-ultra-hot, read-rare (scrape) → **LongAdder** (Q39 — precisely its contract); (c) scrape: iterate-all, tolerate slight staleness → CHM's weakly-consistent iteration is *exactly* sufficient (a metrics scrape over a moving world is the canonical acceptable-staleness case); (d) registration: get-or-create must be atomic → `computeIfAbsent`.

```java
final class MetricsRegistry {
    private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();

    public LongAdder counter(String name) {
        LongAdder a = counters.get(name);                 // fast path: no lambda alloc
        return a != null ? a : counters.computeIfAbsent(name, n -> new LongAdder());
    }
    public Map<String, Long> scrape() {
        Map<String, Long> out = new HashMap<>(counters.size());
        counters.forEach((n, a) -> out.put(n, a.sum()));  // weakly consistent — fine here
        return out;                                       // immutable-ish snapshot to caller
    }
}
```

**Justifications to narrate:** the get-before-computeIfAbsent fast path avoids lambda allocation and any bin-lock touch on the hot path (a real Micrometer-grade optimization — mention that `computeIfAbsent` even when present can contend on the bin in older JDKs, improved since, but the pre-check is still idiomatic); `LongAdder.sum()` staleness is a *stated contract* with scrape consumers (Q39); scrape copies into a plain map — callers get a stable snapshot, registry internals never escape (encapsulation as a concurrency tool); no global lock exists anywhere — nothing to deadlock, nothing to convoy.

**Extension probes and answers:** *histograms/gauges* — same registry pattern, value type changes (LongAdder → striped reservoir / callback ref); *"remove stale metrics"* — now iteration + removal interplay: CHM iterators support safe concurrent `remove` via the map (weakly consistent — no CME), but "stale" detection is a time-based compound → store lastUpdated in the value object, sweep with `entrySet().removeIf(...)` accepting benign races (a counter incremented between check and remove → next inc re-registers: idempotent by design — *designing the race to be benign* is the highest-value sentence in the answer); *"tags/dimensions"* — key becomes an immutable composite (record with proper equals/hash — Q17), CHM unchanged.

**Follow-up trap:** *"Why not `Collections.synchronizedMap` — the writes are rare anyway?"* — the *reads* are the hot path, and the wrapper serializes reads too (Q63); CHM's lock-free `get` is the entire performance story. Rare-writes arguments justify COW-style structures only when iteration dominates lookup — here lookup dominates, so CHM. Pattern-matching the argument form, not just the answer, is what's being graded.

---

# Section 8 — Future, CompletableFuture & Asynchronous Pipelines (Q67–Q76)

---

### Q67. `Future<V>`: the contract (`get`, timed `get`, `cancel`, `isDone`), what `cancel(true)` actually does, the blocking-consumption problem that motivated CompletableFuture, and `FutureTask`'s role.

**Answer:**

A `Future` is a **handle to a pending result**: `get()` blocks until completion (returning the value or throwing `ExecutionException` wrapping the task's failure — Q8's swallow mechanism, from the consumer side); `get(t, u)` adds a deadline (`TimeoutException` — note: **timeout does not cancel the task**; it just stops *your* wait — cancel explicitly if abandoning it, or the work runs on invisibly, a classic leak); `isDone()` — completed in *any* fashion (success/failure/cancel); `cancel(mayInterruptIfRunning)`:

- not yet started → prevented from ever running (queue-removal semantics vary; TPE has `setRemoveOnCancelPolicy` for scheduled tasks);
- running + `cancel(false)` → merely marked cancelled; the code runs to completion anyway;
- running + `cancel(true)` → the worker thread is **interrupted** — cooperative as ever (Q9): a task that ignores interruption is uncancellable. After cancel, `get()` throws `CancellationException`.

**`FutureTask`** = the standard implementation: a `RunnableFuture` — a Runnable that runs your Callable and stores outcome; `submit()` wraps with it (the wrapping behind Q48's priority-queue trap); usable standalone for run-once-many-wait memoization (JCiP's cell-caching example: put a FutureTask in a map via `putIfAbsent`, exactly one thread computes, all callers `get()` — deduplicated computation, worth citing).

**The motivating flaw:** `Future` is **pull-only** — no callback, no composition. To react you must block a thread in `get()` (defeating async), poll `isDone()` (busy-wait), or dedicate threads to waiting — and combining futures ("when both finish", "first of", "then transform") requires hand-rolled latch machinery. Async programming needs *push*: "when done, run this" — which is precisely `CompletionStage`/`CompletableFuture` (Q68). One-sentence history: `Future` (Java 5) models *a result*; `CompletableFuture` (Java 8) models *a computation graph*.

**Follow-up trap:** *"`isDone()` true — is the result good?"* — no: done covers success, exception, and cancellation; you learn which only by calling `get()` (or `state()` in Java 19+'s `Future` additions — `resultNow()`/`exceptionNow()`, worth naming for currency).

---

### Q68. `CompletableFuture` fundamentals: creation (`supplyAsync`/`runAsync`/`completedFuture`), the default executor and why relying on it is risky, manual completion (`complete`, `completeExceptionally`) and what "completable" enables.

**Answer:**

**Creation forms:** `supplyAsync(supplier)` — run on an executor, complete with the result; `runAsync(runnable)` — Void variant; `completedFuture(v)` / `failedFuture(e)` — already-done constants (adapters, caches, test doubles); `new CompletableFuture<>()` — an **empty promise** completed by whoever you wire up.

**Default executor:** the no-executor overloads use **`ForkJoinPool.commonPool()`**. Risks to recite: (1) it's **shared JVM-wide** — your blocking I/O task starves everyone else's parallel streams and CFs (and vice versa); (2) sized to `cores − 1` — catastrophic for I/O-bound fan-out (12 slots for 500 remote calls); (3) in containers, cores may be 1 → common pool ≈ zero parallelism (with parallelism ≤ 1 it even runs tasks in the caller thread); (4) its threads are daemon — JVM can exit under in-flight work (Q6). Rule: **always pass an explicit, named, bounded executor** for anything beyond pure small CPU work:

```java
CompletableFuture<Quote> q = CompletableFuture.supplyAsync(() -> fetchQuote(id), ioPool);
```

**Manual completion — the "completable" superpower:** any thread can settle the promise: `complete(value)`, `completeExceptionally(ex)` (first settle wins; later calls return false), `obtrudeValue` (test-only override — mention, don't use). This lets you **adapt any callback/event world into the CF graph**: NIO handlers, message listeners, timers:

```java
CompletableFuture<Response> cf = new CompletableFuture<>();
client.send(request, (resp, err) -> {              // callback API
    if (err != null) cf.completeExceptionally(err);
    else cf.complete(resp);
});
return cf;                                          // now composable like any async op
```

— plus building blocks like timeout injection (a scheduler calling `completeExceptionally(new TimeoutException())` — exactly how `orTimeout` works, Q73) and test stubbing (hand the code a CF you settle at will). "A CF is a one-shot, thread-safe, multi-subscriber promise; `supplyAsync` is just a convenience producer for it" — the framing sentence.

**Follow-up trap:** *"Who runs the dependent stages of a manually-completed CF?"* — non-async dependents run **on the completing thread** (your callback thread! — a listener thread suddenly executing your pipeline is the classic surprise) — which is Q69's entire subject; anticipate it.

---

### Q69. The `thenApply` vs `thenApplyAsync` question — which thread runs each stage? State the actual rules, why non-async stages can hijack completing threads (event loops!), and the executor-per-stage discipline.

**Answer:**

**The rules** (precise, because folklore here is rampant):

- **Non-async** (`thenApply`, `thenAccept`, `thenCompose`…): if the upstream is **already complete** when the dependent is attached → runs **immediately on the attaching thread**; if not yet complete → runs **on whichever thread completes the upstream** (the pool worker, or the manual `complete()` caller — Q68). You do *not* control it; it's whoever-is-there.
- **Async without executor** (`thenApplyAsync(fn)`): runs as a fresh task on the **common pool** (Q68's caveats apply).
- **Async with executor** (`thenApplyAsync(fn, pool)`): runs on **your pool** — the only fully-determined option.

**Why it matters — thread hijacking:** a cheap-looking `thenApply(this::parse)` attached to a CF completed by a **Netty event-loop thread** runs parse *on the event loop* — and if parse blocks or is heavy, the event loop (serving thousands of connections) stalls: the highest-severity version of this bug. Same shape: completing thread = your scheduler thread, your message-listener thread, a `complete()` caller inside a lock (now your pipeline runs while holding their lock — deadlock fuel). The inverse surprise: in tests the upstream is often already done → non-async stages run synchronously on the test thread → "works in test, hijacks in prod."

**Discipline:** default to **non-async for trivial, non-blocking transforms** (a field extraction — the hop cost isn't worth it); use **`*Async(fn, explicitPool)` at every boundary where the work is non-trivial, blocking, or must not run on the completer** — especially the first stage after any CF you didn't create, and anything downstream of framework callbacks. Never bare `*Async()` (common pool) in server code. Naming pools per concern (io-pool, cpu-pool) makes the decision reviewable (Q44's naming doctrine, applied).

**Follow-up trap:** *"Does `thenApplyAsync` guarantee a different thread than the completer?"* — it guarantees *submission to the executor*; the executor might be the same pool (even conceivably schedule onto the same thread later) — the guarantee is "not inline on the completing/attaching thread," which is what you actually need. Precision over folklore.

---

### Q70. `thenApply` vs `thenCompose` vs `thenCombine` (and the fan-in family `allOf`/`anyOf`): the monadic map-vs-flatMap distinction, combining independent calls, and assembling a real fan-out/fan-in flow.

**Answer:**

- **`thenApply(fn)`** — map: transform the value; fn returns a plain value. If fn itself returns a CompletableFuture, you get the dreaded `CF<CF<X>>` — the signal you needed compose.
- **`thenCompose(fn)`** — flatMap: fn returns a **CompletionStage**; stages chain flat. For *dependent* async calls (need A's result to start B):

```java
cf(userId).thenCompose(user -> fetchOrders(user.id()))     // CF<List<Order>>
```

Saying "thenApply : thenCompose :: map : flatMap on Optional/Stream" earns the functional-literacy point.

- **`thenCombine(other, bifn)`** — fan-in of **two independent** stages: both run concurrently; bifn gets both results when both complete:

```java
priceCf.thenCombine(fxRateCf, (price, rate) -> price.multiply(rate))
```

(`thenAcceptBoth`/`runAfterBoth` are the consuming variants; `applyToEither`/`acceptEither` — first-of-two wins, for hedged requests.)

- **`allOf(cfs...)`** — N-way barrier returning `CF<Void>` — the awkward part everyone must know: results are **not collected**; the idiom re-reads each (safe: all are done):

```java
List<CompletableFuture<Quote>> cfs = ids.stream().map(this::quoteCf).toList();
CompletableFuture<List<Quote>> all =
    CompletableFuture.allOf(cfs.toArray(CompletableFuture[]::new))
        .thenApply(v -> cfs.stream().map(CompletableFuture::join).toList());
```

Failure semantics: allOf completes exceptionally if **any** fails — but only after *all* settle; individual failures are retrievable per-CF (per-item fallback: map each with `exceptionally` *before* allOf — the resilient-fan-out pattern). **`anyOf`** — first to *settle* (including exceptionally!) wins — for racing equivalent sources; note it returns `CF<Object>` (cast) and does **not** cancel the losers (they run on — cancel explicitly if costly).

**Real flow assembly** (the whiteboard deliverable): fetch user → compose(orders) → fan-out per order enrich (map to CFs on io-pool) → per-item exceptionally(fallback) → allOf-collect → combine with independently-fetched preferences → single response CF. Each arrow annotated with its operator is exactly what "design an async aggregation endpoint" wants.

**Follow-up trap:** *"allOf of 1,000 CFs on one small pool — risk?"* — if stages block (I/O without async clients), pool starvation/deadlock (Q50.4 shape); with truly non-blocking stages it's fine — the fan-out bound belongs on concurrent *work* (semaphore — Q53), not on the number of CF objects. Distinguishing CF-count from thread-count is the mature answer.

---

### Q71. Exception propagation semantics in CF graphs: how failures flow, `CompletionException` wrapping, which stages are skipped, and why one unhandled branch silently disappears — with the minimal rules to keep pipelines observable.

**Answer:**

**Flow model:** a CF settles as value or exception; **exceptional completion propagates down the chain, skipping all normal stages** (`thenApply/thenAccept/...` don't run; they forward the exception) until a **handling stage** (`exceptionally`, `handle`, `whenComplete`) intercepts. Mentally: try/catch semantics stretched over an async graph — normal stages are the try-body lines, handlers are catch/finally.

**Wrapping rules** (the fiddly part interviewers check): the original exception thrown in a stage is stored; dependents' handlers usually receive it wrapped in **`CompletionException`** (unwrap via `getCause()` — handlers should peel it: `ex instanceof CompletionException ? ex.getCause() : ex`); blocking retrieval differs by method — `get()` throws checked `ExecutionException` (wrapping), `join()` throws unchecked `CompletionException` (wrapping): same cause, different wrapper — know both names and their checked-ness.

**The silent-disappearance hazard:** a CF whose exceptional outcome **no one observes** — no handler attached, nobody joins — vanishes: no log, no UEH (the exception is *stored*, considered handled-by-storage — Q8's submit-swallow, graph edition). Typical case: fire-and-forget `thenAccept(...)` side-branch fails → nothing anywhere. **Minimal observability rules:** every terminal branch ends in a handler — `exceptionally(logAndFallback)` or `whenComplete((v, e) -> { if (e != null) log(...); })`; fan-outs handle per-item before fan-in (Q70); and one team-level rule "no CF without a terminal handler" enforced in review — the async analogue of "no empty catch."

Branch nuance: handlers attach *per dependent* — two dependents on one failing CF each see the exception independently (it's multicast, not consumed); handling in one branch doesn't "clear" the other. And exceptions in the **handler itself** create a new exceptional completion downstream — handlers should be simple.

**Follow-up trap:** *"Difference between an exception thrown in `supplyAsync`'s lambda vs one thrown while *building* the chain?"* — inside the lambda → captured into the CF (async semantics); in the composing code (e.g., NPE constructing the pipeline) → synchronous throw on the caller, no CF exists to carry it. Where the exception physically occurs decides its channel — a subtle but load-bearing distinction.

---

### Q72. `exceptionally` vs `handle` vs `whenComplete` (and their `*Async`/`Compose` variants): precise contracts, recovery vs observation vs transformation, and idiomatic patterns — fallback, retry, enrichment of errors.

**Answer:**

**Contracts:**

- **`exceptionally(fn)`** — *recovery only*: runs **only on failure**; maps exception → replacement value; success passes through untouched. The async catch-with-default. (`exceptionallyCompose(fn)` — recover with another *async* operation: fallback service call, retry — the compose variant people forget exists.)
- **`handle((v, e) -> r)`** — *transformation always*: runs on **both** outcomes (exactly one of v/e non-null); returns a new value either way — can convert failure→value, value→other, even value→throw (re-fail). The async finally-with-return / Result-mapper.
- **`whenComplete((v, e) -> {})`** — *observation only*: runs on both outcomes but **cannot change them** — the outcome passes through unchanged (unless the callback itself throws, which — nasty detail — replaces/suppresses into a new failure; keep it side-effect-only and infallible). The async finally: logging, metrics, resource release, MDC cleanup.

Mnemonic to say aloud: *exceptionally = catch; handle = catch+map-everything; whenComplete = finally.*

**Idiomatic patterns:**

```java
// Fallback value / fallback call
fetchPrimary(id)
    .exceptionallyCompose(e -> fetchReplica(id))          // async fallback
    .exceptionally(e -> cachedDefault(id));               // last-resort value

// Retry with budget (compose recursion)
CompletableFuture<T> withRetry(Supplier<CompletableFuture<T>> op, int left) {
    return op.get().exceptionallyCompose(e ->
        left > 1 && isRetryable(e)
            ? delayed(backoff(left)).thenCompose(v -> withRetry(op, left - 1))
            : CompletableFuture.failedFuture(e));
}   // delayed() built on delayedExecutor — Q73

// Error enrichment (context for observability)
.handle((v, e) -> { if (e != null) throw new CompletionException(
        new OrderFetchException(orderId, unwrap(e))); return v; })
```

Placement semantics matter and get probed: `exceptionally` **before** a `thenApply` protects only upstream failures — the thenApply's own failure flows past it; handlers guard *what's above them*. Design consequence: put per-item handlers close to their risk (Q70's fan-out), one terminal handler at the pipeline mouth (Q71's rule).

**Follow-up trap:** *"whenComplete vs handle for cleanup that must not alter outcome?"* — whenComplete, and know the exception-in-callback suppression subtlety; and *"where does the handler run?"* — same thread rules as every stage (Q69) — `exceptionallyAsync(fn, pool)` exists (Java 12+) for handlers that do real work.

---

### Q73. Timeouts and cancellation in CF pipelines: `orTimeout` vs `completeOnTimeout`, `delayedExecutor`, what CF `cancel()` does and doesn't do (no interrupt!), and assembling end-to-end deadline discipline.

**Answer:**

- **`orTimeout(t, u)`** (Java 9+): if not settled by the deadline, completes the CF **exceptionally with `TimeoutException`** — downstream handlers treat it like any failure (Q71). Implementation: a shared internal scheduler completes it — i.e., the manual-completion pattern productized (Q68).
- **`completeOnTimeout(fallback, t, u)`**: same trigger, but settles with a **default value** — timeout-as-degradation rather than timeout-as-error (stale cache, partial response).
- **`delayedExecutor(t, u[, pool])`**: an Executor that runs submissions after a delay — the building block for backoff (Q72's retry), scheduled fallbacks, debounce — without owning a ScheduledExecutorService.

**The cancellation truth (heavily tested):** `cf.cancel(true)` completes the CF with `CancellationException` and — unlike `FutureTask` — **does NOT interrupt any thread**: the `mayInterruptIfRunning` flag is **ignored** (a CF doesn't know which thread, if any, is computing it — it's a promise, not a task handle). Consequences: the underlying `supplyAsync` work **keeps running to completion**, its result discarded on arrival (complete() on a settled CF is a no-op); downstream is released (sees the cancellation), upstream/sibling work is untouched. True cooperative cancellation must be wired manually: keep the `Future` from the executor and cancel *that*, pass a cancellation flag/`Context` the task polls (Q9), or cancel at the I/O client level (HTTP client request abort). Also: cancelling/timeout-ing a downstream stage does **not** propagate upstream — no automatic "nobody needs this anymore" reverse flow (structured concurrency exists precisely to fix this — Q85; foreshadow it).

**End-to-end deadline discipline:** one budget at the edge (request deadline), decremented per hop; per-call timeouts strictly inside the remaining budget (client timeout < CF orTimeout < caller's get(t) — layered, each tighter — the "timeout onion"); every timed-out branch both *handled* (Q71) and *actually abandoned* (cancel the I/O, release the semaphore permit — Q53 — or the timeout only frees the waiter while the work leaks — Q67's same trap, restated because it's the #1 async resource leak).

**Follow-up trap:** *"get(5, SECONDS) times out — what's the state of the CF?"* — still running/pending; only your wait ended (nothing was cancelled, nothing settled) — identical trap as raw Future, and the interviewer is checking you carry the lesson across APIs.

---

### Q74. "Never block inside an async pipeline": why `join()`/`get()`/JDBC/blocking HTTP inside CF stages on bounded pools deadlocks or starves, how to detect it, and the legitimate escape hatches (dedicated blocking pools, managed blockers).

**Answer:**

**The mechanism:** async pipelines assume stages **yield the thread quickly**; a stage that blocks (join on another CF, JDBC call, `HttpClient.send` sync, `queue.take`) parks a pool worker for the duration. On a bounded pool: N blocked stages = pool dead; and if the thing they're waiting **for** is itself scheduled on the same pool → **thread-starvation deadlock** (Q50.4, CF edition): all workers wait on stages that can never run. Common-pool variant: your blocked stages also freeze parallel streams and every other library's CFs (Q68) — collateral damage across the JVM. The insidious property recurs: it works at low load, deadlocks exactly at saturation.

**The rules:** inside stages — no `join`/`get` (compose instead — waiting *is* what the graph does for you, Q70); no synchronous I/O on CPU/common pools; blocking work goes to a **dedicated, named, bounded blocking pool** sized by Little's Law (Q45): `supplyAsync(this::jdbcCall, jdbcPool)` — this is the standard, honest bridge between the async world and blocking libraries (most real systems are exactly this hybrid; claiming otherwise is posturing). Alternatives up the purity ladder: true async clients (async HTTP client, R2DBC) where they're mature; virtual threads dissolving the rule entirely — blocking becomes cheap, "just block on a VT" (Q84 — the modern punchline: Loom makes this whole question historical for new code).

**ForkJoinPool's escape hatch — `ManagedBlocker`:** wrapping a blocking section in `ForkJoinPool.managedBlock(...)` tells the pool "I'm about to block" → it **compensates by spawning a spare worker**, preserving parallelism (this is how `Phaser` and some JDK internals block safely on FJP). Niche but the correct name-drop for "how could you ever block on the common pool."

**Detection:** thread dumps — all pool workers in `CompletableFuture.join`/socket reads with deep queues behind them (Q94); metrics — pool active==max with zero throughput; jstack's "parked waiting on CompletableFuture" clusters; and pre-prod: saturation load tests (the only load level where it shows) plus the review rule "no blocking call without a pool annotation."

**Follow-up trap:** *"Is `join()` at the very end of the pipeline — in the controller — okay?"* — In a thread-per-request servlet stack, yes (that thread's job *is* to wait), though prefer returning the CF/`DeferredResult` to let the container go async; in a reactive/event-loop stack, never. The answer depends on *whose thread you're on* — which by now is the section's refrain.

---

### Q75. CompletableFuture vs reactive streams (Reactor/RxJava) vs raw executors: what problem each abstraction models, backpressure as the key differentiator, and honest selection criteria for a backend service.

**Answer:**

**What each models:** raw executors — *task submission* (units of work, no composition vocabulary); CompletableFuture — a **single async value** and its dependency graph (one result, push-based, composable: the async analogue of `Optional`); reactive streams — an **asynchronous sequence over time** (0..∞ elements) with a built-in **flow-control protocol**: `request(n)` — consumers pull permission, producers may not exceed it. That protocol — **backpressure as a first-class wire contract** — is the differentiator to center the answer on: CF has no notion of "slow down" (a CF fan-out floods at whatever rate you create futures — bounding is DIY via semaphores, Q70's follow-up); Reactor's operators (`flatMap(fn, concurrency)`, `limitRate`, buffer/drop/latest strategies) encode Q43's whole policy menu *inside the type*.

**Also in the reactive column:** rich operator algebra over sequences (windowing, retry/backoff, combineLatest, timeouts per element), schedulers as explicit context (`publishOn/subscribeOn` — Q69's thread discipline formalized), lazy assembly (nothing runs until subscribe — CFs are eager: created = running, a real difference that bites in retry logic: `Mono.defer` vs re-calling a CF factory). **Costs:** a steep mental model (assembly vs subscription time, operator fusion), stack traces from hell, colleagues who don't speak it, and the ecosystem bifurcation (blocking JDBC in a reactive chain = Q74 all over again — R2DBC or bust).

**Selection criteria (the honest version):** request/response service, a handful of downstream calls to aggregate → **CF is enough** (or plain blocking + virtual threads — increasingly the simplest correct answer, Q84). Streams of events, per-element flow control, infinite sequences, WebFlux/SSE/websockets end-to-end, or genuine 10k+-concurrency-per-node with small heap → **reactive earns its complexity**. Mixed teams/mixed stacks → beware the boundary costs (every `block()` is a landmine). And say the closing reframe: Loom collapses much of reactive's *scalability* motivation while leaving its *streaming semantics* motivation intact — post-Java-21 the choice is about data-shape (value vs stream), not thread economics.

**Follow-up trap:** *"Map CF concepts to Reactor."* — `CF<T>` ≈ `Mono<T>`; `thenApply/thenCompose` ≈ `map/flatMap`; `exceptionally` ≈ `onErrorResume`; `allOf` ≈ `Mono.zip/when`; no CF analogue of `Flux` — the sequence type *is* the gap. Fluent translation proves both vocabularies are real.

---

### Q76. Async capstone: design "aggregate a user dashboard from 4 services (profile, orders, recommendations, notifications) within 800ms, partial results allowed, downstreams flaky." Full CF solution with pools, timeouts, fallbacks, and observability — narrated with justifications.

**Answer:**

```java
public CompletableFuture<Dashboard> dashboard(String userId) {
    CompletableFuture<Profile> profile =
        call(() -> profileClient.get(userId), ioPool)
            .orTimeout(500, MILLISECONDS);                       // REQUIRED datum: no fallback

    CompletableFuture<List<Order>> orders =
        call(() -> orderClient.recent(userId), ioPool)
            .completeOnTimeout(List.of(), 600, MILLISECONDS)     // optional: degrade to empty
            .exceptionally(e -> { meter("orders.fallback"); return List.of(); });

    CompletableFuture<List<Reco>> recos =
        call(() -> recoClient.forUser(userId), ioPool)
            .completeOnTimeout(List.of(), 400, MILLISECONDS)
            .exceptionally(e -> List.of());

    CompletableFuture<Integer> unread =
        call(() -> notifClient.unreadCount(userId), ioPool)
            .completeOnTimeout(0, 300, MILLISECONDS)
            .exceptionally(e -> 0);

    return profile
        .thenCombine(orders, PartialDash::new)
        .thenCombine(recos, PartialDash::withRecos)
        .thenCombine(unread, PartialDash::withUnread)
        .thenApply(PartialDash::finish)
        .orTimeout(800, MILLISECONDS)                            // end-to-end budget
        .whenComplete((d, e) -> meterLatencyAndOutcome(e));      // terminal observability
}
```

**Narration — each choice earns its point:** all four calls start **immediately and independently** (fan-out concurrency comes from *not* chaining — a sequential thenCompose chain here would be the rookie error worth naming); explicit `ioPool` everywhere, never common pool (Q68/69); **per-source policies**: profile is required (its timeout/failure fails the dashboard — propagates, Q71) while the other three **degrade** (`completeOnTimeout` + `exceptionally` → typed empties, Q72/73) — "partial results allowed" translated into per-branch handlers, not one global catch; per-source timeouts **inside** the 800ms envelope with an end-to-end `orTimeout` as the backstop (timeout onion, Q73); combine-chain fan-in (thenCombine ×3 — or allOf-with-collect, equivalent here; combine reads better for fixed small N, Q70); terminal `whenComplete` for metrics on every outcome (Q71's no-silent-branch rule).

**The probes and one-line answers:** *flaky downstream hammered by retries?* — circuit breaker around `call()` (Resilience4j decorating a CF supplier) + retry budgets only on idempotent GETs with jittered backoff via `delayedExecutor` (Q72); *abandoned work after the 800ms fires?* — CF cancel doesn't interrupt (Q73): use an HTTP client whose requests abort on CF cancellation, or accept bounded waste and cap it with the per-call timeouts (state the trade explicitly); *pool sizing?* — Little's Law on fan-out×rate×latency (Q45), bounded, with rejection mapped to immediate fallback (Q43); *virtual threads instead?* — four blocking calls in a `StructuredTaskScope` with a deadline reads *better* and cancels *properly* (Q85) — offering the Loom rewrite unprompted, with its cancellation advantage named, is the strongest possible finish.

**Follow-up trap:** *"Why start all four before any combine, rather than combine-as-you-go?"* — combining doesn't start anything (CFs are eager — creation is the start signal); the structure of *creation* determines concurrency, the structure of *combination* determines only the join order. Separating those two graphs verbally is the concept the whole capstone exists to test.

---

# Section 9 — ThreadLocal Context, Fork/Join, Parallel Streams & Virtual Threads (Q77–Q88)

---

### Q77. Context propagation across async boundaries: why ThreadLocal context (MDC, security, tenant, trace) breaks with executors and CF, `InheritableThreadLocal`'s false promise, and the correct wrapping/decorator patterns.

**Answer:**

**The break:** ThreadLocal context lives on the *thread* (Q18); hand work to a pool and it executes on a *different, long-lived, recycled* thread — your MDC trace-id, SecurityContext, tenant-id are absent (or worse: **stale from a previous task** — cross-request bleed, the security-grade bug). Every async hop (executor submit, `thenApplyAsync`, `@Asyn`c, message listener) is a context cliff.

**Why `InheritableThreadLocal` doesn't fix it:** it copies parent→child **at thread creation** — but pool threads are created *once*, at pool warmup, from whatever thread built the pool; tasks submitted later inherit *that* stale snapshot, not the submitter's context. ITL is for genuine parent-child thread spawning, useless (dangerously misleading) with pools.

**The correct pattern — capture at submission, restore at execution, clear after:**

```java
static Runnable withContext(Runnable task) {
    Map<String, String> mdc = MDC.getCopyOfContextMap();       // capture on CALLER
    var sec = SecurityContextHolder.getContext();
    return () -> {
        Map<String, String> oldMdc = MDC.getCopyOfContextMap();
        try {
            if (mdc != null) MDC.setContextMap(mdc);
            SecurityContextHolder.setContext(sec);
            task.run();                                        // execute with caller's context
        } finally {
            MDC.setContextMap(oldMdc == null ? Map.of() : oldMdc);
            SecurityContextHolder.clearContext();              // no residue (Q18/Q50.3)
        }
    };
}
```

Industrialized versions: a decorating `ExecutorService` wrapping every submitted task (Spring's `TaskDecorator` — one line of config covering the whole pool); Spring Security's `DelegatingSecurityContextExecutor`; Micrometer **Context Propagation** library (the current standard — snapshots all registered ThreadLocals across CF/Reactor hops); Reactor's `Context` (context as *data flowing with the pipeline* rather than thread state — the philosophically clean fix); and Loom's **`ScopedValue`** (Q86) — immutable, structurally-scoped context that inherits correctly into virtual-thread children by design, retiring this whole problem class for new code.

**Follow-up trap:** *"Your logs show requests with the wrong trace-id occasionally — mechanism?"* — a pool task that *set* MDC but didn't clear in finally; next task on that thread logs under the old id (bleed, not loss). Detecting direction — missing vs wrong — identifies set-without-clear vs never-set. Diagnosing from the symptom is the seniority check.

---

### Q78. ForkJoinPool: the work-stealing architecture — per-worker deques, LIFO-own/FIFO-steal, why that geometry minimizes contention — and how it differs fundamentally from ThreadPoolExecutor.

**Answer:**

**Architecture:** each worker owns a **deque** (double-ended queue). A worker *pushes and pops its own forked subtasks at one end, LIFO*; an idle worker **steals from the *other* end of a victim's deque, FIFO** (random victim selection). This geometry is the whole design — recite its three payoffs:

1. **Contention minimization:** owner and thief operate on *opposite ends* — they contend only when the deque is nearly empty (one element); the common case is contention-free (implemented with CAS at the ends, no global lock). Compare TPE: **one shared queue** every worker fights over — fine for coarse tasks, a bottleneck at fine granularity.
2. **Cache locality:** LIFO-own means a worker processes the subtask it *just created* — data still hot in its cache; recursive divide-and-conquer becomes nearly depth-first per worker.
3. **Steal granularity:** FIFO-steal takes the victim's **oldest = biggest** subtask (highest in the split tree) — a thief grabs a large chunk and goes away for a long time, keeping steal frequency (and its synchronization) low. "Steal big, steal rarely."

**Load balancing emerges:** no central dispatcher — idle threads pull from busy ones; irregular task trees (unbalanced splits) auto-level. This is why FJP exists at all: TPE's shared-queue model collapses when tasks are microseconds long and millions in number (queue lock becomes the program); FJP is built for **fine-grained, recursively-decomposable, CPU-bound** work.

**Differences from TPE beyond the queue:** join-with-helping — a worker blocked on `join()` of an incomplete subtask doesn't just park: it **runs other tasks meanwhile** (its own deque or steals), keeping cores busy (this is why naive blocking join inside FJP tasks is less fatal than in TPE — but real blocking I/O still needs `ManagedBlocker`, Q74); no bounded work queue/rejection semantics (unbounded by design — it assumes tasks come from computation splitting, not external admission — so it is *not* your service's ingest pool, Q43's machinery is absent); `commonPool()` — the shared instance (parallelism = cores−1) used by parallel streams and bare CF async (Q68's caveats).

**Follow-up trap:** *"Why does the common pool have cores−1 threads?"* — the submitting thread itself participates (helps execute) via `ForkJoinTask.join`'s helping — the caller is the missing worker. Elegant, and knowing it explains "parallel stream work appears on main" in profilers (Q81).

---

### Q79. `RecursiveTask`/`RecursiveAction`: write a correct fork/join divide-and-conquer (sum/sort skeleton), the fork-then-compute-then-join ordering idiom, threshold selection, and the classic mistakes (fork both halves, tiny thresholds, blocking).

**Answer:**

```java
class SumTask extends RecursiveTask<Long> {
    static final int THRESHOLD = 10_000;                 // tuned, not guessed
    final long[] a; final int lo, hi;
    SumTask(long[] a, int lo, int hi) { this.a = a; this.lo = lo; this.hi = hi; }

    @Override protected Long compute() {
        if (hi - lo <= THRESHOLD) {
            long s = 0;
            for (int i = lo; i < hi; i++) s += a[i];     // sequential base case
            return s;
        }
        int mid = (lo + hi) >>> 1;
        SumTask left  = new SumTask(a, lo, mid);
        SumTask right = new SumTask(a, mid, hi);
        left.fork();                                     // (1) push left for stealing
        long r = right.compute();                        // (2) do right YOURSELF
        long l = left.join();                            // (3) then join left
        return l + r;
    }
}
long total = ForkJoinPool.commonPool().invoke(new SumTask(a, 0, a.length));
```

**The ordering idiom — fork/compute/join, exactly this order, and why:** fork the left (push to your deque — available for thieves), **compute the right in-line** (no task object, no scheduling overhead for half the tree — the current thread was going to do *something*; let it do real work), then join left (by which time it's done, stolen-and-done, or you execute it yourself via join's helping — Q78). **Classic mistake #1: fork both then join both** — doubles task allocation and makes the worker's first action a join (pure overhead); **mistake #1b: `right.compute(); left.join()` reversed as `left.join(); right.compute()`** — joining before computing serializes (you wait on left while right sits untouched). (`invokeAll(left, right)` encodes the right pattern for you — acceptable shorthand.)

**Threshold selection:** too small → task-creation overhead dominates (millions of objects, deque traffic — the overhead can exceed the work: sequential beats parallel); too large → too few tasks to balance/steal. Heuristics: aim for ~100µs–1ms of work per leaf; or "#leaf tasks ≈ 4–8× parallelism" for steal headroom; then **measure** (JMH) — say measurement is the method, thresholds are workload-specific. **Mistake #3: blocking I/O inside compute** — FJP sized for CPU (cores); blocked workers idle cores (ManagedBlocker or don't use FJP — Q74/78). **Mistake #4: side effects/shared mutable state across subtasks** — the model's correctness rests on subtask independence; results flow *up via join returns*, not out via shared vars (else Sections 2–3 all over again).

**Follow-up trap:** *"Sum via LongAdder all leaves instead of joining returns?"* — works, but you've traded a clean reduction tree for shared-state contention (Q39) and lost composability of partial results; reduction-by-return is idiomatic FJ. Recognizing "accumulate globally" as the smell is the point.

---

### Q80. Parallel streams: the execution model (spliterators, common pool), the four conditions that make `.parallel()` profitable, and the notorious hazards — shared state in lambdas, ordering costs, boxed streams, nested parallelism.

**Answer:**

**Model:** `stream().parallel()` splits the source via its **`Spliterator`** into a balanced task tree executed as fork/join on the **common pool** (Q78) — the calling thread participates too. Source splittability rules the economics: `ArrayList`/arrays/`IntStream.range` split perfectly (index arithmetic); `LinkedList`/iterator-based sources split terribly (walk-to-split); `HashMap`/`ConcurrentHashMap` split well (bucket ranges).

**The profitability conditions (N·Q model — say it):** worthwhile when **N (elements) × Q (per-element cost)** is large — the folk threshold "N×Q ≳ 100µs of total work"; plus: splittable source; per-element work **independent and side-effect-free**; and either unordered or order-cost accepted. Small collections with cheap lambdas *lose* — split/steal/merge overhead swamps the work (the most common real-world parallel-stream sin is `.parallel()` on a 50-element list of string maps).

**Hazards, each with mechanism:**

1. **Shared mutable state in lambdas** — `forEach(x -> list.add(x))` on an ArrayList: races/corruption (Q65); the fix is the *collector* model — `collect(Collectors.toList())` gives each subtask its own container merged at joins (mutable reduction — concurrency handled by the framework's partitioning, not by locks). Never "fix" with a synchronizedList — you've serialized the parallel stream (contention at every element).
2. **Ordering costs:** `findFirst`/`limit`/ordered `collect` on ordered sources force coordination; `.unordered()`/`findAny` release it. `forEach` is unordered even on ordered streams; `forEachOrdered` restores order at parallelism's expense — know the pairs.
3. **Common-pool coupling:** your parallel stream competes with every other library's (Q68); blocking inside stream lambdas starves the JVM-wide pool (Q74). The unofficial-but-working isolation trick — run the terminal op *inside* `customFJP.submit(() -> stream.parallel()...get()` — tasks run in the submitting FJP; know it *and* label it unofficial.
4. **Boxing:** `Stream<Integer>` math allocates per element — use `IntStream/LongStream` primitives or parallelism amplifies GC pressure.
5. **Nested parallelism** (parallel stream inside parallel stream / inside FJ task) — oversubscription and helping-related surprises; flatten the parallel dimension.
6. **`reduce` with non-associative functions** — parallel reduction *requires associativity* (subtraction, float addition order-sensitivity) — wrong answers, silently, only when parallel: the purest concept-check in this list.

**Follow-up trap:** *"Stream is parallel; where does `peek` run?"* — on whatever worker processes that element (any thread, any interleaving) — people log inside `peek` and are shocked by thread names; leads back to Q69's "whose thread?" refrain — a good full-circle moment.

---

### Q81. `@Async`-style framework concurrency (Spring): what the proxy does, the self-invocation and return-type rules, executor configuration, and exception/context behavior — bridging your framework knowledge to first-principles threading.

**Answer:**

**Mechanism:** `@EnableAsync` + `@Async` wraps the bean in a **proxy** that intercepts calls and submits the method body to a `TaskExecutor` — the caller returns immediately. All proxy pathologies from AOP apply verbatim (and interviewers *love* the crossover): **self-invocation bypasses the proxy** (this.asyncMethod() runs synchronously — same mechanics as `@Transactional`'s Q61 in your JPA set — cite the parallel explicitly); non-public methods aren't advised; calling through the interface/injected reference is required.

**Return-type rules:** `void` — fire-and-forget: **exceptions vanish** unless an `AsyncUncaughtExceptionHandler` is configured (the framework's version of Q8's swallow); `Future<T>`/`CompletableFuture<T>` — the proxy returns a future completed by the async execution; exceptions flow into it (Q71's rules apply downstream). Returning anything else non-void is a misconfiguration that runs but returns null-ish — flag it.

**Executor config:** default is a `SimpleAsyncTaskExecutor` in older setups — **creates a new thread per call, unbounded** (Q41's thread-per-task hazard hiding behind an annotation!) — or the application `TaskExecutor` if defined; production rule: define named, bounded `ThreadPoolTaskExecutor` beans and select per method (`@Async("reportExecutor")`) — Q42's seven parameters wearing Spring clothes (`corePoolSize/maxPoolSize/queueCapacity` — note Spring's queue-capacity default is unbounded: Q42's dead-max trap, framework edition; and its `AcceptPolicy` mapping to Q43). Boot 3.2+: `spring.threads.virtual.enabled=true` swaps in virtual-thread-per-task (Q84) — the annotation stays, the economics change.

**Context/exception behavior:** SecurityContext/MDC don't follow (Q77 — `TaskDecorator` is the Spring-native fix); `@Transactional` on an `@Async` method = **new transaction on the worker thread**, never the caller's (thread-bound tx — the Q68-JPA crossover); ordering: `@Async` starts after the *call*, not after the caller's transaction commits — for post-commit work use `@TransactionalEventListener(AFTER_COMMIT)` + async listener (outbox thinking).

**Follow-up trap:** *"@Async method calls another @Async method on the same bean — how many hops?"* — one: the inner call is self-invocation → runs inline on the first async thread. Framework questions resolving to proxy mechanics + thread identity is exactly the bridge this question builds.

---

### Q82. Timers, scheduling drift, and time itself: `System.currentTimeMillis` vs `nanoTime`, monotonic clocks, why `Thread.sleep` precision is bounded, measuring elapsed time correctly, and clock pitfalls in concurrent code.

**Answer:**

**Two clocks, two purposes:** `currentTimeMillis()` — **wall clock**: meaningful absolute time, but *not monotonic* — NTP steps/slews, manual changes, leap adjustments can move it backward or jump it forward. `nanoTime()` — **monotonic elapsed-time counter**: meaningless absolute value (origin arbitrary), guaranteed non-decreasing within a JVM — the *only* correct basis for durations, timeouts, benchmarks, rate limiters (Q56's follow-up, now in full):

```java
long t0 = System.nanoTime();
work();
long elapsedNanos = System.nanoTime() - t0;     // correct even if NTP steps mid-work
```

Elapsed-time math on wall clock is a genuine production-bug class: negative durations, timeouts that never fire (clock stepped back), cache TTLs expiring en masse (clock jumped forward). All JDK timed waits (`parkNanos`, `awaitNanos`, `tryLock(t)`) use the monotonic clock internally — your code should match. (`nanoTime` deltas overflow correctly under two's complement if compared via subtraction — `t1 - t0 < timeout` not `t1 < t0 + timeout` — a deep-cut correctness point.)

**Sleep/scheduling precision:** `sleep(1)` means *at least* 1ms — actual wake depends on OS timer resolution and scheduler load (historically 10–15ms granularity on Windows; modern tickless kernels better but never exact); sleep is a *minimum bound, not a metronome*. Consequences: naive `while(true){work; sleep(period)}` **drifts** (period + work + wake latency accumulate) — fixed-rate scheduling computes next deadline from the *original* t₀ (`scheduleAtFixedRate` does this for you — Q49); micro-waits below ~1ms need `parkNanos`/spin-with-`onSpinWait` (and honesty about burning CPU).

**Concurrent-code clock pitfalls checklist:** timeouts composed across layers must use the same monotonic basis; TTL/expiry logic on wall clock must tolerate steps (or use monotonic + boot-time anchor); distributed timestamps are *not* ordered (clock skew between nodes — never order events across machines by wall clock; logical/hybrid clocks exist for that — one sentence of distributed-systems awareness lands well); and in tests, inject a `Clock` (java.time) — code reading `Instant.now()` directly is untestable and un-mockable, the same lesson as injecting executors.

**Follow-up trap:** *"Is `nanoTime` comparable across threads?"* — Yes within one JVM (same source), with the caveat of measurement cost (~20–30ns per call) and no cross-JVM meaning. And *"why did our metrics show a negative request duration?"* — someone measured with wall clock across an NTP step — instant diagnosis, instant credibility.

---

### Q83. Why platform threads stopped scaling for I/O-bound servers: the thread-per-request math, memory and scheduler ceilings, how the industry responded (async/reactive), and the cost of that response — setting up virtual threads.

**Answer:**

**The math:** thread-per-request holds one thread for the request's full latency; concurrency needed = rate × latency (Little's Law — Q45). 5,000 req/s × 200ms downstream latency = 1,000 concurrent threads *just waiting*; add fan-out (each request awaiting 3 backends) and you're at thousands. Platform-thread ceilings: **memory** — ~1MB stack reservation each (Q1): 10k threads ≈ 10GB of stacks (mostly committed lazily, but real at depth); **scheduler** — 10k+ runnable-ish threads mean kernel run-queue overhead and context-switch load (Q1) even when most are parked; **creation cost** — mitigated by pools, but pools then *cap concurrency* (Q42): the pool becomes the bottleneck — under downstream slowness, workers are all parked in I/O waits, the queue grows, latency explodes: **threads-as-scarce-resource is the root constraint**. Note what the threads are doing at collapse: *nothing* — parked in socket reads. The resource being exhausted is thread *identity*, not CPU.

**The industry response — don't wait on threads:** async/non-blocking I/O (NIO event loops: few threads, many connections, callbacks on readiness — Netty), then abstraction layers to make callbacks livable: CompletableFuture graphs (Section 8), reactive streams (Q75). The economics work — 10 event-loop threads serve 100k connections.

**The cost of the response** (the honest ledger that motivates Loom): the **function-color problem** — blocking and non-blocking code don't compose (one blocking JDBC call poisons an event loop — Q74); lost debuggability (stack traces are scheduler frames, not causal chains; thread dumps show event loops, not requests); every library must be rewritten async (R2DBC, async clients — ecosystem bifurcation); reasoning difficulty (Q69's whose-thread puzzles as a permanent tax); and the control-flow constructs of the language (loops, try/catch, checked exceptions) work awkwardly inside operator chains. Summarize: *the async decade traded a resource problem for a programming-model problem.*

**Follow-up trap:** *"So why not just huge platform-thread pools + more RAM?"* — you can go surprisingly far (tens of thousands with tuned `-Xss`), and some shops did; but scheduler overhead, GC-of-stacks interactions, and the pool-sizing fragility under variable latency (Q45's downstream-bounded reality) keep it brittle. The clean answer was always "threads too cheap to count" — which is exactly the virtual-thread pitch (Q84).

---

### Q84. Virtual threads (Java 21): what they are, mounting/unmounting on carriers, what blocks cheaply vs what pins, why pooling them is wrong, where they don't help, and the migration guidance.

**Answer:**

**What they are:** JVM-managed threads (Project Loom) whose stacks live as **heap-resident continuations** — thousands of bytes growing on demand, not megabyte reservations. Millions are feasible. The JVM multiplexes them onto a small **carrier pool** of platform threads (a dedicated FJP, defaulting to #cores): a virtual thread *mounts* a carrier to run; when it hits a **blocking operation, the JDK parks the virtual thread and unmounts it** — the carrier immediately runs another VT. Blocking becomes ~free: the entire blocking JDK (socket I/O, `sleep`, locks, `BlockingQueue`) was retrofitted to be VT-aware — `Thread.sleep(1s)` on a VT costs a heap object parked, not an OS thread held. **Result: thread-per-request returns** (Q83's model, minus the ceilings) — simple blocking code with async-grade scalability, real stack traces, working debuggers.

**Pinning — the caveat list (the senior section):** a VT that blocks while **it cannot unmount** pins its carrier (carrier lost to other VTs; enough pinned carriers = throughput collapse): (1) blocking **inside a `synchronized` block/method** — historically the big one: monitors were carrier-level… **fixed in JDK 24** (JEP 491 — synchronized no longer pins); for 21–23, hot blocking-under-synchronized paths should migrate to `ReentrantLock` (which parks VT-aware) — knowing the version boundary is premium currency; (2) **native frames / JNI** on the stack — still pins; (3) file I/O historically compensates via temporary carrier expansion rather than unmounting (evolving). Diagnostics: `-Djdk.tracePinnedThreads`, JFR pinning events.

**Rules that change:** **never pool virtual threads** (pooling exists to amortize costly creation — VT creation is cheap; a pool would re-impose the cap you just removed): `Executors.newVirtualThreadPerTaskExecutor()`, one VT per task; **bounding moves from pools to semaphores** — limit concurrent access to scarce downstreams with `Semaphore(30)` (Q53's headline pattern), not by capping threads; **ThreadLocal works but costs** (a million VTs × heavy TLs = heap pain; per-thread caches lose meaning) → `ScopedValue` (Q86); CPU-bound work gains **nothing** (VTs don't add cores — they add *waiting capacity*; FJP/parallel streams remain the CPU story).

**Migration guidance:** blocking servlet-style services — flip on (Boot 3.2 `spring.threads.virtual.enabled=true`), audit synchronized-around-I/O (pre-24) and pool-based bounds → semaphores; reactive codebases — no urgency (already scale), but new code can choose simplicity; libraries — stop assuming thread counts are small (per-thread caching strategies break at 10⁶ threads).

**Follow-up trap:** *"VT vs async/reactive — is reactive dead?"* — VTs solve the *thread-economics* motivation; reactive keeps the *streaming/backpressure semantics* motivation (Q75's separation) — repeat that distinction here and it lands as a thesis, not a hedge.

---

### Q85. Structured concurrency (`StructuredTaskScope`): the problem with free-floating concurrency (leaks, orphans, cancellation gaps), the scope model, ShutdownOnFailure/ShutdownOnSuccess, and how it fixes what CF cancellation couldn't.

**Answer:**

**The disease:** unstructured concurrency — tasks launched into executors/CFs **outlive and escape their creating context**: a request handler forks three lookups and returns early on error — the other two *keep running* (leaked work — Q73: CF cancellation doesn't propagate, doesn't interrupt); failures in forgotten branches vanish (Q71); nothing ties subtask lifetime to the operation that spawned it — like `goto` for control flow, concurrency without block structure (the Loom team's explicit analogy — cite it).

**The model:** subtasks confined to a lexical **scope**; the scope cannot exit until every child completes or is cancelled — concurrency gets block structure:

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<User>  user  = scope.fork(() -> fetchUser(id));      // virtual threads
    Subtask<List<Order>> orders = scope.fork(() -> fetchOrders(id));

    scope.joinUntil(deadline);          // wait for all — bounded by deadline
    scope.throwIfFailed();              // first failure propagates, with cause

    return new Dashboard(user.get(), orders.get());
}   // scope close GUARANTEES: no subtask survives this block
```

**Policies:** `ShutdownOnFailure` — first failure **cancels all siblings** (interrupts their VTs — real cooperative cancellation, Q9, actually wired up) and records the cause: the invoke-all/fail-fast shape; `ShutdownOnSuccess` — first success cancels the rest: hedged/racing requests (Q70's anyOf, but the losers actually stop); custom policies by subclassing (collect-N-of-M, etc.).

**What it fixes versus CF:** cancellation **propagates downward automatically** (scope shutdown → child interrupts → grandchildren via their scopes — a cancellation *tree*, the reverse flow CF never had — Q73); errors can't be silently dropped (unobserved subtask outcomes surface at join/throwIfFailed — Q71's rule enforced structurally); **observability** — thread dumps (`jcmd Thread.dump_to_file -format=json`) show the scope *hierarchy*: request → its subtasks as a tree, restoring the causal stack async code lost (Q83); and inheritance: `ScopedValue` context flows into forks by construction (Q86). Status note: incubating/preview through recent JDKs — API details shifting (newer JDKs rework the policy API into `Joiner`s); state the concept confidently, the API version humbly.

**Follow-up trap:** *"Rewrite Q76's dashboard with a scope — what improves, what's lost?"* — improves: real cancellation of losers after deadline/failure, tree-shaped dumps, straight-line readability; lost: CF's per-branch degradation policies need re-expression (per-subtask try/catch fallbacks inside the fork, or a custom policy — partial-results is *less* built-in with ShutdownOnFailure). Seeing both directions proves it's understanding, not enthusiasm.

---

### Q86. `ScopedValue` vs ThreadLocal: immutability and dynamic scoping, the `where(...).run(...)` model, rebinding, why it exists (Loom-era context), and migration reasoning.

**Answer:**

**The model:** a `ScopedValue` is **bound for the dynamic extent of a code block** and visible to everything called within it — including forked subtasks in structured scopes:

```java
static final ScopedValue<RequestContext> CTX = ScopedValue.newInstance();

ScopedValue.where(CTX, requestContext)
           .run(() -> handle(request));          // CTX.get() anywhere below — bound
// outside run(): CTX.get() throws — no binding, no residue
```

**Contrasts with ThreadLocal — four structural fixes:**

1. **Immutable binding:** no `set()` — the binding cannot be mutated mid-flight (ThreadLocal's set-anywhere made data flow untraceable); *rebinding* is nested and scoped: an inner `where(CTX, other).run(...)` shadows for its extent, and the outer binding **automatically restores** — no save/restore boilerplate, no Q77 wrapper dance.
2. **No leaks by construction:** the binding dies with the block — there is no `remove()` because there's nothing to forget (Q18's entire pathology deleted structurally; the exit is guaranteed even on exceptions).
3. **Cheap inheritance:** child VTs forked in a `StructuredTaskScope` see the parent's bindings via **sharing** (immutability makes sharing safe — no per-thread copy like ITL, no capture/restore like Q77's decorators); one context object serves a million VTs.
4. **Explicit unbound behavior:** `get()` without a binding throws (with `orElse`/`isBound` escapes) — vs ThreadLocal's silent null/initialValue — misconfiguration fails fast.

**Why it exists:** Loom makes threads ephemeral and numerous — ThreadLocal's assumptions (threads are few, long-lived, reused — worth caching against) invert (Q84); and structured concurrency needs context to flow *down the task tree* correctly, which mutable per-thread state can't do safely. **What ScopedValue can't do:** mutable accumulation up the stack (a per-request tally that callees increment) — ThreadLocal-as-mutable-scratchpad has no ScopedValue equivalent (by design; pass a collector object explicitly instead — the honest limitation to name). Also transaction managers and legacy frameworks are ThreadLocal-shaped for now (your JPA doc's `TransactionSynchronizationManager` — migration is ecosystem-paced, not per-app).

**Migration reasoning:** new context = ScopedValue where the stack supports it; per-request read-only context (auth, tenant, trace) is the perfect fit; mutable/legacy integrations stay ThreadLocal with Q77's discipline until frameworks move.

**Follow-up trap:** *"ScopedValue in a plain executor task (not structured scope)?"* — bindings do **not** cross an unstructured submit (the task runs outside the dynamic extent) — inheritance is a *structured-concurrency* feature, not magic; the pairing of Q85+Q86 is deliberate and interviewers test whether you know they're a set.

---

### Q87. Concurrency testing: why races evade unit tests, and the working toolkit — stress harnesses with latches, jcstress, deterministic schedulers, timeout discipline in tests, and what to assert (invariants, not interleavings).

**Answer:**

**Why tests lie:** a race manifests only under specific interleavings; a unit test explores approximately one (same JIT tier, low thread count, tiny heaps, warm caches) — "passed 1,000 times" bounds nothing (Q92's theme). Tests also *perturb*: assertions, logging, coverage instrumentation add synchronization/timing that suppresses the bug (Heisenbug — Q20c).

**The toolkit, ranked by rigor:**

1. **Stress tests with maximum-contention framing:** many threads, a **start-gate latch** so all hit the critical region simultaneously (Q51 pattern 2 — its actual raison d'être), iterations × runs, assert **invariants after the fact** (final counter == N×iters; sum conserved in transfer tests — Q78-JPA crossover; no duplicates in claimed IDs — Q40): catching lost updates, duplicated work, torn invariants probabilistically. Cheap, valuable, still probabilistic — say so.
2. **jcstress** (OpenJDK's concurrency stress tool): declares small concurrent actors + an arbiter observing outcomes; the harness explores interleavings/architectures aggressively and *classifies observed results* against your expected set (ACCEPTABLE/FORBIDDEN) — the professional way to test a lock-free structure or a publication idiom (it will find Q14's partial-construction on weak-memory hardware that your laptop never shows). Knowing jcstress by name and purpose is a real differentiator.
3. **Deterministic/virtual schedulers where the model allows:** single-threaded executors injected in tests (your async pipeline runs deterministically — inject executors everywhere for exactly this reason — Q82's inject-the-Clock lesson generalized); CompletableFuture tests via manually-completed CFs (Q68 — settle promises in controlled order to exercise each race arm *deterministically*); Loom-adjacent deterministic-scheduling research/tools exist but aren't mainstream — honesty point.
4. **Timeout discipline:** every blocking test call gets a timeout (`await(5, SECONDS)`, timed `get`) and asserts on it — a deadlocked test must **fail with diagnostics**, not hang CI (`@Timeout`); on failure, dump threads programmatically (`ThreadMXBean.dumpAllThreads`) into the failure message — the self-diagnosing test pattern, worth describing.
5. **Static/dynamic analysis adjuncts:** ErrorProne/IntelliJ concurrency inspections, `@GuardedBy` checkers, ThreadSanitizer-style tooling for natives — supplements, not substitutes.

**What to assert — the philosophy:** never assert *scheduling* ("B ran after A" without a synchronization reason — flaky by definition); assert **invariants and quiescent outcomes** (conservation, uniqueness, no-loss, idempotency) plus the *presence* of required failures (exactly one winner of an optimistic race). Design-for-testability corollary: the more state is confined/immutable (Q89), the less of this section your codebase needs — the best concurrency test is the shared-mutable state you deleted.

**Follow-up trap:** *"CI passes, prod (ARM/Graviton) fails — why can architecture matter?"* — weaker hardware memory model exposes missing HB edges that x86's strong ordering masked (Q20c) — jcstress on target-arch runners is the systematic answer; recognizing the migration-to-ARM trigger is 2020s-real experience.

---

### Q88. JVM & OS mechanics that leak into concurrent behavior: safepoints and their pauses, GC's interaction with threads, stack sizes, CPU affinity/NUMA at awareness level, and why "the code is correct but p99 is spiky" is often a runtime question.

**Answer:**

**Safepoints:** the JVM must periodically bring **all** Java threads to known-safe states (GC phases, deoptimization, biased-lock revocation historically, thread dumps, some JFR ops). Threads poll a safepoint check at method returns/loop back-edges; a stop-the-world op waits for the *slowest* thread — one thread in a **counted-loop without safepoint polls** (large int-indexed loop, JIT-elided checks) delays *everyone*: time-to-safepoint (TTSP) spikes appear as mysterious all-thread pauses that aren't GC. Diagnostics: `-Xlog:safepoint`; fixes: loop restructuring, `-XX:+UseCountedLoopSafepoints`. Knowing TTSP as distinct from GC pause is a real p99 debugging weapon.

**GC × threads:** STW phases pause all application threads (concurrent collectors — G1 mixed, ZGC/Shenandoah near-pauseless — shrink but don't erase them); GC *worker* threads compete for cores with your pools (a "cores"-sized pool + GC threads = oversubscription at collection time); allocation-heavy concurrent code (per-task garbage, COW copies — Q60, CF node allocation) turns concurrency design into GC load — the "correct but spiky" chain: contention → allocation → GC frequency → p99. TLABs (thread-local allocation buffers) are why allocation itself is nearly lock-free — per-thread bump-pointer regions; worth naming as "the JVM confines allocation per-thread for the same reason you confine state" (Q89 tie-in).

**Stacks & memory:** `-Xss` per platform thread (Q1) — deep recursion vs thread count trade; virtual threads move stacks to heap (chunked, GC-managed — Q84) shifting the cost model; native memory for thread metadata counts against container limits (threads OOM-kill pods via *native*, not heap — an ops classic).

**Affinity/NUMA (awareness level):** threads migrate across cores (cache re-warming per migration); cross-socket (NUMA) memory access is slower and coherence traffic (Q39's ping-pong) costs more across nodes — big-box JVMs use `-XX:+UseNUMA`, latency shops pin threads (taskset/isolcpus) — one sentence each is the expected depth: *know the phenomena exist, know when to suspect them* (multi-socket hosts, p99 sensitivity), defer specifics to measurement.

**The closing diagnostic stance:** when correct code shows spiky latency, suspect in order — GC (logs), safepoints/TTSP, contention (JFR lock profiles — Q93), oversubscription (run-queue depth), allocation storms, then NUMA/affinity — a *runtime* checklist parallel to Q79-JPA's application checklist; presenting it as a checklist is itself the senior signal.

**Follow-up trap:** *"Thread priorities to fix p99?"* — almost never (Q7); the spikes are runtime-mechanical, not scheduling-preference problems — and circling the answer back to earlier fundamentals under pressure is exactly what this last conceptual question is designed to reward.

---

# Section 10 — Concurrent Design Patterns, Debugging & Capstone (Q89–Q100)

---

### Q89. The four strategies for thread safety, as a designer's hierarchy: confinement, immutability, safe delegation to concurrent components, and synchronization — with the argument for why they're ordered that way.

**Answer:**

The hierarchy, best-first, each with its mechanism and its argument:

1. **Don't share (confinement):** state owned by exactly one thread — stack confinement (locals — automatically safe), thread confinement (a component pinned to one thread: single-threaded executor as poor-man's actor — Q46; event loops; Swing's EDT), ThreadLocal as *ad-hoc* confinement (Q18). No sharing ⇒ *no concurrency problem exists* — nothing to get wrong, nothing to test (Q87's closing point). Communication happens by **transferring ownership through queues** (Q52's handoff protocol) — "share memory by communicating." This is Go's/actors'/event-loops' common core, and it's first because it *deletes* the problem.
2. **Share immutably:** immutable objects need no coordination ever (Q17); mutable *identity* over immutable *values* (volatile/AtomicReference snapshot swap). Second because it handles read-sharing perfectly and cheaply, but mutation requires copy-and-swap machinery.
3. **Delegate to engineered components:** compose state into `ConcurrentHashMap`/`BlockingQueue`/`LongAdder`/`AtomicReference` and stay inside their **atomic vocabulary** (compute-family etc.). The catch that keeps this third: delegation is safe only while *each invariant lives entirely inside one component's atomic ops* — cross-component or compound invariants silently break it (Q10, Q63) — you must re-verify at every requirement change.
4. **Synchronize by hand:** locks, conditions, volatile protocols, happens-before reasoning — the full Sections 2–4 toolkit. Last because every line is a proof obligation on *you*, forever, under maintenance by people who weren't in the room.

The design method that falls out (say it as a procedure): for each piece of state ask — can it be local? can it be immutable? does a j.u.c. component's atomic vocabulary cover *all* its invariants? — and only on three no's, design a locking protocol (smallest lock, documented `@GuardedBy`, invariants named — Q30's method). Most "hard concurrency bugs" are state that skipped straight to level 4 without auditioning for levels 1–3.

**Follow-up trap:** *"Isn't this just avoiding the hard stuff?"* — inverted: the hard stuff is a *cost center*, not a merit badge; engineering maturity is minimizing the surface where the hard reasoning is required, then doing it impeccably on the residue. That answer, delivered calmly, is the senior signature.

---

### Q90. Classic coordination puzzle, done right: two threads print odd/even alternately (then generalize to N threads round-robin). Show the wait/notify solution, the semaphore solution, and what each variant teaches.

**Answer:**

**wait/notify version — the guarded-turn pattern:**

```java
class OddEven {
    private final Object lock = new Object();
    private int current = 1; private final int max;
    OddEven(int max) { this.max = max; }

    void print(int parity) throws InterruptedException {   // parity 1=odd, 0=even
        synchronized (lock) {
            while (current <= max) {
                while (current <= max && current % 2 != parity)
                    lock.wait();                           // not my turn — loop! (Q24/25)
                if (current > max) break;
                System.out.println(Thread.currentThread().getName() + ": " + current);
                current++;
                lock.notifyAll();                          // turn changed — wake partner
            }
        }
    }
}
// new Thread(() -> oe.print(1), "odd").start();  new Thread(() -> oe.print(0), "even").start();
```

Teaches: turn state guarded by one monitor; **wait in a loop on the turn predicate**; notify after every state change; termination handled *inside* the guarded region (the double-check `current > max` after the inner wait loop — the detail sloppy solutions miss, leaving one thread waiting forever at the end).

**Semaphore version — turn as a permit token:**

```java
Semaphore odd = new Semaphore(1), even = new Semaphore(0);   // odd starts
// odd thread loop:  odd.acquire();  print(n);  even.release();
// even thread loop: even.acquire(); print(n);  odd.release();
```

Teaches: encoding *whose turn* as *where the permit is* — no shared turn variable, no condition predicate at all; signaling and permission collapse into one primitive (Q53's release-as-signal). Cleaner, and it **generalizes to N**: an array of semaphores, thread i does `sems[i].acquire(); work; sems[(i+1) % n].release();` — a token ring. (The wait/notify N-generalization needs `current % n == myIndex` predicates and notifyAll storms — compare the two aloud: the semaphore ring wakes exactly the right thread; the monitor version wakes everyone to re-check (thundering herd in miniature — Q24's notify-vs-notifyAll trade made concrete).)

Also worth one sentence each: the same puzzle solves with two `Condition`s on one lock (per-role wait sets — Q34's point re-demonstrated), or with a `SynchronousQueue` token pass; and the meta-lesson interviewers actually grade — you *narrate invariants* ("exactly one permit exists in the system ⇒ exactly one thread runnable ⇒ alternation is structural") rather than tracing interleavings.

**Follow-up trap:** *"Your odd/even threads print correctly but the program never exits."* — the final `release()`/`notifyAll` for the *other* thread's termination check is missing (it's parked forever after max) — the end-of-protocol wake is the classic omission; showing you already handled it is the win.

---

### Q91. Implement a thread-safe LRU cache in an interview: the single-lock LinkedHashMap version first, then the honest analysis of why "concurrent LRU" is hard, segmented/lock-free approaches, and what Caffeine actually does.

**Answer:**

**Correct-and-simple first (say you'd start here):**

```java
class LruCache<K, V> {
    private final int capacity;
    private final LinkedHashMap<K, V> map;
    private final Object lock = new Object();

    LruCache(int capacity) {
        this.capacity = capacity;
        this.map = new LinkedHashMap<>(16, 0.75f, /*accessOrder=*/true) {
            @Override protected boolean removeEldestEntry(Map.Entry<K, V> e) {
                return size() > LruCache.this.capacity;    // auto-evict on insert
            }
        };
    }
    V get(K k)        { synchronized (lock) { return map.get(k); } }      // get MUTATES order!
    void put(K k, V v){ synchronized (lock) { map.put(k, v); } }
}
```

Key teaching beat: with `accessOrder=true`, **`get` is a write** (relinks the entry to the tail) — which is precisely why a `ReadWriteLock` "optimization" is *wrong* here (reads mutate — RWL's shared read lock would corrupt the list; Q35's "know when RWL doesn't apply" made concrete) and why `Collections.synchronizedMap` over it is insufficient-looking but actually equivalent to this. One lock, tiny sections — correct, and fine up to serious contention.

**Why concurrent LRU is genuinely hard:** every access must update a *global* recency ordering — a doubly-linked list head/tail is a **single hot spot** touched by all threads (the anti-pattern of Q39: all cores fighting one cache line); exact LRU under concurrency ≈ serialized by definition. The engineering responses, in escalation order: (1) **segmenting** — N independent LRU shards by key hash (Q23's striping): recency approximate globally, exact per-shard — simple, effective; (2) **approximate recency** — sample-based eviction (Redis-style: evict best-of-k random), or CLOCK bits; (3) **buffered recency** — the **Caffeine** design (describe it — this is the differentiator): reads record into striped **ring buffers** (lossy under pressure — by design!), a single maintenance task drains buffers and updates the policy asynchronously; the policy itself is **Window-TinyLFU** (frequency sketch + admission window — better hit rates than pure LRU); result: reads are ~lock-free with batched, amortized policy maintenance. "Exactness traded for scalability, deliberately and measurably" is the thesis sentence.

**Follow-up trap:** *"Add TTL expiry to your simple version."* — timestamp per entry + lazy expiry on access, plus the "who evicts idle expired entries" question (scheduled sweep — Q49's wrapped-runnable discipline — or a `DelayQueue` of expirations — Q64.6); each answer has the by-now-familiar shape, which is the point of asking it here.

---

### Q92. Why concurrency bugs evade reproduction — the Heisenbug taxonomy — and the production debugging playbook when you suspect a race: what evidence to gather, in what order, without redeploying.

**Answer:**

**The taxonomy of evasion (name the mechanisms):** interleaving-dependence (the window is nanoseconds — Q87); **observation perturbation** (logging/debugger adds sync + timing that closes the window; debugger breakpoints serialize everything — the debugger is nearly useless for races, say it plainly); environment-dependence (JIT tier, core count, arch memory model — Q20c/Q87; load level — some bugs *require* saturation, Q50.4); and *benign-looking survival* — a data race is allowed to produce correct results for months (Q11: races may look right in every observed run). Corollary: absence of failures ≠ absence of races; only reasoning (HB analysis) or tools (jcstress) *bound* the behavior.

**The playbook (ordered, non-invasive first):**

1. **Fix the symptom's fingerprint:** what invariant broke — lost update (counter low)? torn/partial state (impossible field combo → publication race, Q14)? stale read forever (missing volatile, Q12)? duplicate work (check-then-act, Q10)? Each fingerprint indicts a specific bug family — this mapping *is* the skill.
2. **Gather without redeploying:** thread dumps ×3 spaced seconds apart (`jstack`/`jcmd` — compare: who moves, who doesn't — Q94); **JFR** recording in production (near-zero overhead — lock contention events, allocation, safepoints — Q93/Q88); existing metrics (the drifting counter's drift *rate* vs load — races scale with concurrency, corruption from a logic bug doesn't: a genuinely diagnostic correlation); heap dump if state corruption persists in objects (inspect the impossible object — its field combination often names the missing edge).
3. **Audit, don't reproduce:** with the fingerprint + suspect code, do a happens-before audit of the state's access paths (every read/write, what edge orders them — Q13/Q20a's method); most production races are *found by reading*, guided by the fingerprint, not by reproducing. `@GuardedBy` annotations (present or absent) accelerate exactly this.
4. **Then** try amplified reproduction offline: stress harness at saturation on target-arch hardware (Q87.1), jcstress for the isolated idiom.
5. **Fix with an ordering argument, not a sleep:** the patch must name the edge it adds ("volatile publish → acquire read"); any fix whose justification is "it stopped happening" is a re-scheduled bug — the review standard to state.

**Follow-up trap:** *"Why not just add logging around the suspect code in prod?"* — the perturbation problem (may suppress it) *plus* logging's own synchronization creates HB edges that mask the race in exactly the runs you're watching — use JFR/async-profiler (designed to observe without ordering side effects) instead. Knowing *why* the obvious move backfires is the depth check.

---

### Q93. Measuring contention and concurrency performance: JFR lock-contention events, async-profiler modes, Hibernate-of-locks folklore vs data, Amdahl's law and the Universal Scalability Law as the mental frames, and the optimization order that follows.

**Answer:**

**The frames first:** **Amdahl** — speedup bounded by the serial fraction: 5% serialized caps you at 20× regardless of cores; locks, shared queues (Q78), even a hot atomic (Q39) *are* the serial fraction. **USL** (Gunther) extends it with the **coherency term** — beyond a point, *adding threads makes throughput fall* (the retrograde region), driven by crosstalk: cache-line ping-pong, lock convoys, coordination — matching the real curves you see (throughput rises, plateaus, *drops*). Diagnosing "we added threads and got slower" as coherency cost, by name, is the senior version of this conversation.

**Measurement toolkit:** **JFR** (production-safe): `jdk.JavaMonitorEnter` (monitor blocked events — which lock, how long, whose stack), `jdk.ThreadPark` (j.u.c. contention — Q4's WAITING world), allocation profiling, safepoint events (Q88) — JMC's lock-contention view ranks your actual hot locks; **async-profiler** in `-e lock` mode (contended-lock flamegraphs) and `-e wall` (wall-clock — where threads *wait*, not just where they burn CPU: for I/O-bound and contention analysis, wall-mode is the truth serum, CPU-mode the classic misleader — a distinction worth stating explicitly); `ThreadMXBean` contention counters programmatically; Hikari-style pool metrics for resource-wait (Q89-JPA crossover); and **JMH** for the microbenchmark layer — with its concurrency features (`@Threads`, `@Group` asymmetric benchmarks) and the standard warnings (warmup, dead-code elimination, coordinated omission for latency — name them to prove you've been burned).

**Optimization order (the discipline the data feeds):** (1) *reduce time under contention* — shrink critical sections, move I/O/allocation out (Q23); (2) *reduce sharing* — striping, LongAdder, per-thread accumulate-then-merge, confinement (Q89) — this attacks USL's coherency term directly and is usually the big win; (3) *change mechanism* — RWL/Stamped optimistic reads (Q35), lock-free CAS (Q37) — only after 1–2, because mechanism swaps on an unshrunk hot section just move the queue; (4) *change the algorithm/architecture* — partition the domain (shard the state so threads don't meet — the end-game that makes the lock question moot). Folklore check: "synchronized is slow" (uncontended: no — Q21), "lock-free is always faster" (high contention: often no — Q37), "more threads = more throughput" (USL: only until coherency) — data over folklore, each time.

**Follow-up trap:** *"Your profiler shows 40% time in `Unsafe.park` — what does that mean?"* — nothing by itself: parked = waiting (idle pool threads park legitimately) — the question is *what they wait for* (queue empty = fine; lock = contention; future = maybe Q50.4) — park time must be attributed via stacks before it's a finding. Refusing to treat a number as a conclusion is exactly what the question screens for.

---

### Q94. Reading a thread dump like a senior: the fields that matter, the five patterns to scan for (deadlock, convoy, pool exhaustion, stuck I/O, idle-normal), multi-dump differencing, and modern tooling (JSON dumps, virtual threads).

**Answer:**

**Per-thread anatomy:** name (why Q44's naming discipline pays here), state (Q4's map — with the BLOCKED-vs-WAITING and RUNNABLE-in-native-I/O caveats), held monitors (`- locked <0x…>`), awaited monitor/synchronizer (`waiting to lock` / `parking to wait for <j.u.c. object>`), and the stack (what it was doing). The JVM appends detected monitor/ownable-synchronizer deadlocks at the bottom — **read the bottom first**.

**The five scan patterns:**

1. **Deadlock:** the appended detector output; or manually — a cycle in "holds X, wants Y" (remember its blind spots: semaphores/latches/external resources don't appear — Q26).
2. **Lock convoy / hot monitor:** dozens BLOCKED on **one** address, one RUNNABLE owner inside a long operation — the dump literally names your hottest lock and its guilty critical section (Q23's shrink-list follows).
3. **Pool exhaustion / starvation deadlock:** all `worker-*` threads parked on futures/queue-takes belonging to work only that same pool can do (Q50.4's signature), or all parked in a downstream wait (connection pool — the `pool-*` waiting on Hikari signature, your JPA Q89 in dump form).
4. **Stuck I/O:** many RUNNABLE in `socketRead0`/native frames (RUNNABLE lies — Q4) with the downstream's name in the stack — timeout audit follows.
5. **Idle-normal:** pool threads WAITING in `getTask`/queue poll — *healthy*; the anti-skill is paging someone over idle workers, so say what normal looks like.

**Differencing — the real technique:** one dump is a photo, three dumps 5–10s apart are a film: threads *in the same frame* across dumps are stuck (a RUNNABLE that never moves = spin or long computation — Q27's livelock shows here as moving-but-repeating stacks); progressing threads clear themselves. Tools: fastThread-style analyzers group by stack fingerprint; `jcmd Thread.print` vs kill -3; and modern: **`jcmd Thread.dump_to_file -format=json`** — includes **virtual threads** (plain jstack historically shows only platform/carriers — a critical gap once you're on Loom: millions of VTs need the JSON dump + tooling, and structured scopes give the tree view — Q85), plus JFR's periodic thread snapshots for after-the-fact dumps you forgot to take.

**Follow-up trap:** *"High CPU, and the dump shows everything WAITING — where's the CPU going?"* — the dump shows *Java* threads at *safepoint-ish* instants: suspects are GC threads (not in the Java section — check GC logs), JIT compilation, a spinning native/VM thread, or profiling overhead — correlate with `top -H` mapping native TIDs to the dump's `nid=` fields (the hex thread-id trick — naming `nid` matching is the practitioner's tell).

---

### Q95. Graceful degradation patterns around concurrency: bulkhead, circuit breaker, rate limiting, load shedding, timeout budgets — as *concurrency* constructs (what each bounds), and how they compose in one service.

**Answer:**

Frame each as "what resource it bounds and what failure it converts":

- **Timeout** (the primitive under everything): bounds *time a thread/VT is held* by any single operation — converts "hung dependency" into a handleable error. No timeout = every other pattern leaks (a bulkhead full of infinite waits is just a smaller catastrophe — Q50.6). Budgeted end-to-end, tightening inward (Q73's onion).
- **Bulkhead:** bounds *concurrency per dependency/feature* — a pool (thread bulkhead — Q45) or semaphore (Q53) per downstream: converts "one dependency's outage" from total-worker-consumption into a bounded compartment's loss; the others sail on. This is Coffman-breaking at the architecture level — no shared thread resource, no systemic hold-and-wait.
- **Circuit breaker:** bounds *work sent to a known-failing dependency* — closed→open on failure-rate threshold (fail fast: callers get instant errors/fallbacks instead of queueing on timeouts — which *frees the concurrency* timeouts alone still consume for their full duration), half-open probes recovery. Its concurrency essence: replacing N threads × timeout-seconds of doomed waiting with zero — a breaker is a *thread-liberation* device, and saying it that way marks the concept as understood.
- **Rate limiter:** bounds *admission rate* (token bucket — Q56) — smooths bursts to sustainable throughput before they become queue depth.
- **Load shedding:** bounds *queue depth/latency debt* — reject (429/503 + Retry-After) when saturated rather than queueing into uselessness (Q43's doctrine: the queue you don't build is latency users don't pay); sophisticated variants shed by priority/cost.

**Composition in one service (the picture to draw):** edge rate-limit → admission load-shed on queue depth → per-dependency bulkhead → circuit breaker per dependency → timeout per call → fallback per feature (Q76's per-branch policies) — each layer converting a would-be resource exhaustion into a fast, observable, partial failure. Resilience4j/Hystrix lineage implements exactly this stack (decorator-composed); the interview twist worth volunteering: **virtual threads change the bulkhead's substance** (semaphores, not pools — Q84) **but not its necessity** — downstreams are still finite even when threads aren't.

**Follow-up trap:** *"Breaker vs retry — tension?"* — retries multiply load on a struggling dependency (retry storms — Q27's livelock cousin); resolution: retries with jittered backoff *inside* a budget, counted by the breaker, disabled when it opens — retry beneath breaker, never beside it. The composition question is the real question.

---

### Q96. Idempotency and exactly-once semantics as concurrency concepts: why retries + concurrency force idempotent design, the in-JVM version (deduplication, at-most-once execution per key), and `computeIfAbsent`/`putIfAbsent` as idempotency primitives.

**Answer:**

**The forcing argument:** any system with retries (timeouts *require* them — did the work happen before the timeout?) and concurrency (two callers, one intent) will execute intents **more than once or concurrently** — you cannot prevent it, only make it harmless: **effect-idempotency** — applying the operation twice ≡ once. This is the same theorem at every scale: in-JVM double-submit, at-least-once message delivery (Q57), HTTP retries (your JPA doc's outbox/idempotency-key discussion — one concept, three costumes; connect them explicitly).

**In-JVM idempotency mechanics — the primitives:**

```java
// 1. At-most-once execution per key — the FutureTask-in-map idiom (Q67):
ConcurrentHashMap<String, CompletableFuture<Receipt>> inFlight = new ConcurrentHashMap<>();
CompletableFuture<Receipt> process(String idemKey) {
    return inFlight.computeIfAbsent(idemKey, k ->        // atomic claim (Q59)
        CompletableFuture.supplyAsync(() -> doProcess(k), pool));
}   // concurrent duplicates get the SAME future — one execution, shared result
```

Teaching beats: `computeIfAbsent`'s per-key atomicity *is* the dedup lock (Q10's fix promoted to a pattern); returning the **future** (not the value) means late duplicates *join the in-flight work* rather than re-running or blocking (memoized concurrency); failure policy is a real decision — a failed future cached forever poisons the key (remove-on-failure via `whenComplete`, or cache with TTL) — naming the negative-caching problem is the senior beat; eviction (completed keys accumulate — Q29's map-growth issue again: TTL store/Caffeine for real systems, and the idempotency window becomes an explicit business parameter).

`putIfAbsent(key, marker) == null` is the lighter claim-a-key primitive (exactly-one-winner election for "who sends the email"); `AtomicBoolean.compareAndSet(false, true)` the single-shot version (one-time initialization/close — idempotent `close()` is this pattern in miniature).

**"Exactly-once" honesty (the phrase to deploy):** exactly-once *delivery* is impossible under failure; achievable is **at-least-once execution + idempotent effects = exactly-once *outcome*** — in-JVM, the map above; cross-system, dedup keys persisted transactionally with the effect (outbox's mirror image — the inbox pattern). One sentence each and the interviewer knows you've built it.

**Follow-up trap:** *"Two app instances — does your CHM dedup work?"* — no: per-JVM only; distributed dedup needs the shared store (DB unique constraint on idem-key — the constraint *is* the putIfAbsent, at database scope — your JPA Q78/95 knowledge closing the loop) — recognizing which scope each primitive covers is the whole distributed-systems bridge.

---

### Q97. Lazy initialization, once-only actions, and memoization under concurrency: the full menu — holder idiom, DCL, `computeIfAbsent`, FutureTask memoization, `Suppliers.memoize`-style, `AtomicBoolean` once-flags — and how to pick.

**Answer:**

The menu, each with its exact niche (this question is a matching exercise):

1. **Eager init** — default; lazy must justify itself (Q16).
2. **Holder idiom** — lazy *static* singleton, zero sync cost after init, JVM-guaranteed once (Q16); pick for expensive static resources.
3. **DCL + volatile** — lazy *instance* field when holder doesn't apply (per-object laziness); the error-prone spelling — prefer 4/5 where shape allows (Q16).
4. **`ConcurrentHashMap.computeIfAbsent`** — lazy *per-key*: caches/registries (Q66); atomic per key, but **blocking per bin** during compute — expensive computations under it stall neighbors (Q59's rule) → escalate to 5.
5. **Future-based memoization** — per-key, *non-blocking claim + shared wait*: `computeIfAbsent(k, → new CF/FutureTask)` then await outside the map lock (Q96's pattern; JCiP's Memoizer): duplicate suppression *and* short bin-lock hold — the industrial per-key answer; add failure-eviction policy (Q96).
6. **Memoized supplier** — lazy *single value* with functional dressing: an `AtomicReference`-or-DCL-under-the-hood `Supplier<T>` (Guava `Suppliers.memoize`) — pick when the laziness is an implementation detail of a dependency.
7. **`AtomicBoolean.compareAndSet` once-flag** — not memoization but *once-only action* (start/close/migrate — Q96): exactly-one-winner, others skip (vs holder: others don't need the result). If losers must *wait* for the winner's completion, that's a latch or the future pattern (5) — distinguishing "skip" from "wait" semantics is the fine point that separates candidates.

**Cross-cutting checks:** what must "once" mean under failure — once-*attempted* (flag) vs once-*succeeded* (retryable: reset flag on failure / evict failed future)?; visibility of the initialized value (every option above carries its HB story — holder via class-init, DCL via volatile, CHM/future via their contracts — Q13; a hand-rolled `if (x == null) x = create();` on a plain field has none and is the bug this whole question orbits); and idempotence of `create()` itself as the escape hatch (if creating twice is harmless and cheap, a benign race with a plain volatile publish — `racy single-check` — is legal and simplest: the expert's occasionally-correct shortcut, named as such).

**Follow-up trap:** *"Spring beans are lazy-initialized singletons — which pattern is the container using?"* — conceptually the future/memoizer family (creation locked per bean definition, dependents wait) — and circular dependencies are its deadlock analogue. Mapping framework behavior onto the primitive patterns is the integration signal.

---

### Q98. API design for concurrency: how to *document and expose* thread-safety — immutable vs thread-safe vs conditionally-safe vs thread-hostile contracts, `@GuardedBy`-style annotations, designing APIs that make misuse hard, and reviewing a class for its concurrency contract.

**Answer:**

**The contract taxonomy** (JCiP's, still the working vocabulary): **immutable** (safe, full stop — Q17); **thread-safe** (any interleaving of public calls is safe — internal protocol handles everything; document *compound* semantics separately: "size() is an estimate" — Q59); **conditionally thread-safe** (safe per-op, but sequences need client locking — and the doc must name *which lock*: `synchronizedList`'s iterate-under-the-wrapper rule — Q63); **not thread-safe** (confine or synchronize externally — most domain objects, and that's *fine* when stated); **thread-hostile** (unsafe even with external sync — mutates global/static state: legacy `SimpleDateFormat` in a static — the cautionary genus). An undocumented class defaults, socially, to "assume not thread-safe" — but the *author's* failure to state it is the actual bug; "every class has a concurrency contract whether or not anyone wrote it down" is the thesis line.

**Documentation tools:** class-level javadoc stating the category + the invariants; `@GuardedBy("lock")` on fields/methods (machine-checkable with ErrorProne — turning documentation into lint); `@Immutable`/`@ThreadSafe` annotations as compiler-visible claims; and *publication requirements* documented ("safe for concurrent use after safe publication" — Q14's fine print that libraries owe their users).

**Designing misuse away (the constructive half):** prefer returning immutable values (`List.copyOf`) over live views — no aliasing questions; take snapshots in, hand snapshots out (Q30's registry); expose *atomic compound* operations rather than getters that invite check-then-act (`putIfAbsent` over `containsKey`+`put` — API-shaping as race prevention, Q10 institutionalized); make callbacks' threading explicit in the doc ("invoked on the maintenance thread; must not block" — Q69's lesson as a contract); constructors don't leak `this` (Q15), fields final by default; and builders/factories that produce fully-initialized objects (safe construction as an API property).

**Review checklist for "what's this class's contract":** every field — final? confined? guarded (by what)? volatile (why)?; every public method — compound over shared state?; every callback/alien call — under a lock (Q23)?; every returned reference — live internal state escaping?; and the one-line summary the class *should* have carried — write it as the review output. That checklist is Q89's hierarchy turned into a code-review instrument, which is exactly the shape senior engineering takes.

**Follow-up trap:** *"Is 'stateless' automatically thread-safe?"* — stateless service objects, yes (nothing to race on) — *if* actually stateless: injected mutable collaborators, "harmless" instance caches, and non-thread-safe tools stored in fields (the SimpleDateFormat-in-a-@Service classic) quietly break the claim — "stateless is a property you verify, not declare."

---

### Q99. The cross-cutting interview chain: "volatile vs synchronized vs Atomic vs Lock — when each?" answered as a decision procedure, then the same for "how do I make X thread-safe?" — the two questions every interview asks, with the answers structured for delivery.

**Answer:**

**Chain 1 — the mechanism ladder, as a decision procedure (deliver in this order):**

1. Is it a **single value, written by one/few, read by many, no compound ops**? → **volatile** (visibility + ordering, no atomicity — flags, published snapshots — Q12). The moment you need read-modify-write, volatile is out.
2. Is it a **single variable with compound updates** (counter, reference swap, claim-a-slot)? → **Atomic\*** (CAS: lock-free, cheap, composable via retry loops — Q37); statistics-grade hot counters → **LongAdder** (Q39).
3. Is it a **multi-field invariant or multi-step critical section**? → a **lock**: `synchronized` by default (less to get wrong, dump-friendly — Q31's rule); **ReentrantLock** when you can name the feature: tryLock/timeout/interruptible/fairness/multi-condition (Q31); read-mostly with long reads → RWL/StampedLock *after* measuring (Q35).
4. Is the state a **standard shape** (map/queue/list)? → skip 1–3: **delegate** to the concurrent collection whose atomic vocabulary covers your invariants (Q89.3, Q64's tree).

Then the qualifier that turns a list into judgment: the ladder orders by *capability*, but selection runs **top-down from the invariant** — "what must be atomic, together?" decides the rung; performance folklore never does (Q93).

**Chain 2 — "make X thread-safe": the strategy procedure (Q89 compressed for delivery):** (1) can X be **confined**? (one thread owns it; queues carry requests — often the best answer and the least expected one); (2) can X be **immutable** (+ snapshot swap for evolution)?; (3) can X **delegate** wholly to concurrent components (audit every compound op — Q63's migration warning)?; (4) else **design the locking**: name invariants → smallest lock set covering each → document (`@GuardedBy`) → alien-call and hold-time hygiene (Q23) → state the HB story for readers (Q20a). Close with testing (invariant stress + jcstress if novel — Q87) and the contract documentation (Q98).

Delivery advice baked in (this is a coaching question): interviewers score the *procedure* — asking "what's the invariant?" before naming a tool — far above tool trivia; the shape of your first three sentences decides the round. These two chains are the spine every earlier question hangs from; rehearse them as monologues.

**Follow-up trap:** *"Quickfire: AtomicLong or synchronized for a counter?"* — Atomic (single variable, single op); *"...that must update together with a timestamp?"* — lock or immutable pair-swap (multi-field invariant — the rung changes mid-sentence, which is exactly the trap: they're testing whether your procedure actually runs, or you memorized answers).

---

### Q100. Capstone — the concurrency review checklist: inheriting a multithreaded Java codebase, your top-15 hunt list, each with the one-line mechanism and the fix — how a staff-level engineer summarizes this entire document.

**Answer:**

1. **Shared mutable state without a declared guard** — fields touched by multiple threads, no volatile/lock/`@GuardedBy` story → HB audit, apply Q99's procedures (the root category; everything below is its instances).
2. **check-then-act on thread-safe components** — `containsKey`+`put`, `!file.exists()`+create → atomic compounds (`computeIfAbsent`, `putIfAbsent`) (Q10/59).
3. **Non-atomic read-modify-write** — `count++`, `x = x + delta` on shared fields → Atomic/LongAdder/lock (Q10/38/39).
4. **Unsafe publication** — objects handed across threads via plain fields; `this`-escape in constructors; listener registration mid-construction → safe-publication idioms, final fields (Q14/15).
5. **synchronized-on-wrong-object** — `this`/class/String/boxed/reassignable locks; instance-lock guarding static state → private final locks, one-guard-per-invariant (Q22/23/29).
6. **wait/notify without the discipline** — `if` instead of `while`; notify with heterogeneous waiters; condition state unguarded → guarded-wait pattern or replace with j.u.c. components (Q24/25).
7. **Swallowed interrupts and swallowed task exceptions** — empty `catch (InterruptedException)`; `submit()` futures never consumed; `@Async void` → restore-interrupt rule, terminal handlers, UEHs (Q8/9/50/71).
8. **Unbounded everything** — `newFixedThreadPool`'s hidden infinite queue, cached pools, unbounded CLQ producers → bounded queues + explicit rejection mapped to backpressure (Q42/43/46).
9. **Blocking inside the wrong context** — I/O under locks, blocking in CF stages/common pool, same-pool blocking joins (starvation deadlock), sleeps under synchronized → pool separation, timeouts, open calls (Q23/50.4/74).
10. **Missing shutdown lifecycle** — executors never shut down (JVM won't exit / deploys drop work); scheduled tasks with unhandled exceptions (silently dead jobs) → Q47's sequence, Q49's wrapped runnables.
11. **ThreadLocal residue and context bleed** — set-without-remove on pooled threads; ITL with pools; MDC bleeding across requests → finally-remove, decorators, ScopedValue trajectory (Q18/77/86).
12. **Lock-ordering exposure** — nested locks across modules, locks held into callbacks → ordering conventions, open calls, structural redesign to single-lock or message-passing (Q26/28).
13. **Collection misuse under concurrency** — plain HashMap shared (silent corruption), synchronizedList iterated unlocked, COW on hot write paths, size() misread on lock-free structures → Q64's access-pattern tree.
14. **Time and timeout malpractice** — wall-clock elapsed math, absent timeouts on blocking calls, unbudgeted retry storms → nanoTime, timeout onion, jittered budgets under breakers (Q73/82/95).
15. **No observability for concurrency** — unnamed threads, no JFR habit, no invariant stress tests, no thread-dump literacy on the team → naming (Q44), Q87's test tiers, Q92–94's playbooks as team practice.

Close as the staff engineer does: almost every item is invisible until load or unlucky timing arrives (Q92) — so concurrency review is *architecture* review: hunt the shared mutable state first (items 1–5 are one disease), the resource bounds second (8–10), the operational safety net third (14–15). Triage by blast radius: correctness corruption (1–6) > systemic outage shapes (8–12) > performance (everything else) — and justify the ordering out loud. That prioritization, plus the reflex of asking "what's the invariant?" before touching any tool, *is* what six years of experience means in this domain.

**Follow-up trap:** *"Pick the three you fix first in a payment service."* — (1) shared-state/HB audit on money-touching paths (corruption-class), (2) idempotency + timeout/retry budgets on external calls (duplicate-payment-class — Q96), (3) bounded pools with real rejection (outage-class) — blast-radius triage, stated with reasons, is the staff-level signal the whole question exists to elicit.

---

## Closing advice

Sections 1–3 are table stakes — miss there and the interview ends early. Sections 4–6 are where mid-level candidates get separated from senior ones. Sections 7–8 dominate practical backend rounds, and Sections 9–10 (virtual threads, debugging playbooks, design capstones) are where senior and staff offers get made. For every concept, practice the four-beat delivery out loud in 60–90 seconds: **definition → mechanism underneath → where it breaks → what you'd use instead**. Pair this document with the Spring Data JPA set — interviewers love crossover questions (`@Transactional` proxies vs `@Async` proxies, connection-pool sizing vs thread-pool sizing, optimistic locking vs CAS), and having both vocabularies wired together is precisely the profile of a strong six-year backend engineer.
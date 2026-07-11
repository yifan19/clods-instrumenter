# `bm_instrument` user guide — building, running, and reading the output of the instrumentation tool

This is a practical "how do I actually use this thing" guide for `blameMasterInstrument`, the
live-JVM bytecode instrumentation tool in this repo. It complements the docs that already exist:

- `README.md` — which bug lives in which folder, per-bug commit/version ground truth.
- `ARTIFACT_EVALUATION.md` — full reproduction recipe for regenerating the paper's results table,
  environment prerequisites, known gaps.

This guide instead answers: what does the tool actually do at the bytecode/socket/disk level, how
do you drive it (standalone, or via the reusable `common/lib`-style orchestration used by the
`final_artifact` reproduction package), and — since nothing else in this repo documents it —
**exactly what format is the data it produces**, including the stack-trace-per-probe hashtable, so
you can write your own reduction scripts against it.

Everything below is grounded in this repo's `main` branch. The `android`/`protobuf`/`protobuf_2`/
`plans`/`production`/etc. branches use a different plan/log format in places (e.g.
`server-bugs/instrumentation_hbase3627/parse.py`'s protobuf-style example belongs to one of those,
not `main`) — pull instrumentation details from `main` only unless you're specifically working on
one of those branches.

---

## 1. What it is

`blameMasterInstrument` attaches to a **running** JVM (a live NameNode, RegionServer, QuorumPeer,
Cassandra daemon, ...) via the JDK Attach API and uses Javassist to splice one logging call into
one exact bytecode offset of one target method, per instrumentation "rule". Once attached, it logs
one value every time that offset executes, for the life of the process — no restart, no source
change, no rebuild of the target.

Three entry points, all under `ca.uoft.drsg.bminstrument.*`, run with:

```bash
java -cp $JAVA_HOME/lib/tools.jar:target/uber-blameMasterInstrument-1.0.jar ca.uoft.drsg.bminstrument.<Class> ...
```

| Class | Role |
|---|---|
| `Launcher <jar> <pid>` | Attaches the agent (`InstrumentationAgent`) to the target JVM by PID. Starts a control-socket listener on the target JVM at `localhost:8089`. Never throws on failure — see §8. |
| `PseudoClient "<command>"` | One-shot client for the control socket: sends `<command>`, prints/returns the one-line response, exits. `PseudoClient file <path>` instead sends every line of `<path>` as a separate command. |
| `CommandLine` | Offline (APK-file) instrumentation entry point used by the Android track. **Not present on `main`** — see `ARTIFACT_EVALUATION.md` §1 (it exists on the `android` branch). |

## 2. Build

```bash
mvn package -DskipTests
# -> target/uber-blameMasterInstrument-1.0.jar
```

Needs JDK 8 (`pom.xml`'s `maven.compiler.source/target=1.8`).

## 3. Instrumentation plans (`.properties`)

A "rule" is one `.properties` file:

```properties
ID=0
strategy=before
className=org.apache.cassandra.db.Columns$Serializer
methodName=deserialize
parameterTypes=org.apache.cassandra.io.util.DataInputPlus,org.apache.cassandra.config.CFMetaData
lineNumber=443
byteCodeIndex=61
variableName=foo
```

| Field | Meaning |
|---|---|
| `ID` | Integer probe ID, unique within a run. **Can be negative** (`-1`, `-39`, ... — many bugs' "entry"-style probe conventionally uses a negative or `-1` ID). This ID is what shows up as the `id` half of every logged (id, value) pair — see §7. |
| `className` / `methodName` / `parameterTypes` | The target method. `parameterTypes` is a comma-separated list of fully-qualified types (empty = no-arg method), used to disambiguate overloads. |
| `lineNumber` | A source line inside the method to splice at, **or** the literal string `ENTRY`/`entry` for a whole-method-entry probe (used by `logCutting`/`stackTrace` strategies, see below). |
| `byteCodeIndex` | The bytecode offset within that line to splice at (found via decompilation/`javap` ahead of time — this is why probe search spaces exist: several candidate offsets get tried as different `ID`s across a bug's rounds). |
| `variableName` | Name of the local variable/operand-stack value to capture (default/`before`/`after` strategies only). |
| `strategy` | See below. |
| `LoopID` (optional) | Groups probes that sit inside the same loop iteration, for loop-aware capture (`putLoop`). |

### Strategies

| `strategy=` | What gets logged | Buffer call spliced in (`Transformer.java`) |
|---|---|---|
| *(unset, or `before`/`after`)* | The live value of `variableName` at that exact bytecode offset — the "conditional" probe used to evidence a bug's root cause. | `buffer.put((long)<variableName>, (long)ID)` |
| `logCutting` | A timestamp (`System.currentTimeMillis()`), captured once on method entry — used for entry/exit timing pairs. | `buffer.putEntry((long)ID)` |
| `stackTrace` | The **full call stack** at method entry (see §7) — used when the bug is about *which caller* reaches a method, not a data value. | `buffer.putStack((long)ID)` |

`lineNumber=ENTRY` is required for `logCutting`/`stackTrace` (they instrument method entry, not a
specific line).

## 4. Driving it — standalone (cheat sheet)

```bash
# 1. build (once)
mvn package -DskipTests

# 2. attach to a running JVM by PID (also starts the control socket on 8089)
java -cp $JAVA_HOME/lib/tools.jar:target/uber-blameMasterInstrument-1.0.jar \
     ca.uoft.drsg.bminstrument.Launcher target/uber-blameMasterInstrument-1.0.jar <PID>

# 3. install a probe
java -cp $JAVA_HOME/lib/tools.jar:target/uber-blameMasterInstrument-1.0.jar \
     ca.uoft.drsg.bminstrument.PseudoClient "add path/to/some.properties"
# -> "OK <ruleId>" or "FAIL"

# 4. drive the workload that exercises the target method, then...

# 5. dump everything logged so far
java -cp $JAVA_HOME/lib/tools.jar:target/uber-blameMasterInstrument-1.0.jar \
     ca.uoft.drsg.bminstrument.PseudoClient "collect all"
# -> "OK <n1> <n2> ..." — see §6/§7 for what this actually means
```

## 5. Driving it — via the reusable orchestration (`final_artifact/common/lib`)

The `final_artifact` reproduction package (the sibling repo that vendors this tool to reproduce all
13 server-side bugs) wraps the exact four commands in §4 in a small set of reusable scripts under
`common/lib/`, shared by every bug so the baseline/inject/collect protocol isn't reimplemented per
bug:

```
common/lib/run_n.sh          per-node driver:
                                run_n.sh inject <AppNameSubstring>   -> Launcher, retried until port 8089 answers
                                run_n.sh add <round-number>          -> PseudoClient "add ..." for every
                                                                          plan in that round (skips .disabled)
                                run_n.sh collect                     -> PseudoClient "collect all"

common/lib/run_workload.sh   per-bug orchestrator: for baseline, then each round —
                                reset the cluster -> inject_round (calls run_n.sh inject + add on
                                every target host) -> run the workload (YCSB/HiBench) -> collect_phase
                                (calls run_n.sh collect, and pulls artifacts off the node — see §6)
```

A concrete worked example (`<final_artifact>/cassandra-13004/run_experiment.sh`) brings up a
4-container Docker Compose cluster, then does exactly:

```bash
docker compose exec master /opt/lib/cluster_ctl.sh prepare
docker compose exec master /opt/lib/cluster_ctl.sh format
docker compose exec master /opt/lib/cluster_ctl.sh start
docker compose exec master /opt/lib/run_workload.sh     # reads WORKLOAD/ROUNDS/... from env, drives
                                                          # baseline + round1 + round2, calling
                                                          # run_n.sh under the hood
```

Every other bug folder in that package follows the identical shape — only the env vars
`run_experiment.sh` exports (`WORKLOAD`, `HADOOP_NAME`, `CASSANDRA_NAME`, `INST_DIR`, ...) differ.

## 6. Where the output actually lands, and what's in it

Two different things come back from a `collect`, and they're easy to conflate:

### 6a. The status line — not the data

Per `Protocol.processCollect()`, the socket response to `collect all` is **only**
`"OK <n1> <n2> ..."` — one number per active per-thread ring buffer, being the *last flushed
sequence index* for that thread, e.g. `OK 4095`. It tells you collection succeeded and roughly how
many events fired; it is **not** the logged values themselves. (If you see just `"OK\n"` with no
numbers, no thread ever logged anything for that probe — check the probe's `byteCodeIndex` actually
gets hit.) In the `final_artifact` package this line is all that ends up in each
`<bug-id>/results/<label><round>_<host>.result` file.

### 6b. `/data/` — the actual payload

The real data is a side effect of `collect` (and of the ring buffer filling up mid-run): each
per-thread ring buffer, when flushed, writes a binary file to `/data/` on the instrumented node,
named `<threadId>_<fileIndex>_<flushTimeMillis>` (see `RingBufferInternal.flushToDisk_normal()`).
**This is "the output data" for a run.** In the `final_artifact` package,
`run_workload.sh`'s `collect_node_artifacts()` pulls all of `/data` off the node into
`<bug-id>/results/<label><round>_<host>_data/` right after each `collect`.

(The same `/data/` directory also gets `new<ClassName>.class` files — `Transformer`'s debug dump of
the post-instrumentation bytecode for whichever class was just spliced. Harmless to ignore when
parsing for probe data; `scripts/parse_bm_data.py` skips them.)

## 7. Binary format of a `/data/<threadId>_<fileIndex>_<ts>` file

```
[log-event records] [stack-trace hashtable, appended once at the very end of the file]
```

**Log-event records** — zero or more, each exactly 16 bytes, big-endian:

```
 offset 0   8 bytes  int64  id       the probe's ID from its .properties file (can be negative)
 offset 8   8 bytes  int64  value    meaning depends on the probe's strategy:
                                        before/after (conditional): the captured variable's value
                                        logCutting:                  System.currentTimeMillis()
                                        stackTrace:                  a *stack ID* — see below, NOT the trace itself
```

(`LogEvent.persistData`/`retrieveData` — 16-byte `ByteBuffer`, two `putLong`/`getLong` calls, which
is Java's default big-endian order.)

**Stack-trace hashtable** — appended once, after every log-event record for that flush
(`RingBufferInternal.persistStackTrace()`, called at the end of every
`flushToDisk_normal()`/`collect`). Every `strategy=stackTrace` probe hit calls `putStack(id)`, which
captures `Thread.currentThread().getStackTrace()` as a `\n`-joined multi-line string, then **dedupes
it through an in-memory `HashMap<String, Long>`** (`stackTraceMap` — this is the "stacktrace is
stored in a hashtable" behaviour): identical call stacks collapse to the same small integer
*stack ID* (assigned sequentially from 0 the first time each unique stack string is seen), and it's
that stack ID — not the trace — that gets written into the 16-byte log-event record above. The
**full text** only gets written once per unique stack, in this trailing block, one entry per
`HashMap` entry, in whatever order Java's `HashMap` iterates (unordered — don't assume ID order):

```
 8 bytes  int64  stack_id   (always ≥ 0 — HashMap value, assigned 0, 1, 2, ... per unique stack string)
 N bytes  UTF-8  stack text  the frames from Thread.currentThread().getStackTrace()[2:], one
                              StackTraceElement.toString() per line, '\n'-terminated, starting
                              with the immediate caller of the instrumented method
```

There is **no length prefix or delimiter** between one stack-text entry and the next entry's 8-byte
`stack_id` — the on-disk format doesn't self-describe how long each string is. `scripts/parse_bm_data.py`
(§8) recovers entry boundaries heuristically (a real `stack_id` is always small and non-negative, so
its high 7 bytes are `0x00`, which essentially never occurs inside genuine stack-trace text) rather
than by any format guarantee — flagging this explicitly rather than pretending it's a solved
problem, same spirit as `ARTIFACT_EVALUATION.md`'s "Known limitations".

If a round has no `strategy=stackTrace` probes, this trailing block is simply empty (0 bytes) — the
file is pure log-event records.

## 8. Parsing the data

`scripts/parse_bm_data.py` implements the format from §7 end to end: point it at a `/data`-style
directory (or a single ring-buffer file) and it splits log-event records from the stack-trace
hashtable, resolves `stackTrace`-strategy values from stack IDs back into readable call stacks, and
emits CSV/JSON or a human-readable summary.

```bash
python3 scripts/parse_bm_data.py /data/
python3 scripts/parse_bm_data.py --plans server-bugs/instrumentation_bug4/round1 /data/ --format csv > round1.csv
```

Passing `--plans <round-dir>` lets the parser cross-check `id`s against that round's known probe
IDs, which makes record-boundary recovery much more reliable than the no-`--plans` fallback (plain
"does this look like a small integer") — though, per the script's own docstring, not literally
exact: a real probe ID can still numerically coincide with a stack ID at the exact point the
log-event region ends and the stack-trace hashtable begins (both are small, non-negative integers).
Run `python3 scripts/parse_bm_data.py --selftest` to see it round-trip a synthetic file built to
match §7's format exactly, including a multi-entry stack-trace hashtable and one such coincidental
ID collision at the boundary.

## 9. Known gaps (still true on `main`)

- **No `CommandLine.java`** — the offline APK-instrumentation entry point the Android driver
  scripts expect isn't in this source tree (it's on the `android` branch). Not relevant to the
  server-side bugs this guide covers; see `ARTIFACT_EVALUATION.md` §1/§6 for the Android-track
  implications.
- **No aggregation script ships with the tool itself.** `bm_generate/study/*` (where a
  `parse_result.py`-style Max%/read/write/mem/lat reduction would live) is present as directories
  but empty. `scripts/parse_bm_data.py` covers the raw `/data` reduction step (records + stack
  traces); turning that into overhead columns is a further, bug-specific step (diff baseline vs.
  round) that still isn't automated end to end.
- **`Launcher` never signals attach failure through its exit code** — `AgentLoader` swallows every
  exception from the JDK Attach API and only logs it. The only reliable "did instrumentation
  actually start" signal is the control port (`8089`) accepting connections.

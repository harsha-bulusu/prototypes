**Seminar Plan: Understanding Event Loop from Scratch**

---

## Part 1: Fundamentals

### 1. Important Hardware Devices

* **RAM**: Temporary storage for running programs.
* **Processor (CPU)**: Executes instructions.
* **Hard Disk (HDD/SSD)**: Persistent data storage.
* **Network Interface Card (NIC)**: Communicates over networks.

### 2. What are System Calls (Layman Terms)

* Interface between a **user-space** program and the **OS kernel**.
* When user programs need to perform privileged operations (like reading files, accessing network, etc.), they use **system calls**.
* The transition from user space to kernel space happens via an **interrupt** or a **trap** instruction.
* Examples: `read`, `write`, `socket`, `accept`, `recv`, etc.

### 3. Firmware and Device Drivers

* **Firmware**: Low-level software embedded into hardware (BIOS/UEFI).
* **Device Drivers**: Kernel modules enabling OS to talk to hardware devices.

### 4. User Space vs Kernel Space (Execution Flow)

* **User space**: Where user programs run (limited privileges)
* **Kernel space**: Where the OS core and drivers run (full privileges)
* **Flow Diagram**:

  * User calls a function → Library → System Call → Kernel Mode → Device/Action → Result returned to user

---

## Part 2: Mental Models (Diagrams)

1. **How a Program Executes**

   * Source code → Compiler → Binary → OS Loads into RAM → CPU fetches & executes
   * User space process → System call → Kernel handles → Returns to user

2. **CPU Intensive Task (Sum of 1M Numbers)**

   * Constant CPU usage, no I/O wait.
   * Entire computation happens in user space.

3. **I/O Intensive Task (Network Transfer)**

   * CPU initiates request → Kernel takes over → NIC handles data transfer
   * CPU goes idle → Data transfer done by **DMA (Direct Memory Access)**
   * Kernel notifies user space when data is ready

4. **Socket Buffer Blocking**

   * NIC receives data → Kernel socket buffer fills
   * If user process hasn't read, data stays in buffer
   * If buffer is full, kernel may block sender or drop packets

5. **Multithreading - CPU Task**

   * Threads split CPU-heavy work across cores
   * Kernel schedules threads on different CPUs

6. **Multithreading - I/O Task**

   * Threads wait on I/O
   * Context switching increases overhead
   * Event loop is better for I/O

---

## Part 3: Event Loop

### 1. Introduction to Event Loop

* Single-threaded mechanism to handle many I/O-bound tasks.
* Works using **I/O multiplexing**: `select`, `poll`, `epoll`, `kqueue`
* Event loop lives in **user space**, interacts with kernel via system calls

### 2. Event Loop Terminology

* **Event Source**: File descriptor, socket, pipe
* **Reactor Pattern**: Handles registered I/O events
* **Registration**: User registers interest in an event via `kevent`, etc.
* **Dispatch**: When kernel signals readiness, user-space callback executes

### 3. Flow Diagram (to be made in draw\.io)

* Main loop → Register syscalls → Kernel waits for I/O → On readiness, returns event → Callback runs

---

## Part 4: Prototyping Progression

### Blocking Server

* `socket()`, `bind()`, `listen()`
* `accept()` blocks
* `read()` blocks
* One client at a time

### Event Loop - Single Client

* Use of `kqueue`
* `server_fd` is non-blocking
* `kevent` waits for incoming connections
* Read is looped for the same client

### Event Loop - Multiple Clients

* Register every client fd with `EV_SET`
* `kevent()` listens for all
* On `EVFILT_READ`, read from correct `fd`
* On disconnection, `EV_DELETE`

### Code Chunk Highlight Order (for teaching)

1. Blocking server (Basic socket setup, `accept`, `read`, `write`)
2. Make `accept()` non-blocking using `fcntl`
3. Introduce `kqueue` to monitor `server_fd`
4. Register `client_fd` dynamically
5. Handle multiple clients with loop over `events[]`
6. Properly delete disconnected clients

---

## Part 5: Advanced Concepts

### Async I/O

* Explain callback-based flow
* Imperative `async/await` mapping
* System calls like `read`/`recv` defer to kernel, wait for readiness, notify user

### Single-threaded vs Multi-threaded

* Offload CPU tasks to thread pool
* I/O stays on main loop
* Examples from Java (`CompletableFuture`, `parallelStream`)

---

## PPT Plan

* Title: "Building Event Loop from Scratch"
* Slide 1-3: Hardware Overview, System Calls, Kernel/User Space
* Slide 4-6: Diagrams (Execution Model, User-Kernel transition)
* Slide 7-8: Blocking Server (Snippet + Drawback)
* Slide 9-10: Event Loop (Single Client)
* Slide 11-12: Event Loop (Multiple Clients)
* Slide 13: Real-world App Mapping
* Slide 14: Async + Thread Model
* Slide 15: Summary + Q\&A

---

## TODO:

1. [ ] Generate draw\.io diagrams for each mental model
2. [ ] Highlight code snippets (to insert in PPT)
3. [ ] Export PPT
4. [ ] Create `event-loop-demo.md` as a teaching script

---

We will continue step-by-step to complete diagrams and code highlights next.

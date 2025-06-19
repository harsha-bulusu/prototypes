Here’s a **detailed, organized summary of all the core internals** we've covered so far on building a non-blocking event loop using sockets on macOS with `kqueue`, and including kernel-level and hardware-level insights like DMA:

---

# 📘 **Event Loop Internals (macOS + kqueue)**

---

## 1. 🧠 **What is an Event Loop?**

An **event loop** waits for events like:

* Incoming connections
* Data ready to be read/written
* Timers firing
* Signals received

Instead of blocking on system calls like `read()` or `accept()`, it:

1. Monitors multiple file descriptors (FDs)
2. Wakes up only when something is **ready**
3. Processes the event (e.g., reads data)
4. Loops again

---

## 2. ⚙️ **System Calls to Understand**

| System Call                      | OS Feature Used       | Purpose                                      |
| -------------------------------- | --------------------- | -------------------------------------------- |
| `socket()`                       | BSD Sockets API       | Create a socket FD                           |
| `bind()`                         | Networking            | Bind socket to port/IP                       |
| `listen()`                       | Networking            | Mark socket as passive (accepts connections) |
| `accept()`                       | Networking            | Accept incoming connection                   |
| `read()/write()`                 | Unix I/O              | Perform I/O on FDs                           |
| `fcntl(fd, F_SETFL, O_NONBLOCK)` | File descriptor flags | Make I/O non-blocking                        |
| `kqueue()`                       | macOS/BSD Kernel      | Create an event queue                        |
| `kevent()`                       | macOS/BSD Kernel      | Register/Wait for I/O events                 |

---

## 3. 🧩 **kqueue() + kevent()** — macOS Event Notification System

* `kqueue()` → Returns a kernel event queue FD.
* `kevent()` → Registers events or polls for them.

### `EVFILT_READ` (for sockets):

Triggered when:

* There's data in the kernel buffer ready to be read.
* For listening socket: new incoming connection.
* For connected client socket: actual payload/data to `read()`.

```c
EV_SET(&change, fd, EVFILT_READ, EV_ADD | EV_ENABLE, 0, 0, NULL);
```

### kevent() signature:

```c
int kevent(int kq, const struct kevent *changelist, int nchanges,
           struct kevent *eventlist, int nevents,
           const struct timespec *timeout);
```

* `changelist` → What to watch
* `eventlist` → Where OS will report ready FDs
* `timeout` → NULL (blocks), 0 (non-blocking), timespec (timeout)

---

## 4. ⚡ **Non-blocking I/O**

### Why?

* You don’t want `read()` to block when there’s no data.
* Avoid CPU idle waiting — stay responsive.

### How?

```c
int flags = fcntl(fd, F_GETFL, 0);
fcntl(fd, F_SETFL, flags | O_NONBLOCK);
```

* `fcntl()` sets file descriptor flags.
* `O_NONBLOCK` ensures system calls return immediately if they can't proceed.

### What Happens if Not Set?

* `read()` will **block** the entire thread until data arrives.
* `accept()` will block until a connection arrives.
* Bad for scalability or single-threaded models.

---

## 5. 🔁 **How the Event Loop Works (High Level Flow)**

```
      ┌────────────────────┐
      │  kqueue() setup    │
      └────────────────────┘
               ↓
      ┌────────────────────┐
      │  Register interest  │ ◄──┐ (EVFILT_READ on FDs)
      └────────────────────┘    │
               ↓                │
      ┌────────────────────┐    │
      │   kevent() blocks   │   │
      └────────────────────┘    │
               ↓                │
      ┌────────────────────┐    │
      │  FDs are ready      │   │
      └────────────────────┘    │
               ↓                │
      ┌────────────────────┐    │
      │  Handle I/O         │   │
      └────────────────────┘    │
               ↓                │
      ┌────────────────────┐    │
      │    Back to loop     │───┘
      └────────────────────┘
```

---

## 6. 🧠 **What does “Socket is Ready” mean?**

"Ready" means:

* **Read-ready**: kernel has buffered data you can `read()`.
* **Write-ready**: you can write without blocking (e.g., TCP window has space).
* **Accept-ready**: for `listen()` sockets, a new connection is waiting.

🎯 **Key:** You don’t read directly from hardware — the **kernel receives** data first.

---

## 7. 🧬 **What Happens Internally When Data is Received**

1. NIC receives TCP packet.
2. NIC → DMA → writes into **kernel buffer (RAM)**.
3. Kernel notes: this FD is now readable.
4. `kevent()` returns this FD in `eventlist`.
5. Your program `read()`s from that buffer.

---

## 8. 🔌 **What is DMA? (Direct Memory Access)**

DMA = Hardware-level feature that lets devices write data **directly into RAM** without CPU copying it byte-by-byte.

### Example in Networking:

* NIC receives data.
* DMA engine puts it into a kernel memory buffer.
* CPU is notified only **after transfer completes**.

### Why Important?

* Avoids CPU bottleneck.
* Enables scalable, high-speed I/O (zero-copy systems).
* Makes non-blocking I/O possible and efficient.

---

## 9. 🧠 Mental Model of Event Loop with kqueue

```
                   ┌─────────────┐
                   │ NIC gets data│
                   └────┬────────┘
                        │
                        ▼
                ┌───────────────┐
                │ DMA writes to │ ◄──────────────┐
                │ kernel buffer │                │
                └───────────────┘                │
                        │                        │
                        ▼                        │
             ┌─────────────────────┐             │
             │ FD marked readable  │             │
             └────────┬────────────┘             │
                      ▼                          │
          ┌──────────────────────────┐           │
          │ kevent() returns FD      │           │
          └──────────┬───────────────┘           │
                     ▼                           │
         ┌──────────────────────────────┐        │
         │ You call read(fd, buffer...) │ ◄──────┘
         └──────────────────────────────┘
```

---

## ✅ Next Steps You Can Try

* Integrate a **kqueue-based event loop** to handle:

  * `server_fd` for new connections
  * `client_fd`s for incoming data
* Make all sockets **non-blocking**
* Use `EVFILT_TIMER` to add periodic tasks
* Add a task queue or callback map per fd

---

Would you like me to turn this into a **Notion-friendly doc** or a markdown file you can reuse in your notes?

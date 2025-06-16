        ┌────────────┐       register       ┌─────────────┐
stdin ─►│ EV_SET(fd) ├─────► with kqueue ──►│ Kernel Event│
        └────────────┘                      │    Queue    │
                                              └────┬──────┘
                                                   │
                                     wait with ┌───▼─────┐
                                   ┌──────────►│ kevent()│
                                   │            └──┬─────┘
                                   │ read-ready    │
                                   │ event returned│
                                   ▼               ▼
                            ┌─────────────┐    ┌──────────────┐
                            │ application │    │ handle input │
                            │    logic    │    └──────────────┘
                            └─────────────┘

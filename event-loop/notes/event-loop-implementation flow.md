1. Implement a simple socket, bind it and listen
2. Observe blocking nature of accept()
3. Make server_fd non blocking and see the accept() behaviour - It doesn't block, if there are no active connections it returns negative Integer immediately
4. Create Kqueue and register server_fd
--> Write an infinite loop and log something in it - observe the behaviour
--> Write an infinite loop, log, and add kevent in it - observe the behaviour (sleeps until an event is received)
5. Event loop for multiple clients

Kqueue -> Registry which tracks all the fd's and the subscribed events

Blocking accept, Blocking read, Blocking write
Unblock accept, unblock read
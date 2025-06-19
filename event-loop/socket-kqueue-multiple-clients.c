#include<unistd.h>
#include<sys/socket.h>
#include<stdio.h>
#include<stdlib.h>
#include<netinet/in.h>
#include<fcntl.h>
#include<sys/event.h>

#define MAX_EVENTS 32

/*
    🧠 What this does:
    fcntl(fd, F_GETFL, 0) gets the current file descriptor flags (e.g., read/write mode).
    fcntl(fd, F_SETFL, flags | O_NONBLOCK) sets the new flags, adding the O_NONBLOCK flag.
    After this, calls like read(fd, ...) or accept(fd, ...) will not block — instead, they’ll return immediately.
*/
void set_nonblocking(int fd) {
    int flags = fcntl(fd, F_GETFL, 0);
    fcntl(fd, F_SETFL, flags | O_NONBLOCK);
}

int main() {
    // create a socket
    /**
     * AF_INET - IPV4
     * SOCK_STREAM - TCP
     * 0 - Protocl (This resolves protocol from first two arguments) or explicitly define 6(TCP) or 17(UDP)
     */ 
    int server_fd = socket(AF_INET, SOCK_STREAM, 6);
    if (server_fd < 0) {
        perror("socket error");
        exit(1);
    }
    set_nonblocking(server_fd);

    // bind socket
    struct sockaddr_in addr = {
        .sin_family = AF_INET, //IPV4
        .sin_port = htons(9000),
        .sin_addr.s_addr = INADDR_ANY
    };
    socklen_t addrlen = sizeof(addr);

    if(bind(server_fd, (struct sockaddr *)&addr, addrlen) < 0) {
        perror("bind error");
        exit(1);
    }

    // Listen on socket
    // OS puts socket on listen state and this is configured with a backlog
    if(listen(server_fd, 10) < 0) {
        perror("Listen error");
        exit(1);
    }
    printf("Listening on port %d\n", 9000);

    // To avoid address already in use errors
    int opt = 1;
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEPORT, &opt, sizeof(opt)) < 0) {
        perror("setsockopt error");
        exit(1);
    }

    // <--------- EVENT LOOP --------->

    // Create Kqueue
    int kq = kqueue();
    if (kq == -1) {
        perror("kqueue");
        exit(1);
    }

    // Subscribe for changes
    struct kevent change;
    EV_SET(&change, server_fd, EVFILT_READ, EV_ADD | EV_ENABLE, 0, 0, NULL); // Fills the struct
    if (kevent(kq, &change, 1, NULL, 0, NULL) == -1) {
        perror("kevent register");
        exit(1);
    }


    struct kevent events[MAX_EVENTS];
    while (1) {
        // Listen for changes
        int nev = kevent(kq, NULL, 0, events, MAX_EVENTS, NULL); // nev -> number of events (0...MAX_EVENTS)

        for (int i = 0; i < nev; i++) {
            int fd = events[i].ident;

            if (fd == server_fd) {
                int client_fd = accept(server_fd, NULL, NULL);
                printf("client_fd %d\n", client_fd);
                if (client_fd > 0) {
                    set_nonblocking(client_fd);

                    // register for client read changes - SUBSCRIBE
                    EV_SET(&change, client_fd, EVFILT_READ, EV_ADD, 0, 0, NULL);
                    kevent(kq, &change, 1, NULL, 0, NULL);
                }
                printf("New Client connected: %d\n", client_fd);
            } else if (events[i].filter == EVFILT_READ) {
                    //  5. Read from client and write back
                    char buffer[1024];
                    while (1) {
                        printf("inside read logic\n");
                        int n = read(fd, buffer, sizeof(buffer));
                        if (n <= 0) {
                            printf("client %d disconnected\n", fd);
                            // close the connection
                            close(fd);
                            // UNSUBSCRIBE
                            EV_SET(&change, fd, EVFILT_READ, EV_DELETE, 0, 0, NULL);
                            kevent(kq, &change, 1, NULL, 0, NULL);
                            break;
                        }
                        write(1, "Received: ", 10);      // write to stdout
                        write(1, buffer, n);            // print what was received
                        write(fd, buffer, n);    // echo it back to client
                    }
            }
        }
    }

    close(server_fd);
    return 0;
}
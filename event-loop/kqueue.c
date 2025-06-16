#include<sys/event.h>
#include<stdio.h>
#include<stdlib.h>
#include<unistd.h>

int main() {
    int kq = kqueue();

    if (kq == -1) {
        perror("kqueue");
        exit(1);
    } else {
        printf("%d .....\n", kq);
    }

    struct kevent changes;
    EV_SET(&changes, 0, EVFILT_READ, EV_ADD | EV_ENABLE, 0, 0, NULL); // fd 0 = stdin

    while(1) {
        printf("Event loop started\n");
        struct kevent event;
        // 🚫 Blocking call since NULL is sent it waits until an event is found
        int nev = kevent(kq, &changes, 1, &event, 1, NULL);
        printf("polled a new event\n");
        if (nev < 0) {
            perror("kevent");
        } else if (nev > 0) {
            if (event.filter == EVFILT_READ) {
                char buf[128];
                int n = read(0, buf, sizeof(buf));
                if (n > 0) {
                    buf[n] = '\0';
                    printf("Read: %s", buf);
                }
            }
        }
    }

    return 0;
}
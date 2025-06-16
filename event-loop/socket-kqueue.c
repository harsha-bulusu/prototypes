#include<unistd.h>
#include<sys/socket.h>
#include<stdio.h>
#include<stdlib.h>
#include <netinet/in.h>

int main() {
    // create a socket
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0) {
        perror("socket error");
        exit(1);
    }

    // To avoid address already in use errors
    int opt = 1;
    if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEPORT, &opt, sizeof(opt)) < 0) {
        perror("setsockopt error");
        exit(1);
    }

    // bind socket
    struct sockaddr_in addr = {
        .sin_family = AF_INET, //IPV4
        .sin_port = htons(8000),
        .sin_addr.s_addr = INADDR_ANY
    };
    socklen_t addrlen = sizeof(addr);

    if(bind(server_fd, (struct sockaddr *)&addr, addrlen) < 0) {
        perror("bind error");
        exit(1);
    }

    // Listen on socket
    if(listen(server_fd, 10) < 0) {
        perror("Listen error");
        exit(1);
    }
    printf("Listening on port %d\n", 8000);

    //accept connections
    struct sockaddr_in client_addr;
    socklen_t client_len = sizeof(client_addr);
    int client_fd = accept(server_fd, (struct sockaddr * )&client_addr, &client_len);
    printf("Client connected.\n");


    char buf[1024];
    while (1) {
        int n = read(client_fd, buf, sizeof(buf));
        if (n <= 0) break;
        write(1, "Received: ", 10);      // write to stdout
        write(1, buf, n);            // print what was received
        write(client_fd, buf, n);    // echo it back to client
    }

    close(server_fd);
    close(client_fd);

    return 0;
}
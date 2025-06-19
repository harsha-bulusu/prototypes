#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <sys/socket.h>

int main() {
    int server_fd, client_fd;
    struct sockaddr_in addr;
    socklen_t addrlen = sizeof(addr);
    char buffer[1024];

    // 1. Create socket
    server_fd = socket(AF_INET, SOCK_STREAM, 0);

    // 2. Bind to port 9090
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = INADDR_ANY;
    addr.sin_port = htons(9090);
    bind(server_fd, (struct sockaddr *)&addr, sizeof(addr));

    // 3. Listen
    listen(server_fd, 5);
    printf("Listening on port 9090...\n");

    while(1) {
        // 4. Accept connection
        client_fd = accept(server_fd, (struct sockaddr *)&addr, &addrlen);
        printf("Client connected.\n");

        // 5. Read from client and write back
        while (1) {
            int n = read(client_fd, buffer, sizeof(buffer));
            printf("n: %d\n", n);
            if (n <= 0) break;
            write(1, "Received: ", 10);      // write to stdout
            write(1, buffer, n);            // print what was received
            write(client_fd, buffer, n);    // echo it back to client
        }

        close(client_fd);
    }

    
    close(server_fd);
    return 0;
}

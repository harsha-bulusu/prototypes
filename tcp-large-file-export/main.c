#include<sys/socket.h>
#include<netinet/in.h>
#include<stdio.h>
#include<stdlib.h>
#include<fcntl.h>
#include<unistd.h>

#define PORT 8080
#define CHUNK_SIZE 65536

void send_file(int client_fd, char* filepath) {
    int file_fd = open(filepath, O_RDONLY);
    if (file_fd < 0) {
        perror("open");
        return;
    }

    char buffer[CHUNK_SIZE]; // Target read size
    ssize_t bytes; // number of bytes read
    while ((bytes = read(file_fd, buffer, CHUNK_SIZE)) > 0) { // Read a chunk
        ssize_t sent = 0;
        while (sent < bytes) { // Send the chunk - sends the chunk read in small chunks which is accepted by socket buffer
            ssize_t n = send(client_fd, buffer + sent, bytes - sent, 0);
            if (n <= 0) {
                perror("send");
                close(file_fd);
                return;
            }
            sent += n;
        }
    }

    close(file_fd);
    close(client_fd);
}

int main() {
    int server_fd = socket(AF_INET, SOCK_STREAM, 0);

    struct sockaddr_in addr = {
        .sin_family = AF_INET,
        .sin_addr.s_addr = INADDR_ANY,
        .sin_port = htons(PORT)
    };

    bind(server_fd, (struct sockaddr*)&addr, sizeof(addr));
    listen(server_fd, 5);
    printf("Listening on port: %d", PORT);

    int client_fd = accept(server_fd, NULL, NULL);
    if (client_fd > 0) {
        printf("Client connected");
        send_file(client_fd, "data.xml");
    }

    return 0;
}
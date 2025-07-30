import time
import random

class Node:
    def __init__(self, uid, node_name):
        self.uid = uid
        self.node_name = node_name
        self.right_node = None
    
    def set_right(self, right_node):
        self.right_node = right_node

    def elect(self):
        self.forward(self.uid)

    def forward(self, uid):
        self.right_node.receive(uid)

    def receive(self, incoming_uid):
        if incoming_uid < self.uid:
            print(f"❌ rejecting incoming_uid {self.node_name} uid: {self.uid} > {incoming_uid}")
            self.right_node.receive(self.uid)
        elif incoming_uid > self.uid:
            print(f"⏩ forwarding incoming_uid {self.node_name} uid: {self.uid} < {incoming_uid}")
            self.right_node.receive(incoming_uid)
        else: # incoming_uid = self.uid
            print(f"✅ {self.node_name} Elected as a leader")



nodes = []

def election():
    # forward, discard, accept and publish
    print("\n🟡 Triggering election from ALL nodes...\n")
    node_id = random.randrange(0, len(nodes))
    nodes[node_id].elect()

    # Every node triggering election
    # for node in nodes:
    #     node.elect()
    


def generate_uid():
    epoch = int(time.time() * 1000)
    salt = random.randrange(0,9999)
    return int(f"{epoch}{salt:04d}")

def create_nodes(cnt):
    global nodes
    nodes = [Node(generate_uid(), f"node-{i}") for i in range(cnt)]

    # form a circle - every node should be aware of right node
    for i in range(len(nodes)):
        node = nodes[i]
        node.right_node = nodes[(i + 1) % len(nodes)]


if __name__ == '__main__':
    create_nodes(5)
    election()